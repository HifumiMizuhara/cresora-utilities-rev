package com.cresora.utilities.weapon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** A single spirit-bond stage of a weapon. */
public record WeaponSpiritBondStageDefinition(int stage, String titleKey, String storyKey) {
	public static final Codec<WeaponSpiritBondStageDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("stage").forGetter(WeaponSpiritBondStageDefinition::stage),
			Codec.STRING.fieldOf("titleKey").forGetter(WeaponSpiritBondStageDefinition::titleKey),
			Codec.STRING.fieldOf("storyKey").forGetter(WeaponSpiritBondStageDefinition::storyKey)
	).apply(instance, WeaponSpiritBondStageDefinition::new));
}
