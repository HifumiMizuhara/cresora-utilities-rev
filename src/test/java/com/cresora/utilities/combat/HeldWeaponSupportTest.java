package com.cresora.utilities.combat;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link HeldWeaponSupport}. */
class HeldWeaponSupportTest {
	@Test
	void nullPlayerReturnsNullHeldWeapon() {
		assertNull(HeldWeaponSupport.mainHand(null));
	}
}
