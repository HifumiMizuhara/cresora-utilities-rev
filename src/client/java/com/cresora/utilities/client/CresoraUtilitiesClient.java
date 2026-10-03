package com.cresora.utilities.client;

import net.fabricmc.api.ClientModInitializer;

import com.cresora.utilities.CresoraUtilities;

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
	}
}
