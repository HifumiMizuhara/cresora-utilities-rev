package com.cresora.utilities.equipment;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;

/**
 * Artifact/equipment rarity. Java port of {@code EquipmentRarity.kt}; only the metadata the
 * weapon upgrade definitions reference is kept.
 */
public enum EquipmentRarity {
	THREE_STAR("3_star", 8, 1, 2),
	FOUR_STAR("4_star", 12, 2, 3),
	FIVE_STAR("5_star", 16, 3, 4);

	private static final Map<String, EquipmentRarity> BY_ID =
			Stream.of(values()).collect(Collectors.toMap(EquipmentRarity::id, Function.identity()));

	private final String id;
	private final int maxLevel;
	private final int initialSubStatCount;
	private final int subStatLimit;

	EquipmentRarity(String id, int maxLevel, int initialSubStatCount, int subStatLimit) {
		this.id = id;
		this.maxLevel = maxLevel;
		this.initialSubStatCount = initialSubStatCount;
		this.subStatLimit = subStatLimit;
	}

	public String id() {
		return id;
	}

	public int maxLevel() {
		return maxLevel;
	}

	public int initialSubStatCount() {
		return initialSubStatCount;
	}

	public int subStatLimit() {
		return subStatLimit;
	}

	public String translationKey() {
		return "item.cresora.equipment.rarity." + id;
	}

	public static final Codec<EquipmentRarity> CODEC = Codec.STRING.xmap(
			id -> {
				EquipmentRarity rarity = BY_ID.get(id);
				if (rarity == null) {
					throw new IllegalArgumentException("Unknown equipment rarity: " + id);
				}
				return rarity;
			},
			EquipmentRarity::id);
}
