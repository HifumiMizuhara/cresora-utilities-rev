package com.cresora.utilities.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.cresora.utilities.combat.CombatDamageType;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link WeaponDefinition} multi-skill dispatch and properties. */
class WeaponDefinitionTest {
	@Test
	void emptySkillsListFallsBackToNone() {
		WeaponDefinition def = new WeaponDefinition(
				"test_sword", "minecraft:iron_sword", 10.0, 1.0, 1.6, 60, 10,
				0.0, 0.0, 0.0, CombatDamageType.PHYSICAL, WeaponZone.MAIN_WEAPON, List.of(),
				List.of(), WeaponUpgradeDefinition.of(0, 0, 0),
				new WeaponCraftDefinition("test_sword", "iron_sword", 0, WeaponRarity.FOUR_STAR, 1, 1),
				new WeaponDropDefinition(new WeaponFragmentDropDefinition(0, 0.0, 0, 0, 0), List.of(), 0.0, 0.0),
				null, null);

		assertEquals(1, def.skillCount());
		assertEquals("none", def.skill().effectId());
		assertEquals("none", def.skillForSlot(0).effectId());
		assertEquals("none", def.skillForSlot(1).effectId());
		assertEquals("none", def.skillForSlot(2).effectId());
	}

	@Test
	void singleSkillWeaponFiresFromAnySlot() {
		WeaponSkillDefinition singleSkill = WeaponSkillDefinition.of("current_hp_true_damage", 10, 10, 5.0, 5.0);
		WeaponDefinition def = new WeaponDefinition(
				"test_blade", "minecraft:diamond_sword", 12.0, 1.5, 1.6, 60, 10,
				5.0, 0.0, 0.0, CombatDamageType.ARCANE, WeaponZone.MAIN_WEAPON, List.of(),
				List.of(singleSkill), WeaponUpgradeDefinition.of(0, 0, 0),
				new WeaponCraftDefinition("test_blade", "diamond_sword", 0, WeaponRarity.FIVE_STAR, 1, 1),
				new WeaponDropDefinition(new WeaponFragmentDropDefinition(0, 0.0, 0, 0, 0), List.of(), 0.0, 0.0),
				null, null);

		assertEquals(1, def.skillCount());
		assertEquals("current_hp_true_damage", def.skill().effectId());
		assertEquals("current_hp_true_damage", def.skillForSlot(0).effectId());
		assertEquals("current_hp_true_damage", def.skillForSlot(1).effectId());
		assertEquals("current_hp_true_damage", def.skillForSlot(2).effectId());
		assertEquals("current_hp_true_damage", def.skillForSlot(99).effectId());
	}

	@Test
	void multiSkillWeaponResolvesStrictlyBySlot() {
		WeaponSkillDefinition skill1 = WeaponSkillDefinition.of("skill_a", 5, 0, 1.0, 1.0);
		WeaponSkillDefinition skill2 = WeaponSkillDefinition.of("skill_b", 10, 0, 2.0, 2.0);
		WeaponSkillDefinition skill3 = WeaponSkillDefinition.of("skill_c", 15, 0, 3.0, 3.0);

		WeaponDefinition def = new WeaponDefinition(
				"multi_blade", "minecraft:netherite_sword", 15.0, 2.0, 1.6, 60, 10,
				10.0, 0.0, 0.0, CombatDamageType.PHYSICAL, WeaponZone.MAIN_WEAPON, List.of(),
				List.of(skill1, skill2, skill3), WeaponUpgradeDefinition.of(0, 0, 0),
				new WeaponCraftDefinition("multi_blade", "netherite_sword", 0, WeaponRarity.FIVE_STAR, 1, 1),
				new WeaponDropDefinition(new WeaponFragmentDropDefinition(0, 0.0, 0, 0, 0), List.of(), 0.0, 0.0),
				null, null);

		assertEquals(3, def.skillCount());
		assertEquals("skill_a", def.skill(0).effectId());
		assertEquals("skill_b", def.skill(1).effectId());
		assertEquals("skill_c", def.skill(2).effectId());

		assertEquals("skill_a", def.skillForSlot(0).effectId());
		assertEquals("skill_b", def.skillForSlot(1).effectId());
		assertEquals("skill_c", def.skillForSlot(2).effectId());

		assertNull(def.skillForSlot(3));
		assertNull(def.skillForSlot(-1));
	}

	@Test
	void translationKeyConventions() {
		WeaponDefinition dummy = WeaponDefinition.DUMMY;
		assertEquals("item.cresora-utilities.dummy", dummy.translationKey());
		assertEquals("item.cresora-utilities.dummy", dummy.fragmentTranslationKey());
	}

	@Test
	void builderBuildsWeaponCorrectly() {
		WeaponSkillDefinition skill = WeaponSkillDefinition.of("shield", 12, 10, 5.0, 1.0);
		WeaponDefinition built = WeaponDefinition.builder(
				"test_builder", "minecraft:stick", 8.0, 0.5, 1.6, 40, 5,
				skill, WeaponUpgradeDefinition.of(1, 2, 3),
				new WeaponCraftDefinition("test_builder", "stick", 1, WeaponRarity.THREE_STAR, 1, 1),
				new WeaponDropDefinition(new WeaponFragmentDropDefinition(0, 0.0, 0, 0, 0), List.of(), 0.0, 0.0))
				.critRateBonusPercent(15.0)
				.zone(WeaponZone.SUPPORT)
				.damageType(CombatDamageType.ARCANE)
				.build();

		assertEquals("test_builder", built.id());
		assertEquals("minecraft:stick", built.baseItemId());
		assertEquals(WeaponZone.SUPPORT, built.zone());
		assertEquals(CombatDamageType.ARCANE, built.damageType());
		assertEquals(15.0, built.critRateBonusPercent());
		assertEquals(1, built.skillCount());
		assertEquals("shield", built.skill().effectId());
	}
}
