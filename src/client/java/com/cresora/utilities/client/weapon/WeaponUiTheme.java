package com.cresora.utilities.client.weapon;

import com.cresora.utilities.combat.CombatDamageType;
import com.cresora.utilities.weapon.WeaponCombatSupport;
import com.cresora.utilities.weapon.WeaponData;
import com.cresora.utilities.weapon.WeaponDefinition;
import com.cresora.utilities.weapon.WeaponRarity;
import com.cresora.utilities.weapon.WeaponRole;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * Visual styling constants and drawing helpers for Cresora weapon HUD and screens.
 */
public final class WeaponUiTheme {
	// Panel backgrounds & borders
	public static final int PANEL_BG_TOP = 0xF00A0D15;
	public static final int PANEL_BG_BOTTOM = 0xF80E1422;
	public static final int PANEL_BORDER = 0xFF2A364E;
	public static final int PANEL_BORDER_LIGHT = 0x256080A8;
	public static final int PANEL_DIVIDER = 0xFF1E2838;

	// Slots
	public static final int SLOT_BG = 0xB0080B13;
	public static final int SLOT_BG_HOVER = 0xD8141C2B;
	public static final int SLOT_BORDER = 0x55384660;
	public static final int SLOT_BORDER_HOVER = 0xC094A3B8;

	// Active Selection
	public static final int SELECTION_GOLD = 0xFFFFD15C;
	public static final int SELECTION_GLOW = 0x60FFD15C;
	public static final int SELECTION_BG_TINT = 0x24FFD15C;

	// Accents
	public static final int ACCENT_CYAN = 0xFF38E5D8;
	public static final int ACCENT_GOLD = 0xFFFFB82E;
	public static final int ACCENT_CORNER = 0x805B749E;

	// Typography colors
	public static final int TEXT_TITLE = 0xFFF8FAFC;
	public static final int TEXT_BODY = 0xFFCBD5E1;
	public static final int TEXT_MUTED = 0xFF64748B;
	public static final int TEXT_HINT = 0xFF94A3B8;
	public static final int TEXT_SUCCESS = 0xFF4ADE80;
	public static final int TEXT_WARNING = 0xFFFBBF24;

	// Vanilla sprites
	public static final Identifier SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot");
	public static final Identifier SWORD_SPRITE = Identifier.withDefaultNamespace("container/slot/sword");

	private WeaponUiTheme() {
	}

	public static int rarityColor(WeaponRarity rarity) {
		if (rarity == null) {
			return 0xFF94A3B8;
		}
		return switch (rarity) {
			case TWO_STAR -> 0xFF4ADE80;   // Bright Emerald / Mint
			case THREE_STAR -> 0xFF38BDF8; // Bright Sky Blue
			case FOUR_STAR -> 0xFFA855F7;  // Amethyst Purple
			case FIVE_STAR -> 0xFFFFB82E;  // Radiant Gold
		};
	}

	public static int rarityBgTint(WeaponRarity rarity) {
		if (rarity == null) {
			return 0x00000000;
		}
		return switch (rarity) {
			case TWO_STAR -> 0x204ADE80;
			case THREE_STAR -> 0x2038BDF8;
			case FOUR_STAR -> 0x25A855F7;
			case FIVE_STAR -> 0x28FFB82E;
		};
	}

	public static String rarityStars(WeaponRarity rarity) {
		if (rarity == null) {
			return "";
		}
		return "★".repeat(rarity.stars());
	}

	public static String rarityName(WeaponRarity rarity) {
		if (rarity == null) {
			return "Common";
		}
		return net.minecraft.network.chat.Component.translatable(rarity.translationKey()).getString();
	}

	public static int roleColor(WeaponRole role) {
		if (role == null) {
			return 0xFF94A3B8;
		}
		return switch (role) {
			case GUARD -> 0xFF38BDF8;
			case VANGUARD -> 0xFFF87171;
			case DEFENDER -> 0xFF60A5FA;
			case SNIPER -> 0xFF34D399;
			case CASTER -> 0xFFA78BFA;
			case MEDIC -> 0xFF4ADE80;
			case SUPPORTER -> 0xFFFBBF24;
			case SPECIALIST -> 0xFFFB923C;
			case CATALYST -> 0xFFE879F9;
		};
	}

	public static int damageTypeColor(CombatDamageType type) {
		if (type == null) {
			return 0xFFCBD5E1;
		}
		return switch (type) {
			case PHYSICAL -> 0xFFE2E8F0;
			case ARCANE -> 0xFFC084FC;
		};
	}

	public static String formatSkillName(WeaponDefinition definition) {
		if (definition == null || definition.skill() == null || definition.skill().effectId().equals("none")) {
			return net.minecraft.network.chat.Component.translatable("cresora.ui.no_skill").getString();
		}
		String specificKey = "item.cresora-utilities." + definition.id() + ".skill";
		if (net.minecraft.locale.Language.getInstance().has(specificKey)) {
			return net.minecraft.network.chat.Component.translatable(specificKey).getString();
		}
		String genericKey = "item.cresora.weapon.skill." + definition.skill().effectId();
		if (net.minecraft.locale.Language.getInstance().has(genericKey)) {
			return net.minecraft.network.chat.Component.translatable(genericKey).getString();
		}
		return definition.skill().effectId().replace('_', ' ');
	}

