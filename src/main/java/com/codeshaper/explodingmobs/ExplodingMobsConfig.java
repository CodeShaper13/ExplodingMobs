package com.codeshaper.explodingmobs;

import com.codeshaper.explodingmobs.entity.EntityFakePlayer;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ExplodingMobsConfig {

	/**
	 * If true, only vanilla mobs will explode.
	 */
	private static boolean useMcMobsOnly;
	private static boolean explodePlayers;

	public static void readConfig(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();

		ExplodingMobsConfig.useMcMobsOnly = config.get(Configuration.CATEGORY_GENERAL, "only_allow_mc_mobs", false).getBoolean();
		ExplodingMobsConfig.explodePlayers = config.get(Configuration.CATEGORY_GENERAL, "explode_players", false).getBoolean();
		
		if (config.hasChanged()) {
			config.save();
		}
	}
	
	/**
	 * Checks if the passed entity entry should explode based on the config.
	 */
	public static boolean shouldExplode(EntityEntry ee) {
		if(ee.getEntityClass() == EntityFakePlayer.class && ExplodingMobsConfig.explodePlayers == false) {
			return false;
		}
		
		if(ExplodingMobsConfig.useMcMobsOnly) {
			ResourceLocation rl = ForgeRegistries.ENTITIES.getKey(ee);
			return rl.getResourceDomain().equals("minecraft") || rl.getResourceDomain().equals(ExplodingMobs.MOD_ID);
		} else {
			return true;
		}
	}
	
	public boolean shouldExplodePlayers() {
		return ExplodingMobsConfig.explodePlayers;
	}
}
