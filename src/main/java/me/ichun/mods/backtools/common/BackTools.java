package me.ichun.mods.backtools.common;

import me.ichun.mods.backtools.client.core.EventHandlerClient;
import me.ichun.mods.backtools.client.thread.ThreadCheckModSupport;
import me.ichun.mods.ichunutil.common.core.Logger;
import me.ichun.mods.ichunutil.common.iChunUtil;
import me.ichun.mods.ichunutil.common.module.update.UpdateChecker;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemSword;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.HashMap;
import java.util.HashSet;

@Mod(modid = BackTools.MOD_ID, name = BackTools.MOD_NAME,
        version = BackTools.VERSION,
        guiFactory = iChunUtil.GUI_CONFIG_FACTORY,
        dependencies = "required-after:ichunutil@[" + iChunUtil.VERSION_MAJOR +".0.2," + (iChunUtil.VERSION_MAJOR + 1) + ".0.0)",
        acceptedMinecraftVersions = iChunUtil.MC_VERSION_RANGE,
        clientSideOnly = true
)
public class BackTools
{
    public static final String MOD_NAME = "BackTools";
    public static final String MOD_ID = "backtools";
    public static final String VERSION = iChunUtil.VERSION_MAJOR + ".0.0";

    public static final Logger logger = Logger.createLogger(BackTools.MOD_NAME);

    @Instance(MOD_ID)
    public static BackTools instance;

    public static EventHandlerClient eventHandlerClient;

    public static HashMap<Class, Integer> orientationMap = new HashMap<>();

    public static HashSet<Item> blacklist = new HashSet<>();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        eventHandlerClient = new EventHandlerClient();
        MinecraftForge.EVENT_BUS.register(eventHandlerClient);

        orientationMap.put(ItemSword.class, 2);
        orientationMap.put(ItemBow.class, 1);

        (new ThreadCheckModSupport()).start();

        UpdateChecker.registerMod(new UpdateChecker.ModVersionInfo(MOD_NAME, iChunUtil.VERSION_OF_MC, VERSION, true));
    }

    @EventHandler
    public void onIMCMessage(FMLInterModComms.IMCEvent event)
    {
        for(FMLInterModComms.IMCMessage message : event.getMessages())
        {
            if(message.key.equalsIgnoreCase("blacklist") && message.isItemStackMessage())
            {
                if(!blacklist.contains(message.getItemStackValue().getItem()))
                {
                    blacklist.add(message.getItemStackValue().getItem());
                    logger.info("Registered " + message.getItemStackValue().getItem().toString() + " to Item blacklist");
                }
            }
            else if(message.key.equalsIgnoreCase("backtool") && message.isItemStackMessage())
            {
                if(!orientationMap.containsKey(message.getItemStackValue().getItem().getClass()))
                {
                    orientationMap.put(message.getItemStackValue().getItem().getClass(), message.getItemStackValue().getCount());
                    logger.warn("Registered " + message.getItemStackValue().getItem().getClass().getName() + " to backtools");
                }
            }
        }
    }

    public static int getOrientation(Class clz)
    {
        try
        {
            Integer i = orientationMap.get(clz);
            if(i == null && clz != Item.class)
            {
                return getOrientation(clz.getSuperclass());
            }
            else
            {
                return i;
            }
        }
        catch(Exception ignored){}
        return 0;
    }
}
