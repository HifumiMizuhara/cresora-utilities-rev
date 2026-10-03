package com.cresora.utilities.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import com.cresora.utilities.CresoraUtilities;

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

	private ModItems() {
	}

	/**
	 * Forces initialisation of this class, which performs the registration above. Called
	 * from {@link CresoraUtilities#onInitialize()}.
	 */
	public static void initialize() {
		CresoraUtilities.LOGGER.debug("Registered item {}", EXAMPLE_ITEM_KEY.identifier());
	}
}
