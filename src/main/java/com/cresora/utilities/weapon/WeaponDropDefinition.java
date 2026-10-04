package com.cresora.utilities.weapon;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Drop configuration of a weapon (fragments + direct drops). */
public record WeaponDropDefinition(
		WeaponFragmentDropDefinition fragmentDrop,
		List<WeaponDropTier> directDropTiers,
		double directDropBaseLevelMultiplier,
		double fiveStarChanceMultiplierAtLevel70
) {
	public static final Codec<WeaponDropDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			WeaponFragmentDropDefinition.CODEC.fieldOf("fragmentDrop").forGetter(WeaponDropDefinition::fragmentDrop),
			WeaponDropTier.CODEC.listOf().fieldOf("directDropTiers").forGetter(WeaponDropDefinition::directDropTiers),
			Codec.DOUBLE.fieldOf("directDropBaseLevelMultiplier").forGetter(WeaponDropDefinition::directDropBaseLevelMultiplier),
			Codec.DOUBLE.fieldOf("fiveStarChanceMultiplierAtLevel70").forGetter(WeaponDropDefinition::fiveStarChanceMultiplierAtLevel70)
	).apply(instance, WeaponDropDefinition::new));
}
