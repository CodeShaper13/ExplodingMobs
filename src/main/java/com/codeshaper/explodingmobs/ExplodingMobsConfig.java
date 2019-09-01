package com.codeshaper.explodingmobs;

import javax.annotation.Nullable;

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
	private static int despawnTime;
	// Doesn't work!
	private static double renderRange;

	public static void readConfig(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();

		ExplodingMobsConfig.useMcMobsOnly = config.get(Configuration.CATEGORY_GENERAL, "only_allow_mc_mobs", false)
				.getBoolean();
		ExplodingMobsConfig.explodePlayers = config.get(Configuration.CATEGORY_GENERAL, "explode_players", false)
				.getBoolean();
		ExplodingMobsConfig.despawnTime = config.get(Configuration.CATEGORY_GENERAL, "despawn_time", 100)
				.getInt();
		//ExplodingMobsConfig.renderRange = config.get(Configuration.CATEGORY_GENERAL, "render_range", 1)
		//		.getDouble();

		if (config.hasChanged()) {
			config.save();
		}
	}

	/**
	 * Checks if the passed entity entry should explode based on the config.
	 * 
	 * @param ee
	 *            The {@link EntityEntry} that corresponds to the entity is
	 *            question.
	 * @return True if the entity should explode, false if it should not.
	 */
	public static boolean shouldExplode(@Nullable EntityEntry ee) {
		if (ee == null) {
			return false;
		}

		if (ee.getEntityClass() == EntityFakePlayer.class && ExplodingMobsConfig.explodePlayers == false) {
			return false;
		}

		if (ExplodingMobsConfig.useMcMobsOnly) {
			ResourceLocation rl = ForgeRegistries.ENTITIES.getKey(ee);
			return rl.getResourceDomain().equals("minecraft") || rl.getResourceDomain().equals(ExplodingMobs.MOD_ID);
		} else {
			return true;
		}
	}

	/**
	 * Gets the time in seconds it should take for a part to disappear.
	 */
	public static int getDespawnTime() {
		return ExplodingMobsConfig.despawnTime;
	}
	
	/**
	 * Gets the distance that a part is visible.
	 */
	public static double getRenderRange() {
		return ExplodingMobsConfig.renderRange;
	}
}
