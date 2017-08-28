package fr.azzizcouscous.benjaminloison.main;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.azzizcouscous.benjaminloison.api.Language;
import fr.azzizcouscous.benjaminloison.skywar.SkyWar;
import fr.azzizcouscous.benjaminloison.skywar.SkyWarManager;

public class EventController implements Listener
{
    public EventController()
    {
        Main.plugin.getServer().getPluginManager().registerEvents(this, Main.plugin);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e)
    {
        String args[] = e.getMessage().split(" ");
        Player p = e.getPlayer();
        if(args[0].equalsIgnoreCase("/skywar"))
        {
            e.setCancelled(true);
            if(args.length < 3)
            {
                if(args.length > 1)
                {
                    if(args[1].equalsIgnoreCase("leave"))
                    {
                        SkyWar skyWar = SkyWarManager.skyWarOf(p);
                        if(skyWar != null)
                            skyWar.leave(p);
                        else
                            p.sendMessage(Language.translate("§cYou are not in a SkyWar !"));
                    }
                    else if(args[1].equalsIgnoreCase("join"))
                        EventController.sendYouHaveToUseThisCommandLikeThis(p, SkyWarManager.exampleJoin);
                    else if(args[1].equalsIgnoreCase("add"))
                        EventController.sendYouHaveToUseThisCommandLikeThis(p, SkyWarManager.exampleAdd);
                    else if(args[1].equalsIgnoreCase("remove"))
                        EventController.sendYouHaveToUseThisCommandLikeThis(p, SkyWarManager.exampleRemove);
                    else
                        EventController.sendYouHaveToUseThisCommandLikeThis(p, SkyWarManager.example);
                }
                else
                    EventController.sendYouHaveToUseThisCommandLikeThis(p, SkyWarManager.example);
            }
            else
            {
                if(args[1].equalsIgnoreCase("add"))
                {
                    if(p.isOp())
                        SkyWarManager.addSkyWar(args, p);
                    else
                        sendOpRequireError(p);
                }
                else if(args[1].equalsIgnoreCase("remove"))
                {
                    if(p.isOp())
                        if(SkyWarManager.removeSkyWar(args[2]))
                            p.sendMessage(Language.translate("§aSkyWar removed !"));
                        else
                            p.sendMessage(Language.translate("§cThis SkyWar doesn't exist !"));
                    else
                        sendOpRequireError(p);
                }
                else if(args[1].equalsIgnoreCase("join"))
                {
                    SkyWar skyWar = SkyWarManager.skyWars.get(args[2]);
                    if(skyWar != null)
                        skyWar.join(p);
                    else
                        p.sendMessage(Language.translate("§cThis SkyWar doesn't exist !"));
                }
                else
                    EventController.sendYouHaveToUseThisCommandLikeThis(p, SkyWarManager.example);
            }
        }
    }
    
    @EventHandler
    public void onDeath(PlayerDeathEvent event)
    {
        SkyWarManager.ejectIfPlayerWasInASkyWar(event.getEntity());
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        SkyWarManager.ejectIfPlayerWasInASkyWar(event.getPlayer());
    }

    public static void sendYouHaveToUseThisCommandLikeThis(Player p, String command)
    {
        p.sendMessage(Language.translate("§cYou have to use the command like this: ") + command);
    }

    private void sendOpRequireError(Player p)
    {
        p.sendMessage(Language.translate("§cYou need to be an operator !"));
    }
}