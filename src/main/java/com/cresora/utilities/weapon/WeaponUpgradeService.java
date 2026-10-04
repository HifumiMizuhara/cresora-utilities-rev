package com.cresora.utilities.weapon;

/**
 * Cost and cap arithmetic for weapon upgrades. Java port of {@code WeaponUpgradeService.kt},
 * with the breakthrough tuning read from {@link WeaponBalance} instead of the combat balance
 * registry.
 */
public final class WeaponUpgradeService {
	private WeaponUpgradeService() {
	}

	public static double breakthroughAttackFactor() {
		return WeaponBalance.BREAKTHROUGH_ATTACK_FACTOR;
	}

	public static double breakthroughCritRateBonus(int breakthrough) {
		return Math.clamp(breakthrough, 0, 2) * WeaponBalance.BREAKTHROUGH_CRIT_RATE_BONUS;
	}

	public static double breakthroughAllDamageBonus(int breakthrough) {
		return Math.clamp(breakthrough, 0, 2) >= 2 ? WeaponBalance.BREAKTHROUGH_ALL_DAMAGE_BONUS : 0.0;
	}

	public static int baseUpgradeCost(WeaponDefinition definition, int currentLevel) {
		int level = Math.max(currentLevel, 1);
		if (level <= 20) {
			return 250 + level * 180;
		}
		if (level <= 40) {
			return 4_000 + (level - 20) * 650;
		}
		return 17_000 + (level - 40) * 1_400;
	}

	public static int skillUpgradeCost(WeaponDefinition definition, int currentLevel) {
		int level = Math.max(currentLevel, 1);
		int configured = definition.upgrades().skillCscLinearCoefficient() * level
				+ definition.upgrades().skillCscQuadraticCoefficient() * level * level;
		return Math.max((int) (configured * 0.6), 750);
	}

	public static int skillArtifactCost(WeaponDefinition definition, int currentLevel) {
		int level = Math.max(currentLevel, 1);
		return definition.upgrades().skillArtifactCountPerLevel() * level;
	}

	public static int levelCap(WeaponDefinition definition, int breakthrough) {
		int bt = Math.clamp(breakthrough, 0, 2);
		return switch (bt) {
			case 0 -> definition.maxBaseLevel() / 2;
			case 1 -> (definition.maxBaseLevel() * 3) / 4;
			default -> definition.maxBaseLevel();
		};
	}

	public static int maxSkillLevelForBreakthrough(int breakthrough) {
		int bt = Math.clamp(breakthrough, 0, 2);
		return switch (bt) {
			case 0 -> 3;
			case 1 -> 7;
			default -> 10;
		};
	}

	public static int breakthroughCscCost(WeaponRarity rarity, int targetBreakthrough) {
		if (targetBreakthrough == 1) {
			return switch (rarity) {
				case TWO_STAR -> 4_000;
				case THREE_STAR -> 8_000;
				case FOUR_STAR -> 20_000;
				case FIVE_STAR -> 40_000;
			};
		}
		if (targetBreakthrough == 2) {
			return switch (rarity) {
				case TWO_STAR -> 16_000;
				case THREE_STAR -> 32_000;
				case FOUR_STAR -> 80_000;
				case FIVE_STAR -> 160_000;
			};
		}
		return 0;
	}

	public static int breakthroughFragmentCost(WeaponRarity rarity, int targetBreakthrough) {
		if (targetBreakthrough == 1) {
			return switch (rarity) {
				case TWO_STAR -> 4;
				case THREE_STAR -> 6;
				case FOUR_STAR -> 8;
				case FIVE_STAR -> 10;
			};
		}
		if (targetBreakthrough == 2) {
			return switch (rarity) {
				case TWO_STAR -> 8;
				case THREE_STAR -> 12;
				case FOUR_STAR -> 16;
				case FIVE_STAR -> 20;
			};
		}
		return 0;
	}

	public static int breakthroughRoleMaterialCost(WeaponRarity rarity, int targetBreakthrough) {
		if (targetBreakthrough == 1 || targetBreakthrough == 2) {
			return switch (rarity) {
				case TWO_STAR -> 1;
				case THREE_STAR -> 2;
				case FOUR_STAR -> 4;
				case FIVE_STAR -> 8;
			};
		}
		return 0;
	}
}
