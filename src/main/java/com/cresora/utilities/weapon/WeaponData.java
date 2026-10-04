package com.cresora.utilities.weapon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Persistent data carried by a weapon {@code ItemStack}. Java port of {@code WeaponData.kt}.
 *
 * <p>The no-argument {@link #normalized()} clamps every field into its valid range. The
 * definition-aware overload from the original depends on {@code WeaponUpgradeService} and is
 * ported together with the upgrade logic.
 */
public record WeaponData(
		String weaponId,
		WeaponRarity rarity,
		int baseLevel,
		int skillLevel,
		int breakthrough,
		int spiritBondStage
) {
	public static final Codec<WeaponData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("weaponId").forGetter(WeaponData::weaponId),
			WeaponRarity.CODEC.fieldOf("rarity").forGetter(WeaponData::rarity),
			Codec.INT.fieldOf("baseLevel").forGetter(WeaponData::baseLevel),
			Codec.INT.fieldOf("skillLevel").forGetter(WeaponData::skillLevel),
			Codec.INT.optionalFieldOf("breakthrough", 0).forGetter(WeaponData::breakthrough),
			Codec.INT.optionalFieldOf("spiritBondStage", 1).forGetter(WeaponData::spiritBondStage)
	).apply(instance, WeaponData::new));

	public static final WeaponData DUMMY = new WeaponData("dummy", WeaponRarity.TWO_STAR, 1, 1, 0, 1);

	public WeaponData normalized() {
		return new WeaponData(
				weaponId.isBlank() ? "rondo_melody" : weaponId,
				rarity,
				Math.clamp(baseLevel, 1, 80),
				Math.clamp(skillLevel, 1, 10),
				Math.clamp(breakthrough, 0, 2),
				Math.clamp(spiritBondStage, 1, 6));
	}

	/** Normalises this data against a weapon definition's caps. */
	public WeaponData normalized(WeaponDefinition definition) {
		int bt = Math.clamp(breakthrough, 0, 2);
		int maxBase = WeaponUpgradeService.levelCap(definition, bt);
		int maxSkill = Math.min(WeaponUpgradeService.maxSkillLevelForBreakthrough(bt), definition.maxSkillLevel());
		return new WeaponData(
				definition.id(),
				rarity,
				Math.clamp(baseLevel, 1, maxBase),
				Math.clamp(skillLevel, 1, maxSkill),
				bt,
				Math.clamp(spiritBondStage, 1, 6));
	}
}
