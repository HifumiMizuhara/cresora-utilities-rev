package com.cresora.utilities;

import com.mojang.serialization.Codec;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

import com.cresora.utilities.weapon.WeaponData;

/**
 * Item data components owned by this mod.
 *
 * <p>Java port of {@code ModDataComponents.kt}, restricted to the components the weapon
 * subsystem needs. {@code initialize()} forces class loading so the static registration runs,
 * exactly like the original.
 */
public final class ModDataComponents {
	public static final DataComponentType<Integer> LEVEL = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			Identifier.fromNamespaceAndPath(CresoraUtilities.MOD_ID, "level"),
			DataComponentType.<Integer>builder()
					.persistent(Codec.INT)
					.networkSynchronized(ByteBufCodecs.VAR_INT)
					.build());

	public static final DataComponentType<WeaponData> WEAPON_DATA = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			Identifier.fromNamespaceAndPath(CresoraUtilities.MOD_ID, "weapon_data"),
			DataComponentType.<WeaponData>builder()
					.persistent(WeaponData.CODEC)
					.networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(WeaponData.CODEC))
					.build());

	public static final DataComponentType<String> SUB_SKILL_EFFECT_ID = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			Identifier.fromNamespaceAndPath(CresoraUtilities.MOD_ID, "sub_skill_effect_id"),
			DataComponentType.<String>builder()
					.persistent(Codec.STRING)
					.networkSynchronized(ByteBufCodecs.STRING_UTF8)
					.build());

	private ModDataComponents() {
	}

	/** Forces class loading and therefore the registrations above. */
	public static void initialize() {
	}
}
