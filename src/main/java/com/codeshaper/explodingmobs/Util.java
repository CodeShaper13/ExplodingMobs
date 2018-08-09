package com.codeshaper.explodingmobs;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
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
	 * Converts and entity into a JSON string.
	 */
	@Nullable
	public static String entityToString(Entity entity) {
		NBTTagCompound tag;
		if(entity instanceof EntityPlayer) {
			NBTTagCompound ret = new NBTTagCompound();
	        ret.setString("id", new ResourceLocation(ExplodingMobs.MOD_ID, ExplodingMobs.ID_FAKE_PLAYER).toString());
	        tag = entity.writeToNBT(ret);
		} else {
			tag = entity.serializeNBT();			
		}
		return tag.toString();	        
	}

	/**
	 * Converts a JSON NBT string into an entity.
	 */
	@Nullable
	public static EntityLivingBase stringToEntity(World world, String jsonString) {
		NBTTagCompound nbt;
		try {
			nbt = JsonToNBT.getTagFromJson(jsonString);
		} catch (NBTException ex) {
			ex.printStackTrace();
			return null;
		}
		return (EntityLivingBase) EntityList.createEntityFromNBT(nbt, world);
	}

	/**
	 * Returns the render for the passes entity, or null if this is a dedicated
	 * server.
	 */
	@Nullable
	public static Render<?> getRendererFromEntity(@Nullable Entity entity) {
		RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
		if (renderManager == null) {
			Util.log("The RenderManager is null, is this a dedicated server?  There will be problems!!!");
			return null;
		} else {
			if(entity instanceof EntityPlayer) {
				try {
					String s = Minecraft.getMinecraft().getConnection().getPlayerInfo(entity.getUniqueID()).getSkinType();
					return renderManager.getSkinMap().get(s);
				} catch (Exception e) {
					Util.log("While attempting to explode a player there was a problem looking up critical model and skin information.  Using the default model.  Exception stack trace will follow.");
					e.printStackTrace();
					return renderManager.getSkinMap().get("default");
				}
			} else {
				return renderManager.getEntityRenderObject(entity);					
			}
		}
	}
	
	public static void log(Object message) {
		System.out.println("ExplodingMobs ERROR: " + message);
	}
	
	public static void warn(Object message) {
		System.out.println("ExplodingMobs WARNING: " + message);
	}
}