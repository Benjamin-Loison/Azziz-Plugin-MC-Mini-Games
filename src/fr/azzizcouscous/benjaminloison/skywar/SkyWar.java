package fr.azzizcouscous.benjaminloison.skywar;

import org.bukkit.Location;

public class SkyWar
{
    public String map;
    public byte minimalPlayer;
    public Location[] locations;
    
    public SkyWar(String map, byte minimumPlayers, Location[] locations)
    {
        this.map = map;
        this.minimalPlayer = minimumPlayers;
        this.locations = locations;
    }
}
