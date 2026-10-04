package com.cresora.utilities.weapon;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Server-side operations on a player's {@link WeaponInventory}.
 *
 * <p>Responsibilities: keep weapons out of the normal inventory (any weapon found there is moved
 * into the bag each tick — this covers {@code /give}, pickups and crafting), and expose the
 * bag/equip/select operations the bag screen drives.
 */
public final class WeaponInventoryService {
	/** Player inventory slots scanned for stray weapons (the 36 main slots, not armour/offhand). */
	private static final int MAIN_INVENTORY_SIZE = 36;

	private WeaponInventoryService() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				collectWeapons(player);
			}
		});
	}

	public static WeaponInventory get(ServerPlayer player) {
		return player.getAttachedOrCreate(WeaponInventoryAttachments.WEAPON_INVENTORY);
	}

	private static void set(ServerPlayer player, WeaponInventory inventory) {
		player.setAttached(WeaponInventoryAttachments.WEAPON_INVENTORY, inventory);
	}

	/** Moves every weapon stack in the player's main inventory into the bag (as far as it fits). */
	public static void collectWeapons(ServerPlayer player) {
		Inventory inventory = player.getInventory();
		WeaponInventory current = null;
		List<ItemStack> bag = null;
		boolean changed = false;

		int limit = Math.min(MAIN_INVENTORY_SIZE, inventory.getContainerSize());
		for (int slot = 0; slot < limit; slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.isEmpty() || !WeaponStackSupport.isWeapon(stack)) {
				continue;
			}
			if (bag == null) {
				current = get(player);
				bag = new ArrayList<>(current.bag());
			}
			int empty = firstEmpty(bag);
			if (empty < 0) {
				break;
			}
			bag.set(empty, stack);
			inventory.setItem(slot, ItemStack.EMPTY);
			changed = true;
		}

		if (changed) {
			set(player, current.withBag(bag));
		}
	}

	/** Equips the weapon in the given bag slot into the bar slot bound to its zone. */
	public static void equipFromBag(ServerPlayer player, int bagIndex) {
		WeaponInventory current = get(player);
		if (bagIndex < 0 || bagIndex >= WeaponInventory.BAG_SIZE) {
			return;
		}
		ItemStack stack = current.bag(bagIndex);
		if (stack.isEmpty() || !WeaponStackSupport.isWeapon(stack)) {
			return;
		}
		WeaponDefinition definition = WeaponStackSupport.getDefinition(stack);
		if (definition == null) {
			return;
		}

		List<ItemStack> bag = new ArrayList<>(current.bag());
		List<ItemStack> equip = new ArrayList<>(current.equip());

		// A weapon is always placed into the bar slot bound to its zone (1主 / 2副 / 3辅助 / 4治疗);
		// the displaced weapon returns to the bag slot it came from.
		int target = definition.zone().slot();
		ItemStack displaced = equip.get(target);
		equip.set(target, stack);
		bag.set(bagIndex, displaced);

		set(player, current.withBag(bag).withEquip(equip));
	}

	/** Returns an equipped weapon to the bag (first free slot). */
	public static void unequip(ServerPlayer player, int equipIndex) {
		WeaponInventory current = get(player);
		if (equipIndex < 0 || equipIndex >= WeaponInventory.EQUIP_SIZE) {
			return;
		}
		ItemStack stack = current.equipped(equipIndex);
		if (stack.isEmpty()) {
			return;
		}

		List<ItemStack> bag = new ArrayList<>(current.bag());
		int empty = firstEmpty(bag);
		if (empty < 0) {
			return;
		}
		bag.set(empty, stack);
		List<ItemStack> equip = new ArrayList<>(current.equip());
		equip.set(equipIndex, ItemStack.EMPTY);

		set(player, current.withBag(bag).withEquip(equip));
	}

	public static void select(ServerPlayer player, int index) {
		WeaponInventory current = get(player);
		WeaponInventory updated = current.withSelected(index);
		if (updated.selected() != current.selected()) {
			set(player, updated);
		}
	}

	/** Selects the main-weapon slot, so the main weapon is the weapon in hand. */
	public static void selectMainWeapon(ServerPlayer player) {
		select(player, WeaponZone.MAIN_WEAPON.slot());
	}

	public static void cycleSelected(ServerPlayer player) {
		WeaponInventory current = get(player);
		set(player, current.withSelected(current.selected() + 1));
	}

	/** Fires a skill of the selected bar weapon. */
	public static void activateSelectedSkill(ServerPlayer player, int skillSlot) {
		WeaponInventory current = get(player);
		ItemStack stack = current.equipped(current.selected());
		if (!stack.isEmpty()) {
			WeaponSkillService.tryActivate(player, stack, skillSlot);
		}
	}

	private static int firstEmpty(List<ItemStack> stacks) {
		for (int index = 0; index < stacks.size(); index++) {
			if (stacks.get(index).isEmpty()) {
				return index;
			}
		}
		return -1;
	}
}
