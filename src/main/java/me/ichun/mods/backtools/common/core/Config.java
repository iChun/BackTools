package me.ichun.mods.backtools.common.core;

import me.ichun.mods.backtools.client.core.EventHandler;
import me.ichun.mods.backtools.common.BackTools;
import me.ichun.mods.ichunutil.common.config.ConfigBase;
import me.ichun.mods.ichunutil.common.config.annotations.CategoryDivider;
import me.ichun.mods.ichunutil.common.config.annotations.Prop;
import net.minecraft.client.Minecraft;
import net.minecraft.item.*;
import net.minecraft.util.Util;
import net.minecraftforge.fml.config.ModConfig;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class Config extends ConfigBase
{
    @CategoryDivider(name = "clientOnly")
    public List<String> enabledToolsID = new ArrayList<>();

    public List<String> enabledToolsClass = Util.make(new ArrayList<>(), list -> {
        list.add(TieredItem.class.getName());
        list.add(ShootableItem.class.getName());
        list.add(ShearsItem.class.getName());
        list.add(FishingRodItem.class.getName());
        list.add(TridentItem.class.getName());
    });

    public List<String> disabledToolsID = Util.make(new ArrayList<>(), list -> {
        list.add("minecraft:shield");
    });

    public List<String> disabledToolsClass = new ArrayList<>();

    public List<String> toolOrientation = Util.make(new ArrayList<>(), list -> {
        list.add(ToolItem.class.getName() + ":180");
        list.add(HoeItem.class.getName() + ":180");
        list.add(FishingRodItem.class.getName() + ":180");
        list.add(TridentItem.class.getName() + ":180");
        list.add(ShootableItem.class.getName() + ":90");
    });

    public List<String> nbtCleaner = Util.make(new ArrayList<>(), list -> {
        list.add("Damage");
        list.add("Charged");
        list.add("ChargedProjectiles");
    });

    @Nonnull
    @Override
    public String getModId()
    {
        return BackTools.MOD_ID;
    }

    @Nonnull
    @Override
    public String getConfigName()
    {
        return BackTools.MOD_NAME;
    }

    @Nonnull
    @Override
    public ModConfig.Type getConfigType()
    {
        return ModConfig.Type.CLIENT;
    }

    @Override
    public void onConfigLoaded()
    {
        Minecraft.getInstance().execute(EventHandler::setupConfig);
    }
}
