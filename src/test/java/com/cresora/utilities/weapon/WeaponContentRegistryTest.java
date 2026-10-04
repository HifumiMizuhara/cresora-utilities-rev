package com.cresora.utilities.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cresora.utilities.combat.CombatDamageType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Verifies the built-in weapon content bundle and registry lookups. */
class WeaponContentRegistryTest {
	@BeforeAll
	static void loadDefaults() {
		WeaponContentRegistry.init();
	}

	@Test
	void loadsAllBuiltInWeapons() {
		assertEquals(11, WeaponContentRegistry.weaponDefinitions().size());
	}

	@Test
	void resolvesWeaponDefinitionAndSkill() {
		WeaponDefinition rondo = WeaponContentRegistry.requireWeapon("rondo_melody");
		assertEquals("minecraft:wooden_sword", rondo.baseItemId());
		assertEquals("shield", rondo.skill().effectId());
		assertEquals(15, rondo.skill().durationSeconds());
		assertEquals(WeaponZone.SUPPORT, rondo.zone());
		assertEquals(CombatDamageType.PHYSICAL, rondo.damageType());
		assertEquals("item.cresora-utilities.rondo_melody", rondo.translationKey());
	}

	@Test
	void singleSkillWeaponResolvesForAnySkillSlot() {
		WeaponDefinition rondo = WeaponContentRegistry.requireWeapon("rondo_melody");
		assertEquals(1, rondo.skillCount());
		assertEquals("shield", rondo.skillForSlot(0).effectId());
		// A weapon with a single skill fires it from any skill key.
		assertEquals("shield", rondo.skillForSlot(2).effectId());
	}

	@Test
	void everyWeaponIsTaggedWithItsZone() {
		assertEquals(WeaponZone.SUPPORT, WeaponContentRegistry.requireWeapon("rondo_melody").zone());
		assertEquals(WeaponZone.HEAL, WeaponContentRegistry.requireWeapon("masquerade_invitation").zone());
		assertEquals(WeaponZone.SUPPORT, WeaponContentRegistry.requireWeapon("pastoral_flute_reverie").zone());
		assertEquals(WeaponZone.SUPPORT, WeaponContentRegistry.requireWeapon("requiem_toward_dawn").zone());
		assertEquals(WeaponZone.HEAL, WeaponContentRegistry.requireWeapon("gaoshan_liushui").zone());
		assertEquals(WeaponZone.SUPPORT, WeaponContentRegistry.requireWeapon("cadenza_allegro").zone());
		assertEquals(WeaponZone.MAIN_WEAPON, WeaponContentRegistry.requireWeapon("lakeside_stride").zone());
		assertEquals(WeaponZone.SUPPORT, WeaponContentRegistry.requireWeapon("kyokusui_no_ryusho").zone());
		assertEquals(WeaponZone.SUB_WEAPON, WeaponContentRegistry.requireWeapon("hanwu_juanxue").zone());
		assertEquals(WeaponZone.SUB_WEAPON, WeaponContentRegistry.requireWeapon("dark_lux").zone());
	}

	@Test
	void zoneMapsToWeaponBarSlot() {
		assertEquals(0, WeaponZone.MAIN_WEAPON.slot());
		assertEquals(1, WeaponZone.SUB_WEAPON.slot());
		assertEquals(2, WeaponZone.SUPPORT.slot());
		assertEquals(3, WeaponZone.HEAL.slot());
		assertEquals(WeaponZone.MAIN_WEAPON, WeaponZone.forSlot(0));
		assertEquals(WeaponZone.HEAL, WeaponZone.forSlot(3));
		assertNull(WeaponZone.forSlot(4));
	}

	@Test
	void carriesArcaneAndAttackCurve() {
		WeaponDefinition hanwu = WeaponContentRegistry.requireWeapon("hanwu_juanxue");
		assertEquals(CombatDamageType.ARCANE, hanwu.damageType());
		assertEquals(6, hanwu.attackCurve().size());
		assertEquals(1, hanwu.attackCurve().get(0).level());
		assertEquals(60, hanwu.attackCurve().get(5).level());
	}

	@Test
	void lookupMissesReturnNullOrThrow() {
		assertNull(WeaponContentRegistry.weapon("does_not_exist"));
		assertThrows(IllegalStateException.class, () -> WeaponContentRegistry.requireWeapon("does_not_exist"));
	}

	@Test
	void resolvesDefinitionByFragmentId() {
		WeaponDefinition byFragment = WeaponContentRegistry.definitionByFragmentId("lakeside_stride_fragment");
		assertNotNull(byFragment);
		assertEquals("lakeside_stride", byFragment.id());
	}

	@Test
	void definitionsAreSortedById() {
		var definitions = WeaponContentRegistry.weaponDefinitions();
		for (int i = 1; i < definitions.size(); i++) {
			assertTrue(definitions.get(i - 1).id().compareTo(definitions.get(i).id()) < 0);
		}
	}
}
