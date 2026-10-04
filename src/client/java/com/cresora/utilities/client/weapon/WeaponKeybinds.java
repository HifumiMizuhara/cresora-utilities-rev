package com.cresora.utilities.client.weapon;

import com.cresora.utilities.CresoraUtilities;
import com.cresora.utilities.net.CycleWeaponSlotPayload;
import com.cresora.utilities.net.UseWeaponSkillPayload;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.platform.InputConstants;

import org.lwjgl.glfw.GLFW;

/**
 * Weapon key bindings, all rebindable under {@code Options -> Controls -> Cresora Utilities}:
 * <ul>
 *   <li><b>B</b> - open the weapon bag</li>
 *   <li><b>R</b> - use the selected weapon's skill</li>
 *   <li><b>V</b> - cycle the selected weapon-bar slot</li>
 * </ul>
 */
public final class WeaponKeybinds {
	private static final KeyMapping.Category CATEGORY =
			KeyMapping.Category.register(CresoraUtilities.id("weapon"));

	private static final KeyMapping OPEN_BAG = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.cresora-utilities.open_bag", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY));

	private static final KeyMapping USE_SKILL = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.cresora-utilities.use_weapon_skill", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY));

	private static final KeyMapping CYCLE_SLOT = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.cresora-utilities.cycle_weapon", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY));

	/** Wires up the key handling. Called once from the client entrypoint. */
	public static void initialize() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (OPEN_BAG.consumeClick()) {
				if (client.player != null) {
					client.setScreenAndShow(new WeaponBagScreen());
				}
			}
			while (USE_SKILL.consumeClick()) {
				if (client.player != null) {
					ClientPlayNetworking.send(new UseWeaponSkillPayload());
				}
			}
			while (CYCLE_SLOT.consumeClick()) {
				if (client.player != null) {
					ClientPlayNetworking.send(new CycleWeaponSlotPayload());
				}
			}
		});
	}

	public static boolean matchesOpenBag(net.minecraft.client.input.KeyEvent event) {
		return OPEN_BAG.matches(event);
	}

	private WeaponKeybinds() {
	}
}
