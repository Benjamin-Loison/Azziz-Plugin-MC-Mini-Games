package fr.mcminigames.benjaminloison.main;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import fr.mcminigames.benjaminloison.api.Language;
import fr.mcminigames.benjaminloison.game.Game;
import fr.mcminigames.benjaminloison.game.GameManager;
import fr.mcminigames.benjaminloison.group.Group;
import fr.mcminigames.benjaminloison.group.GroupManager;

public class EventController implements Listener
{
    private static final String[] game = {"ge", "game"}, add = {"add"}, load = {"ld", "load"}, spawn = {"sp", "spawn"}, finish = {"fn", "finish"}, join = {"jn", "join"}, leave = {"leave", "quit"}, remove = {"rm", "remove"}, group = {"gp", "group"}, create = {"ct", "create"}, invite = {"in", "inv", "invite"}, kick = {"kick", "kk"}, promote = {"pm", "promote"}, disband = {"disband", "delete"};

    public EventController()
    {
        Main.plugin.getServer().getPluginManager().registerEvents(this, Main.plugin);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e)
    {
        String args[] = e.getMessage().split(" ");
        args[0] = args[0].replaceFirst("/", "");
        Player p = e.getPlayer();
        int length = args.length;
        e.setCancelled(true);
        if(contains(game, args[0]))
        {
            if(length < 3)
            {
                if(length > 1)
                {
                    if(contains(leave, args[1]))
                    {
                        Game game = GameManager.gameOf(p);
                        if(game != null)
                            game.leave(p);
                        else
                            p.sendMessage(Language.translate("§cYou are not in a ") + Language.translate("game"));
                    }
                    else if(contains(spawn, args[1]) || contains(finish, args[1]))
                    {
                        Game game = GameManager.addingGames.get(p);
                        if(game == null)
                        {
                            p.sendMessage(Language.translate("§cYou are not creating a ") + Language.translate("game"));
                            return;
                        }
                        if(contains(spawn, args[1]))
                            game.add(p);
                        else
                            GameManager.addingGames.get(p).finish(p);
                    }
                    else if(contains(join, args[1]))
                        EventController.sendYouHaveToUseThisCommandLikeThis(p, GameManager.exampleJoin);
                    else if(contains(add, args[1]))
                        EventController.sendYouHaveToUseThisCommandLikeThis(p, GameManager.exampleAdd);
                    else if(contains(remove, args[1]))
                        EventController.sendYouHaveToUseThisCommandLikeThis(p, GameManager.exampleRemove);
                    else if(contains(load, args[1]))
                        EventController.sendYouHaveToUseThisCommandLikeThis(p, GameManager.exampleLoad);
                    else
                        EventController.sendYouHaveToUseThisCommandLikeThis(p, GameManager.example);
                }
                else
                    EventController.sendYouHaveToUseThisCommandLikeThis(p, GameManager.example);
            }
            else
            {
                if(contains(load, args[1]))
                {
                    if(length > 3)
                        if(p.isOp())
                            GameManager.load(p, args[2], args[3]);
                        else
                            sendOpRequireError(p);
                    else
                        EventController.sendYouHaveToUseThisCommandLikeThis(p, GameManager.exampleLoad);
                }
                else if(contains(add, args[1]))
                {
                    if(p.isOp())
                        GameManager.addGame(args, p);
                    else
                        sendOpRequireError(p);
                }
                else if(contains(remove, args[1]))
                {
                    if(p.isOp())
                        if(GameManager.removeGame(args[2]))
                            p.sendMessage(Language.translate("§aSkyWar removed !"));
                        else
                            p.sendMessage(Language.translate("§cThis") + Language.translate(GameManager.games.get(args[2]).type) + Language.translate("§c doesn't exist !"));
                    else
                        sendOpRequireError(p);
                }
                else if(contains(join, args[1]))
                {
                    Game game = GameManager.games.get(args[2]);
                    if(game != null)
                        game.join(p);
                    else
                        p.sendMessage(Language.translate("§cThis ") + Language.translate("game") + Language.translate("§c doesn't exist !"));
                }
                else
                    EventController.sendYouHaveToUseThisCommandLikeThis(p, GameManager.example);
            }
        }
        else if(contains(group, args[0]))
        {
            if(length < 3)
            {
                if(length > 1)
                {
                    if(contains(create, args[1]))
                        new Group(p);
                    else if(contains(leave, args[1]) || contains(disband, args[1]))
                    {
                        Group gp = GroupManager.groupOf(p);
                        if(gp == null)
                        {
                            p.sendMessage(Language.translate("§cYou are not in a group !"));
                            return;
                        }
                        if(contains(leave, args[1]))
                            gp.leave(p);
                        else
                            gp.disband(p);
                    }
                    else if(contains(invite, args[1]))
                        sendYouHaveToUseThisCommandLikeThis(p, GroupManager.groupInvite);
                    else if(contains(join, args[1]))
                        sendYouHaveToUseThisCommandLikeThis(p, GroupManager.groupJoin);
                    else if(contains(promote, args[1]))
                        sendYouHaveToUseThisCommandLikeThis(p, GroupManager.groupPromote);
                    else if(contains(kick, args[1]))
                        sendYouHaveToUseThisCommandLikeThis(p, GroupManager.groupKick);
                    else
                        sendYouHaveToUseThisCommandLikeThis(p, GroupManager.group);
                }
                else
                    sendYouHaveToUseThisCommandLikeThis(p, GroupManager.group);
            }
            else
            {
                if(!contains(invite, args[1]) && !contains(join, args[1]) && !contains(promote, args[1]) && !contains(kick, args[1]))
                {
                    sendYouHaveToUseThisCommandLikeThis(p, GroupManager.group);
                    return;
                }
                Player searched = GroupManager.getPlayer(args[2]);
                if(searched == null)
                {
                    p.sendMessage(Language.translate("§cThe player §e") + args[2] + Language.translate("§c has not been found !"));
                    return;
                }
                else if(p == searched)
                {
                    p.sendMessage(Language.translate("§cYou can't do this action on yourself !"));
                    return;
                }
                if(contains(join, args[1]))
                {
                    GroupManager.join(p, searched);
                }
                else if(contains(invite, args[1]) || contains(promote, args[1]) || contains(kick, args[1]))
                {
                    Group gp = GroupManager.groupOf(p);
                    if(gp == null)
                    {
                        p.sendMessage(Language.translate("§cYou are not in a group !"));
                        return;
                    }
                    if(contains(invite, args[1]))
                        gp.invite(p, searched);
                    else if(contains(promote, args[1]))
                        gp.promote(p, searched);
                    else
                        gp.kick(p, searched);
                }
            }
        }
        else
            e.setCancelled(false);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        onBlockEvent(event);
    }

