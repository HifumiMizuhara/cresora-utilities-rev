package com.cresora.utilities.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Plain JUnit tests for the Minecraft-independent weapon model. */
class WeaponModelTest {
	@Test
	void scopingRejectsOtherWeapons() {
		assertTrue(WeaponScopeSupport.isActiveWeaponScope("azure_blade", null));
		assertTrue(WeaponScopeSupport.isActiveWeaponScope("azure_blade", "azure_blade"));
		assertFalse(WeaponScopeSupport.isActiveWeaponScope("azure_blade", "crimson_flash"));
	}

	@Test
	void normalizedClampsOutOfRangeValues() {
		WeaponData raw = new WeaponData("  ", WeaponRarity.FIVE_STAR, 999, -5, 9, 0);
		WeaponData normalized = raw.normalized();

		assertEquals("rondo_melody", normalized.weaponId());
		assertEquals(80, normalized.baseLevel());
		assertEquals(1, normalized.skillLevel());
		assertEquals(2, normalized.breakthrough());
		assertEquals(1, normalized.spiritBondStage());
	}

	@Test
	void rarityMetadataIsStable() {
		assertEquals("5_star", WeaponRarity.FIVE_STAR.id());
		assertEquals(5, WeaponRarity.FIVE_STAR.stars());
		assertEquals("weapon_fragment_4_star", WeaponRarity.FOUR_STAR.fragmentItemId());
	}
}
