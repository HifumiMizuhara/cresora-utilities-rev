package com.cresora.utilities.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests the Minecraft-independent output of the weapon compiler. */
class CresoraCompilerTest {
	private static final String FIXTURE = """
			weapon "Golden Fixture" {
			    id: "golden_fixture"
			    rarity: "3_star"
			    base_item: "minecraft:iron_sword"
			    damage_type: physical
			    role: guard
			    stats {
			        base_damage: 6.0
			        damage_per_level: 0.4
			        speed: 1.6
			        max_base_level: 70
			        max_skill_level: 10
			    }
			    skill "Golden Strike" {
			        effect_id: "golden_strike"
			        cooldown: 20
			        duration: 10
			        base_value: 4.0
			        value_per_level: 0.5
			        radius: 0.0
			        buff "golden_buff" {
			            max_stacks: 3
			            duration: 5
			            stats { attack_percent_per_stack: 0.1 }
			        }
			    }
			    translations {
			        en_us { name: "Golden Fixture" }
			    }
			}
			""";

	@Test
	void generatesWeaponJsonLangAndModels(@TempDir Path temp) throws Exception {
		Path inputDir = temp.resolve("cresora");
		Files.createDirectories(inputDir);
		Files.writeString(inputDir.resolve("fixture.cresora"), FIXTURE, StandardCharsets.UTF_8);

		Path generatedResources = temp.resolve("generated/resources");
		File weaponJsonFile = temp.resolve("generated/resources/data/cresora-utilities/cresora/cwc_weapon_content.json").toFile();

		CresoraCompiler compiler = new CresoraCompiler(
				inputDir.toFile(),
				weaponJsonFile,
				temp.resolve("empty-src-resources").toFile(),
				generatedResources.toFile());
		compiler.compile();

		JsonObject root = JsonParser.parseString(Files.readString(weaponJsonFile.toPath())).getAsJsonObject();
		JsonObject weapon = root.getAsJsonArray("weaponDefinitions").get(0).getAsJsonObject();
		assertEquals("golden_fixture", weapon.get("id").getAsString());
		assertEquals(6.0, weapon.get("baseAttackDamage").getAsDouble());
		assertEquals(70, weapon.get("maxBaseLevel").getAsInt());
		assertEquals("golden_strike", weapon.getAsJsonObject("skill").get("effectId").getAsString());
		assertEquals(10, weapon.getAsJsonObject("skill").get("durationSeconds").getAsInt());
		assertEquals("golden_fixture_fragment", weapon.getAsJsonObject("craft").get("fragmentItemId").getAsString());
		assertEquals(999, weapon.getAsJsonObject("drops").getAsJsonObject("fragmentDrop").get("minMobLevel").getAsInt());

		Path langFile = generatedResources.resolve("assets/cresora-utilities/lang/en_us.json");
		JsonObject lang = JsonParser.parseString(Files.readString(langFile)).getAsJsonObject();
		assertEquals("Golden Fixture", lang.get("item.cresora-utilities.golden_fixture").getAsString());
		assertTrue(lang.has("item.cresora.weapon.skill.buff.golden_buff.name"));

		Path modelFile = generatedResources.resolve("assets/cresora-utilities/models/item/golden_fixture.json");
		JsonObject model = JsonParser.parseString(Files.readString(modelFile)).getAsJsonObject();
		assertEquals("minecraft:item/handheld", model.get("parent").getAsString());
		assertEquals("minecraft:item/iron_sword",
				model.getAsJsonObject("textures").get("layer0").getAsString());

		Path itemDefFile = generatedResources.resolve("assets/cresora-utilities/items/golden_fixture.json");
		JsonObject itemDef = JsonParser.parseString(Files.readString(itemDefFile)).getAsJsonObject();
		assertEquals("minecraft:model", itemDef.getAsJsonObject("model").get("type").getAsString());
		assertEquals("cresora-utilities:item/golden_fixture",
				itemDef.getAsJsonObject("model").get("model").getAsString());
	}
}
