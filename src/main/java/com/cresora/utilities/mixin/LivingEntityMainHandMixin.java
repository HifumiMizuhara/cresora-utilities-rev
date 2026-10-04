package com.cresora.utilities.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cresora.utilities.combat.HeldWeaponSupport;

/**
 * Shows and uses the main weapon as the held item while in combat (both sides). It is a no-op for
 * non-player entities, out-of-combat players and players without a main weapon equipped, so vanilla
 * behaviour is untouched otherwise.
 *
 * <p>Nothing is moved out of the weapon bar: the main weapon stack is simply returned instead of
 * the worn main-hand item, which covers rendering ({@code ItemInHandRenderer}) and interactions
 * ({@code getItemInHand(MAIN_HAND)}, attacks, use).
 */
@Mixin(LivingEntity.class)
public class LivingEntityMainHandMixin {
	@Inject(method = "getMainHandItem", at = @At("HEAD"), cancellable = true)
	private void cresoraUtilities$mainWeaponInCombat(CallbackInfoReturnable<ItemStack> info) {
		if ((Object) this instanceof Player player) {
			ItemStack weapon = HeldWeaponSupport.mainHand(player);
			if (weapon != null) {
				info.setReturnValue(weapon);
			}
		}
	}
}
