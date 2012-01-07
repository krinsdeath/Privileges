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
        if (group.getRank() >= groupManager.getRank(sender) && !sender.hasPermission("privileges.self.edit")) {
            sender.sendMessage("Your rank is too low to do that.");
            return;
        }
        if (args.size() == 2 && args.get(1).equalsIgnoreCase("--safe")) {
            for (String user : plugin.getUsers().getConfigurationSection("users").getKeys(false)) {
                if (plugin.getUserNode(user).getString("group").equals(group.getName())) {
                    plugin.getUsers().set("users." + user + ".group", groupManager.getDefaultGroup().getName());
                }
            }
            plugin.saveUsers();
            for (String g : plugin.getGroups().getConfigurationSection("groups").getKeys(false)) {
                List<String> inherits = plugin.getGroupNode(g).getStringList("inheritance");
                if (inherits.contains(group.getName())) {
                    inherits.remove(group.getName());
                    plugin.getGroupNode(g).set("inheritance", inherits);
                }
            }
        }
        plugin.getGroups().set("groups." + group.getName(), null);
        plugin.saveGroups();
        sender.sendMessage("The group " + colorize(ChatColor.GREEN, group.getName()) + " has been removed.");
        plugin.getPermissionManager().reload();
        groupManager.removeGroup(group.getName());
    }

}
