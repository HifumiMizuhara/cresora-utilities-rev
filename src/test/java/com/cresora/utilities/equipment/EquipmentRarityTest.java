package com.cresora.utilities.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link EquipmentRarity}. */
class EquipmentRarityTest {
	@Test
	void statLimitsAndLevelsMatchTiers() {
		assertEquals(8, EquipmentRarity.THREE_STAR.maxLevel());
		assertEquals(1, EquipmentRarity.THREE_STAR.initialSubStatCount());
		assertEquals(2, EquipmentRarity.THREE_STAR.subStatLimit());

		assertEquals(12, EquipmentRarity.FOUR_STAR.maxLevel());
		assertEquals(2, EquipmentRarity.FOUR_STAR.initialSubStatCount());
		assertEquals(3, EquipmentRarity.FOUR_STAR.subStatLimit());

		assertEquals(16, EquipmentRarity.FIVE_STAR.maxLevel());
		assertEquals(3, EquipmentRarity.FIVE_STAR.initialSubStatCount());
		assertEquals(4, EquipmentRarity.FIVE_STAR.subStatLimit());
	}

	@Test
	void translationKeyFollowsPattern() {
		assertEquals("item.cresora.equipment.rarity.3_star", EquipmentRarity.THREE_STAR.translationKey());
		assertEquals("item.cresora.equipment.rarity.4_star", EquipmentRarity.FOUR_STAR.translationKey());
		assertEquals("item.cresora.equipment.rarity.5_star", EquipmentRarity.FIVE_STAR.translationKey());
	}

	@Test
	void codecEncodesAndDecodes() {
		JsonElement encoded = EquipmentRarity.CODEC.encodeStart(JsonOps.INSTANCE, EquipmentRarity.FIVE_STAR).getOrThrow();
		assertEquals(new JsonPrimitive("5_star"), encoded);

		EquipmentRarity decoded = EquipmentRarity.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive("5_star")).getOrThrow();
		assertEquals(EquipmentRarity.FIVE_STAR, decoded);
	}

	@Test
	void codecRejectsInvalidRarity() {
		assertThrows(RuntimeException.class, () ->
				EquipmentRarity.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive("unknown_rarity")).getOrThrow());
	}
}
