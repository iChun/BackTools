package me.ichun.mods.backtools.client.core;

import me.ichun.mods.backtools.client.render.BackToolLayer;
import me.ichun.mods.backtools.common.BackTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.WeakHashMap;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = BackTools.MOD_ID, value = Dist.CLIENT)
public class EventHandler
{
    public static WeakHashMap<AbstractClientPlayerEntity, HeldInfo> heldTools = new WeakHashMap<>();

    public static HashSet<ResourceLocation> disabledTools = new HashSet<>();
    public static HashMap<Class<? extends Item>, Integer> toolOrientations = new HashMap<>();

    public static void addLayers()
    {
        Minecraft.getInstance().getRenderManager().skinMap.forEach((k, v) -> {
            v.addLayer(new BackToolLayer(v));
        });
    }

    public static void setupConfig()
    {
        disabledTools.clear();
        BackTools.config.disabledTools.forEach(s -> disabledTools.add(new ResourceLocation(s)));
        disabledTools.addAll(BackTools.imcDisabledTools);

        toolOrientations.clear();
        for(String s : BackTools.config.toolOrientation)
        {
            String[] split = new String[2];
            int index = s.indexOf(':');
            if (index > 0) {
                split[0] = s.substring(0, index);
                split[1] = s.substring(index + 1);
            }
            else
            {
                BackTools.LOGGER.warn("Could not parse orientation: {}", s);
            }
            try
            {
                Class clz = Class.forName(split[0]);
                if(Item.class.isAssignableFrom(clz))
                {
                    toolOrientations.put(clz, Integer.parseInt(split[1]));
                }
                else
                {
                    BackTools.LOGGER.warn("Class does not extend Item class: {}", split[0]);
                }
            }
            catch(ClassNotFoundException e)
            {
                BackTools.LOGGER.warn("Could not find class to add orientation: {}", split[0]);
            }
            catch(NumberFormatException e)
            {
                BackTools.LOGGER.warn("Could not parse integer: {}", s);
            }
        }
        toolOrientations.putAll(BackTools.imcOrientation);
    }

    public static Integer getToolOrientation(Item item)
    {
        Class clz = item.getClass();
        return toolOrientations.computeIfAbsent(clz, k -> getToolOrientation(clz));
    }

