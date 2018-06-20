package com.codeshaper.explodingmobs;

import java.lang.reflect.Method;
import java.util.HashMap;

import com.codeshaper.explodingmobs.entity.EntityFakePlayer;
import com.codeshaper.explodingmobs.entity.EntityMobPart;
import com.codeshaper.explodingmobs.proxy.IProxy;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

@EventBusSubscriber

@Mod(modid = ExplodingMobs.MOD_ID, version = ExplodingMobs.VERSION, updateJSON = "https://raw.githubusercontent.com/RangerPJ/ExplodingMobs/master/src/main/resources/assets/explodingmobs/updates.json")
public class ExplodingMobs {

	public static final String MOD_ID = "explodingmobs";
	public static final String VERSION = "v1.3";

	public static final String ID_FAKE_PLAYER = "fake_player";
	public static final String ID_MOB_PART = "mob_part";

	/**
	 * Hashmap to hold the methods to retrieve the texture from an entity. We save a
	 * reference to methods and not textures because the texture will vary depending
	 * on the entity we pass in (Entity is saved with the part entity).
	 */
	public static HashMap<RenderLiving<EntityLiving>, Method> rendererToMethod;

	@SidedProxy(clientSide = "com.codeshaper.explodingmobs.proxy.ProxyClient", serverSide = "com.codeshaper.explodingmobs.proxy.ProxyServer")
	public static IProxy proxy;

	@Instance(MOD_ID)
	public static ExplodingMobs instance;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ExplodingMobsConfig.readConfig(event);
		ExplodingMobs.rendererToMethod = new HashMap<>();
		ExplodingMobs.proxy.registerRendering();
	}

	@SubscribeEvent
	public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
		EntityEntry entry = EntityEntryBuilder.create().entity(EntityMobPart.class)
				.id(new ResourceLocation(ExplodingMobs.MOD_ID, ExplodingMobs.ID_MOB_PART), 0)
				.name(ExplodingMobs.ID_MOB_PART).tracker(64, 20, true).build();
		EntityEntry entry1 = EntityEntryBuilder.create().entity(EntityFakePlayer.class)
				.id(new ResourceLocation(ExplodingMobs.MOD_ID, ExplodingMobs.ID_FAKE_PLAYER), 1)
				.name(ExplodingMobs.ID_FAKE_PLAYER).tracker(64, 20, false).build();

		event.getRegistry().register(entry);
		event.getRegistry().register(entry1);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new ExplodingMobsEventHandler());
	}
}
