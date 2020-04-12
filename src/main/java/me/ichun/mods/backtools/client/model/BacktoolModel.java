package me.ichun.mods.backtools.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.ichun.mods.backtools.client.core.EventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
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
    public IRenderTypeBuffer buffer;

    public BacktoolModel(float scale)
    {
        super(scale);
        float offset = 0F;

        this.leftArmPose = BipedModel.ArmPose.EMPTY;
        this.rightArmPose = BipedModel.ArmPose.EMPTY;
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.bipedHead = new ModelRenderer(this);
        this.bipedHeadwear = new ModelRenderer(this);

        this.bipedBody = new BackToolRendererModel(this);
        this.bipedBody.setRotationPoint(0.0F, 0.0F + offset, 0.0F);

        this.bipedRightArm = new ModelRenderer(this);
        this.bipedLeftArm = new ModelRenderer(this);
        this.bipedRightLeg = new ModelRenderer(this);
        this.bipedLeftLeg = new ModelRenderer(this);
    }

    public void setItemRenders(ItemStack main, ItemStack off, HandSide side, int ticks, float partialTick, float offset, IRenderTypeBuffer bufferIn)
    {
        mainStack = main;
        offStack = off;
        mainSide = side;
        this.ticks = ticks;
        this.partialTick = partialTick;
        this.offset = offset;
        this.buffer = bufferIn;
    }

    @Override
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        if (this.isChild) {
            matrixStackIn.push();
            float f1 = 1.0F / 2F;//this.field_228225_h_;
            matrixStackIn.scale(f1, f1, f1);
            matrixStackIn.translate(0.0D, (double)(24F /*this.field_228226_i_*/ / 16.0F), 0.0D);
            this.bipedBody.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            matrixStackIn.pop();
        } else {
            this.bipedBody.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        }
    }

    public static class BackToolRendererModel extends ModelRenderer
    {
        private final BacktoolModel parent;
        public BackToolRendererModel(BacktoolModel parent)
        {
            super(parent);
            this.parent = parent;
        }

        @Override
        public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
            if (this.showModel) {
                matrixStackIn.push();
                this.translateRotate(matrixStackIn);
                this.renderItem(matrixStackIn, packedLightIn);
                matrixStackIn.pop();
            }
        }

        public void renderItem(MatrixStack matrixStackIn, int packedLightIn)
        {
            //parent has reference
            matrixStackIn.translate(0F, 4F/16F, 1.91F/16F + (parent.offset / 16F));
            if(!parent.mainStack.isEmpty())
            {
                matrixStackIn.push();
                matrixStackIn.translate(0F, 0F, 0.025F);
                boolean isShield = parent.mainStack.getItem() instanceof ShieldItem;
                if(parent.mainSide == HandSide.RIGHT)
                {
                    matrixStackIn.scale(-1F, 1F, -1F);
                }
                if(isShield)
                {
                    if(parent.mainSide == HandSide.LEFT)
                    {
                        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180F));
                    }
                    float scale = 1.5F;
                    matrixStackIn.scale(scale, scale, scale);
                    if(parent.mainSide == HandSide.LEFT)
                    {
                        matrixStackIn.translate(-2.5F/16F, 2F/16F, 1.25F/16F);
                        matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(-25F));
                    }
                    else
                    {
                        matrixStackIn.translate(-1F/16F, 0.25F/16F, 1.25F/16F);
                        matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(25F));
                    }
                }

                if(!isShield)
                {
                    int i = EventHandler.getToolOrientation(parent.mainStack.getItem());
                    matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(-i));
                    if(parent.ticks > 0)
                    {
                        matrixStackIn.rotate(Vector3f.ZP.rotationDegrees((parent.ticks + parent.partialTick) * 40F));
                    }
                }
                Minecraft.getInstance().getItemRenderer().renderItem(parent.mainStack, ItemCameraTransforms.TransformType.FIXED, packedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, parent.buffer);
                matrixStackIn.pop();
            }
            if(!parent.offStack.isEmpty())
            {
                boolean isShield = parent.offStack.getItem() instanceof ShieldItem;
                if(parent.mainSide == HandSide.LEFT)
                {
                    matrixStackIn.scale(-1F, 1F, -1F);
                }
                if(isShield)
                {
                    if(parent.mainSide == HandSide.RIGHT)
                    {
                        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180F));
                    }
                    float scale = 1.5F;
                    matrixStackIn.scale(scale, scale, scale);
                    if(parent.mainSide == HandSide.RIGHT)
                    {
                        matrixStackIn.translate(-2.5F/16F, 2F/16F, 1.25F/16F);
                        matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(-25F));
                    }
                    else
                    {
                        matrixStackIn.translate(-1F/16F, 0.25F/16F, 1.25F/16F);
                        matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(25F));
                    }
                }
                if(!isShield)
                {
                    int i = EventHandler.getToolOrientation(parent.offStack.getItem());
                    matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(-i));
                    if(parent.ticks > 0)
                    {
                        matrixStackIn.rotate(Vector3f.ZP.rotationDegrees((parent.ticks + parent.partialTick) * 40F));
                    }
                }
                Minecraft.getInstance().getItemRenderer().renderItem(parent.offStack, ItemCameraTransforms.TransformType.FIXED, packedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, parent.buffer);
            }
        }
    }
}
