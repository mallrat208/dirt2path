package com.mr208.dirt2path;

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
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = Dirt2Path.MOD_ID, name = Dirt2Path.MOD_NAME, version = "1.3.0", acceptedMinecraftVersions = "[1.9,1.12)")
public class Dirt2Path {

	public static final String MOD_ID = "dirt2path";
	public static final String MOD_NAME = "Dirt2Path";
	public static final ItemStack EMPTY = new ItemStack((Item) null);

	@GameRegistry.ObjectHolder("biomesoplenty:grass_path")
	public static final Block BOP_GRASS_PATH = null;
	@GameRegistry.ObjectHolder("biomesoplenty:dirt")
	public static final Block BOP_DIRT = null;

	@Mod.EventHandler
	public void onInit(FMLInitializationEvent event) {

		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onBlockRightclick(PlayerInteractEvent.RightClickBlock event) {

			EntityPlayer player = event.getEntityPlayer();
			World world = event.getWorld();
			BlockPos blockPos = event.getPos();
			ItemStack itemStack = player.getHeldItem(event.getHand());

			if (itemStack == null || itemStack == EMPTY)
				return;

			if (!itemStack.canHarvestBlock(Blocks.SNOW.getDefaultState()))
				return;

			IBlockState iBlockState = world.getBlockState(blockPos);

			if (world.getBlockState(blockPos.up()).getMaterial() == Material.AIR) {
				if (isBlockDirt(iBlockState)) {
					IBlockState pathState = getPathBlockState(iBlockState);
					setPathOrDirt(world, pathState, blockPos, SoundEvents.ITEM_SHOVEL_FLATTEN, player, itemStack, event.getHand());
				} else if (isBlockPath(iBlockState)) {
					IBlockState dirtState = getDirtBlockState(iBlockState);
					setPathOrDirt(world, dirtState, blockPos, SoundEvents.ITEM_HOE_TILL, player, itemStack, event.getHand());
				}
			}
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
		if(iBlockStateIn.getBlock() == BOP_GRASS_PATH && blockMeta <4) return true;
		return false;
	}

	protected boolean isBlockDirt(IBlockState iBlockStateIn) {
		int blockMeta = iBlockStateIn.getBlock().getMetaFromState(iBlockStateIn);
		if(iBlockStateIn.getBlock() == Blocks.DIRT) return true;
		if(iBlockStateIn.getBlock() == BOP_DIRT && blockMeta < 4) return true;
		return false;
	}

	protected IBlockState getDirtBlockState(IBlockState iBlockState) {
		int blockMeta = iBlockState.getBlock().getMetaFromState(iBlockState);
		if(iBlockState.getBlock() == BOP_GRASS_PATH && blockMeta <4) {
			return BOP_DIRT.getStateFromMeta(blockMeta);
		}
		return Blocks.DIRT.getDefaultState();
	}

	protected IBlockState getPathBlockState(IBlockState iBlockStateIn) {
		int blockMeta = iBlockStateIn.getBlock().getMetaFromState(iBlockStateIn);
		if(iBlockStateIn.getBlock() == BOP_DIRT && blockMeta < 4) {
			return BOP_GRASS_PATH.getStateFromMeta(blockMeta);
		}
		return Blocks.GRASS_PATH.getDefaultState();
	}
}
