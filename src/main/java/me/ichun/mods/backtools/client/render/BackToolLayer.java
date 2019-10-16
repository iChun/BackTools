package me.ichun.mods.backtools.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import me.ichun.mods.backtools.client.core.EventHandler;
import me.ichun.mods.backtools.client.model.BacktoolModel;
import me.ichun.mods.ichunutil.common.iChunUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.HandSide;

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
    public void render(AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        //TODO add support for Morph
//        if(iChunUtil.hasMorphMod() && MorphApi.getApiImpl().hasMorph(player.getName(), Side.CLIENT) && (MorphApi.getApiImpl().morphProgress(player.getName(), Side.CLIENT) < 1.0F || !(MorphApi.getApiImpl().getMorphEntity(player.getEntityWorld(), player.getName(), Side.CLIENT) instanceof EntityPlayer)))
//        {
//            return;
//        }

        if(!(player.isWearing(PlayerModelPart.CAPE) && player.getLocationCape() != null) && !player.isInvisible() && !player.isSleeping() && EventHandler.heldTools.containsKey(player))
        {
            EventHandler.HeldInfo info = EventHandler.heldTools.get(player);
            boolean enableEasterEgg = iChunUtil.configClient.easterEgg && (player.getPose().equals(Pose.SWIMMING) || player.isElytraFlying() || player.getName().getUnformattedComponentText().equalsIgnoreCase("iChun"));

            GlStateManager.pushMatrix();

            backtoolModel.setItemRenders(info.lastMain, info.lastOff, player.getPrimaryHand(), enableEasterEgg ? player.ticksExisted : 0, partialTick, !player.getItemStackFromSlot(EquipmentSlotType.CHEST).isEmpty() ? 1.0F : player.isWearing(PlayerModelPart.JACKET) ? 0.5F : 0F);
            getEntityModel().func_217148_a((BipedModel)backtoolModel);
            backtoolModel.setLivingAnimations(player, limbSwing, limbSwingAmount, partialTick);
            backtoolModel.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean shouldCombineTextures()
    {
        return true;
    }
}
