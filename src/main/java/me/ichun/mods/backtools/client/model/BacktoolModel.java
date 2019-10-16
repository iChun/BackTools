package me.ichun.mods.backtools.client.model;

import com.mojang.blaze3d.platform.GlStateManager;
import me.ichun.mods.backtools.client.core.EventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.HandSide;

public class BacktoolModel extends BipedModel<LivingEntity>
{
    public ItemStack mainStack = ItemStack.EMPTY;
    public ItemStack offStack = ItemStack.EMPTY;
    public HandSide mainSide = HandSide.RIGHT;
    public int ticks;
    public float partialTick;
    public float offset;

    public BacktoolModel(float scale)
    {
        super(scale);
        float offset = 0F;

        this.leftArmPose = BipedModel.ArmPose.EMPTY;
        this.rightArmPose = BipedModel.ArmPose.EMPTY;
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.bipedHead = new RendererModel(this);
        this.bipedHeadwear = new RendererModel(this);

        this.bipedBody = new BackToolRendererModel(this);
        this.bipedBody.setRotationPoint(0.0F, 0.0F + offset, 0.0F);

        this.bipedRightArm = new RendererModel(this);
        this.bipedLeftArm = new RendererModel(this);
        this.bipedRightLeg = new RendererModel(this);
        this.bipedLeftLeg = new RendererModel(this);
    }

    public void setItemRenders(ItemStack main, ItemStack off, HandSide side, int ticks, float partialTick, float offset)
    {
        mainStack = main;
        offStack = off;
        mainSide = side;
        this.ticks = ticks;
        this.partialTick = partialTick;
        this.offset = offset;
    }

    @Override
    public void render(LivingEntity living, float f, float f1, float f2, float f3, float f4, float f5) {
        this.setRotationAngles(living, f, f1, f2, f3, f4, f5);
        GlStateManager.pushMatrix();
        if (this.isChild) {
            GlStateManager.scalef(0.5F, 0.5F, 0.5F);
            GlStateManager.translatef(0.0F, 24.0F * f5, 0.0F);
            this.bipedBody.render(f5);
        } else {
            if (living.shouldRenderSneaking()) {
                GlStateManager.translatef(0.0F, 0.2F, 0.0F);
            }
            this.bipedBody.render(f5);
        }
        GlStateManager.popMatrix();
    }

    public static class BackToolRendererModel extends RendererModel
    {
        private final BacktoolModel parent;
        public BackToolRendererModel(BacktoolModel parent)
        {
            super(parent);
            this.parent = parent;
        }

        @Override
        public void render(float scale)
        {
            if(parent.mainStack.isEmpty() && parent.offStack.isEmpty())
            {
                return;
            }

            if (!this.isHidden) {
                if (this.showModel) {

                    GlStateManager.pushMatrix();
                    GlStateManager.translatef(this.offsetX, this.offsetY, this.offsetZ);
                    if (this.rotateAngleX == 0.0F && this.rotateAngleY == 0.0F && this.rotateAngleZ == 0.0F) {
                        if (this.rotationPointX == 0.0F && this.rotationPointY == 0.0F && this.rotationPointZ == 0.0F)
                        {
                            renderItem();
                        } else
                        {
                            GlStateManager.pushMatrix();
                            GlStateManager.translatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
                            renderItem();

                            GlStateManager.popMatrix();
                        }
                    } else {
                        GlStateManager.pushMatrix();
                        GlStateManager.translatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
                        if (this.rotateAngleZ != 0.0F) {
                            GlStateManager.rotatef(this.rotateAngleZ * 57.295776F, 0.0F, 0.0F, 1.0F);
                        }

                        if (this.rotateAngleY != 0.0F) {
                            GlStateManager.rotatef(this.rotateAngleY * 57.295776F, 0.0F, 1.0F, 0.0F);
                        }

                        if (this.rotateAngleX != 0.0F) {
                            GlStateManager.rotatef(this.rotateAngleX * 57.295776F, 1.0F, 0.0F, 0.0F);
                        }

                        renderItem();

                        GlStateManager.popMatrix();
                    }

                    GlStateManager.popMatrix();
                }
            }
        }

        public void renderItem()
        {
            //parent has reference
            GlStateManager.translatef(0F, 4F/16F, 2.501F/16F + (parent.offset / 16F));
            if(!parent.mainStack.isEmpty())
            {
                GlStateManager.pushMatrix();
                GlStateManager.translatef(0F, 0F, 0.025F);
                boolean isShield = parent.mainStack.getItem() instanceof ShieldItem;
                if(isShield)
                {
                    GlStateManager.translatef(8F/16F, 11F/16F, 6F/16F);
                }
                else if(parent.mainSide == HandSide.LEFT)
                {
                    GlStateManager.rotatef(180F, 0F, 1F, 0F);
                }

                if(!isShield)
                {
                    int i = EventHandler.getToolOrientation(parent.mainStack.getItem());
                    GlStateManager.rotatef(i, 0F, 0F, 1F);
                    if(parent.ticks > 0)
                    {
                        GlStateManager.rotatef((parent.ticks + parent.partialTick) * 40F, 0F, 0F, 1F);
                    }
                }
                Minecraft.getInstance().getItemRenderer().renderItem(parent.mainStack, ItemCameraTransforms.TransformType.NONE);
                GlStateManager.popMatrix();
            }
            if(!parent.offStack.isEmpty())
            {
                boolean isShield = parent.offStack.getItem() instanceof ShieldItem;
                if(isShield)
                {
                    GlStateManager.translatef(8F/16F, 11F/16F, 6F/16F);
                }
                else if(parent.mainSide == HandSide.RIGHT)
                {
                    GlStateManager.rotatef(180F, 0F, 1F, 0F);
                }
                if(!isShield)
                {
                    int i = EventHandler.getToolOrientation(parent.offStack.getItem());
                    GlStateManager.rotatef(i, 0F, 0F, 1F);
                    if(parent.ticks > 0)
                    {
                        GlStateManager.rotatef((parent.ticks + parent.partialTick) * 40F, 0F, 0F, 1F);
                    }
                }
                Minecraft.getInstance().getItemRenderer().renderItem(parent.offStack, ItemCameraTransforms.TransformType.NONE);
            }
        }
    }
}
