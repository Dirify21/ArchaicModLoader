package com.github.dirify21.aml.util;

import com.github.dirify21.aml.api.IArchaicBlock;
import com.github.dirify21.aml.api.IArchaicItem;
import com.github.dirify21.aml.api.IIcon;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.HashMap;
import java.util.Map;

public class RedirectHelper {
    private static final Map<String, Potion> POTION_MAP = new HashMap<>();

    static {
        POTION_MAP.put("field_76424_c", MobEffects.SPEED);
        POTION_MAP.put("field_76421_d", MobEffects.SLOWNESS);
        POTION_MAP.put("field_76422_e", MobEffects.HASTE);
        POTION_MAP.put("field_76420_f", MobEffects.MINING_FATIGUE);
        POTION_MAP.put("field_76440_g", MobEffects.STRENGTH);
        POTION_MAP.put("field_76433_i", MobEffects.INSTANT_HEALTH);
        POTION_MAP.put("field_76432_j", MobEffects.INSTANT_DAMAGE);
        POTION_MAP.put("field_76430_k", MobEffects.JUMP_BOOST);
        POTION_MAP.put("field_76431_y", MobEffects.NAUSEA);
        POTION_MAP.put("field_76428_l", MobEffects.REGENERATION);
        POTION_MAP.put("field_76429_m", MobEffects.RESISTANCE);
        POTION_MAP.put("field_76426_n", MobEffects.FIRE_RESISTANCE);
        POTION_MAP.put("field_76427_o", MobEffects.WATER_BREATHING);
        POTION_MAP.put("field_76425_p", MobEffects.INVISIBILITY);
        POTION_MAP.put("field_76441_q", MobEffects.BLINDNESS);
        POTION_MAP.put("field_76442_r", MobEffects.NIGHT_VISION);
        POTION_MAP.put("field_76438_s", MobEffects.HUNGER);
        POTION_MAP.put("field_76437_t", MobEffects.WEAKNESS);
        POTION_MAP.put("field_76436_u", MobEffects.POISON);
        POTION_MAP.put("field_76435_v", MobEffects.WITHER);
    }

    public static String getTextureNameRedirect(Block block) {
        if (block instanceof IArchaicBlock) {
            return ((IArchaicBlock) block).aml$getBlockTextureName();
        }
        return null;
    }

    public static void makeTransparent(Object tile) {
        ((TileEntity) tile).getBlockType().setLightOpacity(0);
    }

    public static PotionEffect createPotionEffect(int id, int duration, int amplifier) {
        Potion p = Potion.getPotionById(id);
        return new PotionEffect(p != null ? p : MobEffects.REGENERATION, duration, amplifier);
    }

    public static ItemFood setPotionEffectRedirect(ItemFood f, int id, int d, int a, float p) {
        Potion pot = Potion.getPotionById(id);
        if (pot != null) f.setPotionEffect(new PotionEffect(pot, d * 20, a), p);
        return f;
    }

    public static Block getBlockRedirect(World world, int x, int y, int z) {
        return world.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    public static int getMetadataRedirect(World world, int x, int y, int z) {
        IBlockState state = world.getBlockState(new BlockPos(x, y, z));
        return state.getBlock().getMetaFromState(state);
    }

    public static boolean isAirBlockRedirect(World world, int x, int y, int z) {
        return world.isAirBlock(new BlockPos(x, y, z));
    }

    public static Item setTextureNameRedirect(Item i, String n) {
        return (i instanceof IArchaicItem a) ? a.aml$setTextureName(n) : i;
    }

    public static Block setBlockTextureNameRedirect(Block b, String n) {
        return (b instanceof IArchaicBlock a) ? a.aml$setBlockTextureName(n) : b;
    }

    public static Potion getPotionFieldRedirect(String f) {
        return POTION_MAP.getOrDefault(f, MobEffects.LUCK);
    }

    public static int getPotionIdRedirect(Potion p) {
        return Potion.getIdFromPotion(p);
    }

    public static int getX(net.minecraft.tileentity.TileEntity te) {
        return te.getPos().getX();
    }

    public static int getY(net.minecraft.tileentity.TileEntity te) {
        return te.getPos().getY();
    }

    public static int getZ(net.minecraft.tileentity.TileEntity te) {
        return te.getPos().getZ();
    }

    public static void registerTileEntityRedirect(Class<? extends net.minecraft.tileentity.TileEntity> c, String id) {
        ModContainer mc = Loader.instance().activeModContainer();
        GameRegistry.registerTileEntity(c, new ResourceLocation(mc != null ? mc.getModId() : "minecraft", id));
    }

    public static void addRecipeRedirect(ItemStack output, Object[] params) {
        if (output == null || output.isEmpty()) return;

        ResourceLocation recipeName = new ResourceLocation("aml_virtual", "recipe_" + System.nanoTime());

        GameRegistry.addShapedRecipe(recipeName, null, output, params);
    }

    public static AxisAlignedBB createAABB(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
    }

    @SideOnly(Side.CLIENT)
    public static RenderManager getRenderManager() {
        return Minecraft.getMinecraft().getRenderManager();
    }

    public static void addShapelessRecipeRedirect(ItemStack output, Object[] params) {
        if (output == null || output.isEmpty()) return;
        ResourceLocation recipeName = new ResourceLocation("aml_virtual", "shapeless_" + System.nanoTime());
        ShapelessOreRecipe recipe = new ShapelessOreRecipe(null, output, params);
        recipe.setRegistryName(recipeName);
        ForgeRegistries.RECIPES.register(recipe);
    }

    public static EnumFacing getFacingFromId(int side) {
        return EnumFacing.byIndex(side);
    }

    public static int getIdFromFacing(EnumFacing facing) {
        return facing.getIndex();
    }

    public static IIcon getIconForBlock(Block block, EnumFacing facing, int meta) {
        if (block instanceof IArchaicBlock) {
            return ((IArchaicBlock) block).aml$getIcon(facing.getIndex(), meta);
        }
        return null;
    }
}