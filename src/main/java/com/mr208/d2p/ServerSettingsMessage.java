package com.mr208.d2p;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class ServerSettingsMessage
{
	private boolean m_raisePath;
	private boolean m_raiseFarm;
	private boolean m_mycelium;
	private boolean m_podzol;
	private boolean m_dirt;
	private boolean m_coarse_dirt;
	
	public ServerSettingsMessage(Config.Blocks blocks,  Config.Options options)
	{
		m_raiseFarm = options.raise_farm.get();
		m_raisePath = options.raise_path.get();
		m_mycelium = blocks.mycelium.get();
		m_podzol = blocks.mycelium.get();
		m_dirt = blocks.mycelium.get();
		m_coarse_dirt = blocks.coarse_dirt.get();
	}
	
	public ServerSettingsMessage(boolean coarse_dirt, boolean dirt, boolean mycelium, boolean podzol, boolean raisepath, boolean raisefarm)
	{
		m_raisePath = raisepath;
		m_mycelium = mycelium;
		m_podzol = podzol;
		m_dirt = dirt;
		m_coarse_dirt = coarse_dirt;
		m_raiseFarm = raisefarm;
	}
	
	public static void encode(ServerSettingsMessage msg, PacketBuffer buf)
	{
		buf.writeBoolean(msg.m_coarse_dirt);
		buf.writeBoolean(msg.m_dirt);
		buf.writeBoolean(msg.m_mycelium);
		buf.writeBoolean(msg.m_podzol);
		buf.writeBoolean(msg.m_raisePath);
		buf.writeBoolean(msg.m_raiseFarm);
	}
	
	public static ServerSettingsMessage decode(PacketBuffer buf)
	{
		return new ServerSettingsMessage(buf.readBoolean(), buf.readBoolean(),buf.readBoolean(),buf.readBoolean(),buf.readBoolean(), buf.readBoolean());
	}
	
	public static class Handler
	{
		public static void handle(final ServerSettingsMessage msg, Supplier<Context> ctx)
		{
			ctx.get().enqueueWork(() -> {
				Dirt2Path.flattenCoarseDirt = msg.m_coarse_dirt;
				Dirt2Path.flattenDirt = msg.m_dirt;
				Dirt2Path.flattenMycelium = msg.m_mycelium;
				Dirt2Path.flattenPodzol = msg.m_podzol;
				Dirt2Path.raisePath = msg.m_raisePath;
				Dirt2Path.raiseFarm = msg.m_raiseFarm;
			});
			
			ctx.get().setPacketHandled(true);
		}
	}
}
