package com.cresora.utilities.compiler;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Weapon compiler driver. Java port of {@code CresoraCompiler.kt}.
 *
 * <p>This class currently covers the Minecraft-independent output: {@code cwc_weapon_content.json},
 * the language files, and the item definition/model JSON. Generation of the Java skill-handler
 * classes (and the compiled skill registry) is deferred until the runtime API they target has
 * been ported; see the migration notes in the repository.
 */
public final class CresoraCompiler {
	private static final String MOD_ID = "cresora-utilities";

	private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private final File inputDir;
	private final File weaponJsonFile;
	private final File sourceResourcesDir;
	private final File generatedResourcesDir;

	public CresoraCompiler(File inputDir, File weaponJsonFile, File sourceResourcesDir, File generatedResourcesDir) {
		this.inputDir = inputDir;
		this.weaponJsonFile = weaponJsonFile;
		this.sourceResourcesDir = sourceResourcesDir;
		this.generatedResourcesDir = generatedResourcesDir;
	}

	public void compile() {
		if (!inputDir.exists()) {
			check(inputDir.mkdirs() || inputDir.isDirectory(), "Could not create input directory: " + inputDir);
		}
		File[] files = inputDir.listFiles((dir, name) -> name.endsWith(".cresora"));

		List<Ast.WeaponDef> weapons = new ArrayList<>();
		List<Ast.DictionaryDef> dictionaries = new ArrayList<>();

		if (files != null) {
			for (File file : files) {
				String source = readString(file);
				List<Ast> nodes = new Parser(source, new Lexer(source).scanTokens()).parse();
				for (Ast node : nodes) {
					if (node instanceof Ast.WeaponDef weapon) {
						weapons.add(weapon);
					} else if (node instanceof Ast.DictionaryDef dictionary) {
						dictionaries.add(dictionary);
					}
				}
			}
		}

		updateWeaponJson(weapons);
		updateLangFiles(weapons, dictionaries);
		updateItemModels(weapons);
	}

	private void updateWeaponJson(List<Ast.WeaponDef> weapons) {
		JsonObject root = new JsonObject();
		JsonArray defs = new JsonArray();
		root.add("weaponDefinitions", defs);

		for (Ast.WeaponDef weapon : weapons) {
			JsonObject weaponObj = new JsonObject();
			weaponObj.addProperty("id", weapon.id());
			weaponObj.addProperty("baseItemId", weapon.baseItem());
			weaponObj.addProperty("baseAttackDamage", weapon.stats().baseAttackDamage());
			weaponObj.addProperty("attackDamagePerLevel", weapon.stats().attackDamagePerLevel());
			weaponObj.addProperty("totalAttackSpeed", weapon.stats().totalAttackSpeed());
			weaponObj.addProperty("maxBaseLevel", weapon.stats().maxBaseLevel());
			weaponObj.addProperty("maxSkillLevel", weapon.stats().maxSkillLevel());
			weaponObj.addProperty("critRateBonusPercent", weapon.stats().critRateBonusPercent());
			weaponObj.addProperty("maxAllDamageBonusPercent", weapon.stats().maxAllDamageBonusPercent());
			weaponObj.addProperty("hpBonusPercent", weapon.stats().hpBonusPercent());
			weaponObj.addProperty("damageType", weapon.damageType());
			weaponObj.addProperty("role", weapon.role());
			if (weapon.customModelData() != null) {
				weaponObj.addProperty("custom_model_data", weapon.customModelData());
			}
			if (weapon.spirit() != null) {
				weaponObj.add("spirit", spiritJson(weapon.spirit()));
			}

			weaponObj.add("skill", skillJson(weapon.skill()));
			weaponObj.add("upgrades", upgradesJson());

			JsonObject craft = new JsonObject();
			craft.addProperty("fragmentItemId", weapon.id() + "_fragment");
			craft.addProperty("fragmentBaseItemId", "minecraft:prismarine_shard");
			craft.addProperty("fragmentsRequired", 8);
			craft.addProperty("craftedRarity", weapon.rarity().toLowerCase());
			craft.addProperty("craftedBaseLevel", 1);
			craft.addProperty("craftedSkillLevel", 1);
			weaponObj.add("craft", craft);

			JsonObject fragmentDrop = new JsonObject();
			fragmentDrop.addProperty("minMobLevel", 999);
			fragmentDrop.addProperty("chance", 0.0);
			fragmentDrop.addProperty("minCount", 0);
			fragmentDrop.addProperty("maxCount", 0);
			fragmentDrop.addProperty("bonusCountAtLevel70", 0);
			JsonObject drops = new JsonObject();
			drops.add("fragmentDrop", fragmentDrop);
			drops.add("directDropTiers", new JsonArray());
			drops.addProperty("directDropBaseLevelMultiplier", 0.0);
			drops.addProperty("fiveStarChanceMultiplierAtLevel70", 1.0);
			weaponObj.add("drops", drops);

			defs.add(weaponObj);
		}

		File parent = weaponJsonFile.getParentFile();
		if (parent != null) {
			check(parent.mkdirs() || parent.isDirectory(), "Could not create weapon JSON directory: " + parent);
		}
		writeString(weaponJsonFile, gson.toJson(root) + "\n");
	}

