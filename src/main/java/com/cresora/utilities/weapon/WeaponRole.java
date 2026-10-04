package com.cresora.utilities.weapon;

import com.mojang.serialization.Codec;

/** Weapon combat roles. Java port of {@code WeaponRole.kt}. */
public enum WeaponRole {
	VANGUARD("vanguard"),
	GUARD("guard"),
	DEFENDER("defender"),
	SNIPER("sniper"),
	CASTER("caster"),
	MEDIC("medic"),
	SUPPORTER("supporter"),
	SPECIALIST("specialist"),
	CATALYST("catalyst");

	private final String id;

	WeaponRole(String id) {
		this.id = id;
	}

	public String id() {
		return id;
	}

	public String translationKey() {
		return "item.cresora.weapon.role." + id;
	}

	public static final Codec<WeaponRole> CODEC = Codec.STRING.xmap(
			value -> {
				String normalized = value.toLowerCase();
				for (WeaponRole role : values()) {
					if (role.id.equals(normalized)) {
						return role;
					}
				}
				throw new IllegalArgumentException("Unknown weapon role: " + value);
			},
			WeaponRole::id);
}
