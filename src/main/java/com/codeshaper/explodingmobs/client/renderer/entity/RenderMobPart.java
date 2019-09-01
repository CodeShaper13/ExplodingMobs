package com.codeshaper.explodingmobs.client.renderer.entity;

import java.lang.reflect.Method;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import org.lwjgl.opengl.GL11;

import com.codeshaper.explodingmobs.ExplodingMobs;
import com.codeshaper.explodingmobs.Util;
import com.codeshaper.explodingmobs.entity.EntityFakePlayer;
import com.codeshaper.explodingmobs.entity.EntityMobPart;

public class RenderMobPart<T extends Entity> extends Render<EntityMobPart> {

	private static final ResourceLocation ERROR_TEXTURES = new ResourceLocation(ExplodingMobs.MOD_ID, "textures/entity/error.png");

	public RenderMobPart(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityMobPart entity, double x, double y, double z, float entityYaw, float partialTicks) {
		EntityLivingBase targetEntity = entity.getTargetEntity();
		if(targetEntity == null) {
			return;
		}
				
		try {
			RenderLivingBase<EntityLivingBase> render = (RenderLivingBase<EntityLivingBase>) Util.getRendererFromEntity(targetEntity);
			if(render == null) {
				Util.logErr("ERROR!  Could not get the renderer of " + targetEntity.getName() + "!");
			} else {
				int partIndex = entity.getPartIndex();

				ModelBase entityModel;

				if(render instanceof RenderFakePlayer) {					
					Map<String, RenderPlayer> map = Minecraft.getMinecraft().getRenderManager().getSkinMap();
					EntityFakePlayer fakePlayer = (EntityFakePlayer)targetEntity;
					entityModel = map.get(fakePlayer.getModelType()).getMainModel();
				} else {
					entityModel = render.getMainModel();
				}
				
				if (entityModel != null && entityModel.boxList != null && partIndex < entityModel.boxList.size()) {
					GlStateManager.pushMatrix();
					
					GlStateManager.translate((float) x, (float) y, (float) z);
					this.bindEntityTexture(entity);

					// Fade the part if it will soon disappear.
					boolean showFadeEffect = (entity.age < 40f);
					if (showFadeEffect) {
						GL11.glEnable(GL11.GL_BLEND);
						GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
						float alpha = entity.age / 60f;
						GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
					}

					// Scale the model.
					render.prepareScale((EntityLivingBase)targetEntity, partialTicks);
					
					// Reset the translation that prepareScale moved.
					GlStateManager.translate(0.0f, 1.501f, 0.0f);

					// Render the part.
					ModelRenderer part = ((ModelRenderer) entityModel.boxList.get(partIndex));

					// Save the old rotations to restore later
					float oldX = part.rotateAngleX;
					float oldY = part.rotateAngleY;
					float oldZ = part.rotateAngleZ;

					// Set the parts rotation equal to the entity's.
					part.rotateAngleX = entity.getRotation().getX();
					part.rotateAngleY = entity.getRotation().getY();
					part.rotateAngleZ = entity.getRotation().getZ();

					// Render the part.
					part.render(entityYaw);

					// Restore the part's rotations, that way the model doesn't look all funny next time it's used.
					part.rotateAngleX = oldX;
					part.rotateAngleY = oldY;
					part.rotateAngleZ = oldZ;

					// Restore the GL11 state.
					if (showFadeEffect) {
						GL11.glDisable(GL11.GL_BLEND);
						GL11.glColor4f(1, 1, 1, 1);
					}

					GlStateManager.popMatrix();
				} else {
					Util.logErr("ERROR!  Could not get the model part corresponding to this part!");
				}
			}
		} catch (Exception e) {
			Util.logErr("ERROR!  Problem rendering MobPart based from " + targetEntity.getName());
			e.printStackTrace();
		}

		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Override
	@Nonnull
	protected ResourceLocation getEntityTexture(EntityMobPart mobPart) {
		EntityLivingBase partTarget = (EntityLivingBase) mobPart.getTargetEntity();
		RenderLivingBase<EntityLiving> entityRenderer = ((RenderLivingBase<EntityLiving>) Util.getRendererFromEntity(partTarget));
		return this.getOtherEntityTexture(entityRenderer, partTarget);			
	}
	
	@Override
	protected boolean bindEntityTexture(EntityMobPart entity) {
		return super.bindEntityTexture(entity);
	}

	/**
	 * Looks up the texture of the passed entity.
	 */
	@Nonnull
	private ResourceLocation getOtherEntityTexture(RenderLivingBase<EntityLiving> targetRenderer, EntityLivingBase targetEntity) {
		Method method;
		if (!ExplodingMobs.rendererToMethod.containsKey(targetRenderer)) {
			method = ReflectionHelper.findMethod(Render.class, "getEntityTexture", "func_110775_a", Entity.class);
			ExplodingMobs.rendererToMethod.put(targetRenderer, method);
		} else {
			method = ExplodingMobs.rendererToMethod.get(targetRenderer);
		}

		try {
			return (ResourceLocation) method.invoke(targetRenderer, targetEntity);
		} catch (Exception e) {
			System.err.println("ERROR! Entity texture could not be found!");
			e.printStackTrace();
			return RenderMobPart.ERROR_TEXTURES;
		}
	}
}
