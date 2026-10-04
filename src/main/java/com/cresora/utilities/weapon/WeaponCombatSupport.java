package com.cresora.utilities.weapon;

import java.util.List;

/**
 * Combat math for weapons. Java port of the subset of {@code WeaponCombatSupport.kt} that the
 * lakeside_stride vertical slice needs; balance tuning comes from {@link WeaponBalance}.
 */
public final class WeaponCombatSupport {
	private static final double PLAYER_BASE_ATTACK_DAMAGE = 1.0;
	private static final double PLAYER_BASE_ATTACK_SPEED = 4.0;

	private WeaponCombatSupport() {
	}

	public static double attackDamage(WeaponDefinition definition, WeaponData data) {
		List<WeaponAttackCurvePoint> curve = definition.attackCurve();
		double raw;
		if (curve.isEmpty()) {
			raw = definition.baseAttackDamage() * WeaponBalance.BASE_DAMAGE_SCALAR
					+ Math.max(data.baseLevel() - 1, 0) * definition.attackDamagePerLevel() * WeaponBalance.LEVEL_GROWTH_SCALAR;
		} else {
			int level = Math.clamp(data.baseLevel(), 1, definition.maxBaseLevel());
			if (level <= curve.get(0).level()) {
				raw = curve.get(0).attackDamage();
			} else {
				double calculated = curve.get(curve.size() - 1).attackDamage();
				for (int index = 1; index < curve.size(); index++) {
					WeaponAttackCurvePoint previous = curve.get(index - 1);
					WeaponAttackCurvePoint next = curve.get(index);
					if (level > next.level()) {
						continue;
					}
					int span = Math.max(next.level() - previous.level(), 1);
					double progress = (double) (level - previous.level()) / span;
					calculated = previous.attackDamage() + (next.attackDamage() - previous.attackDamage()) * progress;
					break;
				}
				raw = calculated;
			}
		}
		return raw * (1.0 + WeaponUpgradeService.breakthroughAttackFactor() * data.breakthrough());
	}

	public static double attackDamageModifier(WeaponDefinition definition, WeaponData data) {
		return attackDamage(definition, data) - PLAYER_BASE_ATTACK_DAMAGE;
	}

	public static double attackSpeedModifier(WeaponDefinition definition) {
		return definition.totalAttackSpeed() - PLAYER_BASE_ATTACK_SPEED;
	}

	public static double allDamageBonusPercent(WeaponDefinition definition, WeaponData data) {
		double maxBonus = Math.max(definition.maxAllDamageBonusPercent(), 0.0);
		double normalBonus;
		if (maxBonus <= 0.0 || definition.maxBaseLevel() <= 1) {
			normalBonus = 0.0;
		} else {
			double progress = (double) (Math.clamp(data.baseLevel(), 1, definition.maxBaseLevel()) - 1)
					/ (definition.maxBaseLevel() - 1);
			normalBonus = maxBonus * Math.clamp(progress, 0.0, 1.0);
		}
		double btBonus = WeaponUpgradeService.breakthroughAllDamageBonus(data.breakthrough());
		return normalBonus + btBonus;
	}

	public static double skillValueHearts(WeaponDefinition definition, WeaponData data) {
		return skillValueHearts(definition.skill(), data);
	}

	public static double secondarySkillValueHearts(WeaponDefinition definition, WeaponData data) {
		return secondarySkillValueHearts(definition.skill(), data);
	}

	public static double skillValuePercent(WeaponDefinition definition, WeaponData data) {
		return skillValuePercent(definition.skill(), data);
	}

	public static double secondarySkillValuePercent(WeaponDefinition definition, WeaponData data) {
		return secondarySkillValuePercent(definition.skill(), data);
	}

	public static double currentHpTrueDamageRatio(WeaponDefinition definition, WeaponData data) {
		return currentHpTrueDamageRatio(definition.skill(), data);
	}

	public static double skillValueHearts(WeaponSkillDefinition skill, WeaponData data) {
		return skillValuePercent(skill, data);
	}

	public static double secondarySkillValueHearts(WeaponSkillDefinition skill, WeaponData data) {
		return secondarySkillValuePercent(skill, data);
	}

	public static double skillValuePercent(WeaponSkillDefinition skill, WeaponData data) {
		return skill.baseValue() + skill.valuePerLevel() * data.skillLevel();
	}

	public static double secondarySkillValuePercent(WeaponSkillDefinition skill, WeaponData data) {
		return skill.secondaryBaseValue() + skill.secondaryValuePerLevel() * data.skillLevel();
	}

	public static double currentHpTrueDamageRatio(WeaponSkillDefinition skill, WeaponData data) {
		return Math.max(skillValuePercent(skill, data) / 100.0, 0.0);
	}
}
