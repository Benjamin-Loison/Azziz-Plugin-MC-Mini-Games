package fr.azzizcouscous.benjaminloison.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.azzizcouscous.benjaminloison.api.Language;

public class GroupManager
{
    public static Map<Player, Group> groups = new HashMap<Player, Group>();
    public static Map<Player, ArrayList<Player>> invitations = new HashMap<Player, ArrayList<Player>>();
    public static final String group = "/group <create/invite/join/leave/kick/disband/promote> [player]", groupInvite = "/group invite player", groupJoin = "/group join player", groupKick = "/group kick player", groupPromote = "/group promote player";

    @SuppressWarnings("deprecation")
    public static Player getPlayer(String name)
    {
        return Bukkit.getServer().getPlayer(name);
    }

    public static void join(Player joiner, Player groupChief)
    {
        ArrayList<Player> invits = invitations.get(joiner);
        if(invits.contains(groupChief))
        {
            joiner.sendMessage(Language.translate("§aYou have join the group of §e") + groupChief.getDisplayName());
            Group joined = groups.get(groupChief);
            ArrayList<Player> players = joined.players;
            for(int i = 0; i < players.size(); i++)
            {
                Player p = players.get(i);
                p.sendMessage(Language.translate("§aThe player §e") + joiner.getDisplayName() + Language.translate("§a has joined your group !"));
            }
            invits.remove(groupChief);
            players.add(joiner);
        }
        else
            joiner.sendMessage(Language.translate("§cYou have not been invited in the group of §e") + groupChief.getDisplayName());
    }
    
    public static Group groupOf(Player p)
    {
        Iterator<Player> it = groups.keySet().iterator();
        while(it.hasNext())
        {
            Player pl = it.next();
            Group gp = groups.get(pl);
            if(gp.players.contains(p))
                return gp;
        }
        return null;
    }
}
