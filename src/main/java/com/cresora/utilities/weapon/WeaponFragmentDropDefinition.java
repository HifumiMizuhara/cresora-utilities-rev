package com.cresora.utilities.weapon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Fragment drop configuration for a weapon. */
public record WeaponFragmentDropDefinition(
		int minMobLevel,
		double chance,
		int minCount,
		int maxCount,
		int bonusCountAtLevel70
) {
	public static final Codec<WeaponFragmentDropDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("minMobLevel").forGetter(WeaponFragmentDropDefinition::minMobLevel),
			Codec.DOUBLE.fieldOf("chance").forGetter(WeaponFragmentDropDefinition::chance),
			Codec.INT.fieldOf("minCount").forGetter(WeaponFragmentDropDefinition::minCount),
			Codec.INT.fieldOf("maxCount").forGetter(WeaponFragmentDropDefinition::maxCount),
			Codec.INT.fieldOf("bonusCountAtLevel70").forGetter(WeaponFragmentDropDefinition::bonusCountAtLevel70)
	).apply(instance, WeaponFragmentDropDefinition::new));
}
