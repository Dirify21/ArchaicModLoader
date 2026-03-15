package com.github.dirify21.aml.helper;

import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public final class PotionHelper {
    private static final Map<String, Potion> POTION_MAP = new HashMap<>();

    static {
        POTION_MAP.put("field_76424_c", MobEffects.SPEED);            // moveSpeed -> SPEED
        POTION_MAP.put("field_76421_d", MobEffects.SLOWNESS);         // moveSlowdown -> SLOWNESS
        POTION_MAP.put("field_76422_e", MobEffects.HASTE);            // digSpeed -> HASTE
        POTION_MAP.put("field_76419_f", MobEffects.MINING_FATIGUE);   // digSlowdown -> MINING_FATIGUE
        POTION_MAP.put("field_76440_q", MobEffects.BLINDNESS);        // blindness -> BLINDNESS
        POTION_MAP.put("field_76433_i", MobEffects.INSTANT_DAMAGE);   // harm -> INSTANT_DAMAGE
        POTION_MAP.put("field_76432_h", MobEffects.INSTANT_HEALTH);   // heal -> INSTANT_DAMAGE
        POTION_MAP.put("field_76430_j", MobEffects.JUMP_BOOST);       // jump -> JUMP_BOOST
        POTION_MAP.put("field_76431_k", MobEffects.NAUSEA);           // confusion -> NAUSEA
        POTION_MAP.put("field_76428_l", MobEffects.REGENERATION);     // regeneration -> REGENERATION
        POTION_MAP.put("field_76429_m", MobEffects.RESISTANCE);       // resistance -> RESISTANCE
        POTION_MAP.put("field_76426_n", MobEffects.FIRE_RESISTANCE);  // fireResistance -> FIRE_RESISTANCE
        POTION_MAP.put("field_76427_o", MobEffects.WATER_BREATHING);  // waterBreathing -> WATER_BREATHING
        POTION_MAP.put("field_76441_p", MobEffects.INVISIBILITY);     // invisibility -> INVISIBILITY
        POTION_MAP.put("field_76420_g", MobEffects.STRENGTH);         // damageBoost -> BLINDNESS
        POTION_MAP.put("field_76439_r", MobEffects.NIGHT_VISION);     // nightVision -> NIGHT_VISION
        POTION_MAP.put("field_76438_s", MobEffects.HUNGER);           // hunger -> HUNGER
        POTION_MAP.put("field_76437_t", MobEffects.WEAKNESS);         // weakness -> WEAKNESS
        POTION_MAP.put("field_76436_u", MobEffects.POISON);           // poison -> POISON
        POTION_MAP.put("field_82731_v", MobEffects.WITHER);           // wither -> WITHER
    }

    public static Potion getPotionField(String f) {
        return POTION_MAP.get(f);
    }
}
