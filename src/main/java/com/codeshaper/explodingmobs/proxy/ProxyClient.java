package com.codeshaper.explodingmobs.proxy;

import com.codeshaper.explodingmobs.client.renderer.entity.RenderFakePlayer;
import com.codeshaper.explodingmobs.client.renderer.entity.RenderMobPart;
import com.codeshaper.explodingmobs.entity.EntityFakePlayer;
import com.codeshaper.explodingmobs.entity.EntityMobPart;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ProxyClient implements IProxy {
	
	@Override
	public void registerRendering() {
		RenderingRegistry.registerEntityRenderingHandler(EntityMobPart.class, new IRenderFactory<EntityMobPart>() {
			@Override
			public Render<EntityMobPart> createRenderFor(RenderManager manager) {
				return new RenderMobPart(manager);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityFakePlayer.class, new IRenderFactory<EntityFakePlayer>() {
			@Override
			public Render<EntityFakePlayer> createRenderFor(RenderManager manager) {
				return new RenderFakePlayer(manager);
			}
		});
	}
}