    @EventHandler
    public void onBlockBreak(BlockPlaceEvent event)
    {
        onBlockEvent(event);
    }

    private void onBlockEvent(BlockEvent event)
    {
        Player p = null;
        if(event instanceof BlockBreakEvent)
        {
            BlockBreakEvent e = (BlockBreakEvent)event;
            p = e.getPlayer();
        }
        else if(event instanceof BlockPlaceEvent)
        {
            BlockPlaceEvent e = (BlockPlaceEvent)event;
            p = e.getPlayer();
        }
        Game g = GameManager.gameOf(p);
        if(g != null)
            if(g.type.equals("PvPSoup"))
            {
                p.sendMessage(Language.translate("§cThe PvPSoup gameplay doesn't allow to break/place any block !"));
                if(event instanceof BlockBreakEvent)
                    ((BlockBreakEvent)event).setCancelled(true);
                else if(event instanceof BlockPlaceEvent)
                    ((BlockPlaceEvent)event).setCancelled(true);
            }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInteractWithItem(PlayerInteractEvent e)
    {
        Player p = e.getPlayer();
        PlayerInventory inv = p.getInventory();
        ItemStack is = inv.getItemInHand();
        Action act = e.getAction();
        Game game = GameManager.gameOf(p);
        if(game != null && game.type.equals("PvPSoup") && is != null && act == Action.RIGHT_CLICK_AIR || act == Action.RIGHT_CLICK_BLOCK)
        {
            Material t = is.getType();
            if(t == Material.MUSHROOM_SOUP)
            {
                inv.setItemInHand(null);
                byte health = 8;
                double actualHealth = p.getHealth(), maxHealth = p.getMaxHealth();
                if(actualHealth + health > maxHealth)
                    p.setHealth(maxHealth);
                else
                    p.setHealth(actualHealth + health);
            }
        }
    }

    private boolean contains(String[] table, String str)
    {
        for(int i = 0; i < table.length; i++)
            if(table[i].equals(str))
                return true;
        return false;
    }

    @EventHandler
    public void onConnect(PlayerLoginEvent event)
    {
        GroupManager.invitations.put(event.getPlayer(), new ArrayList<Player>());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event)
    {
        GameManager.ejectIfPlayerWasInAGame(event.getEntity());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        Player p = event.getPlayer();
        GameManager.ejectIfPlayerWasInAGame(p);
        GameManager.addingGames.remove(p); // TODO: add a system to remember the player when he is come back that the Game has been removed or allow the player to continue to finish the creation
                                           // of the Game
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