    public static Integer getToolOrientation(Class clz)
    {
        if(Item.class.equals(clz))
        {
            return 0;
        }
        return toolOrientations.computeIfAbsent(clz, k -> getToolOrientation(clz.getSuperclass()));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START && event.player.world.isRemote)
        {
            AbstractClientPlayerEntity player = (AbstractClientPlayerEntity)event.player;
            if(!player.isAlive())
            {
                heldTools.remove(player);
            }
            else
            {
                HeldInfo info = heldTools.computeIfAbsent(player, v -> new HeldInfo());
                info.tick(player.getHeldItemMainhand().copy(), player.getHeldItemOffhand().copy());
            }
        }
    }

    @SubscribeEvent
    public static void onItemSpawnEvent(EntityJoinWorldEvent event)
    {
        if(event.getWorld().isRemote && event.getEntity() instanceof ItemEntity)
        {
            ItemEntity item = (ItemEntity)event.getEntity();
            List<PlayerEntity> ents = event.getWorld().getEntitiesWithinAABB(EntityType.PLAYER, item.getBoundingBox().expand(1D, 1D, 1D), k -> true);
            ents.forEach((e) -> {
                if(e instanceof AbstractClientPlayerEntity)
                {
                    heldTools.computeIfPresent((AbstractClientPlayerEntity)e, (k, v) -> {
                        v.itemEntity = item;
                        return v;
                    });
                }
            });
        }
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event)
    {
        Minecraft.getInstance().execute(EventHandler::clean);
    }

    @SubscribeEvent
    public static void onLoggedOutEvent(ClientPlayerNetworkEvent.LoggedOutEvent event)
    {
        Minecraft.getInstance().execute(EventHandler::clean);
    }

    public static void clean()
    {
        heldTools.clear();
    }

    public static boolean isItemTool(Item item)
    {
        if(disabledTools.contains(item.getRegistryName()))
        {
            return false;
        }
        return item instanceof TieredItem ||
                item instanceof ShootableItem ||
                item instanceof ShearsItem ||
                item instanceof FishingRodItem ||
                item instanceof ShieldItem ||
                item instanceof TridentItem;
    }

    public static boolean areItemStacksEqualToolsIgnoreDamage(@Nonnull ItemStack stackA, @Nonnull ItemStack stackB)
    {
        if(stackA.isEmpty() || stackB.isEmpty() || stackA.hasTag() && !stackB.hasTag() || !stackA.hasTag() && stackB.hasTag() || stackA.getItem() != stackB.getItem())
        {
            return false;
        }
        else if(stackA.hasTag() && stackB.hasTag())
        {
            CompoundNBT tagA = stackA.getTag().copy();
            CompoundNBT tagB = stackB.getTag().copy();
            for(String s : BackTools.config.nbtCleaner)
            {
                tagA.remove(s);
                tagB.remove(s);
            }

            return tagA.equals(tagB) && stackA.areCapsCompatible(stackB);
        }
        else
        {
            return stackA.areCapsCompatible(stackB);
        }
    }

    public static class HeldInfo
    {
        public ItemEntity itemEntity = null;

        public ItemStack lastMain = ItemStack.EMPTY;
        public ItemStack lastOff = ItemStack.EMPTY;

        public ItemStack toolMain = ItemStack.EMPTY;
        public ItemStack toolOff = ItemStack.EMPTY;

        public void tick(ItemStack main, ItemStack off)
        {
            if(itemEntity != null && !itemEntity.getItem().isEmpty())
            {
                checkItem(itemEntity);

                itemEntity = null;
                return;
            }

            //check to see if we should remove the mainhand backtool
            if(areItemStacksEqualToolsIgnoreDamage(main, lastMain) || areItemStacksEqualToolsIgnoreDamage(off, lastMain))
            {
                lastMain = ItemStack.EMPTY;
            }

            //Check to see if we should remove the offhand backtool
            if(areItemStacksEqualToolsIgnoreDamage(main, lastOff) || areItemStacksEqualToolsIgnoreDamage(off, lastOff))
            {
                lastOff = ItemStack.EMPTY;
            }

            //set backtool if main tool was an item and we don't see that item anymore.
            if(!toolMain.isEmpty() && !areItemStacksEqualToolsIgnoreDamage(main, toolMain) && !areItemStacksEqualToolsIgnoreDamage(off, toolMain))
            {
                lastMain = toolMain;
                toolMain = ItemStack.EMPTY;
            }

            //set backtool if offhand tool was an item and we don't see that item anymore.
            if(!toolOff.isEmpty() && !areItemStacksEqualToolsIgnoreDamage(main, toolOff) && !areItemStacksEqualToolsIgnoreDamage(off, toolOff))
            {
                lastOff = toolOff;
                toolOff = ItemStack.EMPTY;
            }

            //is our current mainhand item a tool
            if(isItemTool(main.getItem()))
            {
                toolMain = main;
                if(areItemStacksEqualToolsIgnoreDamage(toolMain, toolOff))
                {
                    toolOff = ItemStack.EMPTY;
                }
            }

            //is our current offhand item a tool
            if(isItemTool(off.getItem()))
            {
                toolOff = off;
                if(areItemStacksEqualToolsIgnoreDamage(toolOff, toolMain))
                {
                    toolMain = ItemStack.EMPTY;
                }
            }
        }

        public void checkItem(ItemEntity item)
        {
            //check to see if we should remove the mainhand backtool
            if(areItemStacksEqualToolsIgnoreDamage(item.getItem(), lastMain))
            {
                lastMain = ItemStack.EMPTY;
            }

            if(areItemStacksEqualToolsIgnoreDamage(item.getItem(), toolMain))
            {
                toolMain = ItemStack.EMPTY;
            }

            //Check to see if we should remove the offhand backtool
            if(areItemStacksEqualToolsIgnoreDamage(item.getItem(), lastOff))
            {
                lastOff = ItemStack.EMPTY;
            }

            if(areItemStacksEqualToolsIgnoreDamage(item.getItem(), toolOff))
            {
                toolOff = ItemStack.EMPTY;
            }
        }
    }
}
