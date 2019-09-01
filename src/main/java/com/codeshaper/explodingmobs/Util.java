package com.codeshaper.explodingmobs;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * General utility methods, not static for any real reason or separated, other
 * than we can see that the method isn't dependent on a class, if we want to
 * copy this easily to another mod.
 */
public class Util {

	/**
	 * Converts an Entity into a JSON string.
	 */
	@Nullable
	public static String entityToString(Entity entity) {
		NBTTagCompound tag;
		if (entity instanceof EntityPlayer) {
			NBTTagCompound ret = new NBTTagCompound();
			ret.setString("id", new ResourceLocation(ExplodingMobs.MOD_ID, ExplodingMobs.ID_FAKE_PLAYER).toString());
			tag = entity.writeToNBT(ret);
		} else {
			tag = entity.serializeNBT();
		}
		return tag.toString();
	}

	/**
	 * Converts a JSON NBT string into an Entity.
	 * 
	 * @param world
	 * @param jsonString
	 * @return The entity, or null on error.
	 */
	@Nullable
	public static Entity stringToEntity(World world, String jsonString) {
		NBTTagCompound nbt;
		try {
			nbt = JsonToNBT.getTagFromJson(jsonString);
		} catch (NBTException ex) {
			ex.printStackTrace();
			return null;
		}
		return EntityList.createEntityFromNBT(nbt, world);
	}

	/**
	 * Gets the {@link Render} that the passed Entity uses.
	 * 
	 * @param entity
	 * @return Returns the render for the passes entity. Null is returned if this is
	 *         a dedicated server.
	 */
	@Nullable
	public static Render getRendererFromEntity(@Nullable Entity entity) {
		RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
		if (renderManager == null) {
			Util.logErr("The RenderManager is null, is this a Dedicated Server?  There will be problems down the road!");
			return null;
		} else {
			if (entity instanceof EntityPlayer) {
				return Util.getPlayerRenderer((EntityPlayer) entity);
			} else {
				return renderManager.getEntityRenderObject(entity);
			}
		}
	}

	/**
	 * Gets the model of a Player based on their skin settings online.
	 * 
	 * @param player
	 * @return The model of the player. On error, the Steve model is returned.
	 */
	public static RenderPlayer getPlayerRenderer(EntityPlayer player) {
		RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

		// This is a player, the model depends on the player's skin settings online.
		try {
			String s = Minecraft.getMinecraft().getConnection().getPlayerInfo(player.getUniqueID()).getSkinType();
			return renderManager.getSkinMap().get(s);
		} catch (Exception e) {
			Util.logErr(
					"Could not look up the model of a Player from online, using the default model.  Exception stack trace will follow...");
			e.printStackTrace();
			return renderManager.getSkinMap().get("default");
		}
	}

	public static void logErr(Object message) {
		System.err.println("ExplodingMobs ERROR: " + message);
	}

	public static void logWarn(Object message) {
		System.out.println("ExplodingMobs WARNING: " + message);
	}
}