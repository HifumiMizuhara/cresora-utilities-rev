package com.cresora.utilities.client.weapon;

import com.cresora.utilities.combat.CombatDamageType;
import com.cresora.utilities.net.WeaponBagActionPayload;
import com.cresora.utilities.weapon.WeaponCombatSupport;
import com.cresora.utilities.weapon.WeaponData;
import com.cresora.utilities.weapon.WeaponDefinition;
import com.cresora.utilities.weapon.WeaponInventory;
import com.cresora.utilities.weapon.WeaponRarity;
import com.cresora.utilities.weapon.WeaponRole;
import com.cresora.utilities.weapon.WeaponStackSupport;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Modern RPG-style weapon bag & armory screen:
 * <ul>
 *   <li>Left panel: 4 hotbar equipment cards and a 9x3 weapon storage grid</li>
 *   <li>Right panel: live weapon inspection card showing full attributes, scaling, and active skill</li>
 *   <li>Interaction: Left-click to equip/unequip, Right-click to set active weapon, B/Esc to close</li>
 * </ul>
 */
public final class WeaponBagScreen extends Screen {
	private static final int PANEL_WIDTH = 360;
	private static final int PANEL_HEIGHT = 200;
	private static final int DECK_WIDTH = 206;
	private static final int DETAIL_WIDTH = 146;
	private static final int PANEL_GAP = 8;

	// Equipped slots
	private static final int EQUIP_COUNT = WeaponInventory.EQUIP_SIZE;
	private static final int EQUIP_SLOT_SIZE = 22;
	private static final int EQUIP_GAP = 14;
	private static final int EQUIP_SPAN = EQUIP_COUNT * EQUIP_SLOT_SIZE + (EQUIP_COUNT - 1) * EQUIP_GAP; // 130px

	// Bag grid slots
	private static final int BAG_COLUMNS = 9;
	private static final int BAG_ROWS = 3;
	private static final int BAG_SLOT_SIZE = 18;
	private static final int BAG_GAP = 2;
	private static final int BAG_SPAN = BAG_COLUMNS * BAG_SLOT_SIZE + (BAG_COLUMNS - 1) * BAG_GAP; // 178px

	private int originX;
	private int originY;
	private int deckX;
	private int detailX;
	private int equipStartX;
	private int equipY;
	private int bagStartX;
	private int bagStartY;

	public WeaponBagScreen() {
		super(Minecraft.getInstance(), Minecraft.getInstance().font,
				Component.translatable("screen.cresora-utilities.weapon_bag"));
	}

	@Override
	protected void init() {
		originX = Math.max(2, (width - PANEL_WIDTH) / 2);
		originY = Math.max(2, (height - PANEL_HEIGHT) / 2);
		deckX = originX;
		detailX = originX + DECK_WIDTH + PANEL_GAP;

		equipStartX = deckX + (DECK_WIDTH - EQUIP_SPAN) / 2;
		equipY = originY + 36;

		bagStartX = deckX + (DECK_WIDTH - BAG_SPAN) / 2;
		bagStartY = originY + 94;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (WeaponKeybinds.matchesOpenBag(event)) {
			onClose();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);

		// Dim background
		graphics.fill(0, 0, width, height, 0x75000000);

		WeaponInventory inventory = WeaponSlots.inventory();

		// Track which stack is hovered (for inspection panel)
		ItemStack hoveredStack = ItemStack.EMPTY;
		boolean hoveredIsEquipped = false;
		int hoveredSlotIndex = -1;

		int hoveredEquip = slotAt(mouseX, mouseY, equipStartX, equipY, EQUIP_COUNT, EQUIP_COUNT, EQUIP_SLOT_SIZE, EQUIP_GAP);
		if (hoveredEquip >= 0) {
			hoveredStack = inventory.equipped(hoveredEquip);
			hoveredIsEquipped = true;
			hoveredSlotIndex = hoveredEquip;
		} else {
			int hoveredBag = slotAt(mouseX, mouseY, bagStartX, bagStartY, WeaponInventory.BAG_SIZE, BAG_COLUMNS, BAG_SLOT_SIZE, BAG_GAP);
			if (hoveredBag >= 0) {
				hoveredStack = inventory.bag(hoveredBag);
				hoveredIsEquipped = false;
				hoveredSlotIndex = hoveredBag;
			}
		}

		// 1. Draw Left Panel (Armory Deck)
		drawArmoryDeck(graphics, inventory, mouseX, mouseY, hoveredEquip, hoveredSlotIndex);

		// 2. Draw Right Panel (Weapon Inspection Panel)
		drawInspectionPanel(graphics, inventory, hoveredStack, hoveredIsEquipped, hoveredSlotIndex);
	}

