package com.mr208.dirt2path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = Dirt2Path.MOD_ID, name = Dirt2Path.MOD_NAME, version = "1.6.2", acceptedMinecraftVersions = "[1.9,1.12)")
public class Dirt2Path {

	public static final String MOD_ID = "dirt2path";
	public static final String MOD_NAME = "Dirt2Path";
	public static final ItemStack EMPTY = new ItemStack((Item) null);

	public static Logger logger = LogManager.getLogger(MOD_ID);

	public static Configuration config;
	public static boolean flattenBOP;
	public static boolean flattenBotania;
	public static boolean raisePath;
	public static boolean raisePathSneaky;
	public static boolean raiseFarmland;
	public static boolean patchCOFH;

	@GameRegistry.ObjectHolder("biomesoplenty:grass")
	public static final Block BOP_GRASS = null;
	@GameRegistry.ObjectHolder("biomesoplenty:grass_path")
	public static final Block BOP_GRASS_PATH = null;
	@GameRegistry.ObjectHolder("biomesoplenty:dirt")
	public static final Block BOP_DIRT = null;
	@GameRegistry.ObjectHolder("biomesoplenty:farmland_0")
	public static final Block BOP_FARMLAND_0 = null;
	@GameRegistry.ObjectHolder("biomesoplenty:farmland_1")
	public static final Block BOP_FARMLAND_1 = null;

	@GameRegistry.ObjectHolder("botania:altGrass")
	public static final Block BOTANIA_GRASS_1_10_2 = null;
	@GameRegistry.ObjectHolder("botania:altgrass")
	public static final Block BOTANIA_GRASS_1_11_2 = null;

	public static Class COFHShovel;
	public static boolean COFHWorkaround = false;

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {

		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		raisePath = config.getBoolean("Take Backsies", "General", true, "Convert Path Blocks to Dirt on Right Click");
		raisePathSneaky = config.getBoolean("Take Bascies Sneaky", "General", false, "If True, You have to be sneaking to use Take Backsies");
		raiseFarmland = config.getBoolean("Remove Farmland", "General", true,"Convert Farmland Blocks to Dirt on Right Click.");
		flattenBOP = config.getBoolean("Biomes O Plenty", "General", true, "Convert Biomes O Plenty Loamy, Sandy, and Silty Dirt into the appropriate Path blocks");
		flattenBotania = config.getBoolean("Botania", "General", true, "Convert Botania Grasses to the default Grass Path.");
		patchCOFH = config.getBoolean("COFH Core", "General", false, "Enable COFH ItemShovelCore to convert Grass blocks to Path blocks");
		config.save();
	}

	@Mod.EventHandler
	public void onInit(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);


