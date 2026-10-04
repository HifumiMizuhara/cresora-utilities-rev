package com.cresora.utilities.weapon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** A direct weapon drop tier keyed by rarity. */
public record WeaponDropTier(WeaponRarity rarity, int minMobLevel, double chance) {
	public static final Codec<WeaponDropTier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			WeaponRarity.CODEC.fieldOf("rarity").forGetter(WeaponDropTier::rarity),
			Codec.INT.fieldOf("minMobLevel").forGetter(WeaponDropTier::minMobLevel),
			Codec.DOUBLE.fieldOf("chance").forGetter(WeaponDropTier::chance)
	).apply(instance, WeaponDropTier::new));
}
