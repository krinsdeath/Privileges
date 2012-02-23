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
        setName("Privileges: Group Remove");
        setCommandUsage("/pg remove [group]");
        addCommandExample("/pgr user -- removes the 'user' group and deletes all inheritance references, and sets all users with this group to the default group");
        setArgRange(1, 1);
        addKey("privileges group remove");
        addKey("priv group remove");
        addKey("pgroup remove");
        addKey("pg remove");
        addKey("pgroupr");
        addKey("pgr");
        setPermission("privileges.group.remove", "Allows this user to remove groups.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        if (groupManager.getDefaultGroup().equals(groupManager.getGroup(args.get(0)))) {
            sender.sendMessage(ChatColor.RED + "You can't delete the default group.");
            return;
        }
        Group group = groupManager.getGroup(args.get(0));
        if (group == null) {
            sender.sendMessage("No such group exists.");
            return;
        }
        if (!groupManager.checkRank(sender, group.getRank())) {
            sender.sendMessage(ChatColor.RED + "That rank is too high for you.");
            return;
        }
        for (String user : plugin.getUsers().getConfigurationSection("users").getKeys(false)) {
            if (plugin.getUserNode(user).getString("group").equals(group.getName())) {
                plugin.getUserNode(user).set("group", groupManager.getDefaultGroup().getName());
                plugin.debug("Set " + user + "'s group to default");
            }
        }
        for (String g : plugin.getGroups().getConfigurationSection("groups").getKeys(false)) {
            List<String> inherit = plugin.getGroupNode(g).getStringList("inheritance");
            if (inherit.contains(group.getName())) {
                inherit.remove(group.getName());
                plugin.getGroupNode(g).set("inheritance", inherit);
                plugin.debug("Removed inheritance entry for group " + g);
            }
        }
        plugin.getGroups().set("groups." + group.getName(), null);
        sender.sendMessage("The group " + colorize(ChatColor.GREEN, group.getName()) + " has been removed.");
        sender.sendMessage("When you're done editing permissions, run: " + ChatColor.GREEN + "/priv reload");
        plugin.log(">> " + sender.getName() + ": Removed group '" + group.getName() + "'");
    }

}
