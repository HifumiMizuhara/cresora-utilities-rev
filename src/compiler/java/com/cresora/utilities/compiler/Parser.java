package com.cresora.utilities.compiler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Recursive-descent parser for the weapon subset of the Cresora DSL. Java port of the
 * original {@code Parser.kt} with the artifact and movement productions removed.
 */
public final class Parser {
	private static final Set<String> VALID_ROLES = Set.of(
			"vanguard", "guard", "defender", "sniper", "caster", "medic", "supporter", "specialist", "catalyst"
	);

	private static final Set<String> VALID_FORMATTING_COLORS = Set.of(
			"BLACK", "DARK_BLUE", "DARK_GREEN", "DARK_AQUA", "DARK_RED", "DARK_PURPLE",
			"GOLD", "GRAY", "DARK_GRAY", "BLUE", "GREEN", "AQUA", "RED",
			"LIGHT_PURPLE", "YELLOW", "WHITE"
	);

	private final String source;
	private final List<Token> tokens;
	private int current = 0;

	public Parser(String source, List<Token> tokens) {
		this.source = source;
		this.tokens = tokens;
	}

	public List<Ast> parse() {
		List<Ast> nodes = new ArrayList<>();
		while (!isAtEnd()) {
			if (check(TokenType.KEYWORD_WEAPON)) {
				nodes.add(weapon());
			} else if (check(TokenType.KEYWORD_DICTIONARY)) {
				nodes.add(dictionary());
			} else {
				Token token = peek();
				throw new ParseException("Unexpected top-level token '" + token.lexeme() + "' at line " + token.line());
			}
		}
		return nodes;
	}

	private Ast.WeaponDef weapon() {
		consume(TokenType.KEYWORD_WEAPON, "Expect 'weapon'");
		String name = consume(TokenType.STRING, "Expect weapon name").lexeme();
		consume(TokenType.LEFT_BRACE, "Expect '{' before weapon body");

		String id = "";
		String rarity = "";
		String baseItem = "";
		String damageType = "physical";
		String role = "guard";
		Ast.Stats stats = null;
		Ast.Skill skill = null;
		Ast.Spirit spirit = null;
		Map<String, Map<String, String>> translations = new LinkedHashMap<>();
		List<Ast.SubSkill> subSkills = new ArrayList<>();
		Integer customModelData = null;
		String texture = null;

		while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
			Token token = advance();
			switch (token.lexeme()) {
				case "id" -> {
					consume(TokenType.COLON, "Expect ':' after id");
					id = consume(TokenType.STRING, "Expect id").lexeme();
				}
				case "rarity" -> {
					consume(TokenType.COLON, "Expect ':' after rarity");
					rarity = advance().lexeme();
				}
				case "base_item" -> {
					consume(TokenType.COLON, "Expect ':' after base_item");
					baseItem = consume(TokenType.STRING, "Expect base_item").lexeme();
				}
				case "damage_type" -> {
					consume(TokenType.COLON, "Expect ':' after damage_type");
					damageType = consume(TokenType.IDENTIFIER, "Expect damage_type").lexeme();
				}
				case "role" -> {
					consume(TokenType.COLON, "Expect ':' after role");
					String parsedRole = consume(TokenType.IDENTIFIER, "Expect role").lexeme();
					if (!VALID_ROLES.contains(parsedRole.toLowerCase())) {
						throw new ParseException("Unknown role '" + parsedRole + "' at line " + token.line());
					}
					role = parsedRole.toLowerCase();
				}
				case "stats" -> {
					consume(TokenType.LEFT_BRACE, "Expect '{' for stats");
					stats = stats();
					consume(TokenType.RIGHT_BRACE, "Expect '}' after stats");
				}
				case "skill" -> {
					String skillName = consume(TokenType.STRING, "Expect skill name").lexeme();
					consume(TokenType.LEFT_BRACE, "Expect '{' for skill");
					skill = skill(skillName);
					consume(TokenType.RIGHT_BRACE, "Expect '}' after skill");
				}
				case "spirit" -> {
					consume(TokenType.LEFT_BRACE, "Expect '{' for spirit");
					spirit = spirit();
					consume(TokenType.RIGHT_BRACE, "Expect '}' after spirit");
				}
				case "sub_skill" -> {
					String subSkillName = consume(TokenType.STRING, "Expect sub_skill name").lexeme();
					consume(TokenType.LEFT_BRACE, "Expect '{' for sub_skill");
					subSkills.add(subSkill(subSkillName));
					consume(TokenType.RIGHT_BRACE, "Expect '}' after sub_skill");
				}
				case "translations" -> {
					consume(TokenType.LEFT_BRACE, "Expect '{' for translations");
					translations = translations();
					consume(TokenType.RIGHT_BRACE, "Expect '}' after translations");
				}
				case "custom_model_data" -> {
					consume(TokenType.COLON, "Expect ':' after custom_model_data");
					customModelData = Integer.parseInt(consume(TokenType.NUMBER, "Expect number").lexeme());
				}
				case "texture" -> {
					consume(TokenType.COLON, "Expect ':' after texture");
					texture = check(TokenType.STRING)
							? advance().lexeme()
							: consume(TokenType.IDENTIFIER, "Expect string or identifier").lexeme();
				}
				default -> throw new ParseException("Unknown weapon field '" + token.lexeme() + "' at line " + token.line());
			}
		}

