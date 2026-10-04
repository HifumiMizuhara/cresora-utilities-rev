package com.cresora.utilities.client.mixin;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cresora.utilities.client.combat.CombatStateClient;
import com.cresora.utilities.client.weapon.WeaponBarHud;
import com.cresora.utilities.net.SelectWeaponSlotPayload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Reroutes vanilla key handling while the player is in combat:
 * <ul>
 *   <li>the hotbar number keys 1-4 select weapon-bar slots instead of inventory slots;</li>
 *   <li>the chat key ({@code T}) is suppressed, freeing it for weapon skill 2.</li>
 * </ul>
 *
 * <p>The injection sits at the head of {@code Minecraft.handleKeybinds()} and drains the vanilla
 * key mappings before vanilla processes them, so it only affects the combat state and leaves normal
 * (non-combat) behaviour, other hotbar keys (5-9) and any open screen untouched.
 */
@Mixin(Minecraft.class)
public class MinecraftKeybindMixin {
	private static final int WEAPON_SLOT_KEYS = 4;

	@Inject(at = @At("HEAD"), method = "handleKeybinds")
	private void cresoraUtilities$interceptCombatKeys(CallbackInfo info) {
		Minecraft minecraft = (Minecraft) (Object) this;
		if (minecraft.player == null || minecraft.gui.screen() != null) {
			return;
		}
		if (!CombatStateClient.isInCombat(minecraft.player)) {
			return;
		}

		int slots = Math.min(WEAPON_SLOT_KEYS, minecraft.options.keyHotbarSlots.length);
		for (int slot = 0; slot < slots; slot++) {
			boolean pressed = false;
			while (minecraft.options.keyHotbarSlots[slot].consumeClick()) {
				pressed = true;
			}
			if (pressed) {
				ClientPlayNetworking.send(new SelectWeaponSlotPayload(slot));
				WeaponBarHud.triggerFlyout();
			}
		}

		while (minecraft.options.keyChat.consumeClick()) {
			// Chat stays closed while in combat; T fires weapon skill 2 instead.
		}

		while (minecraft.options.keyDrop.consumeClick()) {
			// The main hand holds a virtual weapon while in combat; dropping it is disabled.
		}
	}
}
