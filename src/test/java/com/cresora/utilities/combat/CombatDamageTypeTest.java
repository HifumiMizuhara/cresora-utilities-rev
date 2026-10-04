package com.cresora.utilities.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link CombatDamageType}. */
class CombatDamageTypeTest {
	@Test
	void allDamageTypesHaveExpectedIds() {
		assertEquals("physical", CombatDamageType.PHYSICAL.id());
		assertEquals("arcane", CombatDamageType.ARCANE.id());
	}

	@Test
	void translationKeysFollowConvention() {
		for (CombatDamageType type : CombatDamageType.values()) {
			assertEquals("item.cresora.weapon.damage_type." + type.id(), type.translationKey());
		}
	}

	@Test
	void codecSerializesAndDeserializes() {
		JsonElement encoded = CombatDamageType.CODEC.encodeStart(JsonOps.INSTANCE, CombatDamageType.ARCANE)
				.getOrThrow();
		assertEquals(new JsonPrimitive("arcane"), encoded);

		CombatDamageType decoded = CombatDamageType.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive("arcane"))
				.getOrThrow();
		assertEquals(CombatDamageType.ARCANE, decoded);

		CombatDamageType physical = CombatDamageType.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive("physical"))
				.getOrThrow();
		assertEquals(CombatDamageType.PHYSICAL, physical);
	}

	@Test
	void codecRejectsInvalidDamageType() {
		assertThrows(RuntimeException.class, () ->
				CombatDamageType.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive("invalid_type"))
						.getOrThrow());
	}
}
