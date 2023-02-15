package me.ichun.mods.backtools.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.backtools.client.core.EventHandler;
import me.ichun.mods.backtools.client.model.BacktoolModel;
import me.ichun.mods.ichunutil.common.iChunUtil;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;

public class BackToolLayer extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>
{
    public final BacktoolModel backtoolModel;

    public BackToolLayer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> renderer)
    {
        super(renderer);
        this.backtoolModel = new BacktoolModel(0.0F);
        this.backtoolModel.isChild = false;
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch)
    {
        if(!(player.isWearing(PlayerModelPart.CAPE) && player.getLocationCape() != null) && !player.isInvisible() && !player.isSleeping() && EventHandler.heldTools.containsKey(player))
        {
            EventHandler.HeldInfo info = EventHandler.heldTools.get(player);
            boolean enableEasterEgg = iChunUtil.configClient.easterEgg && (player.getPose().equals(Pose.SWIMMING) || player.isElytraFlying() || player.getName().getUnformattedComponentText().equalsIgnoreCase("iChun"));

            matrixStackIn.push();

            backtoolModel.setItemRenders(info.lastMain, info.lastOff, player.getPrimaryHand(), enableEasterEgg ? player.ticksExisted : 0, partialTick, !player.getItemStackFromSlot(EquipmentSlotType.CHEST).isEmpty() ? 1.0F : player.isWearing(PlayerModelPart.JACKET) ? 0.5F : 0F, bufferIn);
            getEntityModel().setModelAttributes((BipedModel)backtoolModel);
            backtoolModel.setLivingAnimations(player, limbSwing, limbSwingAmount, partialTick);
            backtoolModel.setRotationAngles(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            int packedOverlay = LivingRenderer.getPackedOverlay(player, 0.0F);
            backtoolModel.render(matrixStackIn, bufferIn.getBuffer(RenderType.getEntityCutoutNoCull(getEntityTexture(player))), packedLightIn, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);

            matrixStackIn.pop();
        }
    }
}
