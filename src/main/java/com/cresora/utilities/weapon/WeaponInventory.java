package com.cresora.utilities.weapon;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * Per-player weapon storage: a 27-slot bag plus the 4 equipped weapon-bar slots and the index
 * of the currently selected bar slot.
 *
 * <p>Held on the player as a Fabric attachment (see {@link WeaponInventoryAttachments}), which
 * makes it both persistent across sessions and mirrored to the owning client for the HUD and the
 * bag screen. The record is immutable; mutate by building a new instance and re-attaching it.
 */
public record WeaponInventory(List<ItemStack> bag, List<ItemStack> equip, int selected) {
	public static final int BAG_SIZE = 27;
	public static final int EQUIP_SIZE = 4;

	public WeaponInventory {
		bag = normalize(bag, BAG_SIZE);
		equip = normalize(equip, EQUIP_SIZE);
		selected = Math.floorMod(selected, EQUIP_SIZE);
	}

	private static List<ItemStack> normalize(List<ItemStack> list, int size) {
		List<ItemStack> result = new ArrayList<>(size);
		for (int index = 0; index < size; index++) {
			ItemStack stack = index < list.size() ? list.get(index) : ItemStack.EMPTY;
			result.add(stack == null ? ItemStack.EMPTY : stack);
		}
		return List.copyOf(result);
	}

	public static WeaponInventory empty() {
		return new WeaponInventory(List.of(), List.of(), 0);
	}

	public ItemStack bag(int index) {
		return bag.get(Math.floorMod(index, BAG_SIZE));
	}

	public ItemStack equipped(int index) {
		return equip.get(Math.floorMod(index, EQUIP_SIZE));
	}

	public WeaponInventory withBag(List<ItemStack> newBag) {
		return new WeaponInventory(newBag, equip, selected);
	}

	public WeaponInventory withEquip(List<ItemStack> newEquip) {
		return new WeaponInventory(bag, newEquip, selected);
	}

	public WeaponInventory withSelected(int newSelected) {
		return new WeaponInventory(bag, equip, newSelected);
	}

	public static final Codec<WeaponInventory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("bag", List.of()).forGetter(WeaponInventory::bag),
			ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("equip", List.of()).forGetter(WeaponInventory::equip),
			Codec.INT.optionalFieldOf("selected", 0).forGetter(WeaponInventory::selected)
	).apply(instance, WeaponInventory::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, WeaponInventory> STREAM_CODEC = StreamCodec.composite(
			ItemStack.OPTIONAL_LIST_STREAM_CODEC, WeaponInventory::bag,
			ItemStack.OPTIONAL_LIST_STREAM_CODEC, WeaponInventory::equip,
			ByteBufCodecs.VAR_INT, WeaponInventory::selected,
			WeaponInventory::new);
}
