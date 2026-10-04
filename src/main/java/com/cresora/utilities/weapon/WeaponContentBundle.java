package com.cresora.utilities.weapon;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Root object of {@code cwc_weapon_content.json}. */
public record WeaponContentBundle(List<WeaponDefinition> weaponDefinitions) {
	public static final Codec<WeaponContentBundle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			WeaponContentRegistry.WEAPON_DEFINITION_CODEC.listOf().fieldOf("weaponDefinitions")
					.forGetter(WeaponContentBundle::weaponDefinitions)
	).apply(instance, WeaponContentBundle::new));
}
