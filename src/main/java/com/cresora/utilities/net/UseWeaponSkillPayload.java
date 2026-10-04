package com.cresora.utilities.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import com.cresora.utilities.CresoraUtilities;

/**
 * Client -> server: fire a skill of the currently selected weapon-bar slot.
 *
 * @param skillSlot the skill slot (0 = first key, 1 = second, 2 = third). A weapon with a single
 *                  skill ignores this and fires that skill.
 */
public record UseWeaponSkillPayload(int skillSlot) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UseWeaponSkillPayload> TYPE =
			new CustomPacketPayload.Type<>(CresoraUtilities.id("use_weapon_skill"));

	public static final StreamCodec<RegistryFriendlyByteBuf, UseWeaponSkillPayload> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.VAR_INT, UseWeaponSkillPayload::skillSlot,
					UseWeaponSkillPayload::new);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
