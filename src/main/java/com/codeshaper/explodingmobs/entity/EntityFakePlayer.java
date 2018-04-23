package com.codeshaper.explodingmobs.entity;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityFakePlayer extends EntityLiving {

	private NetworkPlayerInfo playerInfo;

	public EntityFakePlayer(World worldIn) {
		super(worldIn);
	}

	/**
	 * Returns the texture to use for rendering.
	 */
	public ResourceLocation getTexture() {
		NetworkPlayerInfo info = this.getPlayerInfo();
		return info == null ? DefaultPlayerSkin.getDefaultSkin(this.getUniqueID()) : info.getLocationSkin();
	}

	/**
	 * Returns the String identifier of the model to render with, default or slim.
	 */
	public String getModelType() {
		return this.getPlayerInfo().getSkinType();
	}

	@Nullable
	private NetworkPlayerInfo getPlayerInfo() {
		if (this.playerInfo == null) {
			this.playerInfo = Minecraft.getMinecraft().getConnection().getPlayerInfo(this.getUniqueID());
		}
		return this.playerInfo;
	}
}
