package com.cresora.utilities.weapon;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import com.cresora.utilities.CresoraUtilities;

/**
 * Registers the {@link WeaponInventory} player attachment: persistent, copied on death, and
 * synchronised to the owning client only (the HUD and bag screen render from the synced copy).
 */
public final class WeaponInventoryAttachments {
	public static final AttachmentType<WeaponInventory> WEAPON_INVENTORY =
			AttachmentRegistry.<WeaponInventory>create(CresoraUtilities.id("weapon_inventory"), builder -> builder
					.persistent(WeaponInventory.CODEC)
					.syncWith(WeaponInventory.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
					.copyOnDeath()
					.initializer(WeaponInventory::empty));

	private WeaponInventoryAttachments() {
	}

	/** Forces class loading so the attachment is registered. Called from the mod initialiser. */
	public static void initialize() {
	}
}
