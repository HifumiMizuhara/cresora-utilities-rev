package com.cresora.utilities.client.weapon;

import java.util.ArrayList;
import java.util.List;

import com.cresora.utilities.CresoraUtilities;
import com.cresora.utilities.client.combat.CombatStateClient;
import com.cresora.utilities.weapon.WeaponData;
import com.cresora.utilities.weapon.WeaponDefinition;
import com.cresora.utilities.weapon.WeaponInventory;
import com.cresora.utilities.weapon.WeaponRarity;
import com.cresora.utilities.weapon.WeaponSkillDefinition;
import com.cresora.utilities.weapon.WeaponStackSupport;
import com.cresora.utilities.weapon.WeaponZone;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Draws an RPG-style weapon hotbar HUD in the top-left corner of the screen.
 *
 * <p>Features:
 * <ul>
 *   <li>Sleek dark glassmorphic chassis with corner and accent indicators</li>
 *   <li>Slot frames with empty watermark silhouettes and slot numbers</li>
 *   <li>Rarity-colored accent stripes and level indicators for equipped weapons</li>
 *   <li>Golden glowing active frame with selection pointer</li>
 *   <li>Dynamic quick-readout flyout panel beside the active weapon (name, stars, level, skill prompt)</li>
 * </ul>
 */
public final class WeaponBarHud implements HudElement {
	/** Identifier this element is registered under. */
	public static final Identifier ID = CresoraUtilities.id("weapon_bar");

