package com.cresora.utilities.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Tests the lakeside_stride skill math and number formatting. */
class WeaponCombatSupportTest {
	@BeforeAll
	static void loadContent() {
		WeaponContentRegistry.init();
	}

	@Test
	void currentHpTrueDamageRatioUsesSkillValue() {
		WeaponDefinition lakeside = WeaponContentRegistry.requireWeapon("lakeside_stride");
		WeaponData data = WeaponStackSupport.defaultWeaponData(lakeside);
		// baseValue 0.0 + valuePerLevel 5.0 * skillLevel 1 => 5.0% of current HP
		assertEquals(0.05, WeaponCombatSupport.currentHpTrueDamageRatio(lakeside, data), 1.0e-9);
	}

	@Test
	void attackDamageIncludesCritCurveAndBreakthrough() {
		WeaponDefinition lakeside = WeaponContentRegistry.requireWeapon("lakeside_stride");
		WeaponData data = WeaponStackSupport.defaultWeaponData(lakeside);
		// attackCurve present, level 1 => first point 7.0, no breakthrough factor at bt 0.
		assertEquals(7.0, WeaponCombatSupport.attackDamage(lakeside, data), 1.0e-9);
		// +0.12 per breakthrough stage.
		WeaponData bt1 = new WeaponData(lakeside.id(), data.rarity(), 1, data.skillLevel(), 1, 1);
		assertTrue(WeaponCombatSupport.attackDamage(lakeside, bt1) > 7.0);
	}

	@Test
	void formatNumberTrimsTrailingZero() {
		assertEquals("2.5", WeaponSkillService.formatNumber(2.5));
		assertEquals("3", WeaponSkillService.formatNumber(3.0));
	}
}
