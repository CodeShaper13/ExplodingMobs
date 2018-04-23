package com.codeshaper.explodingmobs.client.renderer.entity;

import com.codeshaper.explodingmobs.entity.EntityFakePlayer;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderFakePlayer extends RenderLiving<EntityFakePlayer> {

	public RenderFakePlayer(RenderManager manager) {
		super(manager, null, 0.5f);
	}

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless
	 * you call Render.bindEntityTexture.
	 */
	@Override
	protected ResourceLocation getEntityTexture(EntityFakePlayer entity) {
		return entity.getTexture();
	}
}
