package com.cresora.utilities.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.cresora.utilities.weapon.WeaponContentRegistry;
import com.cresora.utilities.weapon.WeaponDefinition;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link WeaponSkillRegistry}. */
class WeaponSkillRegistryTest {
	@BeforeAll
	static void initRegistry() {
		WeaponContentRegistry.init();
		WeaponSkillRegistry.initialize();
	}

	@Test
	void registersBuiltInSkills() {
		WeaponSkillHandler handler = WeaponSkillRegistry.getHandler("current_hp_true_damage");
		assertNotNull(handler);
		assertTrue(handler instanceof CurrentHpTrueDamageSkill);
	}

	@Test
	void unknownHandlerReturnsNull() {
		assertNull(WeaponSkillRegistry.getHandler("non_existent_skill_id"));
	}

	@Test
	void resolvesWeaponDefinitionBySkillEffectId() {
		WeaponDefinition definition = WeaponSkillRegistry.getDefinition("current_hp_true_damage");
		assertNotNull(definition);
		assertEquals("lakeside_stride", definition.id());

		assertNull(WeaponSkillRegistry.getDefinition("unknown_effect_id"));
	}

	@Test
	void registersAndRetrievesSubSkills() {
		WeaponSkillRegistry.registerSubSkill("parent_skill", "sub_a");
		WeaponSkillRegistry.registerSubSkill("parent_skill", "sub_b");

		List<String> subSkills = WeaponSkillRegistry.subSkillEffectIds("parent_skill");
		assertTrue(subSkills.contains("sub_a"));
		assertTrue(subSkills.contains("sub_b"));

		assertTrue(WeaponSkillRegistry.subSkillEffectIds("no_such_parent").isEmpty());
	}
}
