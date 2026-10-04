package com.cresora.utilities.weapon;

import com.mojang.serialization.Codec;

/**
 * The weapon partition (分区). Each zone is bound to a fixed weapon-bar slot:
 * <ol>
 *   <li>{@link #MAIN_WEAPON} - 主武器</li>
 *   <li>{@link #SUB_WEAPON} - 副武器</li>
 *   <li>{@link #SUPPORT} - 辅助</li>
 *   <li>{@link #HEAL} - 治疗</li>
 * </ol>
 *
 * <p>Replaces the previous {@code WeaponRole} taxonomy: a weapon's zone both labels it and decides
 * which bar slot it is equipped into.
 */
public enum WeaponZone {
	MAIN_WEAPON("main_weapon", 0),
	SUB_WEAPON("sub_weapon", 1),
	SUPPORT("support", 2),
	HEAL("heal", 3);

	private final String id;
	private final int slot;

	WeaponZone(String id, int slot) {
		this.id = id;
		this.slot = slot;
	}

	public String id() {
		return id;
	}

	/** The zero-based weapon-bar slot this zone is bound to. */
	public int slot() {
		return slot;
	}

	public String translationKey() {
		return "item.cresora.weapon.zone." + id;
	}

	/** Returns the zone bound to the given weapon-bar slot, or {@code null} if out of range. */
	public static WeaponZone forSlot(int slot) {
		for (WeaponZone zone : values()) {
			if (zone.slot == slot) {
				return zone;
			}
		}
		return null;
	}

	public static final Codec<WeaponZone> CODEC = Codec.STRING.xmap(
			value -> {
				String normalized = value.toLowerCase();
				for (WeaponZone zone : values()) {
					if (zone.id.equals(normalized)) {
						return zone;
					}
				}
				throw new IllegalArgumentException("Unknown weapon zone: " + value);
			},
			WeaponZone::id);
}