	private JsonObject spiritJson(Ast.Spirit spirit) {
		JsonObject spiritObj = new JsonObject();
		spiritObj.addProperty("nameKey", spirit.nameKey());
		if (!spirit.voiceLines().isEmpty()) {
			JsonObject voiceLines = new JsonObject();
			spirit.voiceLines().forEach(voiceLines::addProperty);
			spiritObj.add("voiceLines", voiceLines);
		}
		if (!spirit.bondStages().isEmpty()) {
			JsonArray bondStages = new JsonArray();
			for (Ast.BondStage stage : spirit.bondStages()) {
				JsonObject stageObj = new JsonObject();
				stageObj.addProperty("stage", stage.stage());
				stageObj.addProperty("titleKey", stage.titleKey());
				stageObj.addProperty("storyKey", stage.storyKey());
				bondStages.add(stageObj);
			}
			spiritObj.add("bondStages", bondStages);
		}
		if (spirit.awakeningConditionKey() != null) {
			spiritObj.addProperty("awakeningConditionKey", spirit.awakeningConditionKey());
		}
		return spiritObj;
	}

	private JsonObject skillJson(Ast.Skill skill) {
		JsonObject skillObj = new JsonObject();
		if (skill != null) {
			skillObj.addProperty("effectId", skill.effectId());
			skillObj.addProperty("durationSeconds", (int) skill.durationSeconds());
			skillObj.addProperty("cooldownSeconds", (int) skill.cooldownSeconds());
			skillObj.addProperty("baseValue", skill.baseValue());
			skillObj.addProperty("valuePerLevel", skill.valuePerLevel());
			skillObj.addProperty("radiusMeters", skill.radiusMeters());
			if (skill.note() != null) {
				skillObj.addProperty("note", skill.note());
			}
		} else {
			skillObj.addProperty("effectId", "none");
			skillObj.addProperty("durationSeconds", 0);
			skillObj.addProperty("cooldownSeconds", 0);
			skillObj.addProperty("baseValue", 0.0);
			skillObj.addProperty("valuePerLevel", 0.0);
			skillObj.addProperty("radiusMeters", 0.0);
		}
		return skillObj;
	}

	private static JsonObject upgradesJson() {
		JsonObject upgrades = new JsonObject();
		upgrades.addProperty("baseCscQuadraticCoefficient", 85);
		upgrades.addProperty("skillCscLinearCoefficient", 10000);
		upgrades.addProperty("skillCscQuadraticCoefficient", 0);
		upgrades.addProperty("baseFragmentCost", 1);
		return upgrades;
	}