	private void drawArmoryDeck(GuiGraphicsExtractor graphics, WeaponInventory inventory,
			int mouseX, int mouseY, int hoveredEquip, int hoveredSlotIndex) {
		WeaponUiTheme.drawPanel(graphics, deckX, originY, DECK_WIDTH, PANEL_HEIGHT);

		// Header
		String titleText = "✦ " + Component.translatable("cresora.ui.armory_title").getString();
		graphics.text(font, titleText, deckX + 10, originY + 8, WeaponUiTheme.TEXT_TITLE, true);

		int equippedCount = (int) inventory.equip().stream().filter(s -> !s.isEmpty()).count();
		int bagCount = (int) inventory.bag().stream().filter(s -> !s.isEmpty()).count();
		String countText = Component.translatable("cresora.ui.equipped_count", equippedCount).getString()
				+ " · " + Component.translatable("cresora.ui.storage_count", bagCount).getString();
		int countW = font.width(countText);
		graphics.text(font, countText, deckX + DECK_WIDTH - countW - 10, originY + 9, WeaponUiTheme.TEXT_MUTED, false);

		// Equipped Section Header
		graphics.text(font, Component.translatable("cresora.ui.equipped_slots_header").getString(),
				deckX + 12, originY + 24, WeaponUiTheme.TEXT_HINT, false);

		// Equipped Slots (4 slots)
		for (int index = 0; index < EQUIP_COUNT; index++) {
			int slotX = equipStartX + index * (EQUIP_SLOT_SIZE + EQUIP_GAP);
			int slotY = equipY;
			boolean isSelected = (index == inventory.selected());
			boolean isHovered = (hoveredEquip == index);

			ItemStack stack = inventory.equipped(index);
			WeaponDefinition def = WeaponStackSupport.getDefinition(stack);
			WeaponData data = (def != null && !stack.isEmpty()) ? WeaponStackSupport.getWeaponData(stack) : null;
			WeaponRarity rarity = data != null ? data.rarity() : null;

			// Slot chassis
			WeaponUiTheme.drawSlotBackground(graphics, slotX, slotY, EQUIP_SLOT_SIZE, isSelected, isHovered, rarity);

			if (stack.isEmpty()) {
				WeaponUiTheme.drawEmptySlotWatermark(graphics, slotX, slotY, EQUIP_SLOT_SIZE);
				graphics.text(font, String.valueOf(index + 1), slotX + 2, slotY + 2, 0x608090A5, false);
			} else {
				int itemX = slotX + (EQUIP_SLOT_SIZE - 16) / 2;
				int itemY = slotY + (EQUIP_SLOT_SIZE - 16) / 2;
				graphics.item(stack, itemX, itemY);
				graphics.itemDecorations(font, stack, itemX, itemY);

				if (data != null) {
					String lvlStr = String.valueOf(data.baseLevel());
					int lvlW = font.width(lvlStr);
					graphics.text(font, lvlStr, slotX + EQUIP_SLOT_SIZE - lvlW - 1, slotY + EQUIP_SLOT_SIZE - 7, 0xE8FFFFFF, true);
				}
			}

			// Sub-label beneath slot
			int labelY = slotY + EQUIP_SLOT_SIZE + 2;
			if (isSelected) {
				String activeLabel = Component.translatable("cresora.ui.active").getString();
				int labelW = font.width(activeLabel);
				graphics.text(font, activeLabel, slotX + (EQUIP_SLOT_SIZE - labelW) / 2, labelY, WeaponUiTheme.SELECTION_GOLD, true);
			} else {
				String slotLabel = Component.translatable("cresora.ui.slot_n", index + 1).getString();
				int labelW = font.width(slotLabel);
				graphics.text(font, slotLabel, slotX + (EQUIP_SLOT_SIZE - labelW) / 2, labelY, WeaponUiTheme.TEXT_MUTED, false);
			}
		}

		// Horizontal Divider
		int divY = originY + 74;
		graphics.fill(deckX + 10, divY, deckX + DECK_WIDTH - 10, divY + 1, WeaponUiTheme.PANEL_DIVIDER);
		graphics.fill(deckX + DECK_WIDTH / 2 - 2, divY - 1, deckX + DECK_WIDTH / 2 + 2, divY + 2, WeaponUiTheme.ACCENT_CYAN);

		// Bag Grid Section Header
		graphics.text(font, Component.translatable("cresora.ui.storage_header").getString(),
				deckX + 12, originY + 82, WeaponUiTheme.TEXT_HINT, false);

		// Bag Grid (27 slots)
		for (int index = 0; index < WeaponInventory.BAG_SIZE; index++) {
			int col = index % BAG_COLUMNS;
			int row = index / BAG_COLUMNS;
			int slotX = bagStartX + col * (BAG_SLOT_SIZE + BAG_GAP);
			int slotY = bagStartY + row * (BAG_SLOT_SIZE + BAG_GAP);
			boolean isHovered = (hoveredSlotIndex == index && hoveredEquip < 0);

			ItemStack stack = inventory.bag(index);
			WeaponDefinition def = WeaponStackSupport.getDefinition(stack);
			WeaponData data = (def != null && !stack.isEmpty()) ? WeaponStackSupport.getWeaponData(stack) : null;
			WeaponRarity rarity = data != null ? data.rarity() : null;

			WeaponUiTheme.drawSlotBackground(graphics, slotX, slotY, BAG_SLOT_SIZE, false, isHovered, rarity);

			if (!stack.isEmpty()) {
				int itemX = slotX + (BAG_SLOT_SIZE - 16) / 2;
				int itemY = slotY + (BAG_SLOT_SIZE - 16) / 2;
				graphics.item(stack, itemX, itemY);
				graphics.itemDecorations(font, stack, itemX, itemY);
			}
		}

		// Footer Operation Hints
		int footerY = originY + 162;
		graphics.fill(deckX + 8, footerY, deckX + DECK_WIDTH - 8, footerY + 1, WeaponUiTheme.PANEL_DIVIDER);

		graphics.text(font, Component.translatable("cresora.ui.hint_left_click").getString(),
				deckX + 10, footerY + 4, WeaponUiTheme.TEXT_HINT, false);

		graphics.text(font, Component.translatable("cresora.ui.hint_right_click").getString(),
				deckX + 10, footerY + 15, WeaponUiTheme.ACCENT_GOLD, false);

		String closeHint = Component.translatable("cresora.ui.hint_close").getString();
		int closeW = font.width(closeHint);
		graphics.text(font, closeHint, deckX + DECK_WIDTH - closeW - 10, footerY + 15, WeaponUiTheme.TEXT_MUTED, false);
	}

