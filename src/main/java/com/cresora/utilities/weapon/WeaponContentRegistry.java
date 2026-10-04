package com.cresora.utilities.weapon;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.cresora.utilities.CresoraUtilities;
import com.cresora.utilities.combat.CombatDamageType;
import com.cresora.utilities.equipment.EquipmentRarity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry of weapon definitions. Java port of {@code WeaponContentRegistry.kt}.
 *
 * <p>Built-in defaults are applied first; {@code weapon_content.json} and then
 * {@code cwc_weapon_content.json} (both optional) override them when present on the classpath.
 */
public final class WeaponContentRegistry {
	private static final String CONTENT_RESOURCE = "data/cresora-utilities/cresora/weapon_content.json";
	private static final String CWC_CONTENT_RESOURCE = "data/cresora-utilities/cresora/cwc_weapon_content.json";
	private static final Logger LOGGER = LoggerFactory.getLogger(CresoraUtilities.MOD_ID + "/weapon-content");

	static final Codec<WeaponDefinition> WEAPON_DEFINITION_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("id").forGetter(WeaponDefinition::id),
			Codec.STRING.fieldOf("baseItemId").forGetter(WeaponDefinition::baseItemId),
			WeaponStats.CODEC.forGetter(wd -> new WeaponStats(
					wd.baseAttackDamage(), wd.attackDamagePerLevel(), wd.totalAttackSpeed(),
					wd.maxBaseLevel(), wd.maxSkillLevel(), wd.critRateBonusPercent(),
					wd.maxAllDamageBonusPercent(), wd.hpBonusPercent(), wd.damageType(), wd.role())),
			WeaponAttackCurvePoint.CODEC.listOf().optionalFieldOf("attackCurve", List.of()).forGetter(WeaponDefinition::attackCurve),
			WeaponSkillDefinition.CODEC.fieldOf("skill").forGetter(WeaponDefinition::skill),
			WeaponUpgradeDefinition.CODEC.fieldOf("upgrades").forGetter(WeaponDefinition::upgrades),
			WeaponCraftDefinition.CODEC.fieldOf("craft").forGetter(WeaponDefinition::craft),
			WeaponDropDefinition.CODEC.fieldOf("drops").forGetter(WeaponDefinition::drops),
			Codec.INT.optionalFieldOf("custom_model_data").forGetter(wd -> Optional.ofNullable(wd.customModelData())),
			WeaponSpiritDefinition.CODEC.optionalFieldOf("spirit").forGetter(wd -> Optional.ofNullable(wd.spirit()))
	).apply(instance, (id, baseItemId, stats, curve, skill, upgrades, craft, drops, modelData, spirit) ->
			WeaponDefinition.builder(id, baseItemId,
							stats.baseAttackDamage(), stats.attackDamagePerLevel(), stats.totalAttackSpeed(),
							stats.maxBaseLevel(), stats.maxSkillLevel(), skill, upgrades, craft, drops)
					.critRateBonusPercent(stats.critRateBonusPercent())
					.maxAllDamageBonusPercent(stats.maxAllDamageBonusPercent())
					.hpBonusPercent(stats.hpBonusPercent())
					.damageType(stats.damageType())
					.role(stats.role())
					.attackCurve(curve)
					.customModelData(modelData.orElse(null))
					.spirit(spirit.orElse(null))
					.build()));

	private static volatile Map<String, WeaponDefinition> weapons = Map.of();

	private WeaponContentRegistry() {
	}

	public static void init() {
		weapons = Map.of();
		applyBundle(defaultBundle());

		InputStream legacyStream = WeaponContentRegistry.class.getClassLoader().getResourceAsStream(CONTENT_RESOURCE);
		if (legacyStream != null) {
			try (InputStream stream = legacyStream; InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
				var json = JsonParser.parseReader(reader);
				WeaponContentBundle bundle = WeaponContentBundle.CODEC.parse(JsonOps.INSTANCE, json)
						.getOrThrow(message -> new IllegalArgumentException("Invalid weapon content: " + message));
				applyBundle(bundle);
				LOGGER.info("Loaded weapon content from {}", CONTENT_RESOURCE);
			} catch (Exception throwable) {
				LOGGER.error("Failed to load weapon content from {}. Using built-in defaults.", CONTENT_RESOURCE, throwable);
			}
		} else {
			LOGGER.info("Optional legacy weapon content resource {} not found; using built-in defaults.", CONTENT_RESOURCE);
		}

		try {
			applyBundle(loadBundledContent(CWC_CONTENT_RESOURCE));
			LOGGER.info("Loaded CWC weapon content from {}", CWC_CONTENT_RESOURCE);
		} catch (Exception throwable) {
			LOGGER.warn("Failed to load CWC weapon content from {}. If you haven't compiled .cresora files yet, this is expected.",
					CWC_CONTENT_RESOURCE, throwable);
		}
	}

	public static List<WeaponDefinition> weaponDefinitions() {
		List<WeaponDefinition> sorted = new ArrayList<>(weapons.values());
		sorted.sort(Comparator.comparing(WeaponDefinition::id));
		return List.copyOf(sorted);
	}

	public static WeaponDefinition weapon(String id) {
		return weapons.get(id);
	}

	public static WeaponDefinition requireWeapon(String id) {
		WeaponDefinition definition = weapons.get(id);
		if (definition == null) {
			throw new IllegalStateException("Unknown weapon definition: " + id);
		}
		return definition;
	}

	public static WeaponDefinition definitionByFragmentId(String fragmentItemId) {
		for (WeaponDefinition definition : weapons.values()) {
			if (definition.craft().fragmentItemId().equals(fragmentItemId)) {
				return definition;
			}
		}
		return null;
	}

	private static WeaponContentBundle loadBundledContent(String resourcePath) {
		InputStream stream = WeaponContentRegistry.class.getClassLoader().getResourceAsStream(resourcePath);
		if (stream == null) {
			throw new IllegalStateException("Missing resource: " + resourcePath);
		}
		try (InputStream in = stream; InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
			var json = JsonParser.parseReader(reader);
			return WeaponContentBundle.CODEC.parse(JsonOps.INSTANCE, json)
					.getOrThrow(message -> new IllegalArgumentException("Invalid weapon content: " + message));
		} catch (java.io.IOException e) {
			throw new java.io.UncheckedIOException(e);
		}
	}

	private static void applyBundle(WeaponContentBundle bundle) {
		Map<String, WeaponDefinition> weaponMap = new LinkedHashMap<>(weapons);
		Map<String, WeaponDefinition> newWeapons = new LinkedHashMap<>();
		for (WeaponDefinition definition : bundle.weaponDefinitions()) {
			newWeapons.put(definition.id(), definition);
		}
		require(newWeapons.size() == bundle.weaponDefinitions().size(), "Duplicate weapon ids found in content bundle");

		for (WeaponDefinition definition : bundle.weaponDefinitions()) {
			require(definition.maxBaseLevel() >= 1, "Weapon '" + definition.id() + "' maxBaseLevel must be >= 1");
			require(definition.maxSkillLevel() >= 1, "Weapon '" + definition.id() + "' maxSkillLevel must be >= 1");
			require(definition.craft().fragmentsRequired() >= 0, "Weapon '" + definition.id() + "' has negative fragmentsRequired");
			require(definition.upgrades().baseFragmentCost() >= 0, "Weapon '" + definition.id() + "' has negative baseFragmentCost");
			require(definition.upgrades().skillArtifactCountPerLevel() >= 0, "Weapon '" + definition.id() + "' has negative skillArtifactCountPerLevel");
			WeaponSpiritDefinition spirit = definition.spirit();
			if (spirit != null) {
				require(!spirit.nameKey().isBlank(), "Weapon '" + definition.id() + "' spirit.nameKey must not be blank");
				require(spirit.bondStages().stream().map(WeaponSpiritBondStageDefinition::stage).distinct().count() == spirit.bondStages().size(),
						"Weapon '" + definition.id() + "' spirit bond stages must be unique");
				for (WeaponSpiritBondStageDefinition stage : spirit.bondStages()) {
					require(stage.stage() >= 1 && stage.stage() <= 6,
							"Weapon '" + definition.id() + "' spirit bond stage " + stage.stage() + " must be in 1..6");
					require(!stage.titleKey().isBlank(),
							"Weapon '" + definition.id() + "' spirit bond stage " + stage.stage() + " titleKey must not be blank");
					require(!stage.storyKey().isBlank(),
							"Weapon '" + definition.id() + "' spirit bond stage " + stage.stage() + " storyKey must not be blank");
				}
			}
			if (!definition.attackCurve().isEmpty()) {
				List<WeaponAttackCurvePoint> sorted = new ArrayList<>(definition.attackCurve());
				sorted.sort(Comparator.comparingInt(WeaponAttackCurvePoint::level));
				require(sorted.equals(definition.attackCurve()), "Weapon '" + definition.id() + "' attackCurve must be sorted by level");
				require(sorted.get(0).level() == 1, "Weapon '" + definition.id() + "' attackCurve must start at level 1");
				require(sorted.get(sorted.size() - 1).level() == definition.maxBaseLevel(),
						"Weapon '" + definition.id() + "' attackCurve must end at maxBaseLevel " + definition.maxBaseLevel());
			}
		}

		weaponMap.putAll(newWeapons);
		weapons = Map.copyOf(weaponMap);
	}

	private static WeaponContentBundle defaultBundle() {
		List<WeaponDefinition> defs = new ArrayList<>();
		defs.add(WeaponDefinition.builder("rondo_melody", "minecraft:wooden_sword", 7.0, 0.18, 1.6, 60, 10,
						new WeaponSkillDefinition("shield", 15, 20, 3.0, 0.5, 0, 0, 0, 1.0, "bass"),
						WeaponUpgradeDefinition.of(100, 10_000, 1),
						new WeaponCraftDefinition("rondo_melody_fragment", "minecraft:prismarine_shard", 8, WeaponRarity.TWO_STAR, 1, 1),
						fragmentFieldDrops())
				.build());
		defs.add(WeaponDefinition.builder("masquerade_invitation", "minecraft:wooden_sword", 7.0, 0.18, 1.6, 60, 10,
						new WeaponSkillDefinition("heal", 0, 30, 2.0, 0.5, 0, 0, 0, 1.0, "harmony"),
						WeaponUpgradeDefinition.of(100, 10_000, 1),
						new WeaponCraftDefinition("masquerade_invitation_fragment", "minecraft:amethyst_shard", 8, WeaponRarity.TWO_STAR, 1, 1),
						fragmentFieldDrops())
				.build());
		defs.add(WeaponDefinition.builder("lakeside_stride", "minecraft:diamond_sword", 7.0, 0.0, 1.6, 60, 10,
						new WeaponSkillDefinition("current_hp_true_damage", 0, 50, 0.0, 5.0, 5.0, 0, 0, 1.0, "melody"),
						new WeaponUpgradeDefinition(100, 0, 10_000, 1, EquipmentRarity.FOUR_STAR, 1),
						new WeaponCraftDefinition("lakeside_stride_fragment", "minecraft:prismarine_crystals", 8, WeaponRarity.FIVE_STAR, 1, 1),
						resonanceOnlyDrops())
				.critRateBonusPercent(10.0)
				.attackCurve(List.of(new WeaponAttackCurvePoint(1, 7.0), new WeaponAttackCurvePoint(5, 8.0),
						new WeaponAttackCurvePoint(10, 9.5), new WeaponAttackCurvePoint(30, 13.5),
						new WeaponAttackCurvePoint(50, 18.0), new WeaponAttackCurvePoint(60, 22.0)))
				.build());
		defs.add(WeaponDefinition.builder("hanwu_juanxue", "minecraft:diamond_sword", 8.0, 0.0, 1.6, 60, 10,
						new WeaponSkillDefinition("snow_frost", 10, 25, 1.0, 0.3, 0, 0, 0, 1.0, "treble"),
						new WeaponUpgradeDefinition(100, 0, 10_000, 1, EquipmentRarity.FOUR_STAR, 1),
						new WeaponCraftDefinition("hanwu_juanxue_fragment", "minecraft:snowball", 8, WeaponRarity.FIVE_STAR, 1, 1),
						resonanceOnlyDrops())
				.damageType(CombatDamageType.ARCANE)
				.attackCurve(List.of(new WeaponAttackCurvePoint(1, 8.0), new WeaponAttackCurvePoint(5, 9.5),
						new WeaponAttackCurvePoint(10, 11.0), new WeaponAttackCurvePoint(30, 15.5),
						new WeaponAttackCurvePoint(50, 21.5), new WeaponAttackCurvePoint(60, 26.0)))
				.build());
		defs.add(WeaponDefinition.builder("kyokusui_no_ryusho", "minecraft:diamond_sword", 8.0, 0.0, 1.6, 60, 1,
						new WeaponSkillDefinition("orchid_pavilion_echo", 16, 25, 0.0, 0.0, 5.0, 0, 0, 2.0, "melody"),
						WeaponUpgradeDefinition.of(100, 0, 1),
						new WeaponCraftDefinition("kyokusui_no_ryusho_fragment", "minecraft:book", 8, WeaponRarity.FIVE_STAR, 1, 1),
						resonanceOnlyDrops())
				.attackCurve(List.of(new WeaponAttackCurvePoint(1, 8.0), new WeaponAttackCurvePoint(5, 9.0),
						new WeaponAttackCurvePoint(10, 10.5), new WeaponAttackCurvePoint(30, 14.5),
						new WeaponAttackCurvePoint(50, 20.0), new WeaponAttackCurvePoint(60, 24.0)))
				.build());
		defs.add(WeaponDefinition.builder("requiem_toward_dawn", "minecraft:iron_sword", 8.5, 0.20, 1.6, 60, 10,
						new WeaponSkillDefinition("flame_aura", 10, 25, 3.0, 1.0, 4.0, 0, 0, 1.0, "harmony"),
						WeaponUpgradeDefinition.of(100, 10_000, 1),
						new WeaponCraftDefinition("requiem_toward_dawn_fragment", "minecraft:iron_ingot", 8, WeaponRarity.FOUR_STAR, 1, 1),
						resonanceOnlyDrops())
				.maxAllDamageBonusPercent(10.0)
				.damageType(CombatDamageType.ARCANE)
				.build());
		defs.add(WeaponDefinition.builder("gaoshan_liushui", "minecraft:iron_sword", 8.5, 0.20, 1.6, 60, 10,
						new WeaponSkillDefinition("healing_aura", 8, 20, 2.0, 0.5, 5.0, 0.75, 0.25, 2.0, "melody"),
						WeaponUpgradeDefinition.of(100, 10_000, 1),
						new WeaponCraftDefinition("gaoshan_liushui_fragment", "minecraft:lapis_lazuli", 8, WeaponRarity.FOUR_STAR, 1, 1),
						resonanceOnlyDrops())
				.build());
		defs.add(WeaponDefinition.builder("dark_lux", "minecraft:netherite_sword", 8.5, 0.20, 1.6, 60, 10,
						new WeaponSkillDefinition("dark_lux", 30, 30, 0.0, 0.0, 0, 0, 0, 1.0, "bass"),
						new WeaponUpgradeDefinition(100, 0, 10_000, 1, EquipmentRarity.FOUR_STAR, 1),
						new WeaponCraftDefinition("dark_lux_fragment", "minecraft:echo_shard", 8, WeaponRarity.FIVE_STAR, 1, 1),
						resonanceOnlyDrops())
				.damageType(CombatDamageType.PHYSICAL)
				.build());
		defs.add(WeaponDefinition.builder("pastoral_flute_reverie", "minecraft:wooden_sword", 7.0, 0.18, 1.6, 60, 10,
						new WeaponSkillDefinition("sunlit_haste", 50, 50, 40.0, 0.0, 0, 60.0, 0.0, 1.0, "treble"),
						WeaponUpgradeDefinition.of(100, 10_000, 1),
						new WeaponCraftDefinition("pastoral_flute_reverie_fragment", "minecraft:bamboo", 8, WeaponRarity.THREE_STAR, 1, 1),
						resonanceOnlyDrops())
				.damageType(CombatDamageType.PHYSICAL)
				.build());
		defs.add(WeaponDefinition.builder("cadenza_allegro", "minecraft:wooden_sword", 8.5, 0.20, 1.6, 80, 1,
						new WeaponSkillDefinition("baa_mimic", 0, 15, 1.0, 0.0, 10.0, 0, 0, 1.0, "harmony"),
						WeaponUpgradeDefinition.of(100, 0, 1),
						new WeaponCraftDefinition("cadenza_allegro_fragment", "minecraft:white_wool", 8, WeaponRarity.FOUR_STAR, 1, 1),
						resonanceOnlyDrops())
				.build());
		defs.add(WeaponDefinition.builder("dummy_four_star_b", "minecraft:golden_sword", 8.5, 0.20, 1.6, 60, 1,
						new WeaponSkillDefinition("none", 0, 0, 0.0, 0.0, 0, 0, 0, 1.0, null),
						WeaponUpgradeDefinition.of(100, 0, 1),
						new WeaponCraftDefinition("dummy_four_star_b_fragment", "minecraft:gold_ingot", 8, WeaponRarity.FOUR_STAR, 1, 1),
						resonanceOnlyDrops())
				.build());
		return new WeaponContentBundle(List.copyOf(defs));
	}

	private static WeaponDropDefinition fragmentFieldDrops() {
		return new WeaponDropDefinition(
				new WeaponFragmentDropDefinition(5, 0.08, 1, 1, 1),
				List.of(), 0.0, 1.0);
	}

	private static WeaponDropDefinition resonanceOnlyDrops() {
		return new WeaponDropDefinition(
				new WeaponFragmentDropDefinition(999, 0.0, 0, 0, 0),
				List.of(), 0.0, 1.0);
	}

	private static void require(boolean condition, String message) {
		if (!condition) {
			throw new IllegalArgumentException(message);
		}
	}
}
