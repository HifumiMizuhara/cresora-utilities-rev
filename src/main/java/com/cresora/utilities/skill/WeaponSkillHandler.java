package com.cresora.utilities.skill;

import java.util.Set;
import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;

import com.cresora.utilities.weapon.WeaponData;
import com.cresora.utilities.weapon.WeaponDefinition;
import com.cresora.utilities.weapon.WeaponSkillAccess;

/**
 * Contract implemented by every weapon skill (hand-written or DSL-generated). Java port of
 * {@code WeaponSkillHandler.kt}; all methods are defaulted so a handler only overrides the
 * events it cares about. {@code PlayerEntity} maps to {@code ServerPlayer} and {@code ActionResult}
 * to {@code InteractionResult} in 26.2's official names.
 */
public interface WeaponSkillHandler {
	default InteractionResult activate(ServerPlayer player, WeaponDefinition definition, WeaponData data, WeaponSkillAccess access) {
		return InteractionResult.PASS;
	}

	default void onTick(MinecraftServer server, WeaponDefinition definition, WeaponData data) {
	}

	default void onPlayerTick(ServerPlayer player, WeaponDefinition definition, WeaponData data) {
	}

	default float onDamageAbsorbed(ServerPlayer player, float amount, WeaponDefinition definition, WeaponData data) {
		return amount;
	}

	default void onDamageDealt(ServerPlayer player, LivingEntity target, float amount, boolean isTrueDamage, WeaponDefinition definition, WeaponData data) {
	}

	default float onDamageTaken(ServerPlayer player, float amount, WeaponDefinition definition, WeaponData data) {
		return amount;
	}

	default double getAttackDamageScalar(ServerPlayer player) {
		return 0.0;
	}

	default double getArmorScalar(ServerPlayer player) {
		return 0.0;
	}

	default double getCritRateBonus(ServerPlayer player) {
		return 0.0;
	}

	default double getCritDamageBonus(ServerPlayer player) {
		return 0.0;
	}

	default double getHealthBonusPercent(ServerPlayer player) {
		return 0.0;
	}

	default double getAllDamageBonus(ServerPlayer player) {
		return 0.0;
	}

	default int getRegenStageBonus(ServerPlayer player) {
		return 0;
	}

	default void clearTransientState(UUID playerId) {
	}

	default void pruneTransientState(Set<UUID> activePlayerIds) {
	}
}
