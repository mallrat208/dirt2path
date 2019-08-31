package com.mr208.d2p;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.HashSet;
import java.util.Set;

@Mod("d2p")
public class Dirt2Path
{
	public static final String MOD_ID = "d2p";
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final String PROTOCOL = Integer.toString(1);
	
	public static SimpleChannel channel = NetworkRegistry
			.ChannelBuilder.named(new ResourceLocation(Dirt2Path.MOD_ID,"main"))
			.clientAcceptedVersions(PROTOCOL::equals)
			.serverAcceptedVersions(PROTOCOL::equals)
			.networkProtocolVersion(() -> PROTOCOL)
			.simpleChannel();
	
	public Dirt2Path()
	{
		CommentedFileConfig ConfigData = CommentedFileConfig.builder(FMLPaths.CONFIGDIR.get().resolve("Dirt2Path.toml"))
				.sync()
				.autosave()
				.writingMode(WritingMode.REPLACE)
				.build();
		
		ConfigData.load();
		
		Config.SPEC.setConfig(ConfigData);
		
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
	}
	
	public static BlockState DIRT_STATE;
	public static BlockState PATH_STATE;
	
	public static Set<Item> blacklistShovels= new HashSet<>();
	
	public static boolean flattenMycelium;
	public static boolean flattenPodzol;
	public static boolean flattenDirt;
	public static boolean flattenCoarseDirt;
	
	public static boolean raisePath;
	public static boolean raiseFarm;
	public static boolean raiseSneaky;
	
	public void setup(FMLCommonSetupEvent event)
	{
		int packetID = 0;
		
		channel.registerMessage(packetID++,
				ServerSettingsMessage.class,
				ServerSettingsMessage::encode,
				ServerSettingsMessage::decode,
				ServerSettingsMessage.Handler::handle);
	}
	
	public void loadComplete(FMLLoadCompleteEvent event)
	{
		DIRT_STATE = Blocks.DIRT.getDefaultState();
		PATH_STATE = Blocks.GRASS_PATH.getDefaultState();
		
		flattenCoarseDirt = Config.blocks.coarse_dirt.get();
		flattenDirt = Config.blocks.dirt.get();
		flattenMycelium = Config.blocks.mycelium.get();
		flattenPodzol = Config.blocks.podzol.get();
		
		raisePath = Config.options.raise_path.get();
		raiseSneaky = Config.options.raise_path.get();
		raiseFarm = Config.options.raise_farm.get();
		

		for(String shovel:Config.shovels.shovels_blacklist.get())
		{
			Item temp =ForgeRegistries.ITEMS.getValue(new ResourceLocation(shovel));
			if(temp!=Items.AIR)
				blacklistShovels.add(temp);
		}
	}
}
