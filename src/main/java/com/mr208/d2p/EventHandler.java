package com.mr208.d2p;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.network.NetworkDirection;

@EventBusSubscriber()
public class EventHandler
{
	@OnlyIn(Dist.DEDICATED_SERVER)
	@SubscribeEvent
	public static void onServerConnect(PlayerLoggedInEvent loggedInEvent)
	{
		Dirt2Path.channel.sendTo(
				new ServerSettingsMessage(
						Config.blocks,
						Config.options),
				((ServerPlayerEntity)loggedInEvent.getPlayer()).connection.netManager,
				NetworkDirection.PLAY_TO_CLIENT);
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onServerStopping(FMLServerStoppingEvent serverStoppingEvent)
	{
		Dirt2Path.raiseFarm = Config.options.raise_farm.get();
		Dirt2Path.raisePath = Config.options.raise_path.get();
		Dirt2Path.flattenCoarseDirt = Config.blocks.coarse_dirt.get();
		Dirt2Path.flattenDirt = Config.blocks.dirt.get();
		Dirt2Path.flattenMycelium = Config.blocks.mycelium.get();
		Dirt2Path.flattenPodzol = Config.blocks.podzol.get();
	}
	
	@SubscribeEvent
	public static void onRightClickBlock(RightClickBlock event)
	{
		if(event.getResult() != Result.DEFAULT || event.isCanceled())
			return;
		
		PlayerEntity player = event.getPlayer();
		ItemStack itemStack = player.getHeldItem(event.getHand());
		
		if(itemStack.isEmpty())
			return;
		
		if(!(itemStack.getToolTypes().contains(ToolType.SHOVEL)) || Dirt2Path.blacklistShovels.contains(itemStack.getItem()))
			return;
		
		World world = event.getWorld();
		BlockPos pos = event.getPos();
		BlockState state = world.getBlockState(pos);
		
		if(world.getBlockState(pos.up()).getMaterial()==Material.AIR)
		{
			if((!Dirt2Path.raiseSneaky || player.isSneaking()) && isBlockstatePath(state))
			{
				setState(world, Dirt2Path.DIRT_STATE, pos, SoundEvents.ITEM_HOE_TILL, player, itemStack, event.getHand());
			}
			else if(isBlockstateDirt(state))
			{
				setState(world, Dirt2Path.PATH_STATE, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, player, itemStack, event.getHand());
			}
		}
	}
	
	private static void setState(World worldIn, BlockState blockStateIn, BlockPos posIn, SoundEvent soundEventIn, PlayerEntity playerEntity, ItemStack itemStack, Hand hand)
	{
		worldIn.playSound(playerEntity, posIn, soundEventIn, SoundCategory.BLOCKS, 1.0f, 1.0f);
		playerEntity.swingArm(hand);
		if(!worldIn.isRemote)
		{
			worldIn.setBlockState(posIn,blockStateIn,11);
			if(!playerEntity.isCreative())
			{
				itemStack.attemptDamageItem(1, worldIn.rand, (ServerPlayerEntity)playerEntity);
			}
		}
	}
	
	private static boolean isBlockstateDirt(BlockState blockStateIn)
	{
		if(blockStateIn.getBlock()==Blocks.DIRT)
			return true;
		if(Dirt2Path.flattenCoarseDirt && blockStateIn.getBlock()==Blocks.COARSE_DIRT)
			return true;
		if(Dirt2Path.flattenMycelium && blockStateIn.getBlock()==Blocks.MYCELIUM)
			return true;
		if(Dirt2Path.flattenPodzol && blockStateIn.getBlock()==Blocks.PODZOL)
			return true;
		
		return false;
	}
	
	private static boolean isBlockstatePath(BlockState blockStateIn)
	{
		if(Dirt2Path.raisePath && blockStateIn.getBlock()==Blocks.GRASS_PATH)
			return true;
		if(Dirt2Path.raiseFarm && blockStateIn.getBlock()==Blocks.FARMLAND)
			return true;
		
		return false;
		
	}
}
