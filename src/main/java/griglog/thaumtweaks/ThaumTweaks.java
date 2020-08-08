package griglog.thaumtweaks;

import griglog.thaumtweaks.handlers.TTHandler;
import griglog.thaumtweaks.items.TTMaterials;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Mod(modid = ThaumTweaks.MODID, name = ThaumTweaks.NAME, version = ThaumTweaks.VERSION, acceptedMinecraftVersions = ThaumTweaks.MC_VERSION,
dependencies="required-after:baubles@[1.5.2, ); after:thaumcraft@[6.1.BETA26, )")
public class ThaumTweaks
{
    public static final String MODID = "thaumtweaks";
    public static final String NAME = "ThaumTweaks";
    public static final String VERSION = "0.0.1";
    public static final String MC_VERSION = "[1.12.2]";

    public static final Logger LOGGER = LogManager.getLogger(ThaumTweaks.MODID);
    //public static final CreativeTabs MOD_TAB = new CreativeTab();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) throws IOException, NoSuchMethodException {
        TTMaterials.overrideMaterials();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) throws NoSuchMethodException {
        //BookTab.setup();
    }



}
