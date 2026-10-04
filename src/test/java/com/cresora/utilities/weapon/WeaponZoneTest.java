package com.cresora.utilities.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link WeaponZone}. */
class WeaponZoneTest {
	@Test
	void slotMappingIsUniqueAndContinuous() {
		assertEquals(0, WeaponZone.MAIN_WEAPON.slot());
		assertEquals(1, WeaponZone.SUB_WEAPON.slot());
		assertEquals(2, WeaponZone.SUPPORT.slot());
		assertEquals(3, WeaponZone.HEAL.slot());
	}

	@Test
	void forSlotResolvesCorrectZones() {
		assertEquals(WeaponZone.MAIN_WEAPON, WeaponZone.forSlot(0));
		assertEquals(WeaponZone.SUB_WEAPON, WeaponZone.forSlot(1));
		assertEquals(WeaponZone.SUPPORT, WeaponZone.forSlot(2));
		assertEquals(WeaponZone.HEAL, WeaponZone.forSlot(3));

		assertNull(WeaponZone.forSlot(-1));
		assertNull(WeaponZone.forSlot(4));
		assertNull(WeaponZone.forSlot(100));
	}

	@Test
	void translationKeyFollowsNamingPattern() {
		assertEquals("item.cresora.weapon.zone.main_weapon", WeaponZone.MAIN_WEAPON.translationKey());
		assertEquals("item.cresora.weapon.zone.sub_weapon", WeaponZone.SUB_WEAPON.translationKey());
		assertEquals("item.cresora.weapon.zone.support", WeaponZone.SUPPORT.translationKey());
		assertEquals("item.cresora.weapon.zone.heal", WeaponZone.HEAL.translationKey());
	}

	@Test
	void codecEncodesAndDecodesCaseInsensitively() {
		JsonElement encoded = WeaponZone.CODEC.encodeStart(JsonOps.INSTANCE, WeaponZone.MAIN_WEAPON)
				.getOrThrow();
		assertEquals(new JsonPrimitive("main_weapon"), encoded);

		WeaponZone decoded = WeaponZone.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive("MAIN_WEAPON"))
				.getOrThrow();
		assertEquals(WeaponZone.MAIN_WEAPON, decoded);

		WeaponZone support = WeaponZone.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive("support"))
				.getOrThrow();
		assertEquals(WeaponZone.SUPPORT, support);
	}

	@Test
	void codecThrowsOnUnknownZone() {
		assertThrows(RuntimeException.class, () ->
				WeaponZone.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive("unknown_zone"))
						.getOrThrow());
	}
}
