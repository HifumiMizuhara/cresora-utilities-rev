package com.cresora.utilities.weapon;

import com.mojang.serialization.Codec;

/** Weapon rarity tiers. Java port of {@code WeaponRarity.kt}. */
public enum WeaponRarity {
	TWO_STAR("2_star", 2),
	THREE_STAR("3_star", 3),
	FOUR_STAR("4_star", 4),
	FIVE_STAR("5_star", 5);

	private final String id;
	private final int stars;

	WeaponRarity(String id, int stars) {
		this.id = id;
		this.stars = stars;
	}

	public String id() {
		return id;
	}

	public int stars() {
		return stars;
	}

	public String translationKey() {
		return "item.cresora.weapon.rarity." + id;
	}

	public String fragmentItemId() {
		return "weapon_fragment_" + id;
	}

	public String fragmentTranslationKey() {
		return "item.cresora-utilities." + fragmentItemId();
	}

	public static final Codec<WeaponRarity> CODEC = Codec.STRING.xmap(
			value -> {
				for (WeaponRarity rarity : values()) {
					if (rarity.id.equals(value)) {
						return rarity;
					}
				}
				throw new IllegalArgumentException("Unknown weapon rarity: " + value);
			},
			WeaponRarity::id);
}
