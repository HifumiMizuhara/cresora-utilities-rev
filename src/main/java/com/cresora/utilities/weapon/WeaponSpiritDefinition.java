package com.cresora.utilities.weapon;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Spirit companion definition attached to a weapon. */
public record WeaponSpiritDefinition(
		String nameKey,
		Map<String, String> voiceLines,
		List<WeaponSpiritBondStageDefinition> bondStages,
		String awakeningConditionKey
) {
	public static final Codec<WeaponSpiritDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("nameKey").forGetter(WeaponSpiritDefinition::nameKey),
			Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("voiceLines", Map.of())
					.forGetter(WeaponSpiritDefinition::voiceLines),
			WeaponSpiritBondStageDefinition.CODEC.listOf().optionalFieldOf("bondStages", List.of())
					.forGetter(WeaponSpiritDefinition::bondStages),
			Codec.STRING.optionalFieldOf("awakeningConditionKey")
					.forGetter(s -> Optional.ofNullable(s.awakeningConditionKey()))
	).apply(instance, (nameKey, voiceLines, bondStages, awakeningConditionKey) ->
			new WeaponSpiritDefinition(nameKey, voiceLines, bondStages, awakeningConditionKey.orElse(null))));
}
