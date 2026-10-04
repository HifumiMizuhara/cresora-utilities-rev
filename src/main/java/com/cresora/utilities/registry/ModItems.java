package com.cresora.utilities.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import com.cresora.utilities.CresoraUtilities;
import com.cresora.utilities.weapon.CresoraWeaponItem;
import com.cresora.utilities.weapon.WeaponDefinitionRef;
import com.cresora.utilities.weapon.WeaponStackSupport;

/**
 * Example registry class showing the pattern used for every content type
 * (items, blocks, block entities, ...).
 *
 * <p>Each entry is declared as a {@link ResourceKey} plus the registered instance, then
 * registered through {@link #initialize()} so that class loading order stays predictable.
 * The matching assets live under
 * {@code src/main/resources/assets/cresora-utilities/}.
 */
public final class ModItems {
	/** Registry key for the example item; also used as the item's model id. */
	public static final ResourceKey<Item> EXAMPLE_ITEM_KEY =
			ResourceKey.create(Registries.ITEM, CresoraUtilities.id("example_item"));

	/**
	 * A plain item with no special behaviour.
	 *
	 * <p>{@code setId} is required since Minecraft 1.21.2: every item must know its own
	 * registry key before it is registered.
	 */
	public static final Item EXAMPLE_ITEM = Registry.register(
			BuiltInRegistries.ITEM,
			EXAMPLE_ITEM_KEY,
			new Item(new Item.Properties().setId(EXAMPLE_ITEM_KEY))
	);

	/** Registry key for the "Stride by the Lakeside" weapon item. */
	public static final ResourceKey<Item> LAKESIDE_STRIDE_KEY =
			ResourceKey.create(Registries.ITEM, CresoraUtilities.id("lakeside_stride"));

	/** The lakeside_stride weapon. Its definition is resolved from {@code WeaponContentRegistry}. */
	public static final Item LAKESIDE_STRIDE = Registry.register(
			BuiltInRegistries.ITEM,
			LAKESIDE_STRIDE_KEY,
			new CresoraWeaponItem(new WeaponDefinitionRef("lakeside_stride"),
					new Item.Properties().setId(LAKESIDE_STRIDE_KEY).stacksTo(1))
	);

	private ModItems() {
	}

	/**
	 * Forces initialisation of this class, which performs the registration above. Called
	 * from {@link CresoraUtilities#onInitialize()}.
	 */
	public static void initialize() {
		WeaponStackSupport.registerWeaponItem(LAKESIDE_STRIDE, new WeaponDefinitionRef("lakeside_stride"));
		CresoraUtilities.LOGGER.debug("Registered item {}", EXAMPLE_ITEM_KEY.identifier());
		CresoraUtilities.LOGGER.debug("Registered weapon item {}", LAKESIDE_STRIDE_KEY.identifier());
	}
}
