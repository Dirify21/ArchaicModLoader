package com.github.dirify21.aml.mixin;

import fr.catcore.cursedmixinextensions.annotations.NewConstructor;
import fr.catcore.cursedmixinextensions.annotations.ShadowConstructor;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PotionEffect.class)
@SuppressWarnings("unused")
public abstract class PotionEffectMixin {
    @ShadowConstructor
    public abstract void PotionEffect(Potion p_i46813_1, int p_i46813_2, int p_i46813_3);

    @NewConstructor
    public void PotionEffect(int id, int duration, int amplifier) {
        this.PotionEffect(Potion.getPotionById(id), duration, amplifier);
    }
}