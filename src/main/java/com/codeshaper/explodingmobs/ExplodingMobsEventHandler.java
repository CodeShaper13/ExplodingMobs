package com.codeshaper.explodingmobs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import com.codeshaper.explodingmobs.entity.EntityFakePlayer;
import com.codeshaper.explodingmobs.entity.EntityMobPart;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Rotations;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class ExplodingMobsEventHandler {

	private final Method onLivingDeathMethod;

	public ExplodingMobsEventHandler() {
		this.onLivingDeathMethod = ReflectionHelper.findMethod(EntityLivingBase.class, "onDeathUpdate",
				"func_70609_aI");
	}

	@SubscribeEvent
	public void entityDeath(LivingDeathEvent event) {
		EntityLivingBase living = event.getEntityLiving();

		if (!living.world.isRemote) {
			EntityEntry ee = EntityRegistry.getEntry(living.getClass()); // Null for player.
			if ((living instanceof EntityPlayer)) {
				ee = EntityRegistry.getEntry(EntityFakePlayer.class);
			}

			if (!ExplodingMobsConfig.shouldExplode(ee)) {
				return;
			}

			ModelBase model = ((RenderLivingBase<?>) Util.getRendererFromEntity(living)).getMainModel();

			if (model != null && model.boxList != null) {
				float partStartHeight = living.getEyeHeight() / 2;
				for (int i = 0; i < model.boxList.size(); i++) {
					// Summon the part.
					EntityMobPart mobPart = new EntityMobPart(living.world, living.posX, living.posY + partStartHeight,
							living.posZ, living, i);
					living.world.spawnEntity(mobPart);

					// Set the parts motion.
					Random rand = living.getRNG();
					mobPart.setRotation(new Rotations(rand.nextInt(360), rand.nextInt(360), rand.nextInt(360)));
					mobPart.setRotationSpeed(new Rotations(this.getRandomRotSpeed(rand), this.getRandomRotSpeed(rand),
							this.getRandomRotSpeed(rand)));
				}

				// Drop XP.
				living.deathTime = 19;
				try {
					this.onLivingDeathMethod.invoke(living);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					System.out.println("Couldn't spawn XP orbs on entity death.");
					e.printStackTrace();
				}

				// Remove the entity.
				living.setDead();
			}
		}
	}

	private float getRandomRotSpeed(Random rand) {
		return (rand.nextFloat() / 5) + 0.1f;
	}
}
