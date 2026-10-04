package com.cresora.utilities.mixin;

import net.minecraft.server.level.ServerPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cresora.utilities.combat.HeldWeaponSupport;

/**
 * Blocks the "drop held item" action while in combat. Because the main hand is a virtual weapon that
 * does not exist in the inventory, allowing a drop would remove an unrelated inventory slot and
 * spawn the weapon item, so it is cancelled outright.
 */
@Mixin(ServerPlayer.class)
public class ServerPlayerDropMixin {
	@Inject(method = "drop(Z)V", at = @At("HEAD"), cancellable = true)
	private void cresoraUtilities$blockDropInCombat(boolean dropStack, CallbackInfo info) {
		if (HeldWeaponSupport.mainHand((ServerPlayer) (Object) this) != null) {
			info.cancel();
		}
	}
}
