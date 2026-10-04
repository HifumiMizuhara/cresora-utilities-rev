package com.cresora.utilities.client.weapon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeaponBarHudTest {

	@BeforeEach
	void setUp() {
		WeaponBarHud.resetState();
	}

	@Test
	void flyoutLifecycleAndFadeout() {
		long start = 10_000L;

		// 1. Before being triggered: hidden
		assertEquals(0.0f, WeaponBarHud.getFlyoutAlpha(start));

		// 2. Trigger at start (10_000ms)
		WeaponBarHud.triggerFlyoutAt(start);

		// Immediately visible at 100% opacity
		assertEquals(1.0f, WeaponBarHud.getFlyoutAlpha(start));

		// Fully visible during the 1.5s window (e.g. at 0.75s, 1.499s, 1.5s)
		assertEquals(1.0f, WeaponBarHud.getFlyoutAlpha(start + 750L));
		assertEquals(1.0f, WeaponBarHud.getFlyoutAlpha(start + 1499L));
		assertEquals(1.0f, WeaponBarHud.getFlyoutAlpha(start + 1500L));

		// Mid-fadeout at 50% (1750ms = 1500ms + 250ms / 500ms)
		assertEquals(0.5f, WeaponBarHud.getFlyoutAlpha(start + 1750L), 0.001f);

		// 75% faded out (1875ms = 1500ms + 375ms / 500ms -> 25% opacity left)
		assertEquals(0.25f, WeaponBarHud.getFlyoutAlpha(start + 1875L), 0.001f);

		// Fadeout complete at 2000ms (1500ms + 500ms)
		assertEquals(0.0f, WeaponBarHud.getFlyoutAlpha(start + 2000L));

		// Fully hidden thereafter
		assertEquals(0.0f, WeaponBarHud.getFlyoutAlpha(start + 5000L));
	}

	@Test
	void retriggerResetsTimer() {
		long start = 10_000L;
		WeaponBarHud.triggerFlyoutAt(start);

		// Advance to mid-fadeout
		assertEquals(0.5f, WeaponBarHud.getFlyoutAlpha(start + 1750L), 0.001f);

		// Retrigger (e.g. player switched weapons again)
		WeaponBarHud.triggerFlyoutAt(start + 1750L);

		// Opacity should immediately be restored to 1.0f
		assertEquals(1.0f, WeaponBarHud.getFlyoutAlpha(start + 1750L));
		assertEquals(1.0f, WeaponBarHud.getFlyoutAlpha(start + 1750L + 1500L));
		assertEquals(0.0f, WeaponBarHud.getFlyoutAlpha(start + 1750L + 2000L));
	}
}
