package noppes.npcs.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.ModelData;
import noppes.npcs.ModelPartConfig;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.client.model.animation.AniBow;
import noppes.npcs.client.model.animation.AniCrawling;
import noppes.npcs.client.model.animation.AniDancing;
import noppes.npcs.client.model.animation.AniHug;
import noppes.npcs.client.model.animation.AniNo;
import noppes.npcs.client.model.animation.AniPoint;
import noppes.npcs.client.model.animation.AniWaving;
import noppes.npcs.client.model.animation.AniYes;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.util.ObfuscationHelper;

/** Render npc Model */
public class ModelPlayerAlt
extends ModelPlayer {
	
	private ModelRenderer body;
	private ModelRenderer head;
	private Map<EnumParts, List<ModelScaleRenderer>> map;
	private Map<EntityCustomNpc, Boolean> isAttaking;
	private Map<EntityCustomNpc, Boolean> isJump;

	public ModelPlayerAlt(float scale, boolean arms) {
		super(scale, arms);
		this.map = Maps.<EnumParts, List<ModelScaleRenderer>>newHashMap();
		this.isAttaking = Maps.<EntityCustomNpc, Boolean>newHashMap();
		this.isJump = Maps.<EntityCustomNpc, Boolean>newHashMap();
		(this.head = new ModelScaleRenderer((ModelBase) this, 24, 0, EnumParts.HEAD)).addBox(-3.0f, -6.0f, -1.0f, 6, 6, 1, scale);
		(this.body = new ModelScaleRenderer((ModelBase) this, 0, 0, EnumParts.BODY)).setTextureSize(64, 32);
		this.body.addBox(-5.0f, 0.0f, -1.0f, 10, 16, 1, scale);
		ObfuscationHelper.setValue(ModelPlayer.class, this, this.head, 6);
		ObfuscationHelper.setValue(ModelPlayer.class, this, this.body, 5);
		this.bipedLeftArm = this.createScale(this.bipedLeftArm, EnumParts.ARM_LEFT);
		this.bipedLeftArmwear = this.createScale(this.bipedLeftArmwear, EnumParts.ARM_LEFT);
		this.bipedRightArm = this.createScale(this.bipedRightArm, EnumParts.ARM_RIGHT);
		this.bipedRightArmwear = this.createScale(this.bipedRightArmwear, EnumParts.ARM_RIGHT);
		
		this.bipedLeftLeg = this.createScale(this.bipedLeftLeg, EnumParts.LEG_LEFT);
		this.bipedLeftLegwear = this.createScale(this.bipedLeftLegwear, EnumParts.LEG_LEFT);
		this.bipedRightLeg = this.createScale(this.bipedRightLeg, EnumParts.LEG_RIGHT);
		this.bipedRightLegwear = this.createScale(this.bipedRightLegwear, EnumParts.LEG_RIGHT);
		
		this.bipedHead = this.createScale(this.bipedHead, EnumParts.HEAD);
		this.bipedHeadwear = this.createScale(this.bipedHeadwear, EnumParts.HEAD);
		
		this.bipedBody = this.createScale(this.bipedBody, EnumParts.BODY);
		this.bipedBodyWear = this.createScale(this.bipedBodyWear, EnumParts.BODY);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ModelScaleRenderer createScale(ModelRenderer renderer, EnumParts part) {
		int textureX = ObfuscationHelper.getValue(ModelRenderer.class, renderer, 2);
		int textureY = ObfuscationHelper.getValue(ModelRenderer.class, renderer, 3);
		ModelScaleRenderer model = new ModelScaleRenderer((ModelBase) this, textureX, textureY, part);
		model.textureHeight = renderer.textureHeight;
		model.textureWidth = renderer.textureWidth;
		if (renderer.childModels != null) { model.childModels = new ArrayList(renderer.childModels); }
		model.cubeList = new ArrayList(renderer.cubeList);
		copyModelAngles(renderer, (ModelRenderer) model);
		List<ModelScaleRenderer> list = this.map.get(part);
		if (list == null) { this.map.put(part, list = new ArrayList<ModelScaleRenderer>()); }
		list.add(model);
		return model;
	}

	protected EnumHandSide getMainHand(Entity entityIn) {
		if (!(entityIn instanceof EntityLivingBase) || !((EntityLivingBase) entityIn).isSwingInProgress) {
			return super.getMainHand(entityIn);
		}
		EntityLivingBase living = (EntityLivingBase) entityIn;
		if (living.swingingHand == EnumHand.MAIN_HAND) { return EnumHandSide.RIGHT; }
		return EnumHandSide.LEFT;
	}

	public ModelRenderer getRandomModelBox(Random random) {
		switch (random.nextInt(5)) {
			case 0: return this.bipedHead;
			case 1: return this.bipedBody;
			case 2: return this.bipedLeftArm;
			case 3: return this.bipedRightArm;
			case 4: return this.bipedLeftLeg;
			case 5: return this.bipedRightLeg;
			default: return this.bipedHead;
		}
	}

	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		try {
			GlStateManager.pushMatrix();
			if (entityIn.isSneaking()) { GlStateManager.translate(0.0f, 0.2f, 0.0f); }
			this.bipedHead.render(scale);
			this.bipedBody.render(scale);
			this.bipedRightArm.render(scale);
			this.bipedLeftArm.render(scale);
			this.bipedRightLeg.render(scale);
			this.bipedLeftLeg.render(scale);
			this.bipedHeadwear.render(scale);
			this.bipedLeftLegwear.render(scale);
			this.bipedRightLegwear.render(scale);
			this.bipedLeftArmwear.render(scale);
			this.bipedRightArmwear.render(scale);
			this.bipedBodyWear.render(scale);
			GlStateManager.popMatrix();
		}
		catch (Exception ex) { }
	}
	
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
		EntityCustomNpc npc = (EntityCustomNpc) entity;
		ModelData playerdata = npc.modelData;
		for (EnumParts part : this.map.keySet()) {
			ModelPartConfig config = playerdata.getPartConfig(part);
			for (ModelScaleRenderer model : this.map.get(part)) { model.config = config; }
		}
		if (!this.isRiding) { this.isRiding = (npc.currentAnimation == 1); }
		if (this.isSneak && (npc.currentAnimation == 7 || npc.isPlayerSleeping())) { this.isSneak = false; }
		if (npc.currentAnimation == 6) { this.rightArmPose = ModelBiped.ArmPose.BOW_AND_ARROW; }
		this.isSneak = npc.isSneaking();
		this.bipedBody.rotationPointZ =  0.0f;
		this.bipedBody.rotationPointY =  0.0f;
		this.bipedBody.rotationPointX =  0.0f;
		this.bipedBody.rotateAngleZ =  0.0f;
		this.bipedBody.rotateAngleY =  0.0f;
		this.bipedBody.rotateAngleX =  0.0f;
		this.bipedHead.rotateAngleX =  0.0f;
		this.bipedHeadwear.rotateAngleX =  0.0f;
		this.bipedHead.rotateAngleZ =  0.0f;
		this.bipedHeadwear.rotateAngleZ =  0.0f;
		this.bipedHead.rotationPointX =  0.0f;
		this.bipedHeadwear.rotationPointX =  0.0f;
		this.bipedHead.rotationPointY =  0.0f;
		this.bipedHeadwear.rotationPointY =  0.0f;
		this.bipedHead.rotationPointZ =  0.0f;
		this.bipedHeadwear.rotationPointZ =  0.0f;
		this.bipedLeftLeg.rotateAngleX = 0.0f;
		this.bipedLeftLeg.rotateAngleY = 0.0f;
		this.bipedLeftLeg.rotateAngleZ = 0.0f;
		this.bipedRightLeg.rotateAngleX = 0.0f;
		this.bipedRightLeg.rotateAngleY = 0.0f;
		this.bipedRightLeg.rotateAngleZ = 0.0f;
		this.bipedLeftArm.rotationPointX = 0.0f;
		this.bipedLeftArm.rotationPointY = 2.0f;
		this.bipedLeftArm.rotationPointZ = 0.0f;
		this.bipedRightArm.rotationPointX = 0.0f;
		this.bipedRightArm.rotationPointY = 2.0f;
		this.bipedRightArm.rotationPointZ = 0.0f;
		try { super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity); } catch (Exception ex) { }
		
		if (npc.animation.baseAnim!=null && npc.animation.baseAnim.frame >= npc.animation.baseAnim.frames.size()) {
			npc.animation.baseAnim.frame = 0;
		}
		boolean isBaseSet = this.setAnimationRotationAngles(npc, npc.animation.baseAnim, ageInTicks);
		
		AnimationConfig anim = npc.animation.activeAnim != null ? npc.animation.getActiveAnimation(npc.animation.activeAnim.type) : null;
		// Dies
		if (npc.isKilled() && !npc.stats.hideKilledBody) {
			if (anim==null || anim.type!=AnimationKind.DIES) {
				anim = npc.animation.getActiveAnimation(AnimationKind.DIES);
			}
		} else {
			if (anim!=null && anim.type==AnimationKind.DIES && npc.animation.isComplete && !npc.isKilled() || npc.stats.hideKilledBody) {
				anim = null;
				npc.animation.isComplete = false;
			}
			// Hit
			if (npc.hurtTime > 0 && npc.hurtTime == npc.maxHurtTime && npc.getHealth()!=0) {
				anim = npc.animation.getActiveAnimation(AnimationKind.HIT);
			}
			if (anim==null || anim.type!=AnimationKind.INIT) {
				this.isAttaking.put(npc, false);
				if (!this.isJump.containsKey(npc)) { this.isJump.put(npc, false); }
				
				if (this.isAttaking.get(npc) && this.swingProgress>0) {
					npc.swingProgress = 0.0f;
					npc.swingProgressInt = 5;
				}
				// Swing
				if (this.swingProgress>0) {
					anim = npc.animation.getActiveAnimation(AnimationKind.ATTACKING);
					if (anim!=null) {
						npc.swingProgress = 0.0f;
						npc.swingProgressInt = 5;
						this.isAttaking.put(npc, true);
					}
				}
				// Jump
				if (!this.isJump.get(npc) && !(npc.isInWater() || npc.isInLava())) {
					if (!npc.onGround && npc.motionY > 0.0d) {
						anim = npc.animation.getActiveAnimation(AnimationKind.JUMP);
						if (anim!=null) {
							this.isJump.put(npc, true);
						}
					}
				} else if (npc.onGround) {
					this.isJump.put(npc, false);
				}
			}
			// INIT started in EntityNPCInterface.reset()
			if (anim==null || !anim.isEdit) {
				// Moving or Standing
				if (anim==null) {
					boolean isNavigate = npc.isNavigating || npc.motionX!=0.0d || npc.motionZ!=0.0d;
					// Revenge Target
					if (npc.isAttacking()) {
						if (isNavigate && (anim==null || anim.type!=AnimationKind.REVENGE_WALK)) {
							anim = npc.animation.getActiveAnimation(AnimationKind.REVENGE_WALK);
						} else if (!isNavigate && (anim==null || anim.type!=AnimationKind.REVENGE_STAND)) {
							anim = npc.animation.getActiveAnimation(AnimationKind.REVENGE_STAND);
						}
					} else {
						if (npc.isInWater() || npc.isInLava()) {
							if (isNavigate && (anim==null || anim.type!=AnimationKind.WATER_WALK)) {
								anim = npc.animation.getActiveAnimation(AnimationKind.WATER_WALK);
							} else if (!isNavigate && (anim==null || anim.type!=AnimationKind.WATER_STAND)) {
								anim = npc.animation.getActiveAnimation(AnimationKind.WATER_STAND);
							}
						}
						else {
							if (!npc.onGround && npc.ais.getNavigationType()==1) {
								if (isNavigate && (anim==null || anim.type!=AnimationKind.FLY_WALK)) {
									anim = npc.animation.getActiveAnimation(AnimationKind.FLY_WALK);
								} else if (!isNavigate && (anim==null || anim.type!=AnimationKind.FLY_STAND)) {
									anim = npc.animation.getActiveAnimation(AnimationKind.FLY_STAND);
								}
							} else {
								if (isNavigate && (anim==null || anim.type!=AnimationKind.WALKING)) {
									anim = npc.animation.getActiveAnimation(AnimationKind.WALKING);
								} else if (!isNavigate && (anim==null || anim.type!=AnimationKind.STANDING)) {
									anim = npc.animation.getActiveAnimation(AnimationKind.STANDING);
								}
							}
						}
					}
				}
			}
		}
		npc.animation.activeAnim = anim;
		boolean isSet = this.setAnimationRotationAngles(npc, anim, ageInTicks);
		if (npc.animation.changeAnim!=null) {
			if (anim == null) {
				npc.animation.changeAnim = null;
			} else {
				boolean isChange = this.setAnimationRotationAngles(npc, npc.animation.changeAnim, ageInTicks);
				if (isChange) { isSet = true;}
			}
		}
		if (!isSet && !isBaseSet) {
			if (npc.isPlayerSleeping()) {
				if (this.bipedHead.rotateAngleX < 0.0f) {
					this.bipedHead.rotateAngleX = 0.0f;
					this.bipedHeadwear.rotateAngleX = 0.0f;
				}
			} else if (npc.currentAnimation == 9) {
				this.bipedHeadwear.rotateAngleX = 0.7f;
				this.bipedHead.rotateAngleX = 0.7f;
			} else if (npc.currentAnimation == 3) {
				AniHug.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity, (ModelBiped) this);
			} else if (npc.currentAnimation == 7) {
				AniCrawling.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity, (ModelBiped) this);
			} else if (npc.currentAnimation == 10) {
				AniWaving.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity, (ModelBiped) this);
			} else if (npc.currentAnimation == 5) {
				AniDancing.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity, (ModelBiped) this);
			} else if (npc.currentAnimation == 11) {
				AniBow.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity, (ModelBiped) this);
			} else if (npc.currentAnimation == 13) {
				AniYes.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity, (ModelBiped) this);
			} else if (npc.currentAnimation == 12) {
				AniNo.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity, (ModelBiped) this);
			} else if (npc.currentAnimation == 8) {
				AniPoint.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity, (ModelBiped) this);
			} else if (this.isSneak) {
				this.bipedBody.rotateAngleX = 0.5f / playerdata.getPartConfig(EnumParts.BODY).scaleBase[1];
			}
			((ModelScaleRenderer) this.bipedHead).setAnim(null);
			((ModelScaleRenderer) this.bipedLeftArm).setAnim(null);
			((ModelScaleRenderer) this.bipedRightArm).setAnim(null);
			((ModelScaleRenderer) this.bipedBody).setAnim(null);
			((ModelScaleRenderer) this.bipedLeftLeg).setAnim(null);
			((ModelScaleRenderer) this.bipedRightLeg).setAnim(null);
			((ModelScaleRenderer) this.bipedHeadwear).setAnim(null);
			((ModelScaleRenderer) this.bipedLeftArmwear).setAnim(null);
			((ModelScaleRenderer) this.bipedRightArmwear).setAnim(null);
			((ModelScaleRenderer) this.bipedBodyWear).setAnim(null);
			((ModelScaleRenderer) this.bipedLeftLegwear).setAnim(null);
			((ModelScaleRenderer) this.bipedRightLegwear).setAnim(null);
		}
		copyModelAngles(this.bipedLeftLeg, this.bipedLeftLegwear);
		copyModelAngles(this.bipedRightLeg, this.bipedRightLegwear);
		copyModelAngles(this.bipedLeftArm, this.bipedLeftArmwear);
		copyModelAngles(this.bipedRightArm, this.bipedRightArmwear);
		copyModelAngles(this.bipedBody, this.bipedBodyWear);
		copyModelAngles(this.bipedHead, this.bipedHeadwear);
	}

	private boolean setAnimationRotationAngles(EntityCustomNpc npc, AnimationConfig anim, float ageInTicks) {
		if (npc==null || anim==null) { return false; }
		Map<Integer, Float[]> animData = npc.animation.getValues(npc, anim);
		if (animData == null) { return false; }
		// Head
		Float[] head = animData.get(0);
		if (head!=null) {
			if (anim.type.isMoving()) {
				((ModelScaleRenderer) this.bipedHead).clearRotation();
				((ModelScaleRenderer) this.bipedHeadwear).clearRotation();
			}
			((ModelScaleRenderer) this.bipedHead).setAnim(head);
			((ModelScaleRenderer) this.bipedHeadwear).setAnim(head);
		}
		
		// Left Arm
		Float[] leftArm = animData.get(1);
		if (leftArm!=null) {
			if (anim.type.isMoving()) {
				((ModelScaleRenderer) this.bipedLeftArm).clearRotation();
				((ModelScaleRenderer) this.bipedLeftArmwear).clearRotation();
			}
			((ModelScaleRenderer) this.bipedLeftArm).setAnim(leftArm);
			((ModelScaleRenderer) this.bipedLeftArmwear).setAnim(leftArm);
			if (npc.display.getHasLivingAnimation() && (anim.type==AnimationKind.STANDING || anim.type==AnimationKind.FLY_STAND || anim.type==AnimationKind.WATER_STAND)) {
				this.bipedLeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09f) * 0.05f + 0.05f;
				this.bipedLeftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067f) * 0.05f;
			}
		}
		// Right Arm
		Float[] rightArm = animData.get(2);
		if (rightArm!=null) {
			if (anim.type.isMoving()) {
				((ModelScaleRenderer) this.bipedRightArm).clearRotation();
				((ModelScaleRenderer) this.bipedRightArmwear).clearRotation();
			}
			((ModelScaleRenderer) this.bipedRightArm).setAnim(rightArm);
			((ModelScaleRenderer) this.bipedRightArmwear).setAnim(rightArm);
			if (npc.display.getHasLivingAnimation()) {
				this.bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09f) * 0.05f + 0.05f;
				this.bipedRightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067f) * 0.05f;
			}
		}
		// Body
		Float[] body = animData.get(3);
		if (body!=null) {
			if (anim.type.isMoving()) {
				((ModelScaleRenderer) this.bipedBody).clearRotation();
				((ModelScaleRenderer) this.bipedBodyWear).clearRotation();
			}
			((ModelScaleRenderer) this.bipedBody).setAnim(body);
			((ModelScaleRenderer) this.bipedBodyWear).setAnim(body);
		}
		// Left Leg
		Float[] leftLeg = animData.get(4);
		if (body!=null) {
			if (anim.type.isMoving()) {
				((ModelScaleRenderer) this.bipedLeftLeg).clearRotation();
				((ModelScaleRenderer) this.bipedLeftLegwear).clearRotation();
			}
			((ModelScaleRenderer) this.bipedLeftLeg).setAnim(leftLeg);
			((ModelScaleRenderer) this.bipedLeftLegwear).setAnim(leftLeg);
		}

		// Right Leg
		Float[] rightLeg = animData.get(5);
		if (body!=null) {
			if (anim.type.isMoving()) {
				((ModelScaleRenderer) this.bipedRightLeg).clearRotation();
				((ModelScaleRenderer) this.bipedRightLegwear).clearRotation();
			}
			((ModelScaleRenderer) this.bipedRightLeg).setAnim(rightLeg);
			((ModelScaleRenderer) this.bipedRightLegwear).setAnim(rightLeg);
		}
		return true;
	}
	
}
