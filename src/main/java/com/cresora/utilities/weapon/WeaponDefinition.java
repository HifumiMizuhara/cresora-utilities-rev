package com.cresora.utilities.weapon;

import java.util.List;

import com.cresora.utilities.combat.CombatDamageType;

/**
 * Fully resolved definition of a weapon. Java port of {@code WeaponDefinition} from
 * {@code WeaponContentRegistry.kt}. A {@link Builder} is provided because many fields have
 * defaults and the built-in content table constructs instances sparsely.
 */
public record WeaponDefinition(
		String id,
		String baseItemId,
		double baseAttackDamage,
		double attackDamagePerLevel,
		double totalAttackSpeed,
		int maxBaseLevel,
		int maxSkillLevel,
		double critRateBonusPercent,
		double maxAllDamageBonusPercent,
		double hpBonusPercent,
		CombatDamageType damageType,
		WeaponZone zone,
		List<WeaponAttackCurvePoint> attackCurve,
		List<WeaponSkillDefinition> skills,
		WeaponUpgradeDefinition upgrades,
		WeaponCraftDefinition craft,
		WeaponDropDefinition drops,
		Integer customModelData,
		WeaponSpiritDefinition spirit
) {
	public WeaponDefinition {
		skills = (skills == null || skills.isEmpty()) ? List.of(WeaponSkillDefinition.NONE) : List.copyOf(skills);
	}

	/** The weapon's primary skill (slot 1). Never {@code null}. */
	public WeaponSkillDefinition skill() {
		return skills.get(0);
	}

	/** The skill in the given slot, or {@code null} when the weapon has no such slot. */
	public WeaponSkillDefinition skill(int slot) {
		return slot >= 0 && slot < skills.size() ? skills.get(slot) : null;
	}

	/**
	 * Resolves the skill a skill key should fire. A weapon with a single skill always returns it,
	 * so any skill key works; otherwise the skill in the exact slot is returned (or {@code null}).
	 */
	public WeaponSkillDefinition skillForSlot(int slot) {
		if (skills.size() == 1) {
			return skills.get(0);
		}
		return skill(slot);
	}

	public int skillCount() {
		return skills.size();
	}

	public String translationKey() {
		return "item.cresora-utilities." + id;
	}

	public String fragmentTranslationKey() {
		return "item.cresora-utilities." + craft.fragmentItemId();
	}

	public static final WeaponDefinition DUMMY = new WeaponDefinition(
			"dummy", "minecraft:air", 0.0, 0.0, 0.0, 1, 1,
			0.0, 0.0, 0.0, CombatDamageType.PHYSICAL, WeaponZone.MAIN_WEAPON, List.of(),
			List.of(WeaponSkillDefinition.of("none", 0, 0, 0.0, 0.0)),
			WeaponUpgradeDefinition.of(0, 0, 0),
			new WeaponCraftDefinition("dummy", "air", 0, WeaponRarity.TWO_STAR, 1, 1),
			new WeaponDropDefinition(new WeaponFragmentDropDefinition(0, 0.0, 0, 0, 0), List.of(), 0.0, 0.0),
			null, null);

	public static Builder builder(String id, String baseItemId,
			double baseAttackDamage, double attackDamagePerLevel, double totalAttackSpeed,
			int maxBaseLevel, int maxSkillLevel,
			WeaponSkillDefinition skill, WeaponUpgradeDefinition upgrades,
			WeaponCraftDefinition craft, WeaponDropDefinition drops) {
		return new Builder(id, baseItemId, baseAttackDamage, attackDamagePerLevel, totalAttackSpeed,
				maxBaseLevel, maxSkillLevel, skill, upgrades, craft, drops);
	}

	public static final class Builder {
		private final String id;
		private final String baseItemId;
		private final double baseAttackDamage;
		private final double attackDamagePerLevel;
		private final double totalAttackSpeed;
		private final int maxBaseLevel;
		private final int maxSkillLevel;
		private List<WeaponSkillDefinition> skills;
		private final WeaponUpgradeDefinition upgrades;
		private final WeaponCraftDefinition craft;
		private final WeaponDropDefinition drops;
		private double critRateBonusPercent = 0.0;
		private double maxAllDamageBonusPercent = 0.0;
		private double hpBonusPercent = 0.0;
		private CombatDamageType damageType = CombatDamageType.PHYSICAL;
		private WeaponZone zone = WeaponZone.MAIN_WEAPON;
		private List<WeaponAttackCurvePoint> attackCurve = List.of();
		private Integer customModelData = null;
		private WeaponSpiritDefinition spirit = null;

		private Builder(String id, String baseItemId,
				double baseAttackDamage, double attackDamagePerLevel, double totalAttackSpeed,
				int maxBaseLevel, int maxSkillLevel,
				WeaponSkillDefinition skill, WeaponUpgradeDefinition upgrades,
				WeaponCraftDefinition craft, WeaponDropDefinition drops) {
			this.id = id;
			this.baseItemId = baseItemId;
			this.baseAttackDamage = baseAttackDamage;
			this.attackDamagePerLevel = attackDamagePerLevel;
			this.totalAttackSpeed = totalAttackSpeed;
			this.maxBaseLevel = maxBaseLevel;
			this.maxSkillLevel = maxSkillLevel;
			this.skills = (skill == null) ? List.of(WeaponSkillDefinition.NONE) : List.of(skill);
			this.upgrades = upgrades;
			this.craft = craft;
			this.drops = drops;
		}

		public Builder critRateBonusPercent(double value) {
			this.critRateBonusPercent = value;
			return this;
		}

		public Builder maxAllDamageBonusPercent(double value) {
			this.maxAllDamageBonusPercent = value;
			return this;
		}

		public Builder hpBonusPercent(double value) {
			this.hpBonusPercent = value;
			return this;
		}

		public Builder damageType(CombatDamageType value) {
			this.damageType = value;
			return this;
		}

		public Builder zone(WeaponZone value) {
			this.zone = value;
			return this;
		}

		public Builder attackCurve(List<WeaponAttackCurvePoint> value) {
			this.attackCurve = value;
			return this;
		}

		/** Replaces the skill slots (by default the single skill passed to the factory). */
		public Builder skills(List<WeaponSkillDefinition> value) {
			this.skills = value;
			return this;
		}

		public Builder customModelData(Integer value) {
			this.customModelData = value;
			return this;
		}

		public Builder spirit(WeaponSpiritDefinition value) {
			this.spirit = value;
			return this;
		}

		public WeaponDefinition build() {
			return new WeaponDefinition(id, baseItemId, baseAttackDamage, attackDamagePerLevel, totalAttackSpeed,
					maxBaseLevel, maxSkillLevel, critRateBonusPercent, maxAllDamageBonusPercent, hpBonusPercent,
					damageType, zone, attackCurve, skills, upgrades, craft, drops, customModelData, spirit);
		}
	}
}
