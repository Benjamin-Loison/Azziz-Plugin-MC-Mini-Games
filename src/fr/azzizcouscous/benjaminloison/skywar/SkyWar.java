package fr.azzizcouscous.benjaminloison.skywar;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import fr.azzizcouscous.benjaminloison.api.Language;

public class SkyWar
{
    public String map;
    public Location schematic;
    public Location[] locations;
    public boolean waitingPlayers = true;
    public ArrayList<Player> players = new ArrayList<Player>();

    public SkyWar(String map, Location schematic, ArrayList<Location> locations)
    {
        this.map = map;
        this.schematic = schematic;
        this.locations = new Location[locations.size()];
        for(int i = 0; i < this.locations.length; i++)
            this.locations[i] = locations.get(i);
    }

    public void join(Player p)
    {
        if(!players.contains(p))
            if(waitingPlayers)
                if(players.size() < locations.length)
                {
                    players.add(p);
                    p.sendMessage(Language.translate("§aYou have entered this SkyWar !"));
                    int playersNeeded = playersNeeded();
                    if(playersNeeded != 0)
                        if(playersNeeded == 1)
                            p.sendMessage(Language.translate("§bPlease wait §e" + playersNeeded + "§b player..."));
                        else
                            p.sendMessage(Language.translate("§bPlease wait §e" + playersNeeded + "§b players..."));
                    else
                        start();
                }
                else
                    p.sendMessage(Language.translate("§cThis SkyWar is full !"));
            else
                p.sendMessage(Language.translate("§cThis SkyWar has already begun !"));
        else
            p.sendMessage(Language.translate("§cYou are already in this SkyWar !"));
    }

    public void leave(Player p)
    {
        if(players.contains(p))
        {
            players.remove(p);
            p.sendMessage(Language.translate("§aYou have left this SkyWar !"));
            checkEnd();
        }
        else
            p.sendMessage(Language.translate("§cYou are not in this SkyWar !"));
    }

    private int playersNeeded()
    {
        return locations.length - players.size();
    }

    private void start()
    {
        waitingPlayers = false;
        Random random = new Random();
        boolean[] locationsUsed = new boolean[locations.length];
        for(int i = 0; i < players.size(); i++)
        {
            int rand = 0;
            do
                rand = random.nextInt(locations.length);
            while(locationsUsed[rand]);
            locationsUsed[rand] = true;
            Player p = players.get(i);
            clearPlayer(p);
            p.teleport(locations[rand]);
            p.sendMessage(Language.translate("§aThe SkyWar has begun !"));
        }
    }

    private void end()
    {
        if(players.size() == 1)
        {
            Player p = players.get(0);
            p.sendMessage(Language.translate("§aYou have won the SkyWar !"));
            clearPlayer(p);
            p.performCommand("/spawn");
        }
        Player p = null;
        SkyWarManager.loadSchematic(p, map, SkyWarManager.vectorFromLocation(schematic));
        waitingPlayers = true;
    }

    private void clearPlayer(Player p)
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