	private void updateLangFiles(List<Ast.WeaponDef> weapons, List<Ast.DictionaryDef> dictionaries) {
		File generatedLangDir = new File(generatedResourceRoot(), "assets/" + MOD_ID + "/lang");
		File sourceLangDir = sourceResourcesDir == null ? null : new File(sourceResourcesDir, "assets/" + MOD_ID + "/lang");

		Set<String> locales = new LinkedHashSet<>();
		weapons.forEach(w -> locales.addAll(w.translations().keySet()));
		dictionaries.forEach(d -> locales.addAll(d.translations().keySet()));

		for (String locale : locales) {
			File generatedLangFile = new File(generatedLangDir, locale + ".json");
			JsonObject langJson;
			if (generatedLangFile.isFile()) {
				langJson = JsonParser.parseString(readString(generatedLangFile)).getAsJsonObject();
			} else {
				File sourceLangFile = sourceLangDir == null ? null : new File(sourceLangDir, locale + ".json");
				langJson = sourceLangFile != null && sourceLangFile.isFile()
						? JsonParser.parseString(readString(sourceLangFile)).getAsJsonObject()
						: new JsonObject();
			}

			for (Ast.DictionaryDef dictionary : dictionaries) {
				Map<String, String> pairs = dictionary.translations().get(locale);
				if (pairs != null) {
					pairs.forEach(langJson::addProperty);
				}
			}

			for (Ast.WeaponDef weapon : weapons) {
				Map<String, String> trans = weapon.translations().get(locale);
				if (trans == null) {
					continue;
				}
				putIfPresent(langJson, "item." + MOD_ID + "." + weapon.id(), trans.get("name"));
				putIfPresent(langJson, "item." + MOD_ID + "." + weapon.id() + ".skill", trans.get("skill_name"));
				putIfPresent(langJson, "item." + MOD_ID + "." + weapon.id() + "_fragment", trans.get("fragment_name"));
				for (Ast.SubSkill subSkill : weapon.subSkills()) {
					putIfPresent(langJson, "item." + MOD_ID + ".sub_skill." + subSkill.effectId(),
							trans.get("sub_skill_" + subSkill.effectId() + "_name"));
				}
				if (weapon.skill() != null) {
					for (Ast.Buff buff : weapon.skill().buffs()) {
						writeBuffLang(langJson, weapon, buff, trans, locale);
					}
				}
				for (Map.Entry<String, String> entry : trans.entrySet()) {
					String key = entry.getKey();
					if (!key.startsWith("name") && !key.startsWith("skill_name") && !key.startsWith("fragment_name")
							&& !key.startsWith("buff_") && !key.startsWith("sub_skill_")) {
						langJson.addProperty(key, entry.getValue());
					}
				}
			}

			check(generatedLangDir.mkdirs() || generatedLangDir.isDirectory(),
					"Could not create generated language directory: " + generatedLangDir);
			writeString(generatedLangFile, gson.toJson(langJson));
		}
	}

	private void writeBuffLang(JsonObject langJson, Ast.WeaponDef weapon, Ast.Buff buff, Map<String, String> trans, String locale) {
		String buffNameKey = buff.translationKey() != null
				? buff.translationKey()
				: "item.cresora.weapon.skill.buff." + buff.id() + ".name";
		String defaultName = capitalizeWords(buff.id());
		langJson.addProperty(buffNameKey, trans.getOrDefault("buff_" + buff.id() + "_name", defaultName));

		String gained = trans.getOrDefault("buff_" + buff.id() + "_gained", switch (locale) {
			case "zh_cn" -> "获得效果: %s (%s 层)";
			case "ja_jp" -> "効果獲得: %s (%s 層)";
			default -> "Gained buff: %s (%s stacks)";
		});
		langJson.addProperty("item.cresora.weapon.skill.buff." + buff.id() + ".gained", gained);

		String expired = trans.getOrDefault("buff_" + buff.id() + "_expired", switch (locale) {
			case "zh_cn" -> "效果结束: %s";
			case "ja_jp" -> "効果終了: %s";
			default -> "Buff expired: %s";
		});
		langJson.addProperty("item.cresora.weapon.skill.buff." + buff.id() + ".expired", expired);

		if (weapon.skill() != null && weapon.skill().buffs().stream().anyMatch(b -> b.id().equals(buff.id()) && b.decay().equals("independent"))) {
			String decreased = trans.getOrDefault("buff_" + buff.id() + "_decreased", switch (locale) {
				case "zh_cn" -> "层数减少: %s (剩余 %s 层)";
				case "ja_jp" -> "スタック減少: %s (残り %s 層)";
				default -> "Stack decayed: %s (%s remaining)";
			});
			langJson.addProperty("item.cresora.weapon.skill.buff." + buff.id() + ".decreased", decreased);
		}
	}

