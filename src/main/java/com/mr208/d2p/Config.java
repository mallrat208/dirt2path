package com.mr208.d2p;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;

import java.util.ArrayList;
import java.util.List;

public class Config
{
	protected static final ForgeConfigSpec.Builder BUILDER = new Builder();
	
	public static final Shovels shovels = new Shovels();
	public static final Blocks blocks = new Blocks();
	public static final Options options = new Options();
	
	public static class Shovels
	{
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> shovels_blacklist;
		
		private ArrayList<String> shovelsBlaclistDef=Lists.newArrayList();
		
		
		Shovels()
		{
			BUILDER.push("Shovels");
			shovels_blacklist= BUILDER
					.comment("By default, all items with the Shovel tool class work with Dirt2Path","Add the registry name of a Shovel here to blacklist it from working with Dirt2Path.","Only 1 Entry per Line, no Commas")
					.defineList("Blacklisted Shovels", shovelsBlaclistDef, entry -> entry instanceof String);
			BUILDER.pop();
		}
	}
	
	public static class Blocks
	{
		public final ForgeConfigSpec.ConfigValue<Boolean> mycelium;
		public final ForgeConfigSpec.ConfigValue<Boolean> podzol;
		public final ForgeConfigSpec.ConfigValue<Boolean> dirt;
		public final ForgeConfigSpec.ConfigValue<Boolean> coarse_dirt;
		
		Blocks()
		{
			BUILDER.push("Blocks");
			mycelium = BUILDER
					.comment("Mycelium can be turned into a Path.","Synced from Server")
					.define("Flatten Mycelium", true);
			podzol = BUILDER
					.comment("Podzol can be turned into a Path","Synced from Server")
					.define("Flatten Podzol", true);
			dirt = BUILDER
					.comment("Dirt can be turned into a Path","Synced from Server")
					.define("Flatten Dirt", true);
			coarse_dirt= BUILDER
					.comment("Coarse Dirt can be turned into a Path","Synced from Server")
					.define("Flatten Coarse Dirt", true);
			BUILDER.pop();
			
		}
	}
	
	public static class Options
	{
		public final ForgeConfigSpec.ConfigValue<Boolean> raise_path;
		public final ForgeConfigSpec.ConfigValue<Boolean> raise_farm;
		public final ForgeConfigSpec.ConfigValue<Boolean> raise_sneaking;
		
		Options()
		{
			BUILDER.push("Options");
			raise_path = BUILDER
					.comment("Turn Path blocks back into Dirt","Synced from Server")
					.define("Path to Dirt", true);
			raise_farm = BUILDER
					.comment("Turn Farmland blocks back into Dirt","Synced from Server")
					.define("Farmland to Dirt", true);
			raise_sneaking = BUILDER
					.comment("Turning Path blocks to Dirt requries Sneaking")
					.define("Sneak to Remove Paths", true);
			
			BUILDER.pop();
		}
	}
	
	protected static final ForgeConfigSpec SPEC = BUILDER.build();
}
