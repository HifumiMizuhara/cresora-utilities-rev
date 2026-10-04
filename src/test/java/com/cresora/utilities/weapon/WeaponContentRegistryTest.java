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
		assertEquals(WeaponRole.GUARD, rondo.role());
		assertEquals(CombatDamageType.PHYSICAL, rondo.damageType());
		assertEquals("item.cresora-utilities.rondo_melody", rondo.translationKey());
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
