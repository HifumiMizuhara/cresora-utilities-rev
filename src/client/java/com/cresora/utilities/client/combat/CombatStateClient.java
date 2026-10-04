package com.cresora.utilities.client.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import com.cresora.utilities.combat.CombatAttachments;

/**
 * Client-side read access to the synced combat state. The authoritative value lives on the server
 * in {@link CombatAttachments#IN_COMBAT} and is mirrored to the owning client.
 */
public final class CombatStateClient {
	private CombatStateClient() {
	}

	public static boolean isInCombat(LocalPlayer player) {
		return player != null && Boolean.TRUE.equals(player.getAttached(CombatAttachments.IN_COMBAT));
	}

	public static boolean isInCombat() {
		return isInCombat(Minecraft.getInstance().player);
	}
}
