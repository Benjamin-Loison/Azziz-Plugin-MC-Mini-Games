package fr.azzizcouscous.benjaminloison.main;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import fr.azzizcouscous.benjaminloison.api.Language;
import fr.azzizcouscous.benjaminloison.skywar.SkyWarManager;

public class EventController implements Listener
{
    Main pl;

    public EventController(Main p)
    {
        p.getServer().getPluginManager().registerEvents(this, p);
        pl = p;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e)
    {
        String m = e.getMessage(), args[] = m.split(" ");
        Player p = e.getPlayer();
        World w = p.getWorld();
        if(args[0].equalsIgnoreCase("/skywar"))
        {
            e.setCancelled(true);
            if(args.length < 3)
                SkyWarManager.sendSkyWarCommandHelp(p);
            else
            {
                if(args[1].equalsIgnoreCase("add"))
                {
                    if(p.isOp())
                    {
                        String example = "/skywar add name minimalPlayerNumber X0;Y0;Z0 X1;Y1;Z1 ...";
                        if(args.length < 5)
                        {
                            sendYouHaveToUseThisCommandLikeThis(p, example);
                            return;
                        }
                        if(!StringUtils.isNumeric(args[3]))
                        {
                            sendYouHaveToUseThisCommandLikeThis(p, example);
                            return;
                        } 
                        ArrayList<Location> locs = new ArrayList<Location>();
                        for(int i = 4; i < args.length; i++)
                        {
                            String[] pos = args[i].split(";");
                            if(pos.length != 3)
                            {
                                sendYouHaveToUseThisCommandLikeThis(p, example);
                                return;
                            }
                            int[] positions = new int[3];
                            for(int j = 0; j < pos.length; j++)
                                if(!StringUtils.isNumeric(pos[j]))
                                {
                                    sendYouHaveToUseThisCommandLikeThis(p, example);
                                    return;
                                }
                                else
                                    positions[j] = Integer.parseInt(pos[j]);
                            locs.add(new Location(w, positions[0], positions[1], positions[2]));
                        }
                        SkyWarManager.addSkyWar(args[2], Byte.parseByte(args[3]), locs);
                        p.sendMessage(Language.translate("SkyWar added !"));
                    }
                    else
                        sendOpRequireError(p);
                }

                else if(args[1].equalsIgnoreCase("remove"))
                {
                    if(p.isOp())
                        if(SkyWarManager.removeSkyWar(args[2]))
                            p.sendMessage(Language.translate("SkyWar removed !"));
                        else
                            p.sendMessage(Language.translate("This SkyWar doesn't exist !"));
                    else
                        sendOpRequireError(p);
                }

                else if(args[1].equalsIgnoreCase("join"))
                {
                    
                }

                else if(args[1].equalsIgnoreCase("leave"))
                {
                    
                }
            }
        }
    }

    private void sendYouHaveToUseThisCommandLikeThis(Player p, String command)
    {
        p.sendMessage(Language.translate("You have to use the command like this: ") + command);
    }

    private void sendOpRequireError(Player p)
    {
        p.sendMessage(Language.translate("You need to be an operator !"));
    }
}