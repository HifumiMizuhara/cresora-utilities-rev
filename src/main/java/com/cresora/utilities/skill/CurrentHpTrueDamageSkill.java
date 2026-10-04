package com.cresora.utilities.skill;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.Monster;

import com.cresora.utilities.weapon.WeaponCombatSupport;
import com.cresora.utilities.weapon.WeaponData;
import com.cresora.utilities.weapon.WeaponDefinition;
import com.cresora.utilities.weapon.WeaponSkillAccess;
import com.cresora.utilities.weapon.WeaponSkillService;

/**
 * Hand-written Java port of the {@code lakeside_stride} skill ({@code current_hp_true_damage}).
 *
 * <p>Mirrors the DSL {@code on_activate} block: deal a percentage of each nearby hostile mob's
 * current health as true damage in a radius. The original called {@code StoryService.allowsTrueDamage}
 * (always allow here) and {@code AdventureRankService} for display (dropped, per scope).
 */
public final class CurrentHpTrueDamageSkill implements WeaponSkillHandler {
	@Override
	public InteractionResult activate(ServerPlayer player, WeaponDefinition definition, WeaponData data, WeaponSkillAccess access) {
		WeaponSkillService.startCooldown(player, definition.id(), definition.skill().cooldownSeconds() * 20L);
		WeaponSkillService.showCooldownBar(player, definition);

		double radius = Math.max(definition.skill().radiusMeters(), 0.0);
		double ratio = WeaponCombatSupport.currentHpTrueDamageRatio(definition, data);
		ServerLevel world = player.level();
		List<Monster> targets = world.getEntitiesOfClass(Monster.class,
				player.getBoundingBox().inflate(radius), Monster::isAlive);

		int hitCount = 0;
		double totalDamage = 0.0;
		for (Monster target : targets) {
			double damage = Math.max(target.getHealth() * ratio, 0.0);
			if (damage <= 0.0) {
				continue;
			}
			target.setHealth((float) Math.max(target.getHealth() - damage, 0.001));
			hitCount++;
			totalDamage += damage;
		}

		player.sendSystemMessage(Component.translatable(
				"item.cresora.weapon.skill.current_hp_true_damage_activated",
				Component.translatable(definition.translationKey()),
				hitCount,
				WeaponSkillService.formatNumber(totalDamage / 2.0)
		).withStyle(ChatFormatting.AQUA), true);
		return InteractionResult.SUCCESS;
	}
}
