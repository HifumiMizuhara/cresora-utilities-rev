package com.cresora.utilities.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

import com.cresora.utilities.weapon.WeaponInventoryService;

/**
 * Registers the weapon-bar client-to-server payloads and their handlers. Called from the common
 * initialiser so both sides know the payload types.
 */
public final class WeaponNetwork {
	private WeaponNetwork() {
	}

	public static void initialize() {
		PayloadTypeRegistry.serverboundPlay().register(CycleWeaponSlotPayload.TYPE, CycleWeaponSlotPayload.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(UseWeaponSkillPayload.TYPE, UseWeaponSkillPayload.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(WeaponBagActionPayload.TYPE, WeaponBagActionPayload.STREAM_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(CycleWeaponSlotPayload.TYPE, (payload, context) ->
				context.server().execute(() -> WeaponInventoryService.cycleSelected(context.player())));

		ServerPlayNetworking.registerGlobalReceiver(UseWeaponSkillPayload.TYPE, (payload, context) ->
				context.server().execute(() -> WeaponInventoryService.activateSelectedSkill(context.player(), payload.skillSlot())));

		ServerPlayNetworking.registerGlobalReceiver(WeaponBagActionPayload.TYPE, (payload, context) ->
				context.server().execute(() -> {
					ServerPlayer player = context.player();
					switch (payload.action()) {
						case WeaponBagActionPayload.EQUIP -> WeaponInventoryService.equipFromBag(player, payload.index());
						case WeaponBagActionPayload.UNEQUIP -> WeaponInventoryService.unequip(player, payload.index());
						case WeaponBagActionPayload.SELECT -> WeaponInventoryService.select(player, payload.index());
						default -> {
						}
					}
				}));
	}
}
