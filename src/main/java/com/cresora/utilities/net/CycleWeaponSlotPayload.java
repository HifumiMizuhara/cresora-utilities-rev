package com.cresora.utilities.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import com.cresora.utilities.CresoraUtilities;

/** Client -> server: advance the selected weapon-bar slot. */
public record CycleWeaponSlotPayload() implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<CycleWeaponSlotPayload> TYPE =
			new CustomPacketPayload.Type<>(CresoraUtilities.id("cycle_weapon_slot"));

	public static final StreamCodec<RegistryFriendlyByteBuf, CycleWeaponSlotPayload> STREAM_CODEC =
			StreamCodec.unit(new CycleWeaponSlotPayload());

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
