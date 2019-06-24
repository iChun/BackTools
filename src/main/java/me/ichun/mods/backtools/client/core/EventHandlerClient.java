package me.ichun.mods.backtools.client.core;

import com.google.common.base.Predicates;
import me.ichun.mods.backtools.client.layer.LayerBackTool;
import me.ichun.mods.backtools.common.BackTools;
import me.ichun.mods.ichunutil.client.core.event.RendererSafeCompatibilityEvent;
import me.ichun.mods.ichunutil.client.model.item.ModelBaseWrapper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EventHandlerClient
{
    @SubscribeEvent
    public void onRendererSafeCompatibility(RendererSafeCompatibilityEvent event)
    {
        LayerBackTool layer = new LayerBackTool();
        Minecraft.getMinecraft().getRenderManager().skinMap.forEach((k, v) -> v.addLayer(layer));
    }

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.side == Side.CLIENT && event.phase == TickEvent.Phase.END)
        {
            EntityPlayer player = event.player;

            ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
            if(heldItem != currentTool.get(player.getName()) && (heldItem.isEmpty() || (Block.getBlockFromItem(heldItem.getItem()) == Blocks.AIR)))
            {
                if(currentTool.get(player.getName()) != null && (currentTool.get(player.getName()).getItem().isFull3D() || currentTool.get(player.getName()).getItem() instanceof ItemBow) && !BackTools.blacklist.contains(currentTool.get(player.getName()).getItem()) && !BackTools.isBlacklistedInConfig(currentTool.get(player.getName()).getItem()))
                {
                    ItemStack is = currentTool.get(player.getName()).copy();
                    IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(is);
                    if(!(model instanceof ModelBaseWrapper))
                    {
                        is.setItemDamage(0);
                        ItemStack prevTool = playerTool.get(player.getName());
                        if(prevTool == null || (prevTool.isEmpty() || !ItemStack.areItemStacksEqual(prevTool, is)))
                        {
                            playerTool.put(player.getName(), is);
                        }
                    }
                }
                currentTool.put(player.getName(), heldItem);
            }
        }
    }

    @SubscribeEvent
    public void onItemSpawnEvent(EntityJoinWorldEvent event)
    {
        if(event.getWorld().isRemote && event.getEntity() instanceof EntityItem)
        {
            Iterator<Map.Entry<String, ItemStack>> ite = playerTool.entrySet().iterator();
            while(ite.hasNext())
            {
                Map.Entry<String, ItemStack> e = ite.next();
                if(e.getValue().isItemEqual(((EntityItem)event.getEntity()).getItem()))
                {
                    List<Entity> list = event.getWorld().getEntitiesInAABBexcluding(event.getEntity(), event.getEntity().getEntityBoundingBox(), Predicates.instanceOf(EntityPlayer.class));
                    boolean flag = false;

                    for(Entity ent : list)
                    {
                        if(ent.getName().equals(e.getKey()))
                        {
                            flag = true;
                            break;
                        }
                    }

                    if(flag)
                    {
                        ite.remove();
                        break;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event)
    {
        onClientConnection();
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        onClientConnection();
    }

    public void onClientConnection()
    {
        BackTools.eventHandlerClient.currentTool.clear();
        BackTools.eventHandlerClient.playerTool.clear();
    }

    public HashMap<String, ItemStack> playerTool = new HashMap<>();
    public HashMap<String, ItemStack> currentTool = new HashMap<>();
}