	private void drawInspectionPanel(GuiGraphicsExtractor graphics, WeaponInventory inventory,
			ItemStack hoveredStack, boolean hoveredIsEquipped, int hoveredIndex) {
		WeaponUiTheme.drawPanel(graphics, detailX, originY, DETAIL_WIDTH, PANEL_HEIGHT);

		// Determine target weapon to inspect: hovered item, or fallback to currently selected equipped weapon
		ItemStack targetStack = hoveredStack;
		boolean isEquipped = hoveredIsEquipped;
		int slotIndex = hoveredIndex;

		if (targetStack.isEmpty()) {
			targetStack = inventory.equipped(inventory.selected());
			isEquipped = true;
			slotIndex = inventory.selected();
		}

		// Standby empty state if still empty
		if (targetStack.isEmpty()) {
			int centerIconX = detailX + (DETAIL_WIDTH - 16) / 2;
			int centerIconY = originY + 45;
			WeaponUiTheme.drawEmptySlotWatermark(graphics, centerIconX - 3, centerIconY - 3, 22);

			graphics.centeredText(font, Component.translatable("cresora.ui.no_weapon").getString(),
					detailX + DETAIL_WIDTH / 2, originY + 75, WeaponUiTheme.TEXT_MUTED);

			String hint = Component.translatable("cresora.ui.no_weapon_hint").getString();
			graphics.textWithWordWrap(font, Component.literal(hint), detailX + 14, originY + 95,
					DETAIL_WIDTH - 28, WeaponUiTheme.TEXT_MUTED);
			return;
		}

		WeaponDefinition def = WeaponStackSupport.getDefinition(targetStack);
		WeaponData data = (def != null) ? WeaponStackSupport.getWeaponData(targetStack) : null;

		if (def == null || data == null) {
			// Non-weapon item preview
			graphics.text(font, targetStack.getHoverName().getString(), detailX + 10, originY + 12, WeaponUiTheme.TEXT_TITLE, true);
			graphics.centeredText(font, "Item", detailX + DETAIL_WIDTH / 2, originY + 30, WeaponUiTheme.TEXT_MUTED);
			return;
		}

		WeaponRarity rarity = data.rarity();
		int rColor = WeaponUiTheme.rarityColor(rarity);

		// 1. Rarity Header Banner
		int bannerY = originY + 6;
		graphics.fillGradient(detailX + 4, bannerY, detailX + DETAIL_WIDTH - 4, bannerY + 18,
				WeaponUiTheme.rarityBgTint(rarity) | 0x60000000, 0x10000000);
		graphics.outline(detailX + 4, bannerY, DETAIL_WIDTH - 8, 18, WeaponUiTheme.SLOT_BORDER);
		String rarityText = WeaponUiTheme.rarityStars(rarity) + " " + WeaponUiTheme.rarityName(rarity);
		graphics.text(font, rarityText, detailX + 8, bannerY + 5, rColor, true);

		// 2. Weapon Name
		String weaponName = targetStack.getHoverName().getString();
		graphics.text(font, weaponName, detailX + 8, originY + 28, rColor, true);

		// 3. Role & Damage Type Pills
		int badgeY = originY + 40;
		WeaponRole role = def.role();
		CombatDamageType dmgType = def.damageType();
		String roleName = Component.translatable(role.translationKey()).getString();
		String dmgTypeName = Component.translatable(dmgType.translationKey()).getString();
		WeaponUiTheme.drawBadge(graphics, font, roleName, detailX + 8, badgeY,
				WeaponUiTheme.roleColor(role), 0x30182436, WeaponUiTheme.roleColor(role));

		int roleBadgeW = font.width(roleName) + 10;
		WeaponUiTheme.drawBadge(graphics, font, dmgTypeName, detailX + 8 + roleBadgeW, badgeY,
				WeaponUiTheme.damageTypeColor(dmgType), 0x30182436, WeaponUiTheme.damageTypeColor(dmgType));

		// 4. Level & Breakthrough
		int levelY = originY + 54;
		String lvlText = Component.translatable("cresora.ui.level", data.baseLevel(), def.maxBaseLevel()).getString();
		graphics.text(font, lvlText, detailX + 8, levelY, WeaponUiTheme.TEXT_TITLE, false);

		// Progress bar
		double levelRatio = (double) data.baseLevel() / Math.max(1, def.maxBaseLevel());
		WeaponUiTheme.drawProgressBar(graphics, detailX + 8, levelY + 11, DETAIL_WIDTH - 16, 3, levelRatio, rColor, 0x601A2434);

		String btVal = data.breakthrough() > 0 ? "★".repeat(data.breakthrough()) : Component.translatable("cresora.ui.breakthrough_none").getString();
		String btText = Component.translatable("cresora.ui.breakthrough", btVal).getString();
		graphics.text(font, btText, detailX + 8, levelY + 17, WeaponUiTheme.TEXT_MUTED, false);

		// 5. Attributes Section
		int attrY = originY + 84;
		graphics.text(font, Component.translatable("cresora.ui.attributes").getString(), detailX + 8, attrY, WeaponUiTheme.TEXT_MUTED, false);
		graphics.fill(detailX + 8, attrY + 9, detailX + DETAIL_WIDTH - 8, attrY + 10, WeaponUiTheme.PANEL_DIVIDER);

		double atk = WeaponCombatSupport.attackDamage(def, data);
		double spd = def.totalAttackSpeed();
		graphics.text(font, Component.translatable("cresora.ui.atk", atk).getString(),
				detailX + 8, attrY + 13, WeaponUiTheme.TEXT_TITLE, false);
		graphics.text(font, Component.translatable("cresora.ui.spd", spd).getString(),
				detailX + 78, attrY + 13, WeaponUiTheme.TEXT_TITLE, false);

		int statRowY = attrY + 24;
		if (def.critRateBonusPercent() > 0) {
			graphics.text(font, Component.translatable("cresora.ui.crit_rate", def.critRateBonusPercent()).getString(),
					detailX + 8, statRowY, WeaponUiTheme.ACCENT_CYAN, false);
		} else if (def.maxAllDamageBonusPercent() > 0) {
			double allDmg = WeaponCombatSupport.allDamageBonusPercent(def, data);
			graphics.text(font, Component.translatable("cresora.ui.all_dmg", allDmg).getString(),
					detailX + 8, statRowY, WeaponUiTheme.ACCENT_CYAN, false);
		} else if (def.hpBonusPercent() > 0) {
			graphics.text(font, Component.translatable("cresora.ui.hp_bonus", def.hpBonusPercent()).getString(),
					detailX + 8, statRowY, WeaponUiTheme.ACCENT_CYAN, false);
		}

		// 6. Skill Section
		int skillY = originY + 128;
		graphics.text(font, Component.translatable("cresora.ui.active_skill").getString(), detailX + 8, skillY, WeaponUiTheme.TEXT_MUTED, false);

		if (def.skill() != null && !def.skill().effectId().equals("none") && def.skill().cooldownSeconds() > 0) {
			String cdText = Component.translatable("cresora.ui.cooldown_sec", def.skill().cooldownSeconds()).getString();
			int cdW = font.width(cdText);
			graphics.text(font, cdText, detailX + DETAIL_WIDTH - cdW - 8, skillY, WeaponUiTheme.TEXT_MUTED, false);
		}

		graphics.fill(detailX + 8, skillY + 9, detailX + DETAIL_WIDTH - 8, skillY + 10, WeaponUiTheme.PANEL_DIVIDER);

		if (def.skill() != null && !def.skill().effectId().equals("none")) {
			String skillName = WeaponUiTheme.formatSkillName(def);
			graphics.text(font, "⚡ " + skillName, detailX + 8, skillY + 13, WeaponUiTheme.ACCENT_CYAN, false);

			String skillDesc = WeaponUiTheme.formatSkillDescription(def, data);
			graphics.textWithWordWrap(font, Component.literal(skillDesc), detailX + 8, skillY + 24, DETAIL_WIDTH - 16, WeaponUiTheme.TEXT_BODY);
		} else {
			graphics.text(font, Component.translatable("cresora.ui.no_skill").getString(), detailX + 8, skillY + 13, WeaponUiTheme.TEXT_MUTED, false);
		}

		// 7. Bottom Status Pill
		int statusY = originY + 178;
		if (isEquipped && slotIndex == inventory.selected()) {
			WeaponUiTheme.drawBadge(graphics, font, Component.translatable("cresora.ui.active_weapon").getString(),
					detailX + 8, statusY, WeaponUiTheme.SELECTION_GOLD, 0x40FFD15C, WeaponUiTheme.SELECTION_GOLD);
		} else if (isEquipped) {
			WeaponUiTheme.drawBadge(graphics, font, Component.translatable("cresora.ui.equipped_slot_n", slotIndex + 1).getString(),
					detailX + 8, statusY, WeaponUiTheme.ACCENT_CYAN, 0x3038E5D8, WeaponUiTheme.ACCENT_CYAN);
		} else {
			WeaponUiTheme.drawBadge(graphics, font, Component.translatable("cresora.ui.in_storage").getString(),
					detailX + 8, statusY, WeaponUiTheme.TEXT_SUCCESS, 0x304ADE80, WeaponUiTheme.TEXT_SUCCESS);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		int mouseX = (int) event.x();
		int mouseY = (int) event.y();

		int equipIndex = slotAt(mouseX, mouseY, equipStartX, equipY, EQUIP_COUNT, EQUIP_COUNT, EQUIP_SLOT_SIZE, EQUIP_GAP);
		if (equipIndex >= 0) {
			if (event.button() == 1) {
				ClientPlayNetworking.send(new WeaponBagActionPayload(WeaponBagActionPayload.SELECT, equipIndex));
			} else if (event.button() == 0 && !WeaponSlots.get(equipIndex).isEmpty()) {
				ClientPlayNetworking.send(new WeaponBagActionPayload(WeaponBagActionPayload.UNEQUIP, equipIndex));
			}
			return true;
		}

		int bagIndex = slotAt(mouseX, mouseY, bagStartX, bagStartY, WeaponInventory.BAG_SIZE, BAG_COLUMNS, BAG_SLOT_SIZE, BAG_GAP);
		if (bagIndex >= 0) {
			ItemStack stack = WeaponSlots.inventory().bag(bagIndex);
			if (event.button() == 0 && !stack.isEmpty() && WeaponStackSupport.isWeapon(stack)) {
				ClientPlayNetworking.send(new WeaponBagActionPayload(WeaponBagActionPayload.EQUIP, bagIndex));
			}
			return true;
		}

		return super.mouseClicked(event, doubleClick);
	}

	/** Returns the slot index under the cursor, or {@code -1} if none. */
	private static int slotAt(int mouseX, int mouseY, int startX, int startY, int count, int columns, int size, int gap) {
		for (int index = 0; index < count; index++) {
			int col = index % columns;
			int row = index / columns;
			int x = startX + col * (size + gap);
			int y = startY + row * (size + gap);
			if (mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size) {
				return index;
			}
		}
		return -1;
	}
}
