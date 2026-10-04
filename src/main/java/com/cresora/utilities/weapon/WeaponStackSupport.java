package com.cresora.utilities.weapon;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.cresora.utilities.ModDataComponents;

/**
 * Reads and writes weapon data on {@link ItemStack}s and tracks the items registered for each
 * weapon. Java port of the subset of {@code WeaponStackSupport.kt} the lakeside_stride slice
 * needs (fragments/role materials/artifacts belong to the upgrade flow and are omitted).
 */
public final class WeaponStackSupport {
	private static final Map<Item, WeaponDefinitionRef> WEAPONS_BY_ITEM = new LinkedHashMap<>();
	private static final Map<String, Item> ITEMS_BY_WEAPON_ID = new LinkedHashMap<>();
	private static final Map<Item, WeaponDefinitionRef> FRAGMENTS_BY_ITEM = new LinkedHashMap<>();
	private static final Map<String, Item> FRAGMENT_ITEMS_BY_WEAPON_ID = new LinkedHashMap<>();
	private static final Map<WeaponRarity, Item> GENERIC_FRAGMENT_ITEMS_BY_RARITY = new LinkedHashMap<>();

	private WeaponStackSupport() {
	}

	public static void registerWeaponItem(Item item, WeaponDefinitionRef definition) {
		WEAPONS_BY_ITEM.put(item, definition);
		ITEMS_BY_WEAPON_ID.put(definition.id(), item);
	}

	public static void registerFragmentItem(Item item, WeaponDefinitionRef definition) {
		FRAGMENTS_BY_ITEM.put(item, definition);
		FRAGMENT_ITEMS_BY_WEAPON_ID.put(definition.id(), item);
	}

	public static void registerRarityFragmentItem(Item item, WeaponRarity rarity) {
		GENERIC_FRAGMENT_ITEMS_BY_RARITY.put(rarity, item);
	}

	public static boolean isWeapon(ItemStack stack) {
		return WEAPONS_BY_ITEM.containsKey(stack.getItem());
	}

	public static boolean isWeaponFragment(ItemStack stack) {
		return FRAGMENTS_BY_ITEM.containsKey(stack.getItem())
				|| GENERIC_FRAGMENT_ITEMS_BY_RARITY.containsValue(stack.getItem());
	}

	public static WeaponDefinition getDefinition(ItemStack stack) {
		WeaponDefinitionRef ref = WEAPONS_BY_ITEM.get(stack.getItem());
		return ref == null ? null : ref.resolve();
	}

	public static WeaponDefinition getFragmentDefinition(ItemStack stack) {
		WeaponDefinitionRef ref = FRAGMENTS_BY_ITEM.get(stack.getItem());
		return ref == null ? null : ref.resolve();
	}

	public static Item weaponItem(String definitionId) {
		return ITEMS_BY_WEAPON_ID.get(definitionId);
	}

	public static Item fragmentItem(String definitionId) {
		WeaponDefinition definition = WeaponContentRegistry.weapon(definitionId);
		if (definition != null) {
			Item generic = GENERIC_FRAGMENT_ITEMS_BY_RARITY.get(definition.craft().craftedRarity());
			if (generic != null) {
				return generic;
			}
		}
		return FRAGMENT_ITEMS_BY_WEAPON_ID.get(definitionId);
	}

	public static WeaponData getWeaponData(ItemStack stack) {
		WeaponDefinition definition = getDefinition(stack);
		if (definition == null) {
			return null;
		}
		WeaponData data = stack.get(ModDataComponents.WEAPON_DATA);
		if (data != null) {
			return new WeaponData(definition.id(), data.rarity(), data.baseLevel(), data.skillLevel(),
					data.breakthrough(), data.spiritBondStage()).normalized(definition);
		}
		return defaultWeaponData(definition);
	}

	public static WeaponData ensureWeaponData(ItemStack stack) {
		WeaponDefinition definition = getDefinition(stack);
		if (definition == null) {
			throw new IllegalStateException("Non-weapon stack cannot receive weapon data: " + stack.getItem());
		}
		WeaponData data = stack.get(ModDataComponents.WEAPON_DATA);
		if (data != null) {
			return new WeaponData(definition.id(), data.rarity(), data.baseLevel(), data.skillLevel(),
					data.breakthrough(), data.spiritBondStage()).normalized(definition);
		}
		return syncWeaponData(stack, defaultWeaponData(definition));
	}

	public static WeaponData syncWeaponData(ItemStack stack, WeaponData data) {
		WeaponDefinition definition = getDefinition(stack);
		if (definition == null) {
			return data.normalized();
		}
		WeaponData normalized = data.normalized(definition);
		stack.set(ModDataComponents.WEAPON_DATA, normalized);
		return normalized;
	}

	public static WeaponData defaultWeaponData(WeaponDefinition definition) {
		return new WeaponData(
				definition.id(),
				definition.craft().craftedRarity(),
				definition.craft().craftedBaseLevel(),
				definition.craft().craftedSkillLevel(),
				0,
				1).normalized(definition);
	}

	public static ItemStack createWeaponStack(WeaponDefinition definition, WeaponRarity rarity,
			int baseLevel, int skillLevel, int breakthrough) {
		Item item = weaponItem(definition.id());
		if (item == null) {
			throw new IllegalStateException("Weapon item not registered: " + definition.id());
		}
		ItemStack stack = new ItemStack(item);
		syncWeaponData(stack, new WeaponData(definition.id(), rarity, baseLevel, skillLevel, breakthrough, 1));
		return stack;
	}
}
