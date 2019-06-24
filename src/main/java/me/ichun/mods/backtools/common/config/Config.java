package me.ichun.mods.backtools.common.config;

import me.ichun.mods.backtools.common.BackTools;
import me.ichun.mods.ichunutil.common.core.config.ConfigBase;
import me.ichun.mods.ichunutil.common.core.config.annotations.ConfigProp;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;

public class Config extends ConfigBase
{
    @ConfigProp(changeable = false, side = Side.CLIENT)
    public String[] blacklistedItemClasses = new String[0];

    public Config(File file)
    {
        super(file);
    }

    @Override
    public String getModId()
    {
        return BackTools.MOD_ID;
    }

    @Override
    public String getModName()
    {
        return BackTools.MOD_NAME;
    }
}
