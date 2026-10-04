package com.cresora.utilities.combat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.cresora.utilities.weapon.WeaponInventory;
import com.cresora.utilities.weapon.WeaponInventoryAttachments;
import com.cresora.utilities.weapon.WeaponZone;

/**
 * Virtual main-hand holding.
 *
 * <p>While a player is in combat, the main weapon (the weapon-bar slot bound to
 * {@link WeaponZone#MAIN_WEAPON}) is reported as the item held in the main hand, for rendering and
 * interaction, without ever moving items out of the weapon bar. Outside combat (or with no main
 * weapon equipped) the vanilla held item is used.
 */
public final class HeldWeaponSupport {
	private HeldWeaponSupport() {
	}

	/**
	 * The stack that should occupy the player's main hand, or {@code null} to fall back to the
	 * vanilla held item.
	 */
	public static ItemStack mainHand(Player player) {
		if (player == null || !Boolean.TRUE.equals(player.getAttached(CombatAttachments.IN_COMBAT))) {
			return null;
		}
		WeaponInventory inventory = player.getAttached(WeaponInventoryAttachments.WEAPON_INVENTORY);
		if (inventory == null) {
			return null;
		}
		ItemStack stack = inventory.equipped(WeaponZone.MAIN_WEAPON.slot());
		return stack.isEmpty() ? null : stack;
	}
}
