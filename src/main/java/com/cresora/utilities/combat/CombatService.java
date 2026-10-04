package com.cresora.utilities.combat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.cresora.utilities.weapon.WeaponInventoryService;

/**
 * Server-side combat-state operations. The state is a manual toggle; while active, the hotbar
 * number keys select weapon-bar slots and the skill keys fire weapon skills.
 */
public final class CombatService {
	private CombatService() {
	}

	public static boolean isInCombat(ServerPlayer player) {
		return Boolean.TRUE.equals(player.getAttached(CombatAttachments.IN_COMBAT));
	}

	/** Flips the player's combat state and sends an action-bar notice. */
	public static void toggle(ServerPlayer player) {
		boolean inCombat = !isInCombat(player);
		player.setAttached(CombatAttachments.IN_COMBAT, inCombat);
		if (inCombat) {
			// Entering combat: the weapon in hand defaults to the main weapon (slot 1).
			WeaponInventoryService.selectMainWeapon(player);
		}
		player.sendSystemMessage(Component.translatable(
						inCombat ? "cresora.combat.enter" : "cresora.combat.leave")
				.withStyle(inCombat ? ChatFormatting.GOLD : ChatFormatting.GRAY), true);
	}

	/** Forces the combat state (no message). */
	public static void set(ServerPlayer player, boolean inCombat) {
		player.setAttached(CombatAttachments.IN_COMBAT, inCombat);
	}

	/** Forces class loading. Called from the mod initialiser. */
	public static void initialize() {
	}
}
