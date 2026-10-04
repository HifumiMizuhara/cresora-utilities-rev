package com.cresora.utilities.weapon;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

import com.cresora.utilities.skill.WeaponSkillHandler;
import com.cresora.utilities.skill.WeaponSkillRegistry;

/**
 * Dispatches weapon skills on use and tracks their cooldowns. Java port of the
 * {@code WeaponSkillService} surface the lakeside_stride slice needs.
 *
 * <p>Transient state (cooldowns) is held in a server-side map keyed by player UUID rather than
 * on the player entity, so no player mixin is required. Team-wide features of the original
 * (shields, marks, boss-bar indicators, debuff triggers) are out of scope here.
 */
public final class WeaponSkillService {
	private static final Map<UUID, Map<String, Double>> COOLDOWNS = new HashMap<>();

	private WeaponSkillService() {
	}

	/** Registers the server tick that counts cooldowns down. */
	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				tickCooldowns(player);
			}
		});
	}

	public static InteractionResult tryActivate(ServerPlayer player, ItemStack stack) {
		WeaponDefinition definition = WeaponStackSupport.getDefinition(stack);
		if (definition == null) {
			return InteractionResult.PASS;
		}
		WeaponData data = WeaponStackSupport.ensureWeaponData(stack);
		if (isCoolingDown(player, definition.id())) {
			player.sendSystemMessage(Component.translatable("item.cresora.weapon.skill.cooldown")
					.withStyle(ChatFormatting.RED), true);
			return InteractionResult.FAIL;
		}
		if (definition.skill().effectId().equals("none")) {
			player.sendSystemMessage(Component.translatable("item.cresora.weapon.skill.none")
					.withStyle(ChatFormatting.GRAY), true);
			return InteractionResult.SUCCESS;
		}
		WeaponSkillHandler handler = WeaponSkillRegistry.getHandler(definition.skill().effectId());
		return handler != null
				? handler.activate(player, definition, data, WeaponSkillAccess.DUMMY)
				: InteractionResult.PASS;
	}

	public static void startCooldown(ServerPlayer player, String weaponId, long durationTicks) {
		COOLDOWNS.computeIfAbsent(player.getUUID(), key -> new HashMap<>()).put(weaponId, (double) durationTicks);
	}

	/**
	 * The original showed a boss-bar cooldown indicator here. This slice surfaces cooldowns through
	 * the action-bar messages instead, so no boss bar is created.
	 */
	public static void showCooldownBar(ServerPlayer player, WeaponDefinition definition) {
	}

	public static boolean isCoolingDown(ServerPlayer player, String weaponId) {
		return getRemainingCooldownTicks(player, weaponId) > 0.0;
	}

	public static double getRemainingCooldownTicks(ServerPlayer player, String weaponId) {
		Map<String, Double> cooldowns = COOLDOWNS.get(player.getUUID());
		return cooldowns == null ? 0.0 : cooldowns.getOrDefault(weaponId, 0.0);
	}

	public static String formatNumber(double value) {
		double rounded = Math.round(value * 10.0) / 10.0;
		return rounded % 1.0 == 0.0 ? Integer.toString((int) rounded) : Double.toString(rounded);
	}

	private static void tickCooldowns(ServerPlayer player) {
		Map<String, Double> cooldowns = COOLDOWNS.get(player.getUUID());
		if (cooldowns == null) {
			return;
		}
		Iterator<Map.Entry<String, Double>> iterator = cooldowns.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Double> entry = iterator.next();
			double remaining = entry.getValue() - 1.0;
			if (remaining <= 0.0) {
				iterator.remove();
			} else {
				entry.setValue(remaining);
			}
		}
		if (cooldowns.isEmpty()) {
			COOLDOWNS.remove(player.getUUID());
		}
	}
}
