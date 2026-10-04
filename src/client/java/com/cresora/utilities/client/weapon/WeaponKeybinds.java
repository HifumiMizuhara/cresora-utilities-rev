package com.cresora.utilities.client.weapon;

import com.cresora.utilities.CresoraUtilities;
import com.cresora.utilities.client.combat.CombatStateClient;
import com.cresora.utilities.net.CycleWeaponSlotPayload;
import com.cresora.utilities.net.ToggleCombatPayload;
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
 *   <li><b>V</b> - cycle the selected weapon-bar slot</li>
 *   <li><b>G</b> - toggle combat state</li>
 *   <li><b>R / T / Y</b> - fire weapon skill 1 / 2 / 3 (only while in combat)</li>
 * </ul>
 *
 * <p>The number keys 1-4 are handled separately by
 * {@link com.cresora.utilities.client.mixin.MinecraftKeybindMixin}, which reroutes them to weapon
 * slot selection while in combat and suppresses vanilla chat on the {@code T} key.
 */
public final class WeaponKeybinds {
	private static final KeyMapping.Category CATEGORY =
			KeyMapping.Category.register(CresoraUtilities.id("weapon"));

	private static final KeyMapping OPEN_BAG = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.cresora-utilities.open_bag", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY));

	private static final KeyMapping CYCLE_SLOT = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.cresora-utilities.cycle_weapon", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY));

	private static final KeyMapping TOGGLE_COMBAT = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.cresora-utilities.toggle_combat", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY));

	private static final KeyMapping SKILL_1 = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.cresora-utilities.skill_1", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY));

	private static final KeyMapping SKILL_2 = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.cresora-utilities.skill_2", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_T, CATEGORY));

	private static final KeyMapping SKILL_3 = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.cresora-utilities.skill_3", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Y, CATEGORY));

	/** Wires up the key handling. Called once from the client entrypoint. */
	public static void initialize() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (OPEN_BAG.consumeClick()) {
				if (client.player != null) {
					client.setScreenAndShow(new WeaponBagScreen());
				}
			}
			while (CYCLE_SLOT.consumeClick()) {
				if (client.player != null) {
					ClientPlayNetworking.send(new CycleWeaponSlotPayload());
					WeaponBarHud.triggerFlyout();
				}
			}
			while (TOGGLE_COMBAT.consumeClick()) {
				if (client.player != null) {
					ClientPlayNetworking.send(new ToggleCombatPayload());
					WeaponBarHud.triggerFlyout();
				}
			}
			handleSkillKey(client, SKILL_1, 0);
			handleSkillKey(client, SKILL_2, 1);
			handleSkillKey(client, SKILL_3, 2);
		});
	}

	private static void handleSkillKey(Minecraft client, KeyMapping mapping, int skillSlot) {
		boolean clicked = false;
		while (mapping.consumeClick()) {
			clicked = true;
		}
		if (!clicked || client.player == null || !CombatStateClient.isInCombat(client.player)) {
			return;
		}
		ClientPlayNetworking.send(new UseWeaponSkillPayload(skillSlot));
	}

	/** The key currently bound to the given skill slot (0-2), for HUD hints. */
	static String skillKeyLabel(int skillSlot) {
		KeyMapping mapping = switch (skillSlot) {
			case 0 -> SKILL_1;
			case 1 -> SKILL_2;
			default -> SKILL_3;
		};
		return mapping.getTranslatedKeyMessage().getString();
	}

	public static boolean matchesOpenBag(net.minecraft.client.input.KeyEvent event) {
		return OPEN_BAG.matches(event);
	}

	private WeaponKeybinds() {
	}
}
