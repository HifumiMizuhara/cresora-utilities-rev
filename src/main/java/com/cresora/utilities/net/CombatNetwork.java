package com.cresora.utilities.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import com.cresora.utilities.combat.CombatService;
import com.cresora.utilities.weapon.WeaponInventoryService;

/**
 * Registers the combat-state client-to-server payloads and their handlers. Called from the common
 * initialiser so both sides know the payload types.
 */
public final class CombatNetwork {
	private CombatNetwork() {
	}

	public static void initialize() {
		PayloadTypeRegistry.serverboundPlay().register(ToggleCombatPayload.TYPE, ToggleCombatPayload.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SelectWeaponSlotPayload.TYPE, SelectWeaponSlotPayload.STREAM_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(ToggleCombatPayload.TYPE, (payload, context) ->
				context.server().execute(() -> CombatService.toggle(context.player())));

		ServerPlayNetworking.registerGlobalReceiver(SelectWeaponSlotPayload.TYPE, (payload, context) ->
				context.server().execute(() -> WeaponInventoryService.select(context.player(), payload.index())));
	}
}
