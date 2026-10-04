package com.cresora.utilities.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import com.cresora.utilities.CresoraUtilities;

/** Client -> server: toggle the player's combat state. */
public record ToggleCombatPayload() implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ToggleCombatPayload> TYPE =
			new CustomPacketPayload.Type<>(CresoraUtilities.id("toggle_combat"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ToggleCombatPayload> STREAM_CODEC =
			StreamCodec.unit(new ToggleCombatPayload());

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
