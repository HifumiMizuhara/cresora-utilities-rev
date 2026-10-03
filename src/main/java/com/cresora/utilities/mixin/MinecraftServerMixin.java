package com.cresora.utilities.mixin;

import net.minecraft.server.MinecraftServer;

import com.cresora.utilities.CresoraUtilities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Example common mixin, loaded on both the client and the dedicated server.
 *
 * <p>This is injected into the start of {@code MinecraftServer.loadLevel()V}. Delete it
 * once you have real mixins, and remember to remove the entry from
 * {@code cresora-utilities.mixins.json}.
 */
@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@Inject(at = @At("HEAD"), method = "loadLevel")
	private void cresoraUtilities$onLoadLevel(CallbackInfo info) {
		CresoraUtilities.LOGGER.debug("Server is loading a level");
	}
}
