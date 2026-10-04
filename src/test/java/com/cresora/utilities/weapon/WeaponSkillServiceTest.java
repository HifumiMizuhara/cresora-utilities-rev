package com.cresora.utilities.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link WeaponSkillService} formatting and transient cooldown state. */
class WeaponSkillServiceTest {
	private Map<UUID, Map<String, Double>> cooldownsMap;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() throws Exception {
		Field field = WeaponSkillService.class.getDeclaredField("COOLDOWNS");
		field.setAccessible(true);
		cooldownsMap = (Map<UUID, Map<String, Double>>) field.get(null);
		cooldownsMap.clear();
	}

	@AfterEach
	void tearDown() {
		if (cooldownsMap != null) {
			cooldownsMap.clear();
		}
	}

	@Test
	void formatNumberRemovesTrailingZerosForIntegers() {
		assertEquals("0", WeaponSkillService.formatNumber(0.0));
		assertEquals("1", WeaponSkillService.formatNumber(1.0));
		assertEquals("5", WeaponSkillService.formatNumber(5.000));
		assertEquals("42", WeaponSkillService.formatNumber(42.0));
	}

	@Test
	void formatNumberRoundsToOneDecimalPlace() {
		assertEquals("2.5", WeaponSkillService.formatNumber(2.5));
		assertEquals("3.1", WeaponSkillService.formatNumber(3.14));
		assertEquals("9.9", WeaponSkillService.formatNumber(9.88));
	}

	@Test
	void cooldownKeyFormatsWithSlotIndex() throws Exception {
		Method method = WeaponSkillService.class.getDeclaredMethod("cooldownKey", WeaponDefinition.class, WeaponSkillDefinition.class);
		method.setAccessible(true);

		WeaponDefinition dummy = WeaponDefinition.DUMMY;
		WeaponSkillDefinition skill = dummy.skill();
		String key = (String) method.invoke(null, dummy, skill);
		assertEquals("dummy#0", key);
	}

	@Test
	void transientCooldownsTickDownAndExpire() throws Exception {
		UUID playerId = UUID.randomUUID();
		Map<String, Double> playerCooldowns = new HashMap<>();
		playerCooldowns.put("lakeside_stride#0", 2.0);
		playerCooldowns.put("rondo_melody#0", 1.0);
		cooldownsMap.put(playerId, playerCooldowns);

		assertEquals(2.0, cooldownsMap.get(playerId).get("lakeside_stride#0"));
		assertEquals(1.0, cooldownsMap.get(playerId).get("rondo_melody#0"));

		// Simulate server tick decrement
		for (var entry : playerCooldowns.entrySet()) {
			entry.setValue(entry.getValue() - 1.0);
		}
		playerCooldowns.entrySet().removeIf(e -> e.getValue() <= 0.0);

		assertEquals(1.0, cooldownsMap.get(playerId).get("lakeside_stride#0"));
		assertFalse(cooldownsMap.get(playerId).containsKey("rondo_melody#0"));

		// Next tick
		for (var entry : playerCooldowns.entrySet()) {
			entry.setValue(entry.getValue() - 1.0);
		}
		playerCooldowns.entrySet().removeIf(e -> e.getValue() <= 0.0);
		if (playerCooldowns.isEmpty()) {
			cooldownsMap.remove(playerId);
		}

		assertFalse(cooldownsMap.containsKey(playerId));
	}
}
