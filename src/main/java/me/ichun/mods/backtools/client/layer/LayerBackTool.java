package me.ichun.mods.backtools.client.layer;

import me.ichun.mods.backtools.common.BackTools;
import me.ichun.mods.ichunutil.client.render.RendererHelper;
import me.ichun.mods.ichunutil.common.iChunUtil;
import me.ichun.mods.morph.api.MorphApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class LayerBackTool implements LayerRenderer<EntityPlayer>
{
    @Override
    //func_177093_a(entity, f8, f7, partialTicks, f5, f4, f9, 0.0625F);
    public void doRenderLayer(EntityPlayer player, float f, float f1, float renderTick, float f2, float f3, float f4, float f5)
    {
        if(iChunUtil.hasMorphMod() && MorphApi.getApiImpl().hasMorph(player.getName(), Side.CLIENT) && (MorphApi.getApiImpl().morphProgress(player.getName(), Side.CLIENT) < 1.0F || !(MorphApi.getApiImpl().getMorphEntity(player.worldObj, player.getName(), Side.CLIENT) instanceof EntityPlayer)))
        {
            return;
        }
        if(!(player.isWearing(EnumPlayerModelParts.CAPE) && ((AbstractClientPlayer)player).getLocationCape() != null) && !player.isInvisible() && !player.isPlayerSleeping())
        {
            ItemStack is = BackTools.eventHandlerClient.playerTool.get(player.getName());

            ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
            if(heldItem != null)
            {
                ItemStack is1 = heldItem.copy();
                is1.setItemDamage(0);
                heldItem = is1;
            }

            if(is != null && !ItemStack.areItemStacksEqual(is, heldItem))
            {
                GlStateManager.pushMatrix();

                GlStateManager.translate(0.0f, 0.35F, 0.16F);

                if(player.inventory.armorItemInSlot(2) != null)
                {
                    GlStateManager.translate(0.0F, player.isSneaking() ? -0.1F : 0.0F, player.isSneaking() ? 0.025F : 0.06F);
                }
                if(player.isSneaking())
                {
                    GlStateManager.translate(0F, 0.08F, 0.13F);
                    GlStateManager.rotate(28.8F, 1.0F, 0.0F, 0.0F);
                }

                GlStateManager.rotate((float)(BackTools.getOrientation(is.getItem().getClass()) - 1) * -90F, 0.0F, 0.0F, 1.0F);

                GlStateManager.rotate(180F, 0.0F, 1.0F, 0.0F);

                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(is);
                RendererHelper.renderBakedModel(model, -1, is);

                GlStateManager.disableBlend();
                GlStateManager.enableLighting();
                GlStateManager.enableAlpha();

                GlStateManager.popMatrix();
            }
        }
    }

    public boolean shouldCombineTextures() //Should render red overlay as well?
    {
        return true;
    }
}