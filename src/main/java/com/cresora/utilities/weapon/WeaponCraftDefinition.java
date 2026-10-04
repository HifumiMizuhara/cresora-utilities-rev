package com.cresora.utilities.weapon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Crafting recipe of a weapon from its fragments. */
public record WeaponCraftDefinition(
		String fragmentItemId,
		String fragmentBaseItemId,
		int fragmentsRequired,
		WeaponRarity craftedRarity,
		int craftedBaseLevel,
		int craftedSkillLevel
) {
	public static final Codec<WeaponCraftDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("fragmentItemId").forGetter(WeaponCraftDefinition::fragmentItemId),
			Codec.STRING.fieldOf("fragmentBaseItemId").forGetter(WeaponCraftDefinition::fragmentBaseItemId),
			Codec.INT.fieldOf("fragmentsRequired").forGetter(WeaponCraftDefinition::fragmentsRequired),
			WeaponRarity.CODEC.fieldOf("craftedRarity").forGetter(WeaponCraftDefinition::craftedRarity),
			Codec.INT.fieldOf("craftedBaseLevel").forGetter(WeaponCraftDefinition::craftedBaseLevel),
			Codec.INT.fieldOf("craftedSkillLevel").forGetter(WeaponCraftDefinition::craftedSkillLevel)
	).apply(instance, WeaponCraftDefinition::new));
}
