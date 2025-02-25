package noppes.npcs.client.renderer;

import java.io.File;
import java.security.MessageDigest;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomRegisters;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.client.Client;
import noppes.npcs.client.ImageDownloadAlt;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

public class RenderNPCInterface<T extends EntityNPCInterface>
extends RenderLiving<T> {
	
	private static final DynamicTexture TEXTURE_BRIGHTNESS = new DynamicTexture(16, 16);
	public static int LastTextureTick;

	public RenderNPCInterface(ModelBase model, float f) {
		super(Minecraft.getMinecraft().getRenderManager(), model, f);
	}

	protected void applyRotations(T npc, float f, float f1, float f2) {
		if (npc.isEntityAlive() && npc.isPlayerSleeping()) {
			GlStateManager.rotate(npc.ais.orientation, 0.0f, 1.0f, 0.0f);
			GlStateManager.rotate(this.getDeathMaxRotation(npc), 0.0f, 0.0f, 1.0f);
			GlStateManager.rotate(270.0f, 0.0f, 1.0f, 0.0f);
		} else if (npc.isEntityAlive() && npc.currentAnimation == 7) {
			GlStateManager.rotate(270.0f - f1, 0.0f, 1.0f, 0.0f);
			float scale = ((EntityCustomNpc) npc).display.getSize() / 5.0f;
			GlStateManager.translate(-scale + ((EntityCustomNpc) npc).modelData.getLegsY() * scale, 0.14f, 0.0f);
			GlStateManager.rotate(270.0f, 0.0f, 0.0f, 1.0f);
			GlStateManager.rotate(270.0f, 0.0f, 1.0f, 0.0f);
		} else {
			super.applyRotations(npc, f, f1, f2);
		}
	}

	public void doRender(T npc, double x, double y, double z, float entityYaw, float partialTicks) {
		if (!CustomNpcs.EnableInvisibleNpcs && npc.display.getVisible()==1) {
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			if (!player.capabilities.isCreativeMode && player.getHeldItemMainhand().getItem()!=CustomRegisters.wand) { return; }
		}
		if (npc.isKilled() && npc.stats.hideKilledBody && npc.deathTime > 20) { return; }
		if (npc.ais.getStandingType() == 3 && !npc.isWalking() && !npc.isInteracting()) {
			float n = npc.ais.orientation;
			npc.renderYawOffset = n;
			npc.prevRenderYawOffset = n;
		}
		try {
			GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
			super.doRender(npc, x, y, z, entityYaw, partialTicks);
		} catch (Throwable t) { }
	}

	public void doRenderShadowAndFire(Entity par1Entity, double par2, double par4, double par6, float par8, float par9) {
		EntityNPCInterface npc = (EntityNPCInterface) par1Entity;
		this.shadowSize = npc.width / 1.25f;
		if (!npc.isKilled()) {
			super.doRenderShadowAndFire(par1Entity, par2, par4, par6, par8, par9);
		}
	}

	public ResourceLocation getEntityTexture(T npc) {
		if (npc.textureLocation == null) {
			if (npc.display.skinType == 0) {
				npc.textureLocation = new ResourceLocation(npc.display.getSkinTexture());
			} else {
				if (RenderNPCInterface.LastTextureTick < 5) {
					return DefaultPlayerSkin.getDefaultSkinLegacy();
				}
				if (npc.display.skinType == 1 && npc.display.playerProfile != null) {
					Minecraft minecraft = Minecraft.getMinecraft();
					Map<Type, MinecraftProfileTexture> map = minecraft.getSkinManager()
							.loadSkinFromCache(npc.display.playerProfile);
					if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
						npc.textureLocation = minecraft.getSkinManager().loadSkin(
								(MinecraftProfileTexture) map.get(MinecraftProfileTexture.Type.SKIN),
								MinecraftProfileTexture.Type.SKIN);
					}
				} else if (npc.display.skinType == 2) {
					try {
						MessageDigest digest = MessageDigest.getInstance("MD5");
						byte[] hash = digest.digest(npc.display.getSkinUrl().getBytes("UTF-8"));
						StringBuilder sb = new StringBuilder(2 * hash.length);
						for (byte b : hash) {
							sb.append(String.format("%02x", b & 0xFF));
						}
						this.loadSkin(null, npc.textureLocation = new ResourceLocation("skins/" + sb.toString()),
								npc.display.getSkinUrl());
					} catch (Exception ex) {
					}
				}
			}
		}
		if (npc.textureLocation == null) {
			return DefaultPlayerSkin.getDefaultSkinLegacy();
		}
		return npc.textureLocation;
	}

	protected float handleRotationFloat(T npc, float par2) {
		if (npc.isKilled() || !npc.display.getHasLivingAnimation()) {
			return 0.0f;
		}
		return super.handleRotationFloat(npc, par2);
	}

	private void loadSkin(File file, ResourceLocation resource, String par1Str) {
		TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
		if (texturemanager.getTexture(resource) != null) {
			return;
		}
		ITextureObject object = (ITextureObject) new ImageDownloadAlt(file, par1Str, DefaultPlayerSkin.getDefaultSkinLegacy(), (IImageBuffer) new ImageBufferDownloadAlt());
		texturemanager.loadTexture(resource, object);
	}

	protected void preRenderCallback(T npc, float f) {
		this.renderColor(npc);
		int size = npc.display.getSize();
		GlStateManager.scale(npc.scaleX / 5.0f * size, npc.scaleY / 5.0f * size, npc.scaleZ / 5.0f * size);
	}

	protected void renderColor(EntityNPCInterface npc) {
		if (npc.hurtTime <= 0 && npc.deathTime <= 0) {
			float red = (npc.display.getTint() >> 16 & 0xFF) / 255.0f;
			float green = (npc.display.getTint() >> 8 & 0xFF) / 255.0f;
			float blue = (npc.display.getTint() & 0xFF) / 255.0f;
			GlStateManager.color(red, green, blue, 1.0f);
		}
	}

	protected void renderLivingAt(T npc, double d, double d1, double d2) {
		this.shadowSize = npc.display.getSize() / 10.0f;
		float xOffset = 0.0f;
		float yOffset = (npc.currentAnimation == 0) ? (npc.ais.bodyOffsetY / 10.0f - 0.5f) : 0.0f;
		float zOffset = 0.0f;
		if (npc.isEntityAlive()) {
			if (npc.isPlayerSleeping()) {
				xOffset = (float) (-Math.cos(Math.toRadians(180 - npc.ais.orientation)));
				zOffset = (float) (-Math.sin(Math.toRadians(npc.ais.orientation)));
				yOffset += 0.14f;
			} else if (npc.currentAnimation == 1 || npc.isRiding()) {
				yOffset -= 0.5f - ((EntityCustomNpc) npc).modelData.getLegsY() * 0.8f;
			}
		}
		xOffset = xOffset / 5.0f * npc.display.getSize();
		yOffset = yOffset / 5.0f * npc.display.getSize();
		zOffset = zOffset / 5.0f * npc.display.getSize();
		super.renderLivingAt(npc, d + xOffset, d1 + yOffset, d2 + zOffset);
	}

	protected void renderLivingLabel(EntityNPCInterface npc, double d, double e, double d2, int i, String name, String title) {
		FontRenderer fontrenderer = this.getFontRendererFromRenderManager();
		float f1 = npc.baseHeight / 5.0f * npc.display.getSize();
		float f2 = 0.01666667f * f1;
		GlStateManager.pushMatrix();
		GlStateManager.translate(d, e, d2);
		GL11.glNormal3f(0.0f, 1.0f, 0.0f);
		GlStateManager.rotate(-this.renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
		GlStateManager.rotate(this.renderManager.playerViewX, 1.0f, 0.0f, 0.0f);
		float height = f1 / 6.5f * 2.0f;
		int color = npc.getFaction().color;
		GlStateManager.disableLighting();
		GlStateManager.depthMask(false);
		GlStateManager.translate(0.0f, height, 0.0f);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		// New
		if (name.isEmpty()) { // Rarity
			float f3 = 0.01666667f * f1 * 0.6f;
			GlStateManager.translate(0.0f, -f1 / 6.5f * 0.9f, 0.0f);
			GlStateManager.scale(-f3, -f3, f3);
			fontrenderer.drawString(title, -fontrenderer.getStringWidth(title) / 2, 0, color);
			GlStateManager.scale(1.0f / -f3, 1.0f / -f3, 1.0f / f3);
			GlStateManager.translate(0.0f, f1 / 6.5f * 0.85f, 0.0f);
		} else if (!title.isEmpty()) {
			title = "<" + title + ">";
			float f3 = 0.01666667f * f1 * 0.6f;
			GlStateManager.translate(0.0f, -f1 / 6.5f * 0.4f, 0.0f);
			GlStateManager.scale(-f3, -f3, f3);
			fontrenderer.drawString(title, -fontrenderer.getStringWidth(title) / 2, 0, color);
			GlStateManager.scale(1.0f / -f3, 1.0f / -f3, 1.0f / f3);
			GlStateManager.translate(0.0f, f1 / 6.5f * 0.85f, 0.0f);
		}
		GlStateManager.scale(-f2, -f2, f2);
		if (npc.isInRange(this.renderManager.renderViewEntity, 4.0) && !name.isEmpty()) {
			GlStateManager.disableDepth();
			fontrenderer.drawString(name, -fontrenderer.getStringWidth(name) / 2, 0, color + 1426063360);
			GlStateManager.enableDepth();
		}
		GlStateManager.depthMask(true);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		if (!name.isEmpty()) {
			fontrenderer.drawString(name, -fontrenderer.getStringWidth(name) / 2, 0, color);
		}
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.popMatrix();
	}

	protected void renderModel(T npc, float par2, float par3, float par4, float par5, float par6, float par7) {
		
		try { super.renderModel(npc, par2, par3, par4, par5, par6, par7); }
		catch (Exception e) { }
		
		if (!npc.display.getOverlayTexture().isEmpty()) {
			GlStateManager.depthFunc(515);
			if (npc.textureGlowLocation == null) {
				npc.textureGlowLocation = new ResourceLocation(npc.display.getOverlayTexture());
			}
			this.bindTexture(npc.textureGlowLocation);
			float f1 = 1.0f;
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(1, 1);
			GlStateManager.disableLighting();
			if (npc.isInvisible()) {
				GlStateManager.depthMask(false);
			} else {
				GlStateManager.depthMask(true);
			}
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.pushMatrix();
			GlStateManager.scale(1.001f, 1.001f, 1.001f);
			this.mainModel.render(npc, par2, par3, par4, par5, par6, par7);
			GlStateManager.popMatrix();
			GlStateManager.enableLighting();
			GlStateManager.color(1.0f, 1.0f, 1.0f, f1);
			GlStateManager.depthFunc(515);
			GlStateManager.disableBlend();
		}
	}

	public void renderName(T npc, double d, double d1, double d2) {
		if (npc == null || !this.canRenderName(npc) || this.renderManager.renderViewEntity == null) {
			return;
		}
		double d3 = npc.getDistance(this.renderManager.renderViewEntity);
		if (d3 > 512.0) {
			return;
		}
		if (npc.messages != null) {
			float height = npc.baseHeight / 5.0f * npc.display.getSize();
			float offset = npc.height * (1.2f + (npc.display.showName() ? (npc.display.getTitle().isEmpty() ? 0.15f : 0.25f) : 0.0f));
			npc.messages.renderMessages(d, d1 + offset, d2, 0.666667f * height, npc.isInRange(this.renderManager.renderViewEntity, 4.0));
		}
		float scale = npc.baseHeight / 5.0f * npc.display.getSize();
		if (npc.display.showName()) {
			this.renderLivingLabel(npc, d, d1 + npc.height - 0.06f * scale, d2, 64, npc.getName(), npc.display.getTitle());
			if (!CustomNpcs.showLR) { return; }
			Client.sendDataDelayCheck(EnumPlayerPacket.NpcVisualData, npc, 5000, npc.getEntityId());
			if (!npc.stats.getRarityTitle().isEmpty()) {
				this.renderLivingLabel(npc, d, d1 + npc.height - 0.06f * scale, d2, 64, "", npc.stats.getRarityTitle());
			}
		}
	}
	
	protected boolean setBrightness(T npc, float partialTicks, boolean combineTextures) {
		float f = npc.getBrightness();
		int i = this.getColorMultiplier(npc, f, partialTicks);
		boolean flag = (i >> 24 & 255) > 0;
		boolean flag1 = npc.hurtTime > 0 || npc.deathTime > 0;
		if (flag1 && npc.animation.activeAnim!=null && npc.animation.activeAnim.type == AnimationKind.DIES) {
			flag1 = false; // cancel red death color
		}
		if (!flag && !flag1) { return false; }
		else if (!flag && !combineTextures) { return false; }
		else {
			GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
			GlStateManager.enableTexture2D();
			GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PRIMARY_COLOR);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
			GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
			GlStateManager.enableTexture2D();
			GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, OpenGlHelper.GL_INTERPOLATE);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_CONSTANT);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE2_RGB, OpenGlHelper.GL_CONSTANT);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND2_RGB, 770);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
			this.brightnessBuffer.position(0);
			if (flag1) {
				this.brightnessBuffer.put(1.0F);
				this.brightnessBuffer.put(0.0F);
				this.brightnessBuffer.put(0.0F);
				this.brightnessBuffer.put(0.3F);
			}
			else {
				float f1 = (float)(i >> 24 & 255) / 255.0F;
				float f2 = (float)(i >> 16 & 255) / 255.0F;
				float f3 = (float)(i >> 8 & 255) / 255.0F;
				float f4 = (float)(i & 255) / 255.0F;
				this.brightnessBuffer.put(f2);
				this.brightnessBuffer.put(f3);
				this.brightnessBuffer.put(f4);
				this.brightnessBuffer.put(1.0F - f1);
			}
			this.brightnessBuffer.flip();
			GlStateManager.glTexEnv(8960, 8705, this.brightnessBuffer);
			GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
			GlStateManager.enableTexture2D();
			GlStateManager.bindTexture(TEXTURE_BRIGHTNESS.getGlTextureId());
			GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_PREVIOUS);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.lightmapTexUnit);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
			GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
			return true;
		}
	}
	
}
