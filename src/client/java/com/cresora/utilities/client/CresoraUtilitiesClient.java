package com.cresora.utilities.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import com.cresora.utilities.CresoraUtilities;
import com.cresora.utilities.client.weapon.WeaponBarHud;
import com.cresora.utilities.client.weapon.WeaponKeybinds;

/**
 * Client-only entrypoint of Cresora Utilities.
 *
 * <p>Referenced by the {@code client} entrypoint in {@code fabric.mod.json}. Code in this
 * source set (and in the {@code client} mixin package) is never loaded on a dedicated
 * server.
 */
public class CresoraUtilitiesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Suitable for client-only setup such as renderers, key bindings and HUD layers.
		CresoraUtilities.LOGGER.info("Initialising {} (client)", CresoraUtilities.MOD_NAME);

		// The weapon bar is drawn right after the vanilla hotbar, so it inherits the same
		// visibility rules (hidden by F1, not drawn in spectator mode or while a screen is open).
		HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, WeaponBarHud.ID, new WeaponBarHud());

		WeaponKeybinds.initialize();
	}
}
