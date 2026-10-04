package com.cresora.utilities.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.minecraft.world.item.ItemStack;

/** Tests the pure normalisation helpers of {@link WeaponInventory}. */
class WeaponInventoryTest {
	@Test
	void emptyHasFixedSizes() {
		WeaponInventory inventory = WeaponInventory.empty();
		assertEquals(WeaponInventory.BAG_SIZE, inventory.bag().size());
		assertEquals(WeaponInventory.EQUIP_SIZE, inventory.equip().size());
		assertEquals(0, inventory.selected());
		assertTrue(inventory.bag().stream().allMatch(ItemStack::isEmpty));
	}

	@Test
	void shortListsArePaddedWithEmptyStacks() {
		WeaponInventory inventory = new WeaponInventory(List.of(), List.of(), 0);
		assertEquals(WeaponInventory.BAG_SIZE, inventory.bag().size());
		assertTrue(inventory.bag(WeaponInventory.BAG_SIZE - 1).isEmpty());
		assertTrue(inventory.equipped(WeaponInventory.EQUIP_SIZE - 1).isEmpty());
	}

	@Test
	void selectedWrapsAround() {
		assertEquals(1, new WeaponInventory(List.of(), List.of(), 5).selected());
		assertEquals(3, WeaponInventory.empty().withSelected(-1).selected());
	}

	@Test
	void withBagKeepsEquipAndSelection() {
		WeaponInventory inventory = WeaponInventory.empty().withSelected(2);
		WeaponInventory updated = inventory.withBag(inventory.bag());
		assertEquals(2, updated.selected());
		assertEquals(WeaponInventory.EQUIP_SIZE, updated.equip().size());
	}
}
