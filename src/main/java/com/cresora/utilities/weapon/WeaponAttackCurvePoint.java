package com.cresora.utilities.weapon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** A single point on a weapon's level-to-attack-damage curve. */
public record WeaponAttackCurvePoint(int level, double attackDamage) {
	public static final Codec<WeaponAttackCurvePoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("level").forGetter(WeaponAttackCurvePoint::level),
			Codec.DOUBLE.fieldOf("attackDamage").forGetter(WeaponAttackCurvePoint::attackDamage)
	).apply(instance, WeaponAttackCurvePoint::new));
}
