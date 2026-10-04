package com.cresora.utilities.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import com.cresora.utilities.CresoraUtilities;

/**
 * Client -> server: a bag-screen interaction.
 *
 * @param action one of {@link #EQUIP}, {@link #UNEQUIP} or {@link #SELECT}
 * @param index  the bag slot (equip) or bar slot (unequip/select) the action applies to
 */
public record WeaponBagActionPayload(int action, int index) implements CustomPacketPayload {
	public static final int EQUIP = 0;
	public static final int UNEQUIP = 1;
	public static final int SELECT = 2;

	public static final CustomPacketPayload.Type<WeaponBagActionPayload> TYPE =
			new CustomPacketPayload.Type<>(CresoraUtilities.id("weapon_bag_action"));

	public static final StreamCodec<RegistryFriendlyByteBuf, WeaponBagActionPayload> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.VAR_INT, WeaponBagActionPayload::action,
					ByteBufCodecs.VAR_INT, WeaponBagActionPayload::index,
					WeaponBagActionPayload::new);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
