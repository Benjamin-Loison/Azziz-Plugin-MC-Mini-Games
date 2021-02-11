package fr.mcminigames.benjaminloison.game;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import fr.mcminigames.benjaminloison.api.Language;
import fr.mcminigames.benjaminloison.group.Group;
import fr.mcminigames.benjaminloison.group.GroupManager;

public class Game
{
    public String map, type;
    public Location schematic;
    public ArrayList<Location> locations = new ArrayList<Location>();
    public boolean isCreated = true, waitingPlayers = true;
    public ArrayList<Player> players = new ArrayList<Player>();

    public Game(String map, String type, Location schematic)
    {
        this.map = map;
        this.type = type;
        this.schematic = schematic;
        GameManager.games.put(map, this);
    }

    public Game(String map, String type, Location schematic, boolean helped)
    {
        this(map, type, schematic);
        isCreated = false;
    }

    public Game(String map, String type, Location schematic, ArrayList<Location> locations)
    {
        this(map, type, schematic);
        this.locations = locations;
    }

    public void add(Player p)
    {
        Location loc = p.getLocation();
        locations.add(loc);
        p.sendMessage(Language.translate("§aLocation ") + locations.size() + Language.translate(" added with coordinates: ") + GameManager.toString(loc));
    }

    public void finish(Player p)
    {
        if(isCreated)
            p.sendMessage(Language.translate("§cThis ") + Language.translate(type) + Language.translate("§c was already created !"));
        else if(locations.size() == 0)
            p.sendMessage(Language.translate("§cPlease add spawn location before finishing !"));
        else if(GameManager.addGame(p, map, type, schematic, locations))
        {
            GameManager.addingGames.remove(p);
            isCreated = true;
        }
    }

    public void join(Player p)
    {
        if(isCreated)
            if(!players.contains(p))
                if(waitingPlayers)
                    if(players.size() < locations.size())
                    {
                        Group gp = GroupManager.groupOf(p);
                        if(gp != null)
                        {
                            ArrayList<Player> pls = gp.players;
                            if(pls.size() > locations.size() - players.size())
                            {
                                p.sendMessage(Language.translate("§cThis") + Language.translate(type) + Language.translate("§c is full !"));
                                return;
                            }
                            else if(gp.chief != p)
                                p.sendMessage(Language.translate("§cYou are not the chief of the group !"));
                            else
                                for(int i = 0; i < pls.size(); i++)
                                    addPlayer(pls.get(i));
                        }
                        else
                            addPlayer(p);
                    }
                    else
                        p.sendMessage(Language.translate("§cThis") + Language.translate(type) + Language.translate("§c is full !"));
                else
                    p.sendMessage(Language.translate("§cThis") + Language.translate(type) + Language.translate("§c has already begun !"));
            else
                p.sendMessage(Language.translate("§cYou are already in this ") + Language.translate(type));
        else
            p.sendMessage(Language.translate("§cThis ") + Language.translate(type) + Language.translate("§c is not created yet !"));
    }
    
    private void addPlayer(Player p)
    {
        players.add(p);
        p.sendMessage(Language.translate("§aYou have entered the ") + Language.translate(type) + ": " + map);
        int playersNeeded = playersNeeded();
        if(playersNeeded != 0)
            sendWaitMessage(p);
        else
            start();
    }
    
    private void sendWaitMessage(Player p) // TODO: Not needed for all - 1 iteration of players in a group
    {
        int playersNeeded = playersNeeded();
        if(playersNeeded == 1)
            p.sendMessage(Language.translate("§bPlease wait §e") + playersNeeded + Language.translate("§b player..."));
        else
            p.sendMessage(Language.translate("§bPlease wait §e") + playersNeeded + Language.translate("§b players..."));
    }

    public void leave(Player p)
    {
        if(players.contains(p))
        {
            players.remove(p);
            p.sendMessage(Language.translate("§aYou have left this ") + Language.translate(type));
            checkEnd();
        }
        else
            p.sendMessage(Language.translate("§cYou are not in this ") + Language.translate(type));
    }

    private int playersNeeded()
    {
        return locations.size() - players.size();
    }

    private void start()
    {
        waitingPlayers = false;
        Random random = new Random();
        boolean[] locationsUsed = new boolean[locations.size()];
        for(int i = 0; i < players.size(); i++)
        {
            int rand = 0;
            do
                rand = random.nextInt(locations.size());
            while(locationsUsed[rand]);
            locationsUsed[rand] = true;
            Player p = players.get(i);
            clearPlayer(p);
            p.teleport(locations.get(rand));
            p.sendMessage(Language.translate("§aThe ") + Language.translate(type) + Language.translate("§a has begun !"));
        }
    }

    private void end()
    {
        if(players.size() == 1)
        {
            Player p = players.get(0);
            p.sendMessage(Language.translate("§aYou have won the ") + Language.translate(type));
            clearPlayer(p);
            p.performCommand("/spawn");
            players.remove(p);
        }
        Player p = null;
        GameManager.loadSchematic(p, map, GameManager.vectorFromLocation(schematic), false);
        waitingPlayers = true;
    }

    void clearPlayer(Player p)
    {
        p.setGameMode(GameMode.SURVIVAL);
        p.setExp(0);
        p.setLevel(0);
        p.setFoodLevel(20);
        p.setHealth(p.getMaxHealth());
        PlayerInventory pInv = p.getInventory();
        pInv.clear();
        pInv.setArmorContents(null);
    }

    public void checkEnd()
    {
        if(players.size() <= 1)
            end();
    }
}
