package fr.mcminigames.benjaminloison.group;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.mcminigames.benjaminloison.api.Language;

public class Group
{
    public ArrayList<Player> players = new ArrayList<Player>();
    public Player chief;

    public Group(Player chief)
    {
        this.chief = chief;
        if(GroupManager.groups.containsKey(chief))
        {
            this.chief.sendMessage(Language.translate("§cYou already have a group !"));
            return;
        }
        players.add(this.chief);
        GroupManager.groups.put(chief, this);
        this.chief.sendMessage(Language.translate("§aYou have create your group !"));
    }

    public void invite(Player chief, Player p)
    {
        if(this.chief == chief)
        {
            ArrayList<Player> pls = GroupManager.invitations.get(p);
            if(pls.contains(chief))
            {
                chief.sendMessage(Language.translate("§cThe player §e") + p.getDisplayName() + Language.translate("§c has already received your invitation !"));
                return;
            }
            else if(players.contains(p))
            {
                chief.sendMessage(Language.translate("§cThe player §e") + p.getDisplayName() + Language.translate("§c is already in your group ! !"));
                return;
            }
            else
                pls.add(chief);
            chief.sendMessage(Language.translate("§aThe player §e") + p.getDisplayName() + Language.translate("§a has received your invitation !"));
            p.sendMessage(Language.translate("§aYou have received a group invitation from §e") + chief.getDisplayName() + Language.translate("§a use this command to join: ") + GroupManager.groupJoin.replace("player", chief.getDisplayName()));
        }
        else
            p.sendMessage(Language.translate("§cYou are not the chief of the group !"));
    }

    public void leave(Player leaver)
    {
        players.remove(leaver);
        if(leaver == chief)
        {
            GroupManager.groups.remove(leaver);
            leaver.sendMessage(Language.translate("§aYou have deleted your group !"));
        }
        else
            leaver.sendMessage(Language.translate("§aYou have left the group of §e") + chief.getDisplayName());
        for(int i = 0; i < players.size(); i++)
        {
            Player p = players.get(i);
            if(leaver == chief)
                p.sendMessage(Language.translate("§aThe chief §e") + leaver.getDisplayName() + Language.translate("§a has deleted the group !"));
            else
                p.sendMessage(Language.translate("§aThe player §e") + leaver.getDisplayName() + Language.translate("§a has left the group !"));
        }
    }

    public void kick(Player chief, Player kicked)
    {
        if(this.chief == chief)
        {
            if(!players.contains(kicked))
            {
                chief.sendMessage(Language.translate("§cThe player §e") + kicked.getDisplayName() + Language.translate("§c is not in the group !"));
                return;
            }
            players.remove(kicked);
            chief.sendMessage(Language.translate("§aYou have kicked §e") + kicked.getDisplayName());
            for(int i = 0; i < players.size(); i++)
            {
                Player p = players.get(i);
                if(chief != p)
                    p.sendMessage(Language.translate("§aThe chief §e") + chief.getDisplayName() + Language.translate("§a has kicked from the group player §e") + kicked.getDisplayName());
            }
            kicked.sendMessage(Language.translate("§cYou have been kicked from the group of §e") + chief.getDisplayName());
        }
        else
            chief.sendMessage(Language.translate("§cYou are not the chief of the group !"));
    }

    public void disband(Player p)
    {
        if(chief == p)
        {
            for(int i = 0; i < players.size(); i++)
                if(players.get(i) != p)
                    players.get(i).sendMessage(Language.translate("§aThe chief §e") + p.getDisplayName() + Language.translate("§a has deleted the group !"));
                else
                    players.get(i).sendMessage(Language.translate("§aYou have deleted your group !"));
            GroupManager.groups.remove(p);
        }
        else
            p.sendMessage(Language.translate("§cYou are not the chief of the group !"));
    }

    public void promote(Player chief, Player newChief)
    {
        if(this.chief == chief)
        {
            if(players.contains(newChief))
            {
                this.chief = newChief;
                chief.sendMessage(Language.translate("§aYou are no longer the chief of the group !"));
                newChief.sendMessage(Language.translate("§aYou are the new chief of the group !"));
                for(int i = 0; i < players.size(); i++)
                {
                    Player p = players.get(i);
                    if(p != chief && p != newChief)
                        p.sendMessage(Language.translate("§aThe new group chief is §e" + newChief.getDisplayName()));
                }
                Map<Player, ArrayList<Player>> inv = GroupManager.invitations;;
                Iterator<Player> players = inv.keySet().iterator();
                while(players.hasNext())
                {
                    Player p = players.next();
                    ArrayList<Player> invs = inv.get(p);
                    if(invs.contains(chief))
                    {
                        invs.remove(chief);
                        invs.add(newChief);
                    }
                }
            }
            else
                chief.sendMessage(Language.translate("§cThis player is not in your group !"));
        }
        else
            chief.sendMessage(Language.translate("§cYou are not the chief of the group !"));
    }
}
