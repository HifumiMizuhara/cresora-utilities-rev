package com.cresora.utilities.weapon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.cresora.utilities.equipment.EquipmentRarity;

/** Upgrade coefficients and costs for a weapon. */
public record WeaponUpgradeDefinition(
		int baseCscQuadraticCoefficient,
		int skillCscLinearCoefficient,
		int skillCscQuadraticCoefficient,
		int baseFragmentCost,
		EquipmentRarity skillArtifactMinRarity,
		int skillArtifactCountPerLevel
) {
	public static final Codec<WeaponUpgradeDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("baseCscQuadraticCoefficient").forGetter(WeaponUpgradeDefinition::baseCscQuadraticCoefficient),
			Codec.INT.fieldOf("skillCscLinearCoefficient").forGetter(WeaponUpgradeDefinition::skillCscLinearCoefficient),
			Codec.INT.optionalFieldOf("skillCscQuadraticCoefficient", 0).forGetter(WeaponUpgradeDefinition::skillCscQuadraticCoefficient),
			Codec.INT.fieldOf("baseFragmentCost").forGetter(WeaponUpgradeDefinition::baseFragmentCost),
			EquipmentRarity.CODEC.optionalFieldOf("skillArtifactMinRarity").forGetter(u -> java.util.Optional.ofNullable(u.skillArtifactMinRarity())),
			Codec.INT.optionalFieldOf("skillArtifactCountPerLevel", 0).forGetter(WeaponUpgradeDefinition::skillArtifactCountPerLevel)
	).apply(instance, (baseCoeff, skillLinear, skillQuadratic, fragmentCost, artifactRarity, artifactCount) ->
			new WeaponUpgradeDefinition(baseCoeff, skillLinear, skillQuadratic, fragmentCost,
					artifactRarity.orElse(null), artifactCount)));

	public static WeaponUpgradeDefinition of(int baseCscQuadraticCoefficient, int skillCscLinearCoefficient, int baseFragmentCost) {
		return new WeaponUpgradeDefinition(baseCscQuadraticCoefficient, skillCscLinearCoefficient, 0, baseFragmentCost, null, 0);
	}
}
