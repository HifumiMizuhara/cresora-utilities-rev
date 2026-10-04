package com.cresora.utilities.weapon;

import java.util.HashMap;
import java.util.Map;

/**
 * Bridge to the transient, per-player weapon skill state (shield, cooldown, marks) that lives
 * on the player entity. Java port of {@code WeaponSkillAccess.kt}.
 */
public interface WeaponSkillAccess {
	float cresoraGetShieldHp();

	void cresoraSetShieldHp(float value);

	long cresoraGetShieldExpireTick();

	void cresoraSetShieldExpireTick(long value);

	String cresoraGetShieldWeaponId();

	void cresoraSetShieldWeaponId(String value);

	long cresoraGetSkillCooldownExpireTick();

	void cresoraSetSkillCooldownExpireTick(long value);

	String cresoraGetSkillCooldownWeaponId();

	void cresoraSetSkillCooldownWeaponId(String value);

	Map<String, Double> cresoraGetCooldowns();

	WeaponSkillAccess DUMMY = new WeaponSkillAccess() {
		@Override
		public float cresoraGetShieldHp() {
			return 0.0f;
		}

		@Override
		public void cresoraSetShieldHp(float value) {
		}

		@Override
		public long cresoraGetShieldExpireTick() {
			return 0L;
		}

		@Override
		public void cresoraSetShieldExpireTick(long value) {
		}

		@Override
		public String cresoraGetShieldWeaponId() {
			return null;
		}

		@Override
		public void cresoraSetShieldWeaponId(String value) {
		}

		@Override
		public long cresoraGetSkillCooldownExpireTick() {
			return 0L;
		}

		@Override
		public void cresoraSetSkillCooldownExpireTick(long value) {
		}

		@Override
		public String cresoraGetSkillCooldownWeaponId() {
			return null;
		}

		@Override
		public void cresoraSetSkillCooldownWeaponId(String value) {
		}

		@Override
		public Map<String, Double> cresoraGetCooldowns() {
			return new HashMap<>();
		}
	};
}
