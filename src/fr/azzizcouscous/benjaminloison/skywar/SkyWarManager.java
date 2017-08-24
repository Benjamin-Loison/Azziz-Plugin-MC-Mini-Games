package fr.azzizcouscous.benjaminloison.skywar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.azzizcouscous.benjaminloison.api.FileAPI;
import fr.azzizcouscous.benjaminloison.api.Language;

public class SkyWarManager
{
    static Map<String, SkyWar> skyWars = new HashMap<String, SkyWar>();
    static Map<Player, String> skyWarPlayers = new HashMap<Player, String>(); 

    public static void initialize()
    {
        loadSkyWarsMap();
    }
    
    private static void loadSkyWarsMap()
    {
        
    }
    
    public static void sendSkyWarCommandHelp(Player player)
    {
        player.sendMessage(Language.translate("Command help: /skywar "));
    }

    public static void addSkyWar(String map, byte minimalPlayer, ArrayList<Location> loc)
    {
        String locs = "";
        for(int i = 0; i < locs.length(); i++)
        {
            Location location = loc.get(i);
            locs += location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
            if(i != locs.length() - 1)
                locs += "\n";
        }
        FileAPI.write(FileAPI.skyWarsFolder + map + ".txt", locs);
        
        Location[] locations = new Location[loc.size()];
        for(int i = 0; i < locations.length; i++)
            locations[i] = loc.get(i);
        skyWars.put(map, new SkyWar(map, minimalPlayer, locations));
    }

    public static boolean removeSkyWar(String map)
    {
        File mapFile = new File(FileAPI.skyWarsFolder + map + ".txt");
        if(!mapFile.exists())
            return false;
        mapFile.delete();
        skyWars.remove(map);
        return true;
    }
    
    public static boolean skyWarExist(String map)
    {
        if(skyWars.containsKey(map))
            return true;
        return false;
    }
}
