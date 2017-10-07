package fr.azzizcouscous.benjaminloison.game;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

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

public class GameManager
{
    public static final String example = "/game <load/spawn/finish/add/remove/join/leave> [map] [XG;YG;ZG] [X0;Y0;Z0] [...]", exampleLoad = "/game load name type", exampleAdd = "/game add name type XG;YG;ZG X0;Y0;Z0 [X1;Y1;Z1] [...]", exampleRemove = "/game remove name", exampleJoin = "/game join name";
    public static Map<String, Game> games = new HashMap<String, Game>();
    public static Map<Player, Game> addingGames = new HashMap<Player, Game>();

    public static void initialize()
    {
        loadGamesMap();
    }

    private static void loadGamesMap()
    {
        String[] gamesMap = new File(FileAPI.gamesFolder).list();
        for(int i = 0; i < gamesMap.length; i++)
        {
            String map = gamesMap[i].substring(0, gamesMap[i].lastIndexOf('-'));
            try
            {
                Scanner scanner = new Scanner(new File(FileAPI.gamesFolder + gamesMap[i]));
                ArrayList<Location> locations = new ArrayList<Location>();
                if(!scanner.hasNextLine())
                {
                    sendLogForMapError(gamesMap[i], scanner);
                    return;
                }
                while(scanner.hasNextLine())
                {
                    String line = scanner.nextLine(), coords[] = line.split(" ");
                    int[] coordsNb = new int[3];
                    if(coords.length != 3)
                    {
                        sendLogForMapError(gamesMap[i], scanner);
                        return;
                    }
                    for(int j = 0; j < coords.length; j++)
                    {
                        if(!FileAPI.isNumeric(coords[j]))
                        {
                            sendLogForMapError(gamesMap[i], scanner);
                            return;
                        }
                        coordsNb[j] = Integer.parseInt(coords[j]);
                    }
                    locations.add(new Location(Bukkit.getWorlds().get(0), coordsNb[0], coordsNb[1], coordsNb[2]));
                }
                if(locations.size() < 2)
                {
                    sendLogForMapError(gamesMap[i], scanner);
                    return;
                }
                Location schematic = locations.get(0);
                locations.remove(schematic);
                scanner.close();
                new Game(map, gamesMap[i].substring(gamesMap[i].lastIndexOf('-') + 1, gamesMap[i].lastIndexOf('.')), schematic, locations);
            }
            catch(FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void addGame(String[] args, Player p)
    {
        if(args.length < 6)
        {
            EventController.sendYouHaveToUseThisCommandLikeThis(p, exampleAdd);
            return;
        }
        ArrayList<Location> locs = new ArrayList<Location>();
        for(int i = 4; i < args.length; i++)
        {
            String[] pos = args[i].split(";");
            if(pos.length != 3)
            {
                EventController.sendYouHaveToUseThisCommandLikeThis(p, exampleAdd);
                return;
            }
            int[] positions = new int[3];
            for(int j = 0; j < pos.length; j++)
                if(!FileAPI.isNumeric(pos[j]))
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
        addGame(p, args[2], args[3], schematic, locs);
    }

    public static boolean addGame(Player p, String map, String type, Location schematic, ArrayList<Location> loc)
    {
        String path = FileAPI.gamesFolder + map + "-" + type + ".txt";
        if(new File(path).exists())
        {
            p.sendMessage(Language.translate("§cPlease remove first this ") + Language.translate(type));
            return false;
        }
        String toWrite = toString(schematic) + '\n';
        for(int i = 0; i < loc.size(); i++)
        {
            toWrite += toString(loc.get(i));
            if(i != loc.size() - 1)
                toWrite += '\n';
        }
        p.sendMessage(Language.translate("§cLoading the ") + Language.translate(type) + Language.translate("§c's map..."));
        if(!loadSchematic(p, map, new Vector(schematic.getBlockX(), schematic.getBlockY(), schematic.getBlockZ()), false))
            return false;
        FileAPI.write(path, toWrite);
        new Game(map, type, schematic, loc);
        p.sendMessage(Language.translate(type) + Language.translate("§a added !"));
        return true;
    }

    public static String toString(Location location)
    {
        return location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
    }

    public static boolean removeGame(String map)
    {
        File mapFile = new File(FileAPI.gamesFolder + map + "-" + getType(map) + ".txt");
        if(!mapFile.exists())
            return false;
        mapFile.delete();
        games.remove(map);
        return true;
    }

    public static boolean gameExist(String map)
    {
        if(games.containsKey(map))
            return true;
        return false;
    }

    public static String getType(String map)
    {
        if(games.containsKey(map))
            return games.get(map).type;
        return null;
    }

    public static void ejectIfPlayerWasInAGame(Player p)
    {
        Game game = gameOf(p);
        if(game == null)
            return;
        p.sendMessage(Language.translate("§aYou have lost the ") + Language.translate(game.type));
        game.clearPlayer(p);
        game.players.remove(p);
        game.checkEnd();
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
    public static boolean loadSchematic(Player p, String str, Vector origin, boolean onlyLoad)
    {
        File file = new File(FileAPI.schematicsFolder + str + ".schematic");
        if(!file.exists())
        {
            printPlayerOrServer(p, Language.translate("§cThe file: ") + file.getAbsolutePath() + Language.translate(" doesn't exist !"), Consequence.Warn);
            return false;
        }
        EditSession es = new EditSession(new BukkitWorld(Bukkit.getWorlds().get(0)), Integer.MAX_VALUE);
        try
        {
            CuboidClipboard cc = CuboidClipboard.loadSchematic(file);
            if(!onlyLoad)
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

    public static Game gameOf(Player p)
    {
        Collection<Game> games = GameManager.games.values();
        Iterator<Game> itGame = games.iterator();
        while(itGame.hasNext())
        {
            Game game = itGame.next();
            if(game.players.contains(p))
                return game;
        }
        return null;
    }

    public static Vector vectorFromLocation(Location location)
    {
        return new Vector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static boolean isValidType(String type)
    {
        if(type.equals("SkyWar") || type.equals("PvPSoup"))
            return true;
        return false;
    }

    public static void load(Player p, String map, String type)
    {
        if(gameExist(map))
        {
            p.sendMessage(Language.translate("§cPlease remove first this ") + Language.translate(getType(map)));
            return;
        }
        if(!isValidType(type))
        {
            p.sendMessage(Language.translate("§cThe type: ") + type + Language.translate(" is invalid !"));
            return;
        }
        if(!loadSchematic(p, map, new Vector(), true))
            return;
        Location loc = p.getLocation();
        addingGames.put(p, new Game(map, type, loc, true));
        p.performCommand("/schem load " + map);
        p.performCommand("/paste");
        String locs = toString(loc);
        p.sendMessage(Language.translate("§bYour coordinates while loading the schematic were: ") + locs);
        p.sendMessage(Language.translate("§5We recommend you to use /tp ") + p.getDisplayName() + " " + locs + Language.translate("§5 to adjust your needs when loading again after using //undo to remove your actual schematic !"));
        p.sendMessage(Language.translate("§bThis command is used only to check the position and width and height of the schematic ! Please use //undo before using /game add"));
    }
}
