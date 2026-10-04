package com.cresora.utilities.weapon;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import com.cresora.utilities.CresoraUtilities;

/**
 * Applies the held weapon's attack damage/speed as entity attribute modifiers each server tick.
 * Java port of the core of {@code WeaponAttributeService.kt}.
 *
 * <p>The original also applied dynamic skill scalars ({@code CombatStatSupport}); those are not
 * ported in this weapon-only slice, so only the static weapon modifiers are managed.
 */
public final class WeaponAttributeService {
	private static final Identifier ATTACK_DAMAGE_ID =
			Identifier.fromNamespaceAndPath(CresoraUtilities.MOD_ID, "weapon_attack_damage");
	private static final Identifier ATTACK_SPEED_ID =
			Identifier.fromNamespaceAndPath(CresoraUtilities.MOD_ID, "weapon_attack_speed");

	private WeaponAttributeService() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				WeaponInventory inventory = WeaponInventoryService.get(player);
				// Normal (left-click) attacks always use the main weapon's stats; the selected slot
				// only decides which weapon's skill fires, so switching weapons never changes them.
				ItemStack stack = inventory.equipped(WeaponZone.MAIN_WEAPON.slot());
				WeaponDefinition definition = WeaponStackSupport.getDefinition(stack);
				WeaponData data = definition == null ? null : WeaponStackSupport.getWeaponData(stack);
				boolean armed = definition != null && data != null;

				updateModifier(player.getAttribute(Attributes.ATTACK_DAMAGE), ATTACK_DAMAGE_ID,
						armed ? WeaponCombatSupport.attackDamageModifier(definition, data) : 0.0,
						AttributeModifier.Operation.ADD_VALUE);
				updateModifier(player.getAttribute(Attributes.ATTACK_SPEED), ATTACK_SPEED_ID,
						armed ? WeaponCombatSupport.attackSpeedModifier(definition) : 0.0,
						AttributeModifier.Operation.ADD_VALUE);
			}
		});
	}

	private static void updateModifier(AttributeInstance instance, Identifier id, double value, AttributeModifier.Operation operation) {
		if (instance == null) {
			return;
		}
		if (value == 0.0) {
			instance.removeModifier(id);
			return;
		}
		AttributeModifier existing = instance.getModifier(id);
		if (existing != null && existing.amount() == value && existing.operation() == operation) {
			return;
		}
		instance.removeModifier(id);
		instance.addOrUpdateTransientModifier(new AttributeModifier(id, value, operation));
	}
}
