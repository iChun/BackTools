package me.ichun.mods.backtools.common;

import me.ichun.mods.backtools.client.core.EventHandler;
import me.ichun.mods.backtools.common.core.Config;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;

@Mod(BackTools.MOD_ID)
public class BackTools //TODO remember to depend on iChunUtil, scale config, whiteblacklist config
{
    public static final String MOD_ID = "backtools";
    public static final String MOD_NAME = "Back Tools";

    public static final Logger LOGGER = LogManager.getLogger();

    public static Config config;

    public static HashMap<Class<? extends Item>, Integer> imcOrientation = new HashMap<>();
    public static HashSet<ResourceLocation> imcDisabledTools = new HashSet<>();

    public BackTools()
    {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            setupConfig();
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::finishLoading);
            ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> me.ichun.mods.ichunutil.client.core.EventHandlerClient::getConfigGui);
        });
        DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> () -> LOGGER.log(Level.ERROR, "You are loading " + MOD_NAME + " on a server. " + MOD_NAME + " is a client only mod!"));

        //Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }

    private void setupConfig()
    {
        config = new Config().init();
    }

    @OnlyIn(Dist.CLIENT)
    private void finishLoading(FMLLoadCompleteEvent event)
    {
        EventHandler.addLayers();
    }

    @OnlyIn(Dist.CLIENT)
    private void processIMC(InterModProcessEvent event)
    {
        event.getIMCStream(m -> m.equalsIgnoreCase("blacklist")).forEach(msg -> {  //Supports Item Stacks, Items, Resource Locations, Strings
            Object o = msg.getMessageSupplier().get();
            if(o instanceof ItemStack)
            {
                ItemStack is = (ItemStack)o;
                if(!is.isEmpty() && imcDisabledTools.add(is.getItem().getRegistryName()))
                {
                    LOGGER.info("IMC-{}: Disabled {}", msg.getSenderModId(), is.getItem().getRegistryName());
                }
                else
                {
                    LOGGER.warn("IMC-{}: Unable to disable: {}", msg.getSenderModId(), is);
                }
            }
            else if(o instanceof Item)
            {
                Item item = (Item)o;
                if(imcDisabledTools.add(item.getRegistryName()))
                {
                    LOGGER.info("IMC-{}: Disabled {}", msg.getSenderModId(), item);
                }
                else
                {
                    LOGGER.warn("IMC-{}: Unable to disable: {}", msg.getSenderModId(), item);
                }
            }
            else if(o instanceof ResourceLocation)
            {
                ResourceLocation rl = (ResourceLocation)o;
                if(imcDisabledTools.add(rl))
                {
                    LOGGER.info("IMC-{}: Disabled {}", msg.getSenderModId(), rl);
                }
                else
                {
                    LOGGER.warn("IMC-{}: Unable to disable: {}", msg.getSenderModId(), rl);
                }
            }
            else if(o instanceof String)
            {
                String s = (String)o;
                if(imcDisabledTools.add(new ResourceLocation(s)))
                {
                    LOGGER.info("IMC-{}: Disabled {}", msg.getSenderModId(), s);
                }
                else
                {
                    LOGGER.warn("IMC-{}: Unable to disable: {}", msg.getSenderModId(), s);
                }
            }
        });
        event.getIMCStream(m -> m.equalsIgnoreCase("orientation")).forEach(msg -> {
            Object o = msg.getMessageSupplier().get();
            if(!(o instanceof String))
            {
                BackTools.LOGGER.warn("IMC-{}: Passed object is not a string: {}", msg.getSenderModId(), o);
                return;
            }
            String s = (String)o;

            String[] split = new String[2];
            int index = s.indexOf(':');
            if (index > 0) {
                split[0] = s.substring(0, index);
                split[1] = s.substring(index + 1);
            }
            else
            {
                BackTools.LOGGER.warn("IMC-{}: Could not parse orientation: {}", msg.getSenderModId(), s);
            }
            try
            {
                Class clz = Class.forName(split[0]);
                if(Item.class.isAssignableFrom(clz))
                {
                    imcOrientation.put(clz, Integer.parseInt(split[1]));
                }
                else
                {
                    BackTools.LOGGER.warn("IMC-{}: Class does not extend Item class: {}", msg.getSenderModId(), split[0]);
                }
            }
            catch(ClassNotFoundException e)
            {
                BackTools.LOGGER.warn("IMC-{}: Could not find class to add orientation: {}", msg.getSenderModId(), split[0]);
            }
            catch(NumberFormatException e)
            {
                BackTools.LOGGER.warn("IMC-{}: Could not parse integer: {}", msg.getSenderModId(), s);
            }
        });
    }
}
