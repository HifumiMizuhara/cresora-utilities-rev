package com.cresora.utilities.combat;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;

import com.cresora.utilities.CresoraUtilities;

/**
 * Registers the per-player combat-state attachment.
 *
 * <p>The state is a manual toggle, held on the server and synchronised to the owning client so the
 * HUD, key handling and hotbar interception all agree on it. It is intentionally not persistent:
 * dying, logging out or changing dimension resets the player to the normal (non-combat) state.
 */
public final class CombatAttachments {
	public static final AttachmentType<Boolean> IN_COMBAT =
			AttachmentRegistry.<Boolean>create(CresoraUtilities.id("in_combat"), builder -> builder
					.initializer(() -> false)
					.syncWith(ByteBufCodecs.BOOL, AttachmentSyncPredicate.targetOnly()));

	private CombatAttachments() {
	}

	/** Forces class loading so the attachment is registered. Called from the mod initialiser. */
	public static void initialize() {
	}
}
