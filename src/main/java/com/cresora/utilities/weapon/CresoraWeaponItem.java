package com.cresora.utilities.weapon;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A weapon item. Java port of the core of {@code CresoraWeaponItem.kt}: it stamps default weapon
 * data onto a fresh stack and fires the bound skill on use.
 *
 * <p>The rich tooltip and the sneak-to-open-upgrade screen from the original depend on the
 * equipment/tooltip subsystems and are not part of this weapon-only slice.
 */
public class CresoraWeaponItem extends Item {
	private final WeaponDefinitionRef definition;

	public CresoraWeaponItem(WeaponDefinitionRef definition, Item.Properties properties) {
		super(properties);
		this.definition = definition;
	}

	@Override
	public ItemStack getDefaultInstance() {
		ItemStack stack = super.getDefaultInstance();
		WeaponStackSupport.syncWeaponData(stack, WeaponStackSupport.defaultWeaponData(definition.resolve()));
		return stack;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.FAIL;
		}
		// Right-clicking the weapon fires its primary skill (skill slot 0).
		return WeaponSkillService.tryActivate(serverPlayer, player.getItemInHand(hand), 0);
	}
}
