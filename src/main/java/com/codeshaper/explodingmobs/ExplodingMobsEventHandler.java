package com.codeshaper.explodingmobs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import javax.annotation.Nullable;

import com.codeshaper.explodingmobs.entity.EntityFakePlayer;
import com.codeshaper.explodingmobs.entity.EntityMobPart;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
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

	/**
	 * Reference to the {@link EntityLivingBase.onDeathUpdate} method. This is
	 * called to spawn the XP orbs when an Entity is killed and parts are killed.
	 */
	@Nullable
	private final Method onLivingDeathMethod;

	public ExplodingMobsEventHandler() {
		this.onLivingDeathMethod = ReflectionHelper.findMethod(EntityLivingBase.class, "onDeathUpdate",
				"func_70609_aI");
	}

	@SubscribeEvent
	public void entityDeath(LivingDeathEvent event) {
		EntityLivingBase living = event.getEntityLiving();

		if (living.world.isRemote) {
			// This event is happening on the client side, ignore.
		} else {
			// Check if the Entity should explode. If not, control is returned.
			EntityEntry ee;
			if ((living instanceof EntityPlayer)) {
				ee = EntityRegistry.getEntry(EntityFakePlayer.class);
			} else {
				ee = EntityRegistry.getEntry(living.getClass()); // Null for Players or if the killed entity was not
																	// register (bad and shouldn't happen).
			}

			if (!ExplodingMobsConfig.shouldExplode(ee)) {
				return; // Do not explode the Entity.
			}

			// Explode the Entity.

			// Gets the renderer, continuing if one is found.
			Render<?> r = Util.getRendererFromEntity(living);
			if (r == null) {
				Util.log("Could not find a Render class for Entity with name " + ee.getName()
						+ ".  No Parts will be spawned.");
			}

			if (r instanceof RenderLivingBase<?>) {
				ModelBase model = ((RenderLivingBase<?>) r).getMainModel();

				if (model != null && model.boxList != null) {
					float partStartHeight = living.getEyeHeight() / 2;
					for (int i = 0; i < model.boxList.size(); i++) {
						// Summon the part.
						EntityMobPart mobPart = new EntityMobPart(living.world, living.posX,
								living.posY + partStartHeight, living.posZ, living, i);
						living.world.spawnEntity(mobPart);

						// Set the parts motion.
						Random rand = living.getRNG();
						mobPart.setRotation(new Rotations(rand.nextInt(360), rand.nextInt(360), rand.nextInt(360)));
						mobPart.setRotationSpeed(new Rotations(this.getRndRotSpeed(rand), this.getRndRotSpeed(rand),
								this.getRndRotSpeed(rand)));
					}

					// Drop XP.
					living.deathTime = 19;

					try {
						if (this.onLivingDeathMethod != null) {
							this.onLivingDeathMethod.invoke(living);
						} else {
							throw new NullPointerException(
									"Tried to call EntityLivingBase.onDeathUpdate, but the method could not be found via reflection!");
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
							| NullPointerException e) {
						Util.log("Unable spawn XP orbs on Entity death.  See stack trace for more on error.");
						e.printStackTrace();
					}

					// Remove the entity.
					living.setDead();
				}
			} else {
				// No error needed, this is "normal" as an Entities that fire this event will
				// not always have Render classes that subclass RenderLivingBase (but most,
				// including all vanilla classes, do).
			}
		}
	}

	/**
	 * Generates a random speed for the part to rotate.
	 * 
	 * @param rand
	 *            A random number generator.
	 * @return The speed as a float.
	 */
	private float getRndRotSpeed(Random rand) {
		return (rand.nextFloat() / 5) + 0.1f;
	}
}