	private void updateItemModels(List<Ast.WeaponDef> weapons) {
		File generatedAssetsDir = new File(generatedResourceRoot(), "assets/" + MOD_ID);
		File sourceAssetsDir = sourceResourcesDir == null ? generatedAssetsDir : new File(sourceResourcesDir, "assets/" + MOD_ID);
		File itemsDir = new File(generatedAssetsDir, "items");
		File modelsDir = new File(generatedAssetsDir, "models/item");
		check(itemsDir.mkdirs() || itemsDir.isDirectory(), "Could not create item definition directory: " + itemsDir);
		check(modelsDir.mkdirs() || modelsDir.isDirectory(), "Could not create item model directory: " + modelsDir);

		for (Ast.WeaponDef weapon : weapons) {
			File itemDefFile = new File(itemsDir, weapon.id() + ".json");
			if (!itemDefFile.exists()) {
				writeString(itemDefFile, gson.toJson(itemDefinition(MOD_ID + ":item/" + weapon.id())));
			}
			writeString(new File(itemsDir, weapon.id() + "_fragment.json"),
					gson.toJson(itemDefinition(MOD_ID + ":item/" + weapon.id() + "_fragment")));

			JsonObject modelJson = new JsonObject();
			modelJson.addProperty("parent", "minecraft:item/handheld");
			JsonObject textures = new JsonObject();
			File customTextureFile = new File(sourceAssetsDir, "textures/item/" + weapon.id() + ".png");
			String texturePath = weapon.texture() != null
					? weapon.texture()
					: customTextureFile.exists()
							? MOD_ID + ":item/" + weapon.id()
							: baseItemTexture(weapon.baseItem());
			textures.addProperty("layer0", texturePath);
			modelJson.add("textures", textures);
			writeString(new File(modelsDir, weapon.id() + ".json"), gson.toJson(modelJson));

			JsonObject fragmentModelJson = new JsonObject();
			fragmentModelJson.addProperty("parent", "minecraft:item/generated");
			JsonObject fragmentTextures = new JsonObject();
			File customFragmentTexture = new File(sourceAssetsDir, "textures/item/" + weapon.id() + "_fragment.png");
			fragmentTextures.addProperty("layer0", customFragmentTexture.exists()
					? MOD_ID + ":item/" + weapon.id() + "_fragment"
					: "minecraft:item/prismarine_shard");
			fragmentModelJson.add("textures", fragmentTextures);
			writeString(new File(modelsDir, weapon.id() + "_fragment.json"), gson.toJson(fragmentModelJson));
		}

		writeString(new File(itemsDir, "sub_skill_dummy.json"), gson.toJson(subSkillDummy(weapons)));
	}

	private static JsonObject itemDefinition(String model) {
		JsonObject modelObj = new JsonObject();
		modelObj.addProperty("type", "minecraft:model");
		modelObj.addProperty("model", model);
		JsonObject root = new JsonObject();
		root.add("model", modelObj);
		return root;
	}

	private static JsonObject subSkillDummy(List<Ast.WeaponDef> weapons) {
		JsonObject model = new JsonObject();
		model.addProperty("type", "minecraft:select");
		model.addProperty("property", "minecraft:component");
		model.addProperty("component", MOD_ID + ":sub_skill_effect_id");

		JsonArray cases = new JsonArray();
		Set<String> seen = new LinkedHashSet<>();
		for (Ast.WeaponDef weapon : weapons) {
			for (Ast.SubSkill subSkill : weapon.subSkills()) {
				if (subSkill.icon().isBlank() || !seen.add(subSkill.effectId())) {
					continue;
				}
				JsonObject caseObj = new JsonObject();
				caseObj.addProperty("when", subSkill.effectId());
				JsonObject caseModel = new JsonObject();
				caseModel.addProperty("type", "minecraft:model");
				caseModel.addProperty("model", iconModelPath(subSkill.icon()));
				caseObj.add("model", caseModel);
				cases.add(caseObj);
			}
		}
		model.add("cases", cases);

		JsonObject fallback = new JsonObject();
		fallback.addProperty("type", "minecraft:model");
		fallback.addProperty("model", "minecraft:item/barrier");
		model.add("fallback", fallback);

		JsonObject root = new JsonObject();
		root.add("model", model);
		return root;
	}

	private static String iconModelPath(String icon) {
		String namespace;
		String path;
		if (icon.contains(":")) {
			String[] parts = icon.split(":", 2);
			namespace = parts[0];
			path = parts[1];
		} else {
			namespace = "minecraft";
			path = icon;
		}
		String folder = (path.equals("lightning_rod") || path.equals("blue_ice")) ? "block" : "item";
		return namespace + ":" + folder + "/" + path;
	}

	private static String baseItemTexture(String baseItem) {
		if (baseItem.contains(":")) {
			String[] parts = baseItem.split(":", 2);
			return parts[0] + ":item/" + parts[1];
		}
		return "minecraft:item/" + baseItem;
	}

	private static String capitalizeWords(String id) {
		StringBuilder sb = new StringBuilder();
		for (String word : id.split(" ")) {
			if (word.isEmpty()) {
				continue;
			}
			if (sb.length() > 0) {
				sb.append(' ');
			}
			sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
		}
		return sb.toString();
	}

	private File generatedResourceRoot() {
		return generatedResourcesDir != null
				? generatedResourcesDir
				: weaponJsonFile.getParentFile().getParentFile().getParentFile().getParentFile();
	}

	private static void putIfPresent(JsonObject json, String key, String value) {
		if (value != null) {
			json.addProperty(key, value);
		}
	}

	private static String readString(File file) {
		try {
			return Files.readString(file.toPath(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static void writeString(File file, String content) {
		try {
			Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}
}
