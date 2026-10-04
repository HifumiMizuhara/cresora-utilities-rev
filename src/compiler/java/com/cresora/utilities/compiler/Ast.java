package com.cresora.utilities.compiler;

import java.util.List;
import java.util.Map;

/**
 * Abstract syntax tree for the weapon subset of the Cresora DSL.
 *
 * <p>Java port of the original Kotlin {@code AST.kt}. Artifact and movement nodes are
 * intentionally omitted. Nested records mirror the source data classes and are referenced
 * from {@link Parser} as {@code Ast.WeaponDef} and so on.
 */
public sealed interface Ast {

	record WeaponDef(
			String name,
			String id,
			String rarity,
			String baseItem,
			String damageType,
			String role,
			Stats stats,
			Skill skill,
			Spirit spirit,
			Map<String, Map<String, String>> translations,
			List<SubSkill> subSkills,
			Integer customModelData,
			String texture
	) implements Ast {
	}

	record Spirit(
			String nameKey,
			Map<String, String> voiceLines,
			List<BondStage> bondStages,
			String awakeningConditionKey
	) implements Ast {
	}

	record BondStage(int stage, String titleKey, String storyKey) implements Ast {
	}

	record DictionaryDef(String id, Map<String, Map<String, String>> translations) implements Ast {
	}

	record SubSkill(String name, String effectId, String icon, List<SkillHandler> handlers) implements Ast {
	}

	record Stats(
			double baseAttackDamage,
			double attackDamagePerLevel,
			double totalAttackSpeed,
			int maxBaseLevel,
			int maxSkillLevel,
			double critRateBonusPercent,
			double maxAllDamageBonusPercent,
			double hpBonusPercent
	) implements Ast {
	}

	record Skill(
			String name,
			String effectId,
			double cooldownSeconds,
			double durationSeconds,
			double baseValue,
			double valuePerLevel,
			double radiusMeters,
			List<SkillHandler> handlers,
			List<Buff> buffs,
			String note
	) implements Ast {
	}

	record Buff(
			String id,
			String translationKey,
			int maxStacks,
			double durationSeconds,
			Map<String, Double> stats,
			String decay
	) implements Ast {
	}

	record SkillHandler(String eventName, List<Action> actions) implements Ast {
	}

	/**
	 * Duration argument of a typed action. A bare number in the DSL means ticks, an {@code Ns}
	 * suffix means seconds, and {@code skill_duration} resolves to the skill's configured
	 * duration at runtime.
	 */
	sealed interface Duration {
		/** Renders this duration as a Java expression producing a tick count. */
		String toTicksExpression();

		record Seconds(double value) implements Duration {
			@Override
			public String toTicksExpression() {
				return (long) (value * 20) + "L";
			}
		}

		record Ticks(long value) implements Duration {
			@Override
			public String toTicksExpression() {
				return value + "L";
			}
		}

		record SkillDuration() implements Duration {
			@Override
			public String toTicksExpression() {
				return "(long) (definition.skill().durationSeconds() * 20)";
			}
		}
	}

	sealed interface Action extends Ast {
	}

	record AreaOfEffect(double radius, List<Action> actions) implements Action {
	}

	record Command(String commandName, List<String> arguments) implements Action {
	}

	record ApplyMark(String target, String markId, Duration duration) implements Action {
	}

	record GrantInvulnerability(String target, Duration duration) implements Action {
	}

	record AddBuff(String buffId, int stacks) implements Action {
	}

	record StartCooldown(Duration duration) implements Action {
	}

	record SendMessage(String key, String color) implements Action {
	}

	record ApplyStatusEffect(String effectId, Duration duration, int amplifier) implements Action {
	}

	record InstructionCall(String functionName, List<String> arguments) implements Action {
	}

	record SendLocalizedMessage(String key, String color, List<String> arguments) implements Action {
	}

	record Expression(String content) implements Action {
	}

	record OpenSkillMenu(List<String> subSkillEffectIds, double durationSeconds) implements Action {
	}

	record CloseSkillMenu() implements Action {
	}

	record Execute(List<Action> statements) implements Action {
	}
}
