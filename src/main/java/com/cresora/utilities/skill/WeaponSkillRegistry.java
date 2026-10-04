package com.cresora.utilities.skill;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.cresora.utilities.weapon.WeaponContentRegistry;
import com.cresora.utilities.weapon.WeaponDefinition;

/**
 * Maps a skill's {@code effectId} to its handler. Java port of {@code WeaponSkillRegistry.kt}.
 *
 * <p>The original populated this from the compiler-generated {@code CompiledWeaponSkillRegistry};
 * this weapon-only slice registers the lakeside_stride skill ({@code current_hp_true_damage})
 * directly. Additional skills are added here until the DSL code generator is ported.
 */
public final class WeaponSkillRegistry {
	private static final Map<String, WeaponSkillHandler> HANDLERS = new LinkedHashMap<>();
	private static final Map<String, List<String>> SUB_SKILLS_BY_PARENT = new LinkedHashMap<>();

	static {
		register("current_hp_true_damage", new CurrentHpTrueDamageSkill());
	}

	private WeaponSkillRegistry() {
	}

	/** Forces class loading so the static registration runs. Called from the mod initialiser. */
	public static void initialize() {
	}

	public static void register(String effectId, WeaponSkillHandler handler) {
		HANDLERS.put(effectId, handler);
	}

	public static void registerSubSkill(String parentEffectId, String subSkillEffectId) {
		SUB_SKILLS_BY_PARENT.computeIfAbsent(parentEffectId, key -> new ArrayList<>()).add(subSkillEffectId);
	}

	public static WeaponSkillHandler getHandler(String effectId) {
		return HANDLERS.get(effectId);
	}

	public static WeaponDefinition getDefinition(String effectId) {
		for (WeaponDefinition definition : WeaponContentRegistry.weaponDefinitions()) {
			if (definition.skill() != null && definition.skill().effectId().equals(effectId)) {
				return definition;
			}
		}
		return null;
	}

	public static Map<String, WeaponSkillHandler> allHandlers() {
		return HANDLERS;
	}

	public static List<String> subSkillEffectIds(String parentEffectId) {
		return SUB_SKILLS_BY_PARENT.getOrDefault(parentEffectId, List.of());
	}
}
