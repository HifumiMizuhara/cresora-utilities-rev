package com.cresora.utilities.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/** Unit tests for network packet payloads used in combat and weapon UI actions. */
class NetworkPayloadsTest {
	@Test
	void toggleCombatPayloadTypeAndState() {
		ToggleCombatPayload payload = new ToggleCombatPayload();
		assertEquals("cresora-utilities:toggle_combat", payload.type().id().toString());
		assertNotNull(ToggleCombatPayload.STREAM_CODEC);
	}

	@Test
	void selectWeaponSlotPayloadTypeAndIndex() {
		SelectWeaponSlotPayload payload = new SelectWeaponSlotPayload(2);
		assertEquals("cresora-utilities:select_weapon_slot", payload.type().id().toString());
		assertEquals(2, payload.index());
		assertNotNull(SelectWeaponSlotPayload.STREAM_CODEC);
	}

	@Test
	void cycleWeaponSlotPayloadType() {
		CycleWeaponSlotPayload payload = new CycleWeaponSlotPayload();
		assertEquals("cresora-utilities:cycle_weapon_slot", payload.type().id().toString());
		assertNotNull(CycleWeaponSlotPayload.STREAM_CODEC);
	}

	@Test
	void useWeaponSkillPayloadTypeAndSkillSlot() {
		UseWeaponSkillPayload payload = new UseWeaponSkillPayload(1);
		assertEquals("cresora-utilities:use_weapon_skill", payload.type().id().toString());
		assertEquals(1, payload.skillSlot());
		assertNotNull(UseWeaponSkillPayload.STREAM_CODEC);
	}

	@Test
	void weaponBagActionPayloadConstantsAndFields() {
		assertEquals(0, WeaponBagActionPayload.EQUIP);
		assertEquals(1, WeaponBagActionPayload.UNEQUIP);
		assertEquals(2, WeaponBagActionPayload.SELECT);

		WeaponBagActionPayload payload = new WeaponBagActionPayload(WeaponBagActionPayload.EQUIP, 5);
		assertEquals("cresora-utilities:weapon_bag_action", payload.type().id().toString());
		assertEquals(WeaponBagActionPayload.EQUIP, payload.action());
		assertEquals(5, payload.index());
		assertNotNull(WeaponBagActionPayload.STREAM_CODEC);
	}
}
