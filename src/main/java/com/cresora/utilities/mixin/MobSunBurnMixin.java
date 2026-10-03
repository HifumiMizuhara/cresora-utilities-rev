package com.cresora.utilities.mixin;

import net.minecraft.world.entity.Mob;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mob mechanic #1: monsters no longer burn in sunlight.
 *
 * <p>In Minecraft 26.2 the daylight-burning chain is:
 * <pre>
 *   Mob.aiStep()
 *     -&gt; if (this.is(EntityTypeTags.BURN_IN_DAYLIGHT)) this.burnUndead();
 *   Mob.burnUndead()
 *     -&gt; if (this.isAlive() &amp;&amp; this.isSunBurnTick()) { ...igniteForSeconds(8.0F)... }
 *   Mob.isSunBurnTick()
 *     -&gt; level().environmentAttributes().getValue(EnvironmentAttributes.MONSTERS_BURN, position())
 *        &amp;&amp; light &gt; 0.5F &amp;&amp; canSeeSky(...) &amp;&amp; random check
 * </pre>
 *
 * <p>{@code isSunBurnTick()} is private, declared only in {@code Mob}, and called from exactly
 * one place ({@code burnUndead()}) - verified against the decompiled 26.2 sources. Forcing it
 * to return {@code false} therefore disables the whole mechanic for every mob (zombies,
 * skeletons, phantoms, zombie horses, modded mobs, ...) without touching unrelated logic.
 *
 * <p>Why not override the {@code minecraft:gameplay/monsters_burn} environment attribute with a
 * datapack instead? Vanilla sets it to {@code true} during the day in
 * {@code data/minecraft/timeline/day.json}, and attribute layers are applied in order with the
 * timeline's {@code or} modifier. Since datapack tags only ever <em>add</em> timelines, the
 * relative order of our layer and the vanilla one is not guaranteed, so a data-driven override
 * cannot reliably force the value to {@code false}. The mixin is order-independent.
 *
 * <p>Side effect: undead wearing a helmet no longer take sunlight durability damage either,
 * because {@code burnUndead()} returns before it reaches the helmet branch.
 */
@Mixin(Mob.class)
public abstract class MobSunBurnMixin {
	@Inject(method = "isSunBurnTick", at = @At("HEAD"), cancellable = true)
	private void cresoraUtilities$disableSunBurn(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}
}