	public static String formatSkillDescription(WeaponDefinition definition, WeaponData data) {
		if (definition == null || definition.skill() == null) {
			return net.minecraft.network.chat.Component.translatable("cresora.ui.no_skill").getString();
		}
		String effect = definition.skill().effectId();
		String descKey = "cresora.skill_desc." + effect;
		if (effect.equals("current_hp_true_damage")) {
			double ratio = WeaponCombatSupport.currentHpTrueDamageRatio(definition, data) * 100.0;
			return net.minecraft.network.chat.Component.translatable(descKey, ratio).getString();
		} else if (effect.equals("shield")) {
			double val = WeaponCombatSupport.skillValueHearts(definition, data);
			return net.minecraft.network.chat.Component.translatable(descKey, val, definition.skill().durationSeconds()).getString();
		} else if (effect.equals("heal")) {
			double val = WeaponCombatSupport.skillValueHearts(definition, data);
			return net.minecraft.network.chat.Component.translatable(descKey, val).getString();
		} else if (net.minecraft.locale.Language.getInstance().has(descKey)) {
			return net.minecraft.network.chat.Component.translatable(descKey).getString();
		}
		return effect.replace('_', ' ');
	}

	/** Draws an RPG window panel with subtle gradient, sleek borders, and corner accents. */
	public static void drawPanel(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
		graphics.fillGradient(x, y, x + width, y + height, PANEL_BG_TOP, PANEL_BG_BOTTOM);
		graphics.outline(x, y, width, height, PANEL_BORDER);
		graphics.outline(x + 1, y + 1, width - 2, height - 2, PANEL_BORDER_LIGHT);
		drawCornerTicks(graphics, x, y, width, height, ACCENT_CORNER);
	}

	/** Draws a simple bordered card box. */
	public static void drawCard(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int bgColor, int borderColor) {
		graphics.fill(x, y, x + width, y + height, bgColor);
		graphics.outline(x, y, width, height, borderColor);
	}

	/** Draws high-tech corner tick marks. */
	public static void drawCornerTicks(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int color) {
		int right = x + w - 1;
		int bottom = y + h - 1;

		// Top-left
		graphics.fill(x, y, x + 3, y + 1, color);
		graphics.fill(x, y, x + 1, y + 3, color);

		// Top-right
		graphics.fill(right - 2, y, right + 1, y + 1, color);
		graphics.fill(right, y, right + 1, y + 3, color);

		// Bottom-left
		graphics.fill(x, bottom, x + 3, bottom + 1, color);
		graphics.fill(x, bottom - 2, x + 1, bottom + 1, color);

		// Bottom-right
		graphics.fill(right - 2, bottom, right + 1, bottom + 1, color);
		graphics.fill(right, bottom - 2, right + 1, bottom + 1, color);
	}

	/** Draws a stylish slot base with optional rarity highlight and active selection glowing frame. */
	public static void drawSlotBackground(GuiGraphicsExtractor graphics, int x, int y, int size,
			boolean selected, boolean hovered, WeaponRarity rarity) {
		// Slot recessed background
		int bg = hovered ? SLOT_BG_HOVER : SLOT_BG;
		graphics.fill(x + 1, y + 1, x + size - 1, y + size - 1, bg);

		// Rarity accent tint inside slot
		if (rarity != null) {
			graphics.fill(x + 1, y + 1, x + size - 1, y + size - 1, rarityBgTint(rarity));
			// Bottom rarity bar
			int color = rarityColor(rarity);
			graphics.fill(x + 1, y + size - 2, x + size - 1, y + size - 1, color);
		}

		if (selected) {
			// Double glowing border for selected slot
			graphics.outline(x - 1, y - 1, size + 2, size + 2, SELECTION_GLOW);
			graphics.outline(x, y, size, size, SELECTION_GOLD);
			graphics.fill(x + 1, y + 1, x + size - 1, y + size - 1, SELECTION_BG_TINT);
		} else if (hovered) {
			graphics.outline(x, y, size, size, SLOT_BORDER_HOVER);
		} else {
			graphics.outline(x, y, size, size, SLOT_BORDER);
		}
	}

	/** Draws an empty slot watermark sword silhouette. */
	public static void drawEmptySlotWatermark(GuiGraphicsExtractor graphics, int x, int y, int size) {
		int inset = (size - 16) / 2;
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SWORD_SPRITE, x + inset, y + inset, 16, 16, 0x30607590);
	}

	/** Draws a compact rounded pill badge with border and text. */
	public static void drawBadge(GuiGraphicsExtractor graphics, Font font, String text, int x, int y,
			int textColor, int bgColor, int borderColor) {
		int textWidth = font.width(text);
		int badgeWidth = textWidth + 6;
		int badgeHeight = 10;
		graphics.fill(x, y, x + badgeWidth, y + badgeHeight, bgColor);
		graphics.outline(x, y, badgeWidth, badgeHeight, borderColor);
		graphics.text(font, text, x + 3, y + 1, textColor, false);
	}

	/** Draws a mini progress bar. */
	public static void drawProgressBar(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
			double ratio, int barColor, int trackColor) {
		graphics.fill(x, y, x + width, y + height, trackColor);
		int filledWidth = (int) Math.round(width * Math.clamp(ratio, 0.0, 1.0));
		if (filledWidth > 0) {
			graphics.fill(x, y, x + filledWidth, y + height, barColor);
		}
		graphics.outline(x, y, width, height, 0x40FFFFFF);
	}
}