		if(patchCOFH && Loader.isModLoaded("cofhcore")) {
			try {
				COFHShovel = Class.forName("cofh.core.item.tool.ItemShovelCore");
				COFHWorkaround = COFHShovel!=null;
			} catch (ClassNotFoundException e) {
				logger.info("COFH Core detected but unable to find ItemShovelCore");
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onBlockRightclick(PlayerInteractEvent.RightClickBlock event) {

		if(event.getResult() != Event.Result.DEFAULT || event.isCanceled())
			return;

		EntityPlayer player = event.getEntityPlayer();
		World world = event.getWorld();
		BlockPos blockPos = event.getPos();
		ItemStack itemStack = player.getHeldItem(event.getHand());

		if (itemStack == null || itemStack == EMPTY || itemStack.getItem() == Item.getItemFromBlock(Blocks.AIR) || itemStack.getItemDamage() < -32768 || itemStack.getItemDamage() > 65535)
			return;

		if (!itemStack.canHarvestBlock(Blocks.SNOW.getDefaultState()))
			return;

		IBlockState iBlockState = world.getBlockState(blockPos);

		if (world.getBlockState(blockPos.up()).getMaterial() == Material.AIR) {
			if (isBlockDirt(iBlockState, itemStack)) {
				IBlockState pathState = getPathBlockState(iBlockState, itemStack);
				setPathOrDirt(world, pathState, blockPos, SoundEvents.ITEM_SHOVEL_FLATTEN, player, itemStack, event.getHand());
			} else if (raisePath && requiresSneaking(raisePathSneaky, player) && isBlockPath(iBlockState)) {
				IBlockState dirtState = getDirtBlockState(iBlockState);
				setPathOrDirt(world, dirtState, blockPos, SoundEvents.ITEM_HOE_TILL, player, itemStack, event.getHand());
			} else if (raiseFarmland && player.isSneaking() && isBlockFarmland(iBlockState)) {
				IBlockState dirtState = getDirtBlockState(iBlockState);
				setPathOrDirt(world, dirtState, blockPos, SoundEvents.ITEM_HOE_TILL, player, itemStack, event.getHand());
			}
		}
	}

	protected boolean requiresSneaking(boolean sneaky, EntityPlayer player) {
		return !sneaky || player.isSneaking();
	}

	protected void setPathOrDirt(World world, IBlockState blockState, BlockPos blockPos, SoundEvent soundEvent, EntityPlayer player, ItemStack itemStack, EnumHand hand) {
		world.playSound(player, blockPos, soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
		player.swingArm(hand);
		if(!world.isRemote) {
			world.setBlockState(blockPos, blockState, 11);
			itemStack.damageItem(1, player);
		}
	}

	protected boolean isBlockPath(IBlockState iBlockStateIn) {
		int blockMeta = iBlockStateIn.getBlock().getMetaFromState(iBlockStateIn);
		if(iBlockStateIn.getBlock() == Blocks.GRASS_PATH) return true;
		if(flattenBOP && (iBlockStateIn.getBlock() == BOP_GRASS_PATH && blockMeta <4)) return true;
		return false;
	}

	protected boolean isBlockDirt(IBlockState iBlockStateIn, ItemStack itemStackIn) {
		int blockMeta = iBlockStateIn.getBlock().getMetaFromState(iBlockStateIn);
		if(iBlockStateIn.getBlock() == Blocks.DIRT) return true;
		if(flattenBOP  && (iBlockStateIn.getBlock() == BOP_DIRT && blockMeta < 4)) return true;
		if(flattenBotania && iBlockStateIn.getBlock() == BOTANIA_GRASS_1_10_2) return true;
		if(flattenBotania && iBlockStateIn.getBlock() == BOTANIA_GRASS_1_11_2) return true;
		if(COFHWorkaround && COFHShovel.isInstance(itemStackIn.getItem())) {
			if(iBlockStateIn.getBlock() == Blocks.GRASS) return  true;
			if(iBlockStateIn.getBlock() == BOP_GRASS && blockMeta <4) return true;
		}
		return false;
	}

	protected boolean isBlockFarmland(IBlockState iBlockStateIn) {
		int blockMeta = iBlockStateIn.getBlock().getMetaFromState(iBlockStateIn);
		if(iBlockStateIn.getBlock() == Blocks.FARMLAND) return true;
		if(iBlockStateIn.getBlock() == BOP_FARMLAND_0) return true;
		if(iBlockStateIn.getBlock() == BOP_FARMLAND_1) return true;
		return false;
	}

	protected IBlockState getDirtBlockState(IBlockState iBlockState) {
		int blockMeta = iBlockState.getBlock().getMetaFromState(iBlockState);
		if(iBlockState.getBlock() == BOP_GRASS_PATH && blockMeta <4) {
			return BOP_DIRT.getStateFromMeta(blockMeta);
		}
		if(iBlockState.getBlock() == BOP_FARMLAND_0) {
			return BOP_DIRT.getStateFromMeta(blockMeta & 1);
		}
		if(iBlockState.getBlock() == BOP_FARMLAND_1) {
			return BOP_DIRT.getStateFromMeta(3);
		}
		return Blocks.DIRT.getDefaultState();
	}

	protected IBlockState getPathBlockState(IBlockState iBlockStateIn, ItemStack itemStackIn) {
		int blockMeta = iBlockStateIn.getBlock().getMetaFromState(iBlockStateIn);
		if(iBlockStateIn.getBlock() == BOP_DIRT && blockMeta < 4) {
			return BOP_GRASS_PATH.getStateFromMeta(blockMeta);
		}
		if(COFHWorkaround && COFHShovel.isInstance(itemStackIn.getItem())) {
			if(iBlockStateIn.getBlock() == BOP_GRASS && blockMeta <4) {
				return BOP_GRASS_PATH.getStateFromMeta(blockMeta);
			}
		}
		return Blocks.GRASS_PATH.getDefaultState();
	}
}
