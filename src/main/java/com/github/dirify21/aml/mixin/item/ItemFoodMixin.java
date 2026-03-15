package com.github.dirify21.aml.mixin.item;

import fr.catcore.cursedmixinextensions.annotations.Public;
import net.minecraft.item.ItemFood;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemFood.class)
@SuppressWarnings("unused")
public class ItemFoodMixin {
    @Public
    private ItemFood func_77844_a(int id, int d, int a, float p) {
        ItemFood itemFood = (ItemFood) (Object) this;
        Potion pot = Potion.getPotionById(id);
        itemFood.setPotionEffect(new PotionEffect(pot, d, a), p);
        return itemFood;
    }
}
