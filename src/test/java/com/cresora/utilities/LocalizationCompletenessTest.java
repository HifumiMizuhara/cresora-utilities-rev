package com.cresora.utilities;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.cresora.utilities.combat.CombatDamageType;
import com.cresora.utilities.weapon.WeaponContentRegistry;
import com.cresora.utilities.weapon.WeaponDefinition;
import com.cresora.utilities.weapon.WeaponZone;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Regression guard for translation assets completeness across locales.
 *
 * <p>Ensures that {@code en_us.json} and {@code zh_cn.json} have matching key sets, and that
 * all registered weapons, weapon zones, combat damage types, and core UI strings have
 * corresponding translations.
 */
class LocalizationCompletenessTest {
	private static final Path LANG_DIR = Path.of("src/main/resources/assets/cresora-utilities/lang");

	@BeforeAll
	static void initRegistry() {
		WeaponContentRegistry.init();
	}

	@Test
	void enUsAndZhCnShareIdenticalKeySet() throws Exception {
		Set<String> enKeys = loadKeys("en_us");
		Set<String> zhKeys = loadKeys("zh_cn");

		List<String> missingInZh = new ArrayList<>(enKeys);
		missingInZh.removeAll(zhKeys);
		Collections.sort(missingInZh);

		List<String> missingInEn = new ArrayList<>(zhKeys);
		missingInEn.removeAll(enKeys);
		Collections.sort(missingInEn);

		assertTrue(missingInZh.isEmpty(), "zh_cn.json is missing keys present in en_us.json: " + missingInZh);
		assertTrue(missingInEn.isEmpty(), "en_us.json is missing keys present in zh_cn.json: " + missingInEn);
	}

	@Test
	void allWeaponsHaveRequiredTranslations() throws Exception {
		Set<String> enKeys = loadKeys("en_us");
		Set<String> zhKeys = loadKeys("zh_cn");

		for (WeaponDefinition weapon : WeaponContentRegistry.weaponDefinitions()) {
			String nameKey = weapon.translationKey();
			assertTrue(enKeys.contains(nameKey), "en_us is missing name translation for weapon: " + weapon.id());
			assertTrue(zhKeys.contains(nameKey), "zh_cn is missing name translation for weapon: " + weapon.id());

			if (!weapon.skill().effectId().equals("none")) {
				String skillKey = "item.cresora-utilities." + weapon.id() + ".skill";
				assertTrue(enKeys.contains(skillKey), "en_us is missing skill translation for weapon: " + weapon.id());
				assertTrue(zhKeys.contains(skillKey), "zh_cn is missing skill translation for weapon: " + weapon.id());
			}
		}
	}

	@Test
	void allWeaponZonesHaveTranslations() throws Exception {
		Set<String> enKeys = loadKeys("en_us");
		Set<String> zhKeys = loadKeys("zh_cn");

		for (WeaponZone zone : WeaponZone.values()) {
			String zoneKey = zone.translationKey();
			assertTrue(enKeys.contains(zoneKey), "en_us is missing translation for zone: " + zone.id());
			assertTrue(zhKeys.contains(zoneKey), "zh_cn is missing translation for zone: " + zone.id());
		}
	}

	@Test
	void allCombatDamageTypesHaveTranslations() throws Exception {
		Set<String> enKeys = loadKeys("en_us");
		Set<String> zhKeys = loadKeys("zh_cn");

		for (CombatDamageType type : CombatDamageType.values()) {
			String typeKey = type.translationKey();
			assertTrue(enKeys.contains(typeKey), "en_us is missing translation for damage type: " + type.id());
			assertTrue(zhKeys.contains(typeKey), "zh_cn is missing translation for damage type: " + type.id());
		}
	}

	@Test
	void coreCombatAndUiStringsArePresent() throws Exception {
		Set<String> enKeys = loadKeys("en_us");

		assertTrue(enKeys.contains("cresora.combat.enter"));
		assertTrue(enKeys.contains("cresora.combat.leave"));
		assertTrue(enKeys.contains("screen.cresora-utilities.weapon_bag"));
		assertTrue(enKeys.contains("cresora.ui.armory_title"));
		assertTrue(enKeys.contains("key.cresora-utilities.toggle_combat"));
	}

	private Set<String> loadKeys(String locale) throws Exception {
		Path path = LANG_DIR.resolve(locale + ".json");
		assertTrue(Files.isRegularFile(path), "Missing language file: " + path);
		try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
			return new HashSet<>(json.keySet());
		}
	}
}
