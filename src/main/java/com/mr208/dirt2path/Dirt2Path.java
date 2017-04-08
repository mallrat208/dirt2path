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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = Dirt2Path.MOD_ID, name = Dirt2Path.MOD_NAME, version = "1.1.0", acceptedMinecraftVersions = "[1.9,1.12)")
public class Dirt2Path {

	public static final String MOD_ID = "dirt2path";
	public static final String MOD_NAME = "Dirt2Path";
	public static final ItemStack EMPTY = new ItemStack((Item) null);

	@Mod.EventHandler
	public void onInit(FMLInitializationEvent event) {

		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onBlockRightclick(PlayerInteractEvent.RightClickBlock event) {

		EntityPlayer player = event.getEntityPlayer();
		World world = event.getWorld();
		BlockPos blockPos = event.getPos();
		ItemStack itemStack = player.getHeldItemMainhand();

		if(itemStack==null || itemStack==EMPTY) return;

		if(!itemStack.canHarvestBlock(Blocks.SNOW.getDefaultState())) return;

		IBlockState iBlockState = world.getBlockState(blockPos);
		Block block = iBlockState.getBlock();

		if(world.getBlockState(blockPos.up()).getMaterial() == Material.AIR && block == Blocks.DIRT) {
			IBlockState pathState = Blocks.GRASS_PATH.getDefaultState();
			world.playSound(player, blockPos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);

			player.swingArm(EnumHand.MAIN_HAND);

			if(!world.isRemote) {

				world.setBlockState(blockPos, pathState, 11);
				itemStack.damageItem(1, player);
			}
		}
	}
}