		consume(TokenType.RIGHT_BRACE, "Expect '}' after weapon body");
		if (stats == null) {
			throw new ParseException("Weapon '" + name + "' is missing required stats block");
		}
		if (id.isBlank()) {
			throw new ParseException("Weapon '" + name + "' is missing required id");
		}
		if (rarity.isBlank()) {
			throw new ParseException("Weapon '" + name + "' is missing required rarity");
		}
		if (baseItem.isBlank()) {
			throw new ParseException("Weapon '" + name + "' is missing required base_item");
		}
		return new Ast.WeaponDef(name, id, rarity, baseItem, damageType, role, stats, skill, spirit,
				translations, subSkills, customModelData, texture);
	}

	private Ast.Spirit spirit() {
		String nameKey = "";
		String awakeningConditionKey = null;
		Map<String, String> voiceLines = new LinkedHashMap<>();
		List<Ast.BondStage> bondStages = new ArrayList<>();

		while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
			Token token = advance();
			switch (token.lexeme()) {
				case "name" -> {
					consume(TokenType.COLON, "Expect ':' after spirit name");
					nameKey = consume(TokenType.STRING, "Expect spirit name translation key").lexeme();
				}
				case "awakening_condition" -> {
					consume(TokenType.COLON, "Expect ':' after awakening_condition");
					awakeningConditionKey = consume(TokenType.STRING, "Expect awakening condition translation key").lexeme();
				}
				case "voice_lines" -> {
					consume(TokenType.LEFT_BRACE, "Expect '{' for voice_lines");
					while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
						Token next = advance();
						if (next.type() == TokenType.SEMICOLON) {
							continue;
						}
						String key;
						if (next.type() == TokenType.IDENTIFIER || next.type() == TokenType.STRING) {
							key = next.lexeme();
						} else {
							throw new ParseException("Expect voice_lines key at line " + next.line());
						}
						consume(TokenType.COLON, "Expect ':' after voice_lines key");
						voiceLines.put(key, consume(TokenType.STRING, "Expect voice line translation key").lexeme());
					}
					consume(TokenType.RIGHT_BRACE, "Expect '}' after voice_lines");
				}
				case "bond_stage" -> {
					int stage = Integer.parseInt(consume(TokenType.NUMBER, "Expect bond stage number").lexeme());
					if (stage < 1 || stage > 6) {
						throw new ParseException("Spirit bond stage must be between 1 and 6, got " + stage + " at line " + token.line());
					}
					consume(TokenType.LEFT_BRACE, "Expect '{' for bond_stage");
					String titleKey = "";
					String storyKey = "";
					while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
						Token field = advance();
						if (field.type() == TokenType.SEMICOLON) {
							continue;
						}
						switch (field.lexeme()) {
							case "title" -> {
								consume(TokenType.COLON, "Expect ':' after bond stage title");
								titleKey = consume(TokenType.STRING, "Expect bond stage title translation key").lexeme();
							}
							case "story" -> {
								consume(TokenType.COLON, "Expect ':' after bond stage story");
								storyKey = consume(TokenType.STRING, "Expect bond stage story translation key").lexeme();
							}
							default -> throw new ParseException("Unknown bond_stage field '" + field.lexeme() + "' at line " + field.line());
						}
					}
					consume(TokenType.RIGHT_BRACE, "Expect '}' after bond_stage");
					if (titleKey.isBlank()) {
						throw new ParseException("Spirit bond_stage " + stage + " is missing required title");
					}
					if (storyKey.isBlank()) {
						throw new ParseException("Spirit bond_stage " + stage + " is missing required story");
					}
					for (Ast.BondStage existing : bondStages) {
						if (existing.stage() == stage) {
							throw new ParseException("Duplicate spirit bond_stage " + stage);
						}
					}
					bondStages.add(new Ast.BondStage(stage, titleKey, storyKey));
				}
				default -> {
					if (token.type() != TokenType.SEMICOLON) {
						throw new ParseException("Unknown spirit field '" + token.lexeme() + "' at line " + token.line());
					}
				}
			}
		}

		if (nameKey.isBlank()) {
			throw new ParseException("Spirit block is missing required name");
		}
		bondStages.sort((a, b) -> Integer.compare(a.stage(), b.stage()));
		return new Ast.Spirit(nameKey, voiceLines, bondStages, awakeningConditionKey);
	}

	private Ast.DictionaryDef dictionary() {
		consume(TokenType.KEYWORD_DICTIONARY, "Expect 'dictionary'");
		String id = consume(TokenType.IDENTIFIER, "Expect dictionary id").lexeme();
		consume(TokenType.LEFT_BRACE, "Expect '{' before dictionary body");

		Map<String, Map<String, String>> translations = new LinkedHashMap<>();

		while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
			Token token = advance();
			if (token.lexeme().equals("translations")) {
				consume(TokenType.LEFT_BRACE, "Expect '{' for translations");
				translations = translations();
				consume(TokenType.RIGHT_BRACE, "Expect '}' after translations");
			} else {
				throw new ParseException("Unknown dictionary field '" + token.lexeme() + "' at line " + token.line());
			}
		}

		consume(TokenType.RIGHT_BRACE, "Expect '}' after dictionary body");
		return new Ast.DictionaryDef(id, translations);
	}

	private Ast.Stats stats() {
		double baseAttackDamage = 0.0;
		double attackDamagePerLevel = 0.0;
		double totalAttackSpeed = 1.6;
		int maxBaseLevel = 60;
		int maxSkillLevel = 10;
		double critRateBonusPercent = 0.0;
		double maxAllDamageBonusPercent = 0.0;
		double hpBonusPercent = 0.0;

		while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
			String field = advance().lexeme();
			consume(TokenType.COLON, "Expect ':'");
			switch (field) {
				case "base_damage" -> baseAttackDamage = Double.parseDouble(consume(TokenType.NUMBER, "Expect number").lexeme());
				case "damage_per_level" -> attackDamagePerLevel = Double.parseDouble(consume(TokenType.NUMBER, "Expect number").lexeme());
				case "speed" -> totalAttackSpeed = Double.parseDouble(consume(TokenType.NUMBER, "Expect number").lexeme());
				case "max_base_level" -> maxBaseLevel = Integer.parseInt(consume(TokenType.NUMBER, "Expect number").lexeme());
				case "max_skill_level" -> maxSkillLevel = Integer.parseInt(consume(TokenType.NUMBER, "Expect number").lexeme());
				case "crit_rate_bonus" -> critRateBonusPercent = Double.parseDouble(consume(TokenType.NUMBER, "Expect number").lexeme());
				case "max_all_damage_bonus" -> maxAllDamageBonusPercent = Double.parseDouble(consume(TokenType.NUMBER, "Expect number").lexeme());
				case "hp_bonus" -> hpBonusPercent = Double.parseDouble(consume(TokenType.NUMBER, "Expect number").lexeme());
				default -> throw new ParseException("Unknown stats field '" + field + "'");
			}
		}
		return new Ast.Stats(baseAttackDamage, attackDamagePerLevel, totalAttackSpeed, maxBaseLevel,
				maxSkillLevel, critRateBonusPercent, maxAllDamageBonusPercent, hpBonusPercent);
	}

	private Ast.Skill skill(String name) {
		String effectId = "";
		double cooldown = 0.0;
		double duration = 0.0;
		double baseValue = 0.0;
		double valuePerLevel = 0.0;
		double radius = 0.0;
		String note = null;
		List<Ast.SkillHandler> handlers = new ArrayList<>();
		List<Ast.Buff> buffs = new ArrayList<>();

		while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
			Token token = advance();
			if (token.type() == TokenType.IDENTIFIER && token.lexeme().startsWith("on_")) {
				consume(TokenType.LEFT_BRACE, "Expect '{' for handler");
				handlers.add(handler(token.lexeme()));
				consume(TokenType.RIGHT_BRACE, "Expect '}' after handler");
			} else if (token.type() == TokenType.KEYWORD_BUFF) {
				String buffId = consume(TokenType.STRING, "Expect buff id").lexeme();
				consume(TokenType.LEFT_BRACE, "Expect '{' for buff");
				buffs.add(buff(buffId));
				consume(TokenType.RIGHT_BRACE, "Expect '}' after buff");
			} else if (token.type() == TokenType.SEMICOLON) {
				// Skip optional semicolons
			} else {
				consume(TokenType.COLON, "Expect ':'");
				switch (token.lexeme()) {
					case "effect_id" -> effectId = consume(TokenType.STRING, "Expect effect_id").lexeme();
					case "cooldown" -> cooldown = parseTime();
					case "duration" -> duration = parseTime();
					case "base_value" -> baseValue = Double.parseDouble(consume(TokenType.NUMBER, "Expect number").lexeme());
					case "value_per_level" -> valuePerLevel = Double.parseDouble(consume(TokenType.NUMBER, "Expect number").lexeme());
					case "radius" -> radius = Double.parseDouble(consume(TokenType.NUMBER, "Expect number").lexeme());
					case "note" -> {
						String noteValue = consume(TokenType.STRING, "Expect note string").lexeme();
						if (!Set.of("treble", "bass", "melody", "harmony").contains(noteValue.toLowerCase())) {
							throw new ParseException("Unknown note '" + noteValue + "' at line " + token.line()
									+ ". Valid: treble, bass, melody, harmony");
						}
						note = noteValue.toLowerCase();
					}
					default -> throw new ParseException("Unknown skill field '" + token.lexeme() + "' at line " + token.line());
				}
			}
		}
		return new Ast.Skill(name, effectId, cooldown, duration, baseValue, valuePerLevel, radius, handlers, buffs, note);
	}

	private Ast.SubSkill subSkill(String name) {
		String effectId = "";
		String icon = "";
		List<Ast.SkillHandler> handlers = new ArrayList<>();

		while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
			String field = advance().lexeme();
			switch (field) {
				case "effect_id" -> {
					consume(TokenType.COLON, "Expect ':'");
					effectId = consume(TokenType.STRING, "Expect string").lexeme();
				}
				case "icon" -> {
					consume(TokenType.COLON, "Expect ':'");
					icon = consume(TokenType.STRING, "Expect string").lexeme();
				}
				default -> {
					if (field.startsWith("on_")) {
						consume(TokenType.LEFT_BRACE, "Expect '{' for handler");
						handlers.add(handler(field));
						consume(TokenType.RIGHT_BRACE, "Expect '}' after handler");
					} else {
						throw new ParseException("Unknown sub_skill field '" + field + "'");
					}
				}
			}
		}
		return new Ast.SubSkill(name, effectId, icon, handlers);
	}

	private Ast.Buff buff(String id) {
		String translationKey = null;
		int maxStacks = 1;
		double duration = 0.0;
		Map<String, Double> stats = new LinkedHashMap<>();
		String decay = "refresh";

		while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
			Token token = advance();
			if (token.type() == TokenType.SEMICOLON) {
				continue;
			}
			switch (token.lexeme()) {
				case "translation_key" -> {
					consume(TokenType.COLON, "Expect ':'");
					translationKey = consume(TokenType.STRING, "Expect string").lexeme();
				}
				case "max_stacks" -> {
					consume(TokenType.COLON, "Expect ':'");
					maxStacks = Integer.parseInt(consume(TokenType.NUMBER, "Expect number").lexeme());
				}
				case "duration" -> {
					consume(TokenType.COLON, "Expect ':'");
					duration = parseTime();
				}
				case "decay" -> {
					consume(TokenType.COLON, "Expect ':'");
					decay = advance().lexeme();
				}
				case "stats" -> {
					consume(TokenType.LEFT_BRACE, "Expect '{' for stats");
					while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
						Token nextStat = advance();
						if (nextStat.type() == TokenType.SEMICOLON) {
							continue;
						}
						String statName = nextStat.lexeme();
						consume(TokenType.COLON, "Expect ':'");
						stats.put(statName, Double.parseDouble(consume(TokenType.NUMBER, "Expect number").lexeme()));
					}
					consume(TokenType.RIGHT_BRACE, "Expect '}' after stats");
				}
				default -> throw new ParseException("Unknown buff field '" + token.lexeme() + "'");
			}
		}
		return new Ast.Buff(id, translationKey, maxStacks, duration, stats, decay);
	}

	private Map<String, Map<String, String>> translations() {
		Map<String, Map<String, String>> locales = new LinkedHashMap<>();
		while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
			String locale = consume(TokenType.IDENTIFIER, "Expect locale id (e.g. ja_jp)").lexeme();
			consume(TokenType.LEFT_BRACE, "Expect '{' for locale");
			Map<String, String> pairs = new LinkedHashMap<>();
			while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
				Token token = advance();
				if (token.type() == TokenType.SEMICOLON) {
					continue;
				}
				String key;
				if (token.type() == TokenType.IDENTIFIER || token.type() == TokenType.STRING) {
					key = token.lexeme();
				} else {
					throw new ParseException("Expect key at line " + token.line());
				}
				consume(TokenType.COLON, "Expect ':'");
				pairs.put(key, consume(TokenType.STRING, "Expect string value").lexeme());
			}
			consume(TokenType.RIGHT_BRACE, "Expect '}' after locale");
			locales.put(locale, pairs);
		}
		return locales;
	}

	private Ast.Action action(String eventName) {
		String name = advance().lexeme();
		if (name.equals("execute") && check(TokenType.LEFT_BRACE)) {
			return new Ast.Execute(executeBlock());
		}

		List<String> args = new ArrayList<>();
		if (check(TokenType.LEFT_PAREN)) {
			consume(TokenType.LEFT_PAREN, "Expect '('");
			while (!check(TokenType.RIGHT_PAREN) && !isAtEnd()) {
				Token argToken = advance();
				if (argToken.type() == TokenType.STRING) {
					args.add("\"" + argToken.lexeme() + "\"");
				} else {
					String value = argToken.lexeme();
					if (check(TokenType.IDENTIFIER) && peek().lexeme().equals("s")) {
						value += advance().lexeme();
					}
					args.add(value);
				}
				if (check(TokenType.COMMA)) {
					advance();
				}
			}
			consume(TokenType.RIGHT_PAREN, "Expect ')'");
		}

		if (check(TokenType.LEFT_BRACE)) {
			consume(TokenType.LEFT_BRACE, "Expect '{' for block action");
			List<Ast.Action> blockActions = new ArrayList<>();
			while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
				blockActions.add(action(eventName));
				if (check(TokenType.SEMICOLON)) {
					advance();
				}
			}
			consume(TokenType.RIGHT_BRACE, "Expect '}'");

			if (name.equals("area_of_effect")) {
				double radius = args.isEmpty() ? 5.0 : parseDoubleOr(args.get(0), 5.0);
				return new Ast.AreaOfEffect(radius, blockActions);
			}
			throw new ParseException("Unsupported block action '" + name + "' in handler '" + eventName + "'");
		}

		return switch (name) {
			case "open_skill_menu" -> {
				if (args.size() < 2) {
					throw new ParseException("open_skill_menu requires at least one sub-skill and a duration");
				}
				List<String> subSkillIds = new ArrayList<>();
				for (int i = 0; i < args.size() - 1; i++) {
					subSkillIds.add(removeSurrounding(args.get(i), "\""));
				}
				yield new Ast.OpenSkillMenu(subSkillIds, parseTimeFromValue(args.get(args.size() - 1)));
			}
			case "close_skill_menu" -> new Ast.CloseSkillMenu();
			case "send_localized_message" -> {
				if (args.size() < 2) {
					throw new ParseException("send_localized_message requires at least a key and a color");
				}
				String key = removeSurrounding(args.get(0), "\"");
				String color = removeSurrounding(args.get(1), "\"");
				yield new Ast.SendLocalizedMessage(key, color, List.copyOf(args.subList(2, args.size())));
			}
			case "apply_mark" -> {
				requireArity(name, args, 3, 3);
				yield new Ast.ApplyMark(args.get(0), stringArg(name, "mark id", args.get(1)), durationArg(name, args.get(2)));
			}
			case "grant_invulnerability" -> {
				requireArity(name, args, 2, 2);
				yield new Ast.GrantInvulnerability(args.get(0), durationArg(name, args.get(1)));
			}
			case "add_buff" -> {
				requireArity(name, args, 1, 2);
				int stacks = 1;
				if (args.size() > 1) {
					try {
						stacks = Integer.parseInt(args.get(1));
					} catch (NumberFormatException e) {
						throw new ParseException("add_buff stacks must be an integer, got '" + args.get(1) + "'");
					}
				}
				yield new Ast.AddBuff(stringArg(name, "buff id", args.get(0)), stacks);
			}
			case "start_cooldown" -> {
				requireArity(name, args, 0, 1);
				yield new Ast.StartCooldown(args.isEmpty() ? null : durationArg(name, args.get(0)));
			}
			case "send_message" -> {
				requireArity(name, args, 1, 2);
				String color = (args.size() > 1 ? stringArg(name, "color", args.get(1)) : "WHITE").toUpperCase();
				if (!VALID_FORMATTING_COLORS.contains(color)) {
					throw new ParseException("send_message color '" + color + "' is not a Minecraft Formatting color");
				}
				yield new Ast.SendMessage(stringArg(name, "translation key", args.get(0)), color);
			}
			case "apply_status_effect" -> {
				requireArity(name, args, 2, 3);
				String effectId = stringArg(name, "effect id", args.get(0));
				if (!effectId.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") && !effectId.matches("[a-z0-9_./-]+")) {
					throw new ParseException("apply_status_effect effect id '" + effectId + "' is not a valid identifier");
				}
				int amplifier = 0;
				if (args.size() > 2) {
					try {
						amplifier = Integer.parseInt(args.get(2));
					} catch (NumberFormatException e) {
						throw new ParseException("apply_status_effect amplifier must be an integer, got '" + args.get(2) + "'");
					}
				}
				yield new Ast.ApplyStatusEffect(effectId, durationArg(name, args.get(1)), amplifier);
			}
			default -> InstructionMapping.isKnown(name)
					? new Ast.InstructionCall(name, args)
					: new Ast.Command(name, args);
		};
	}

	private Ast.SkillHandler handler(String eventName) {
		List<Ast.Action> actions = new ArrayList<>();
		while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
			actions.add(action(eventName));
			if (check(TokenType.SEMICOLON)) {
				advance();
			}
		}
		return new Ast.SkillHandler(eventName, actions);
	}

	private List<Ast.Action> executeBlock() {
		consume(TokenType.LEFT_BRACE, "Expect '{'");
		List<Ast.Action> statements = new ArrayList<>();
		while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
			String name = peek().lexeme();
			if (InstructionMapping.isKnown(name)
					|| name.equals("area_of_effect")
					|| name.equals("send_localized_message")
					|| name.equals("close_skill_menu")) {
				statements.add(action("execute"));
				if (check(TokenType.SEMICOLON)) {
					advance();
				}
			} else {
				statements.add(statement());
			}
		}
		consume(TokenType.RIGHT_BRACE, "Expect '}'");
		return statements;
	}

	private Ast.Action statement() {
		Token token = peek();
		if (token.type() == TokenType.IDENTIFIER
				&& peekNext().type() == TokenType.LEFT_PAREN
				&& InstructionMapping.isKnown(token.lexeme())) {
			String name = advance().lexeme();
			consume(TokenType.LEFT_PAREN, "Expect '('");
			List<String> args = new ArrayList<>();
			while (!check(TokenType.RIGHT_PAREN) && !isAtEnd()) {
				Token arg = advance();
				if (arg.type() == TokenType.STRING) {
					args.add("\"" + arg.lexeme() + "\"");
				} else {
					String value = arg.lexeme();
					if (check(TokenType.IDENTIFIER) && peek().lexeme().equals("s")) {
						value += advance().lexeme();
					}
					args.add(value);
				}
				if (check(TokenType.COMMA)) {
					advance();
				}
			}
			consume(TokenType.RIGHT_PAREN, "Expect ')'");
			if (check(TokenType.SEMICOLON)) {
				advance();
			}
			return new Ast.InstructionCall(name, args);
		}

		Token startToken = peek();
		Token lastToken = startToken;
		int braceCount = 0;
		while (!isAtEnd()) {
			if (braceCount == 0 && (check(TokenType.SEMICOLON) || check(TokenType.RIGHT_BRACE))) {
				break;
			}
			if (braceCount == 0 && peek().line() > lastToken.line()) {
				TokenType nextType = peek().type();
				TokenType lastType = lastToken.type();
				boolean isContinuation = nextType == TokenType.OPERATOR || nextType == TokenType.DOT || nextType == TokenType.COMMA
						|| nextType == TokenType.RIGHT_PAREN || nextType == TokenType.RIGHT_BRACKET || nextType == TokenType.RIGHT_BRACE
						|| lastType == TokenType.OPERATOR || lastType == TokenType.DOT || lastType == TokenType.COMMA
						|| lastType == TokenType.LEFT_PAREN || lastType == TokenType.LEFT_BRACKET || lastType == TokenType.LEFT_BRACE
						|| lastToken.lexeme().equals("else") || lastToken.lexeme().equals("in") || lastToken.lexeme().equals("is")
						|| peek().lexeme().equals("else") || peek().lexeme().equals("in") || peek().lexeme().equals("is");
				if (!isContinuation) {
					break;
				}
			}
			Token t = advance();
			if (t.type() == TokenType.LEFT_BRACE) {
				braceCount++;
			} else if (t.type() == TokenType.RIGHT_BRACE) {
				braceCount--;
			}
			lastToken = t;
		}
		String rawContent = source.substring(startToken.startOffset(), lastToken.endOffset());
		if (check(TokenType.SEMICOLON)) {
			advance();
		}
		return new Ast.Expression(rawContent.trim());
	}

	private void requireArity(String command, List<String> args, int min, int max) {
		if (args.size() < min || args.size() > max) {
			String expected = min == max ? Integer.toString(min) : min + ".." + max;
			throw new ParseException(command + " expects " + expected + " argument(s), got " + args.size());
		}
	}

	private String stringArg(String command, String role, String arg) {
		if (!arg.startsWith("\"") || !arg.endsWith("\"")) {
			throw new ParseException(command + " " + role + " must be a string literal, got '" + arg + "'");
		}
		return removeSurrounding(arg, "\"");
	}

	private Ast.Duration durationArg(String command, String arg) {
		if (arg.equals("skill_duration")) {
			return new Ast.Duration.SkillDuration();
		}
		if (arg.endsWith("s")) {
			Double seconds = toDoubleOrNull(arg.substring(0, arg.length() - 1));
			if (seconds != null) {
				return new Ast.Duration.Seconds(seconds);
			}
		}
		Long ticks = toLongOrNull(arg);
		if (ticks != null) {
			return new Ast.Duration.Ticks(ticks);
		}
		throw new ParseException(command + " duration must be a number (ticks), an Ns seconds literal, or skill_duration — got '" + arg + "'");
	}

	private double parseTime() {
		double value = Double.parseDouble(consume(TokenType.NUMBER, "Expect number").lexeme());
		if (check(TokenType.IDENTIFIER) && peek().lexeme().equals("s")) {
			advance();
		}
		return value;
	}

	private double parseTimeFromValue(String value) {
		if (value.isBlank()) {
			return 0.0;
		}
		if (value.endsWith("s")) {
			Double seconds = toDoubleOrNull(value.substring(0, value.length() - 1));
			return seconds != null ? seconds : 0.0;
		}
		Double number = toDoubleOrNull(value);
		return number != null ? number : 0.0;
	}

	private Token consume(TokenType type, String message) {
		if (check(type)) {
			return advance();
		}
		Token token = peek();
		throw new ParseException(message + " at line " + token.line()
				+ " (expected " + type + ", got " + token.type() + " '" + token.lexeme() + "')");
	}

	private boolean check(TokenType type) {
		return !isAtEnd() && peek().type() == type;
	}

	private Token advance() {
		if (!isAtEnd()) {
			current++;
		}
		return previous();
	}

	private boolean isAtEnd() {
		return peek().type() == TokenType.EOF;
	}

	private Token peek() {
		return tokens.get(current);
	}

	private Token peekNext() {
		return current + 1 >= tokens.size() ? tokens.get(tokens.size() - 1) : tokens.get(current + 1);
	}

	private Token previous() {
		return tokens.get(current - 1);
	}

	private static String removeSurrounding(String value, String delimiter) {
		if (value.length() >= delimiter.length() * 2
				&& value.startsWith(delimiter)
				&& value.endsWith(delimiter)) {
			return value.substring(delimiter.length(), value.length() - delimiter.length());
		}
		return value;
	}

	private static Double toDoubleOrNull(String value) {
		try {
			return Double.parseDouble(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static Long toLongOrNull(String value) {
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static double parseDoubleOr(String value, double fallback) {
		Double parsed = toDoubleOrNull(value);
		return parsed != null ? parsed : fallback;
	}
}
