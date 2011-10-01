package net.krinsoft.privileges.commands;

import java.util.List;
import net.krinsoft.privileges.Privileges;
import net.krinsoft.privileges.groups.Group;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

/**
 *
 * @author krinsdeath
 */
public class GroupRemoveCommand extends GroupCommand {

    public GroupRemoveCommand(Privileges plugin) {
        super(plugin);
        this.setName("privileges group remove");
        this.setCommandUsage("/privileges group remove [group] [--safe]");
        this.addCommandExample("/pgr ? -- show command help");
        this.addCommandExample("/pgr user --safe -- removes the 'user' group and deletes all inheritance references, and sets all users with this group to the default group");
        this.addCommandExample("/pgr admin -- deletes the admin group, but leaves any inheritance and user references");
        this.setArgRange(1, 2);
        this.addKey("privileges group remove");
        this.addKey("priv group remove");
        this.addKey("pg remove");
        this.addKey("pgr");
        this.setPermission("privileges.group.remove", "Allows this user to remove groups.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        if (groupManager.getDefaultGroup().equals(groupManager.getGroup(args.get(0)))) {
            sender.sendMessage("You can't delete the default group.");
            return;
        }
        Group group = groupManager.getGroup(args.get(0));
        if (group == null) {
            sender.sendMessage("No such group exists.");
            return;
        }
        if (group.getRank() >= groupManager.getRank(sender)) {
            sender.sendMessage("Your rank is too low to do that.");
            return;
        }
        if (args.size() == 2 && args.get(1).equalsIgnoreCase("--safe")) {
            for (String user : plugin.getUsers().getKeys("users")) {
                if (plugin.getUserNode(user).getString("group").equals(group.getName())) {
                    plugin.getUsers().setProperty("users." + user + ".group", groupManager.getDefaultGroup().getName());
                }
            }
            plugin.getUsers().save();
            for (String g : plugin.getGroups().getKeys("groups")) {
                List<String> inherits = plugin.getGroupNode(g).getStringList("inheritance", null);
                if (inherits.contains(group.getName())) {
                    inherits.remove(group.getName());
                    plugin.getGroupNode(g).setProperty("inheritance", inherits);
                }
            }
        }
        plugin.getGroups().removeProperty("groups." + group.getName());
        plugin.getGroups().save();
        sender.sendMessage("The group " + colorize(ChatColor.GREEN, group.getName()) + " has been removed.");
        groupManager.removeGroup(group.getName());
    }

}
