package com.cresora.utilities.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Maps DSL action names to the runtime calls the code generator emits.
 *
 * <p>Java port of {@code InstructionMapping.kt}, restricted to the weapon context. The
 * original targeted {@code hifumi.cresora.*} (Yarn-mapped) classes; the templates here are
 * retargeted at {@code com.cresora.utilities.*} and Mojang's official Minecraft names. The
 * exact runtime method signatures are finalised alongside the runtime port (phase 4).
 */
public final class InstructionMapping {
	private static final Map<String, String> MAPPINGS = Map.ofEntries(
			Map.entry("heal", "com.cresora.utilities.weapon.WeaponCombatSupport.healHp(player, %args%)"),
			Map.entry("grant_shield", "com.cresora.utilities.weapon.WeaponCombatSupport.grantShield(player, %args%)"),
			Map.entry("start_cooldown", "com.cresora.utilities.weapon.WeaponSkillService.startCooldown(player, definition.id, %args%)"),
			Map.entry("apply_mark", "com.cresora.utilities.weapon.WeaponSkillService.applyMark(%args%)"),
			Map.entry("log", "player.sendSystemMessage(net.minecraft.network.chat.Component.literal(%args%))"),
			Map.entry("grant_invulnerability", "com.cresora.utilities.weapon.WeaponSkillService.grantInvulnerability(%args%)"),
			Map.entry("send_message", "player.displayClientMessage(net.minecraft.network.chat.Component.translatable(%args%), true)"),
			Map.entry("apply_status_effect", "com.cresora.utilities.weapon.WeaponCombatSupport.applyStatusEffect(player, %args%)"),
			Map.entry("close_skill_menu", "com.cresora.utilities.weapon.HotbarOverrideService.restoreHotbar(player)"),
			Map.entry("spawn_particles", "com.cresora.utilities.weapon.WeaponCombatSupport.spawnParticles(player, %args%)"),
			Map.entry("deal_true_damage", "com.cresora.utilities.weapon.WeaponSkillService.dealTrueDamage(player, target, (float) (%args%))"),
			Map.entry("ignite", "target.setSecondsOnFire((int) (%args%))")
	);

	private static final Set<String> INTRINSICS = Set.of("add_buff");

	private InstructionMapping() {
	}

	public static boolean isKnown(String functionName) {
		return MAPPINGS.containsKey(functionName) || INTRINSICS.contains(functionName);
	}

	public static String expand(String functionName, List<String> args) {
		String pattern = MAPPINGS.get(functionName);
		if (pattern == null) {
			return functionName + "(" + String.join(", ", args) + ")";
		}

		List<String> processedArgs;
		if (args.isEmpty()) {
			processedArgs = defaultArguments(functionName);
		} else {
			processedArgs = new ArrayList<>(args.size());
			for (int i = 0; i < args.size(); i++) {
				String arg = args.get(i);
				if (isDurationArgument(functionName, i)) {
					processedArgs.add(durationToTicks(functionName, arg));
				} else if (arg.equals("skill_value")) {
					processedArgs.add("definition.skill().baseValue()");
				} else if (arg.equals("skill_duration")) {
					processedArgs.add("definition.skill().durationSeconds()");
				} else {
					processedArgs.add(arg);
				}
			}
		}

		return pattern.replace("%args%", String.join(", ", processedArgs));
	}

	private static List<String> defaultArguments(String functionName) {
		return switch (functionName) {
			case "start_cooldown" -> List.of("definition.skill().cooldownSeconds() * 20L");
			case "heal" -> List.of("definition.skill().baseValue()");
			case "grant_shield" -> List.of("definition.skill().baseValue()", "definition.skill().durationSeconds() * 20L");
			default -> List.of();
		};
	}

	private static boolean isDurationArgument(String functionName, int index) {
		return switch (functionName) {
			case "grant_shield" -> index == 1;
			case "start_cooldown" -> index == 0;
			case "apply_mark" -> index == 2;
			case "grant_invulnerability" -> index == 1;
			case "apply_status_effect" -> index == 1;
			default -> false;
		};
	}

