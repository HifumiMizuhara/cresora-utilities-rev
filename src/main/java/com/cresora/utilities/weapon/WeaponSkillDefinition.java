package com.cresora.utilities.weapon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Static definition of a weapon's active skill. */
public record WeaponSkillDefinition(
		String effectId,
		int durationSeconds,
		int cooldownSeconds,
		double baseValue,
		double valuePerLevel,
		double radiusMeters,
		double secondaryBaseValue,
		double secondaryValuePerLevel,
		double tickIntervalSeconds,
		String note
) {
	public static final Codec<WeaponSkillDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("effectId").forGetter(WeaponSkillDefinition::effectId),
			Codec.INT.fieldOf("durationSeconds").forGetter(WeaponSkillDefinition::durationSeconds),
			Codec.INT.fieldOf("cooldownSeconds").forGetter(WeaponSkillDefinition::cooldownSeconds),
			Codec.DOUBLE.fieldOf("baseValue").forGetter(WeaponSkillDefinition::baseValue),
			Codec.DOUBLE.fieldOf("valuePerLevel").forGetter(WeaponSkillDefinition::valuePerLevel),
			Codec.DOUBLE.optionalFieldOf("radiusMeters", 0.0).forGetter(WeaponSkillDefinition::radiusMeters),
			Codec.DOUBLE.optionalFieldOf("secondaryBaseValue", 0.0).forGetter(WeaponSkillDefinition::secondaryBaseValue),
			Codec.DOUBLE.optionalFieldOf("secondaryValuePerLevel", 0.0).forGetter(WeaponSkillDefinition::secondaryValuePerLevel),
			Codec.DOUBLE.optionalFieldOf("tickIntervalSeconds", 1.0).forGetter(WeaponSkillDefinition::tickIntervalSeconds),
			Codec.STRING.optionalFieldOf("note").forGetter(s -> java.util.Optional.ofNullable(s.note()))
	).apply(instance, (effectId, durationSeconds, cooldownSeconds, baseValue, valuePerLevel,
			radiusMeters, secondaryBaseValue, secondaryValuePerLevel, tickIntervalSeconds, note) ->
			new WeaponSkillDefinition(effectId, durationSeconds, cooldownSeconds, baseValue, valuePerLevel,
					radiusMeters, secondaryBaseValue, secondaryValuePerLevel, tickIntervalSeconds, note.orElse(null))));

	public static WeaponSkillDefinition of(String effectId, int durationSeconds, int cooldownSeconds,
			double baseValue, double valuePerLevel) {
		return new WeaponSkillDefinition(effectId, durationSeconds, cooldownSeconds, baseValue, valuePerLevel,
				0.0, 0.0, 0.0, 1.0, null);
	}
}
