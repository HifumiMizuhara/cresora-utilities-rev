package com.cresora.utilities.weapon;

/**
 * Weapon-relevant balance constants.
 *
 * <p>The original reads these from the combat balance registry
 * ({@code data/cresora-utilities/cresora/combat_balance.json}, {@code tuning} block). Only the
 * weapon-facing subset is kept here so the weapon subsystem stays self-contained; the values
 * match the shipped defaults. When the combat subsystem is ported, this can delegate to it.
 */
public final class WeaponBalance {
	public static final double BASE_DAMAGE_SCALAR = 0.92;
	public static final double LEVEL_GROWTH_SCALAR = 1.1;
	public static final double BREAKTHROUGH_ATTACK_FACTOR = 0.12;
	public static final double BREAKTHROUGH_CRIT_RATE_BONUS = 5.0;
	public static final double BREAKTHROUGH_ALL_DAMAGE_BONUS = 8.0;

	private WeaponBalance() {
	}
}
