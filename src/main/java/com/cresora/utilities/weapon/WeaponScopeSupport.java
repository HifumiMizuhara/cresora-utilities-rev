package com.cresora.utilities.weapon;

/**
 * Pure scoping rules for weapon-skill passive bonuses. Kept free of Minecraft types so the
 * held-weapon isolation guarantee can be regression-tested in plain JUnit.
 *
 * <p>The invariant: a passive bonus is only contributed when the request either targets the
 * held weapon explicitly (matching id) or is unscoped ({@code requestedWeaponId == null}).
 * This prevents an unequipped weapon's stats from blending into the held weapon's attribute
 * calculation.
 */
public final class WeaponScopeSupport {
	private WeaponScopeSupport() {
	}

	public static boolean isActiveWeaponScope(String activeWeaponId, String requestedWeaponId) {
		return requestedWeaponId == null || activeWeaponId.equals(requestedWeaponId);
	}
}
