package com.cresora.utilities.weapon;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.cresora.utilities.combat.CombatDamageType;

/** The flat set of combat stats shared by weapon definitions. */
public record WeaponStats(
		double baseAttackDamage,
		double attackDamagePerLevel,
		double totalAttackSpeed,
		int maxBaseLevel,
		int maxSkillLevel,
		double critRateBonusPercent,
		double maxAllDamageBonusPercent,
		double hpBonusPercent,
		CombatDamageType damageType,
		WeaponRole role
) {
	public static final MapCodec<WeaponStats> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			com.mojang.serialization.Codec.DOUBLE.fieldOf("baseAttackDamage").forGetter(WeaponStats::baseAttackDamage),
			com.mojang.serialization.Codec.DOUBLE.fieldOf("attackDamagePerLevel").forGetter(WeaponStats::attackDamagePerLevel),
			com.mojang.serialization.Codec.DOUBLE.fieldOf("totalAttackSpeed").forGetter(WeaponStats::totalAttackSpeed),
			com.mojang.serialization.Codec.INT.fieldOf("maxBaseLevel").forGetter(WeaponStats::maxBaseLevel),
			com.mojang.serialization.Codec.INT.fieldOf("maxSkillLevel").forGetter(WeaponStats::maxSkillLevel),
			com.mojang.serialization.Codec.DOUBLE.optionalFieldOf("critRateBonusPercent", 0.0).forGetter(WeaponStats::critRateBonusPercent),
			com.mojang.serialization.Codec.DOUBLE.optionalFieldOf("maxAllDamageBonusPercent", 0.0).forGetter(WeaponStats::maxAllDamageBonusPercent),
			com.mojang.serialization.Codec.DOUBLE.optionalFieldOf("hpBonusPercent", 0.0).forGetter(WeaponStats::hpBonusPercent),
			CombatDamageType.CODEC.optionalFieldOf("damageType", CombatDamageType.PHYSICAL).forGetter(WeaponStats::damageType),
			WeaponRole.CODEC.optionalFieldOf("role", WeaponRole.GUARD).forGetter(WeaponStats::role)
	).apply(instance, WeaponStats::new));
}
