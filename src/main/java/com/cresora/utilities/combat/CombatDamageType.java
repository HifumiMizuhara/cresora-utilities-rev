package com.cresora.utilities.combat;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;

/** Damage type of a weapon skill. Java port of {@code CombatDamageType.kt}. */
public enum CombatDamageType {
	PHYSICAL("physical"),
	ARCANE("arcane");

	private static final Map<String, CombatDamageType> BY_ID =
			Stream.of(values()).collect(Collectors.toMap(CombatDamageType::id, Function.identity()));

	private final String id;

	CombatDamageType(String id) {
		this.id = id;
	}

	public String id() {
		return id;
	}

	public String translationKey() {
		return "item.cresora.weapon.damage_type." + id;
	}

	public static final Codec<CombatDamageType> CODEC = Codec.STRING.xmap(
			id -> {
				CombatDamageType type = BY_ID.get(id);
				if (type == null) {
					throw new IllegalArgumentException("Unknown combat damage type: " + id);
				}
				return type;
			},
			CombatDamageType::id);
}
