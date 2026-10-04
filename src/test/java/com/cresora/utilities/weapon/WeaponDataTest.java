package com.cresora.utilities.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link WeaponData} normalization, definition-aware clamping, and codecs. */
class WeaponDataTest {
	@BeforeAll
	static void initRegistry() {
		WeaponContentRegistry.init();
	}

	@Test
	void blankWeaponIdNormalizesToDefault() {
		WeaponData blank = new WeaponData("", WeaponRarity.FOUR_STAR, 10, 2, 0, 1);
		assertEquals("rondo_melody", blank.normalized().weaponId());

		WeaponData whitespace = new WeaponData("   \t", WeaponRarity.FOUR_STAR, 10, 2, 0, 1);
		assertEquals("rondo_melody", whitespace.normalized().weaponId());
	}

	@Test
	void levelClampingToAbsoluteLimits() {
		WeaponData underflow = new WeaponData("lakeside_stride", WeaponRarity.FIVE_STAR, -10, -5, -1, 0);
		WeaponData normalizedUnder = underflow.normalized();
		assertEquals(1, normalizedUnder.baseLevel());
		assertEquals(1, normalizedUnder.skillLevel());
		assertEquals(0, normalizedUnder.breakthrough());
		assertEquals(1, normalizedUnder.spiritBondStage());

		WeaponData overflow = new WeaponData("lakeside_stride", WeaponRarity.FIVE_STAR, 500, 99, 10, 20);
		WeaponData normalizedOver = overflow.normalized();
		assertEquals(80, normalizedOver.baseLevel());
		assertEquals(10, normalizedOver.skillLevel());
		assertEquals(2, normalizedOver.breakthrough());
		assertEquals(6, normalizedOver.spiritBondStage());
	}

	@Test
	void definitionAwareNormalizationRespectsBreakthroughCaps() {
		WeaponDefinition lakeside = WeaponContentRegistry.requireWeapon("lakeside_stride");

		// At breakthrough 0: base level cap is 30, max skill level is 3
		WeaponData dataBt0 = new WeaponData("other_id", WeaponRarity.FIVE_STAR, 50, 8, 0, 1);
		WeaponData normalizedBt0 = dataBt0.normalized(lakeside);
		assertEquals(lakeside.id(), normalizedBt0.weaponId());
		assertEquals(30, normalizedBt0.baseLevel());
		assertEquals(3, normalizedBt0.skillLevel());
		assertEquals(0, normalizedBt0.breakthrough());

		// At breakthrough 1: base level cap is 45, max skill level is 7
		WeaponData dataBt1 = new WeaponData("other_id", WeaponRarity.FIVE_STAR, 50, 8, 1, 1);
		WeaponData normalizedBt1 = dataBt1.normalized(lakeside);
		assertEquals(45, normalizedBt1.baseLevel());
		assertEquals(7, normalizedBt1.skillLevel());
		assertEquals(1, normalizedBt1.breakthrough());

		// At breakthrough 2: base level cap is 60 (for lakeside maxBaseLevel=60), max skill level is 10
		WeaponData dataBt2 = new WeaponData("other_id", WeaponRarity.FIVE_STAR, 70, 15, 2, 1);
		WeaponData normalizedBt2 = dataBt2.normalized(lakeside);
		assertEquals(60, normalizedBt2.baseLevel());
		assertEquals(10, normalizedBt2.skillLevel());
		assertEquals(2, normalizedBt2.breakthrough());
	}

	@Test
	void codecEncodesAndDecodesCorrectly() {
		WeaponData original = new WeaponData("lakeside_stride", WeaponRarity.FIVE_STAR, 25, 4, 1, 2);
		JsonElement json = WeaponData.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
		assertNotNull(json);

		WeaponData decoded = WeaponData.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
		assertEquals(original, decoded);
	}

	@Test
	void codecAppliesDefaultsForOptionalFields() {
		JsonObject obj = new JsonObject();
		obj.addProperty("weaponId", "lakeside_stride");
		obj.addProperty("rarity", "5_star");
		obj.addProperty("baseLevel", 15);
		obj.addProperty("skillLevel", 2);

		WeaponData parsed = WeaponData.CODEC.parse(JsonOps.INSTANCE, obj).getOrThrow();
		assertEquals(0, parsed.breakthrough());
		assertEquals(1, parsed.spiritBondStage());
	}
}