	private static String durationToTicks(String functionName, String arg) {
		if (arg.equals("skill_duration")) {
			return functionName.equals("apply_status_effect")
					? "(int) (definition.skill().durationSeconds() * 20)"
					: "(long) (definition.skill().durationSeconds() * 20)";
		}
		if (arg.endsWith("s")) {
			Double seconds = toDoubleOrNull(arg.substring(0, arg.length() - 1));
			if (seconds == null) {
				throw new ParseException(functionName + " duration must be a number of seconds, got '" + arg + "'");
			}
			double ticks = seconds * 20.0;
			if (functionName.equals("apply_status_effect")) {
				return ticks % 1.0 == 0.0 ? Long.toString((long) ticks) : "(int) (" + seconds + " * 20)";
			}
			return ticks % 1.0 == 0.0 ? ((long) ticks) + "L" : "(long) (" + seconds + " * 20)";
		}
		if (arg.equals("skill_value")) {
			throw new ParseException("skill_value is not a valid duration for " + functionName);
		}
		return arg;
	}

	/** Recursively expands every known instruction call found in {@code content}. */
	public static String expandAll(String content) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		int len = content.length();
		while (i < len) {
			char c = content.charAt(i);
			if (Character.isLetter(c) || c == '_') {
				int start = i;
				while (i < len && (Character.isLetterOrDigit(content.charAt(i)) || content.charAt(i) == '_')) {
					i++;
				}
				String id = content.substring(start, i);
				boolean dotPrefixed = false;
				int p = start - 1;
				while (p >= 0 && Character.isWhitespace(content.charAt(p))) {
					p--;
				}
				if (p >= 0 && content.charAt(p) == '.') {
					dotPrefixed = true;
				}
				int temp = i;
				while (temp < len && Character.isWhitespace(content.charAt(temp))) {
					temp++;
				}
				if (temp < len && content.charAt(temp) == '(' && isKnown(id) && !dotPrefixed) {
					i = temp + 1;
					int depth = 1;
					int argStart = i;
					while (i < len && depth > 0) {
						char cc = content.charAt(i);
						if (cc == '(') {
							depth++;
						} else if (cc == ')') {
							depth--;
						}
						i++;
					}
					int argEnd = i - 1;
					String argsStr = content.substring(argStart, argEnd);
					List<String> args = argsStr.trim().isEmpty() ? List.of() : splitArguments(argsStr);
					List<String> expandedArgs = args.stream().map(InstructionMapping::expandAll).toList();
					sb.append(expand(id, expandedArgs));
				} else {
					sb.append(id);
				}
			} else {
				sb.append(c);
				i++;
			}
		}
		return sb.toString();
	}

	private static List<String> splitArguments(String argsStr) {
		List<String> result = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		int depth = 0;
		boolean inString = false;
		boolean escape = false;
		for (int i = 0; i < argsStr.length(); i++) {
			char c = argsStr.charAt(i);
			if (escape) {
				current.append(c);
				escape = false;
				continue;
			}
			if (c == '\\') {
				current.append(c);
				escape = true;
				continue;
			}
			if (c == '"') {
				inString = !inString;
				current.append(c);
				continue;
			}
			if (inString) {
				current.append(c);
				continue;
			}
			if (c == '(' || c == '[' || c == '{') {
				depth++;
				current.append(c);
			} else if (c == ')' || c == ']' || c == '}') {
				depth--;
				current.append(c);
			} else if (c == ',' && depth == 0) {
				result.add(current.toString().trim());
				current.setLength(0);
			} else {
				current.append(c);
			}
		}
		if (current.length() > 0 || !result.isEmpty()) {
			result.add(current.toString().trim());
		}
		return result;
	}

	private static Double toDoubleOrNull(String value) {
		try {
			return Double.parseDouble(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
