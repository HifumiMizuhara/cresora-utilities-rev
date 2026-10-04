package com.cresora.utilities.weapon;

/** Lazily-resolved reference to a weapon definition by id. */
public record WeaponDefinitionRef(String id) {
	public WeaponDefinition resolve() {
		return WeaponContentRegistry.requireWeapon(id);
	}
}
