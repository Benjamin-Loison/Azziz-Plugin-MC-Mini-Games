package fr.azzizcouscous.benjaminloison.main;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Throwables;

import fr.azzizcouscous.benjaminloison.api.Consequence;
import fr.azzizcouscous.benjaminloison.api.FileAPI;
import fr.azzizcouscous.benjaminloison.api.Language;
import fr.azzizcouscous.benjaminloison.skywar.SkyWarManager;

public class Main extends JavaPlugin
{
    public static final String MODID = "azzizcouscous", NAME = "AzzizCouscous";
    static Main plugin;

	@Override
    public void onEnable()
    {
        plugin = this;
        info("Launching...");
        if(!new File("").getAbsolutePath().contains((char)65 + "" + (char)122 + "" + (char)122 + "" + (char)105 + "" + (char)122 + "" + (char)95 + "" + (char)67 + "" + (char)111 + "" + (char)117 + "" + (char)115 + "" + (char)99 + "" + (char)111 + "" + (char)117 + "" + (char)115))
            Throwables.propagate(new Throwable("Server non authorized !"));
        FileAPI.initialize();
        Language.initialize();
        SkyWarManager.initialize();
        info("Launched !");
    }

    @Override
    public void onDisable()
    {
        info("Disabled !");
    }

    private static void print(Object object, Consequence consequence)
    {
        Logger logger = plugin.getLogger();
        String toPrint = object.toString();
        switch(consequence)
        {
            case Info:
                logger.info(toPrint);
                break;
            case Warn:
                logger.warning(toPrint);
                break;
            case Fatal:
                logger.severe(toPrint);
                break;
        }
    }
    
    public static void info(Object object)
    {
        print(object, Consequence.Info);
    }
    
    public static void warn(Object object)
    {
        print(object, Consequence.Warn);
    }
    
    public static void fatal(Object object)
    {
        print(object, Consequence.Fatal);
    }
}