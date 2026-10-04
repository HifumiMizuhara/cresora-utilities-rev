package com.cresora.utilities;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cresora.utilities.net.WeaponNetwork;
import com.cresora.utilities.registry.ModItems;
import com.cresora.utilities.skill.WeaponSkillRegistry;
import com.cresora.utilities.weapon.WeaponAttributeService;
import com.cresora.utilities.weapon.WeaponContentRegistry;
import com.cresora.utilities.weapon.WeaponInventoryAttachments;
import com.cresora.utilities.weapon.WeaponInventoryService;
import com.cresora.utilities.weapon.WeaponSkillService;

/**
 * Common (both client and dedicated server) entrypoint of Cresora Utilities.
 *
 * <p>This class is referenced by the {@code main} entrypoint in
 * {@code src/main/resources/fabric.mod.json}. Anything registered here must be safe to
 * load on a dedicated server, so keep client-only code in
 * {@link com.cresora.utilities.client.CresoraUtilitiesClient}.
 */
public class CresoraUtilities implements ModInitializer {
	/** The mod id. Must match the {@code id} field in fabric.mod.json. */
	public static final String MOD_ID = "cresora-utilities";

	/** Human readable mod name, used for logging and messages. */
	public static final String MOD_NAME = "Cresora Utilities";

	/**
	 * Shared logger. Best practice is to name the logger after the mod id so it is obvious
	 * which mod produced a given log line.
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

	@Override
	public void onInitialize() {
		// Runs once the game has reached a mod-load-ready state. Some things (such as
		// resources) may still be uninitialised here, so proceed with mild caution.
		LOGGER.info("Initialising {}", MOD_NAME);

		// Register content. Add further registry classes here as the mod grows.
		ModDataComponents.initialize();
		WeaponContentRegistry.init();
		ModItems.initialize();

		// Weapon runtime services.
		WeaponSkillRegistry.initialize();
		WeaponSkillService.init();
		WeaponInventoryAttachments.initialize();
		WeaponInventoryService.init();
		WeaponAttributeService.init();
		WeaponNetwork.initialize();
	}

	/**
	 * Creates an {@link Identifier} in this mod's namespace.
	 *
	 * @param path the path, e.g. {@code "example_item"}
	 */
	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
