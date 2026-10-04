package com.cresora.utilities.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Tests the weapon upgrade cost/cap arithmetic. */
class WeaponUpgradeServiceTest {
	@Test
	void levelCapScalesWithBreakthrough() {
		WeaponDefinition rondo = WeaponContentRegistry.requireWeapon("rondo_melody");
		assertEquals(30, WeaponUpgradeService.levelCap(rondo, 0));
		assertEquals(45, WeaponUpgradeService.levelCap(rondo, 1));
		assertEquals(60, WeaponUpgradeService.levelCap(rondo, 2));
	}

	@Test
	void skillLevelCapByBreakthrough() {
		assertEquals(3, WeaponUpgradeService.maxSkillLevelForBreakthrough(0));
		assertEquals(7, WeaponUpgradeService.maxSkillLevelForBreakthrough(1));
		assertEquals(10, WeaponUpgradeService.maxSkillLevelForBreakthrough(2));
	}

	@Test
	void baseUpgradeCostTiers() {
		WeaponDefinition rondo = WeaponContentRegistry.requireWeapon("rondo_melody");
		assertEquals(430, WeaponUpgradeService.baseUpgradeCost(rondo, 1));
		assertEquals(7_250, WeaponUpgradeService.baseUpgradeCost(rondo, 25));
		assertEquals(31_000, WeaponUpgradeService.baseUpgradeCost(rondo, 50));
	}

	@Test
	void breakthroughCosts() {
		assertEquals(20, WeaponUpgradeService.breakthroughFragmentCost(WeaponRarity.FIVE_STAR, 2));
		assertEquals(40_000, WeaponUpgradeService.breakthroughCscCost(WeaponRarity.FIVE_STAR, 1));
		assertEquals(8, WeaponUpgradeService.breakthroughRoleMaterialCost(WeaponRarity.FIVE_STAR, 1));
		assertEquals(0, WeaponUpgradeService.breakthroughCscCost(WeaponRarity.FIVE_STAR, 0));
	}
}
