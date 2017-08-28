package fr.azzizcouscous.benjaminloison.skywar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;

import fr.azzizcouscous.benjaminloison.api.Consequence;
import fr.azzizcouscous.benjaminloison.api.FileAPI;
import fr.azzizcouscous.benjaminloison.api.Language;
import fr.azzizcouscous.benjaminloison.main.EventController;
import fr.azzizcouscous.benjaminloison.main.Main;

public class SkyWarManager
{
    public static final String example = "/skywar <add/remove/join/leave> [map] [XG;YG;ZG] [X0;Y0;Z0] [...]", exampleAdd = "/skywar add name XG;YG;ZG X0;Y0;Z0 X1;Y1;Z1 ...", exampleRemove = "/skywar remove name", exampleJoin = "/skywar join name";

    public static Map<String, SkyWar> skyWars = new HashMap<String, SkyWar>();

    public static void initialize()
    {
        loadSkyWarsMap();
    }

    private static void loadSkyWarsMap()
    {
        String[] skyWarsMap = new File(FileAPI.skyWarsFolder).list();
        for(int i = 0; i < skyWarsMap.length; i++)
        {
            String map = skyWarsMap[i].substring(0, skyWarsMap[i].lastIndexOf('.'));
            try
            {
                Scanner scanner = new Scanner(new File(FileAPI.skyWarsFolder + skyWarsMap[i]));
                ArrayList<Location> locations = new ArrayList<Location>();
                if(!scanner.hasNextLine())
                {
                    sendLogForMapError(skyWarsMap[i], scanner);
                    return;
                }
                while(scanner.hasNextLine())
                {
                    String line = scanner.nextLine(), coords[] = line.split(" ");
                    int[] coordsNb = new int[3];
                    if(coords.length != 3)
                    {
                        sendLogForMapError(skyWarsMap[i], scanner);
                        return;
                    }
                    for(int j = 0; j < coords.length; j++)
                    {
                        if(!StringUtils.isNumeric(coords[j]))
                        {
                            sendLogForMapError(skyWarsMap[i], scanner);
                            return;
                        }
                        coordsNb[j] = Integer.parseInt(coords[j]);
                    }
                    locations.add(new Location(Bukkit.getWorlds().get(0), coordsNb[0], coordsNb[1], coordsNb[2]));
                }
                if(locations.size() < 2)
                {
                    sendLogForMapError(skyWarsMap[i], scanner);
                    return;
                }
                Location schematic = locations.get(0);
                locations.remove(schematic);
                scanner.close();
                skyWars.put(map, new SkyWar(map, schematic, locations));
            }
            catch(FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void addSkyWar(String[] args, Player p)
    {
        if(args.length < 5)
        {
            EventController.sendYouHaveToUseThisCommandLikeThis(p, exampleAdd);
            return;
        }
        ArrayList<Location> locs = new ArrayList<Location>();
        for(int i = 3; i < args.length; i++)
        {
            String[] pos = args[i].split(";");
            if(pos.length != 3)
            {
                EventController.sendYouHaveToUseThisCommandLikeThis(p, exampleAdd);
                return;
            }
            int[] positions = new int[3];
            for(int j = 0; j < pos.length; j++)
                if(!StringUtils.isNumeric(pos[j]))
                {
                    EventController.sendYouHaveToUseThisCommandLikeThis(p, exampleAdd);
                    return;
                }
                else
                    positions[j] = Integer.parseInt(pos[j]);
            locs.add(new Location(p.getWorld(), positions[0], positions[1], positions[2]));
        }
        Location schematic = locs.get(0);
        locs.remove(schematic);
        SkyWarManager.addSkyWar(p, args[2], schematic, locs);
    }

    public static void addSkyWar(Player p, String map, Location schematic, ArrayList<Location> loc)
    {
        String path = FileAPI.skyWarsFolder + map + ".txt";
        if(new File(path).exists())
        {
            p.sendMessage(Language.translate("§cPlease remove this SkyWar first !"));
            return;
        }
        String toWrite = toString(schematic) + '\n';
        for(int i = 0; i < loc.size(); i++)
        {
            toWrite += toString(loc.get(i));
            if(i != loc.size() - 1)
                toWrite += '\n';
        }
        p.sendMessage(Language.translate("§cLoading the SkyWar's map..."));
        if(!loadSchematic(p, map, new Vector(schematic.getBlockX(), schematic.getBlockY(), schematic.getBlockZ())))
            return;
        FileAPI.write(path, toWrite);
        skyWars.put(map, new SkyWar(map, schematic, loc));
        p.sendMessage(Language.translate("§aSkyWar added !"));
    }

    private static String toString(Location location)
    {
        return location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
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

    public static void ejectIfPlayerWasInASkyWar(Player p)
    {
        SkyWar skyWar = skyWarOf(p);
        if(skyWar == null)
            return;
        skyWar.players.remove(p);
        skyWar.checkEnd();
    }

    private static void sendLogForMapError(String path, Scanner scanner)
    {
        Main.warn("There is an error in the map file: " + path + "\nNormally it must seem like this:" + "\n0 5 0" + "\n10 5 10" + "\n20 5 20");
        scanner.close();
    }

    private static void printPlayerOrServer(Player p, String message, Consequence consequence)
    {
        if(p == null)
        {
            if(consequence == Consequence.Warn)
                Main.warn(message);
            else if(consequence == Consequence.Fatal)
                Main.fatal(message);
        }
        else
            p.sendMessage(message);
    }

    @SuppressWarnings("deprecation")
    public static boolean loadSchematic(Player p, String str, Vector origin)
    {
        File file = new File(FileAPI.schematicsFolder + str + ".schematic");
        if(!file.exists())
        {
            printPlayerOrServer(p, Language.translate("§cThe file: ") + file.getAbsolutePath() + Language.translate(" doesn't exist !"), Consequence.Warn);
            return false;
        }
        EditSession es = new EditSession(new BukkitWorld(Bukkit.getWorlds().get(0)), Integer.MAX_VALUE);
        CuboidClipboard cc;
        try
        {
            cc = CuboidClipboard.loadSchematic(file);
            cc.paste(es, origin, false);
        }
        catch(DataException | IOException e1)
        {
            printPlayerOrServer(p, Language.translate("§4THE FILE: ") + file.getAbsolutePath() + Language.translate(" HAS A VERY IMPORTANT PROBLEM !!! PLEASE CHECK THE LOGS !!!"), Consequence.Fatal);
            Main.fatal(Language.translate("THERE WAS A PROBLEM WHILE LOADING THE SCHEMATIC !!! MAY BE A DATA EXCEPTION"));
            e1.printStackTrace();
        }
        catch(MaxChangedBlocksException e)
        {
            printPlayerOrServer(p, Language.translate("THERE WAS A PROBLEM WHILE LOADING THE SCHEMATIC !!! THE NUMBER OF BLOCKS CHANGED WAS OVER: ") + Integer.MAX_VALUE, Consequence.Fatal);
            e.printStackTrace();
        }
        catch(Exception e)
        {
            return false;
        }
        return true;
    }

    public static SkyWar skyWarOf(Player p)
    {
        Collection<SkyWar> skyWars = SkyWarManager.skyWars.values();
        Iterator<SkyWar> itSkyWar = skyWars.iterator();
        while(itSkyWar.hasNext())
        {
            SkyWar skyWar = itSkyWar.next();
            if(skyWar.players.contains(p))
                return skyWar;
        }
        return null;
    }

    public static Vector vectorFromLocation(Location location)
    {
        return new Vector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
