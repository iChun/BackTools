package me.ichun.mods.backtools.common.core;

import me.ichun.mods.backtools.client.core.EventHandler;
import me.ichun.mods.backtools.common.BackTools;
import me.ichun.mods.ichunutil.common.config.ConfigBase;
import me.ichun.mods.ichunutil.common.config.annotations.Prop;
import net.minecraft.client.Minecraft;
import net.minecraft.item.*;
import net.minecraftforge.fml.config.ModConfig;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class Config extends ConfigBase
{
    @Prop(comment = "Disabled tools, by their resource name. Eg: minecraft:diamond_hoe")
    public List<String> disabledTools = new ArrayList() {{
        add("minecraft:shield");
    }};
    @Prop(comment = "Tool orientation, by class file and degrees. Separate with \":\" . See defaults for examples.")
    public List<String> toolOrientation = new ArrayList() {{
        add(ToolItem.class.getName() + ":180");
        add(HoeItem.class.getName() + ":180");
        add(FishingRodItem.class.getName() + ":180");
        add(TridentItem.class.getName() + ":180");
        add(ShootableItem.class.getName() + ":90");
    }};
    @Prop(comment = "Some nbt tags will cause back tools to appear improperly. Use this setting to cleanse the item tags.")
    public List<String> nbtCleaner = new ArrayList() {{
        add("Damage");
        add("Charged");
        add("ChargedProjectiles");
    }};

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
        Minecraft.getInstance().execute(() -> {
            EventHandler.setupConfig();
        });
    }
}
