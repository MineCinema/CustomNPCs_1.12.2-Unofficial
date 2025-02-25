package noppes.npcs.client.layer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.ModelData;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.entity.EntityCustomNpc;

public abstract class LayerInterface<T extends EntityLivingBase>
implements LayerRenderer<T> {
	
	public ModelBiped model;
	protected EntityCustomNpc npc;
	protected ModelData playerdata;
	protected RenderLiving<?> render;

	public LayerInterface(RenderLiving<?> render) {
		this.render = render;
		this.model = (ModelBiped) render.getMainModel();
	}

	private int blend(int color1, int color2, float ratio) {
		if (ratio >= 1.0f) {
			return color2;
		}
		if (ratio <= 0.0f) {
			return color1;
		}
		int aR = (color1 & 0xFF0000) >> 16;
		int aG = (color1 & 0xFF00) >> 8;
		int aB = color1 & 0xFF;
		int bR = (color2 & 0xFF0000) >> 16;
		int bG = (color2 & 0xFF00) >> 8;
		int bB = color2 & 0xFF;
		int R = (int) (aR + (bR - aR) * ratio);
		int G = (int) (aG + (bG - aG) * ratio);
		int B = (int) (aB + (bB - aB) * ratio);
		return R << 16 | G << 8 | B;
	}

	public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.npc = (EntityCustomNpc) entity;
		if (this instanceof LayerHeadwear && !this.npc.equals(((LayerHeadwear<?>) this).npc)) {
			((LayerHeadwear<?>) this).preRender(this.npc);
		}
		if (this.npc.isInvisibleToPlayer((EntityPlayer) Minecraft.getMinecraft().player)) { return; }
		this.playerdata = this.npc.modelData;
		this.model = (ModelBiped) this.render.getMainModel();
		this.rotate(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		GlStateManager.pushMatrix();
		if (entity.isInvisible()) {
			GlStateManager.color(1.0f, 1.0f, 1.0f, 0.15f);
			GlStateManager.depthMask(false);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(770, 771);
			GlStateManager.alphaFunc(516, 0.003921569f);
		}
		if (this.npc.hurtTime > 0 || this.npc.deathTime > 0) { GlStateManager.color(1.0f, 0.0f, 0.0f, 0.3f); }
		if (this.npc.isSneaking()) { GlStateManager.translate(0.0f, 0.2f, 0.0f); }
		GlStateManager.enableRescaleNormal();
		this.render(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		GlStateManager.disableRescaleNormal();
		if (entity.isInvisible()) {
			GlStateManager.disableBlend();
			GlStateManager.alphaFunc(516, 0.1f);
			GlStateManager.depthMask(true);
		}
		GlStateManager.popMatrix();
	}

	public void preRender(ModelPartData data) {
		if (data.playerTexture) {
			ClientProxy.bindTexture(this.npc.textureLocation);
		} else {
			ClientProxy.bindTexture(data.getResource());
		}
		if (this.npc.hurtTime > 0 || this.npc.deathTime > 0) {
			return;
		}
		int color = data.color;
		if (this.npc.display.getTint() != 16777215) {
			if (data.color != 16777215) {
				color = this.blend(data.color, this.npc.display.getTint(), 0.5f);
			} else {
				color = this.npc.display.getTint();
			}
		}
		float red = (color >> 16 & 0xFF) / 255.0f;
		float green = (color >> 8 & 0xFF) / 255.0f;
		float blue = (color & 0xFF) / 255.0f;
		GlStateManager.color(red, green, blue, this.npc.isInvisible() ? 0.15f : 0.99f);
	}

	public abstract void render(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale);

	public abstract void rotate(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale);

	public void setColor(ModelPartData data, EntityLivingBase entity) {
	}

	public void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public boolean shouldCombineTextures() {
		return false;
	}
	
}