	private static final int MARGIN = 8;
	private static final int SLOT_SIZE = 22;
	private static final int SLOT_GAP = 3;
	private static final int BAR_WIDTH = 28;
	private static final int HEADER_HEIGHT = 12;

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker delta) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.player.isSpectator()) {
			return;
		}

		WeaponInventory inventory = WeaponSlots.inventory();
		int selected = inventory.selected();
		int totalSlots = WeaponSlots.SLOT_COUNT;
		int barHeight = HEADER_HEIGHT + 4 + totalSlots * (SLOT_SIZE + SLOT_GAP) + 1;

		// 1. Draw chassis backplate
		graphics.fillGradient(MARGIN, MARGIN, MARGIN + BAR_WIDTH, MARGIN + barHeight, 0xD80A0D15, 0xE80F1522);
		graphics.outline(MARGIN, MARGIN, BAR_WIDTH, barHeight, 0x602A364E);

		// Header accents
		graphics.text(minecraft.font, "◆", MARGIN + 4, MARGIN + 3, WeaponUiTheme.ACCENT_CYAN, false);
		graphics.text(minecraft.font, "V", MARGIN + 15, MARGIN + 3, 0xFF64748B, false);

		// 2. Draw slots
		for (int index = 0; index < totalSlots; index++) {
			int slotX = MARGIN + 3;
			int slotY = MARGIN + HEADER_HEIGHT + 2 + index * (SLOT_SIZE + SLOT_GAP);
			boolean isSelected = (index == selected);

			ItemStack stack = inventory.equipped(index);
			WeaponDefinition def = WeaponStackSupport.getDefinition(stack);
			WeaponData data = (def != null && !stack.isEmpty()) ? WeaponStackSupport.getWeaponData(stack) : null;
			WeaponRarity rarity = data != null ? data.rarity() : null;

			// Base slot & frame
			WeaponUiTheme.drawSlotBackground(graphics, slotX, slotY, SLOT_SIZE, isSelected, false, rarity);

			if (stack.isEmpty()) {
				// Empty watermark and slot number
				WeaponUiTheme.drawEmptySlotWatermark(graphics, slotX, slotY, SLOT_SIZE);
				graphics.text(minecraft.font, String.valueOf(index + 1), slotX + 2, slotY + 2, 0x608090A5, false);
			} else {
				// Render item icon & decorations
				int itemX = slotX + (SLOT_SIZE - 16) / 2;
				int itemY = slotY + (SLOT_SIZE - 16) / 2;
				graphics.item(stack, itemX, itemY);
				graphics.itemDecorations(minecraft.font, stack, itemX, itemY);

				// Level badge at bottom right
				if (data != null) {
					String lvlStr = String.valueOf(data.baseLevel());
					int lvlW = minecraft.font.width(lvlStr);
					graphics.text(minecraft.font, lvlStr, slotX + SLOT_SIZE - lvlW - 1, slotY + SLOT_SIZE - 7, 0xE8FFFFFF, true);
				}
			}

			// Active selection arrow / notch
			if (isSelected) {
				graphics.fill(slotX + SLOT_SIZE + 1, slotY + 8, slotX + SLOT_SIZE + 3, slotY + 14, WeaponUiTheme.SELECTION_GOLD);
			}
		}

		// 3. Floating readout flyout beside the active slot
		int activeSlotY = MARGIN + HEADER_HEIGHT + 2 + selected * (SLOT_SIZE + SLOT_GAP);
		int flyoutX = MARGIN + BAR_WIDTH + 4;
		int flyoutY = activeSlotY - 2;

		ItemStack activeStack = inventory.equipped(selected);
		WeaponDefinition activeDef = WeaponStackSupport.getDefinition(activeStack);
		WeaponData activeData = (activeDef != null && !activeStack.isEmpty()) ? WeaponStackSupport.getWeaponData(activeStack) : null;

		WeaponZone selectedZone = WeaponZone.forSlot(selected);
		String zoneName = selectedZone == null ? "" : net.minecraft.network.chat.Component.translatable(selectedZone.translationKey()).getString();
		int zoneColor = WeaponUiTheme.zoneColor(selectedZone);

		if (!activeStack.isEmpty() && activeData != null) {
			WeaponRarity activeRarity = activeData.rarity();
			int rColor = WeaponUiTheme.rarityColor(activeRarity);
			String titleText = activeStack.getHoverName().getString();

			boolean inCombat = CombatStateClient.isInCombat(minecraft.player);
			String statusText = net.minecraft.network.chat.Component.translatable(
					inCombat ? "cresora.ui.combat_active" : "cresora.ui.combat_idle").getString();
			String subText = zoneName + " · "
					+ net.minecraft.network.chat.Component.translatable("cresora.ui.level", activeData.baseLevel(), activeDef.maxBaseLevel()).getString()
					+ " · " + statusText;

			List<String> skillLines = new ArrayList<>();
			for (int slot = 0; slot < activeDef.skillCount(); slot++) {
				WeaponSkillDefinition skill = activeDef.skill(slot);
				if (skill == null || skill.effectId().equals("none")) {
					continue;
				}
				skillLines.add("[" + WeaponKeybinds.skillKeyLabel(slot) + "] " + WeaponUiTheme.formatSkillName(activeDef, slot));
			}

			int flyoutH = 24 + skillLines.size() * 10 + 2;
			int textW = Math.max(minecraft.font.width(titleText), minecraft.font.width(subText));
			for (String line : skillLines) {
				textW = Math.max(textW, minecraft.font.width(line));
			}
			int flyoutW = Math.max(120, textW + 14);

			graphics.fillGradient(flyoutX, flyoutY, flyoutX + flyoutW, flyoutY + flyoutH, 0xD80A0D15, 0xE80F1522);
			graphics.outline(flyoutX, flyoutY, flyoutW, flyoutH, 0x702A364E);
			graphics.fill(flyoutX, flyoutY, flyoutX + 2, flyoutY + flyoutH, rColor);

			graphics.text(minecraft.font, titleText, flyoutX + 6, flyoutY + 4, rColor, true);
			graphics.text(minecraft.font, subText, flyoutX + 6, flyoutY + 14, 0xFF94A3B8, true);

			int skillLineY = flyoutY + 25;
			for (String line : skillLines) {
				graphics.text(minecraft.font, line, flyoutX + 6, skillLineY, WeaponUiTheme.ACCENT_CYAN, false);
				skillLineY += 10;
			}
		} else {
			int flyoutH = 26;
			String emptyTitle = zoneName;
			String openBagHint = net.minecraft.network.chat.Component.translatable("cresora.ui.open_bag_hint").getString();
			int textW = Math.max(minecraft.font.width(emptyTitle), minecraft.font.width(openBagHint));
			int flyoutW = Math.max(90, textW + 14);

			graphics.fillGradient(flyoutX, flyoutY, flyoutX + flyoutW, flyoutY + flyoutH, 0xD80A0D15, 0xE80F1522);
			graphics.outline(flyoutX, flyoutY, flyoutW, flyoutH, 0x702A364E);
			graphics.fill(flyoutX, flyoutY, flyoutX + 2, flyoutY + flyoutH, zoneColor);

			graphics.text(minecraft.font, emptyTitle, flyoutX + 6, flyoutY + 4, zoneColor, true);
			graphics.text(minecraft.font, openBagHint, flyoutX + 6, flyoutY + 14, 0xFF64748B, true);
		}
	}
}
