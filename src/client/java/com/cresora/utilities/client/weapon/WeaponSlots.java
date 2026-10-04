package com.cresora.utilities.client.weapon;

import com.cresora.utilities.weapon.WeaponInventory;
import com.cresora.utilities.weapon.WeaponInventoryAttachments;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Client-side read access to the weapon bar.
 *
 * <p>The authoritative state lives on the server and is mirrored to this client through the
 * {@link WeaponInventoryAttachments#WEAPON_INVENTORY} attachment, so this is just a live view.
 */
public final class WeaponSlots {
	/** Number of slots in the weapon bar. */
	public static final int SLOT_COUNT = WeaponInventory.EQUIP_SIZE;

	private WeaponSlots() {
	}

	/** Returns the synced weapon inventory, or an empty one when not in a world. */
	public static WeaponInventory inventory() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return WeaponInventory.empty();
		}
		WeaponInventory inventory = player.getAttached(WeaponInventoryAttachments.WEAPON_INVENTORY);
		return inventory == null ? WeaponInventory.empty() : inventory;
	}

	public static ItemStack get(int index) {
		return inventory().equipped(index);
	}

	public static int getSelected() {
		return inventory().selected();
	}
}
