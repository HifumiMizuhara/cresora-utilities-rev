package com.cresora.utilities.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link WeaponRarity}. */
class WeaponRarityTest {
	@Test
	void starCountsAndIdsMatchTiers() {
		assertEquals("2_star", WeaponRarity.TWO_STAR.id());
		assertEquals(2, WeaponRarity.TWO_STAR.stars());

		assertEquals("3_star", WeaponRarity.THREE_STAR.id());
		assertEquals(3, WeaponRarity.THREE_STAR.stars());

		assertEquals("4_star", WeaponRarity.FOUR_STAR.id());
		assertEquals(4, WeaponRarity.FOUR_STAR.stars());

		assertEquals("5_star", WeaponRarity.FIVE_STAR.id());
		assertEquals(5, WeaponRarity.FIVE_STAR.stars());
	}

	@Test
	void fragmentItemIdsAndTranslationKeys() {
		assertEquals("weapon_fragment_3_star", WeaponRarity.THREE_STAR.fragmentItemId());
		assertEquals("weapon_fragment_4_star", WeaponRarity.FOUR_STAR.fragmentItemId());
		assertEquals("weapon_fragment_5_star", WeaponRarity.FIVE_STAR.fragmentItemId());

		assertEquals("item.cresora.weapon.rarity.5_star", WeaponRarity.FIVE_STAR.translationKey());
		assertEquals("item.cresora-utilities.weapon_fragment_5_star", WeaponRarity.FIVE_STAR.fragmentTranslationKey());
	}

	@Test
	void codecRoundTrip() {
		JsonElement encoded = WeaponRarity.CODEC.encodeStart(JsonOps.INSTANCE, WeaponRarity.FIVE_STAR).getOrThrow();
		assertEquals(new JsonPrimitive("5_star"), encoded);

		WeaponRarity decoded = WeaponRarity.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive("5_star")).getOrThrow();
		assertEquals(WeaponRarity.FIVE_STAR, decoded);
	}

	@Test
	void codecThrowsOnUnknownRarity() {
		assertThrows(RuntimeException.class, () ->
				WeaponRarity.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive("6_star")).getOrThrow());
	}
}
