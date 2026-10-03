package com.cresora.utilities.client.mixin;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cresora.utilities.CresoraUtilities;

/**
 * Example client-only mixin.
 *
 * <p>Injected into the start of {@code Minecraft.run()V}. Delete it once you have real
 * client mixins, and remember to remove the entry from
 * {@code cresora-utilities.client.mixins.json}.
 */
@Mixin(Minecraft.class)
public class MinecraftClientMixin {
	@Inject(at = @At("HEAD"), method = "run")
	private void cresoraUtilities$onRun(CallbackInfo info) {
		// Client-side logging goes through the shared logger.
		CresoraUtilities.LOGGER.debug("Client main loop starting");
	}
}
