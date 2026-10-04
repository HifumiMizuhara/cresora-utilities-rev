package com.cresora.utilities.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import com.cresora.utilities.CresoraUtilities;

/**
 * Client -> server: directly select a weapon-bar slot (fired by the hotbar number keys while in
 * combat).
 *
 * @param index the weapon-bar slot to select
 */
public record SelectWeaponSlotPayload(int index) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<SelectWeaponSlotPayload> TYPE =
			new CustomPacketPayload.Type<>(CresoraUtilities.id("select_weapon_slot"));

	public static final StreamCodec<RegistryFriendlyByteBuf, SelectWeaponSlotPayload> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.VAR_INT, SelectWeaponSlotPayload::index,
					SelectWeaponSlotPayload::new);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
