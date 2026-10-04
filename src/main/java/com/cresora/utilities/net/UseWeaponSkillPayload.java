package com.cresora.utilities.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import com.cresora.utilities.CresoraUtilities;

/** Client -> server: fire the skill of the currently selected weapon-bar slot. */
public record UseWeaponSkillPayload() implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UseWeaponSkillPayload> TYPE =
			new CustomPacketPayload.Type<>(CresoraUtilities.id("use_weapon_skill"));

	public static final StreamCodec<RegistryFriendlyByteBuf, UseWeaponSkillPayload> STREAM_CODEC =
			StreamCodec.unit(new UseWeaponSkillPayload());

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
