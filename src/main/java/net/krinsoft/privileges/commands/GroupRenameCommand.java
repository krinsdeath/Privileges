package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * @author krinsdeath
 */
public class GroupRenameCommand extends GroupCommand {
    
    public GroupRenameCommand(Privileges plugin) {
        super(plugin);
        setName("Privileges: Group Rename");
        setCommandUsage("/priv group rename [group] [new name]");
        setArgRange(2, 2);
        addKey("privileges group rename");
        addKey("priv group rename");
        addKey("pgroup rename");
        addKey("pgroup ren");
        addKey("pg rename");
        addKey("pg ren");
        setPermission("privileges.group.rename", "Allows this user to rename groups.", PermissionDefault.OP);
    }
    
    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        if (plugin.getGroupManager().getGroup(args.get(0)) == null || plugin.getGroupManager().getGroup(args.get(1)) != null) {
            sender.sendMessage(ChatColor.RED + "Invalid group(s).");
            return;
        }
        if (plugin.getGroupManager().getRank(sender) <= plugin.getGroupManager().getGroup(args.get(0)).getRank() && !sender.hasPermission("privileges.self.edit")) {
            sender.sendMessage(ChatColor.RED + "That group's rank is too high.");
            return;
        }
        String o = args.get(0);
        String n = args.get(1);
        for (String group : plugin.getGroups().getConfigurationSection("groups").getKeys(false)) {
            List<String> inherit = plugin.getGroupNode(group).getStringList("inheritance");
            if (inherit.contains(o)) {
                inherit.remove(o);
                inherit.add(n);
                plugin.getGroupNode(group).set("inheritance", inherit);
            }
        }
        for (String user : plugin.getUsers().getConfigurationSection("users").getKeys(false)) {
            String group = plugin.getUserNode(user).getString("group");
            if (group.equalsIgnoreCase(o)) {
                plugin.getUserNode(user).set("group", n);
            }
        }
        if (plugin.getGroupManager().getGroup(o).equals(plugin.getGroupManager().getDefaultGroup())) {
            plugin.getConfig().set("default_group", n);
        }
        sender.sendMessage("'" + colorize(ChatColor.GREEN, o) + "' has been renamed to '" + colorize(ChatColor.GREEN, n) + "'");
        sender.sendMessage("When you're done editing permissions, run: " + ChatColor.GREEN + "/priv reload");
        plugin.log(">> " + sender.getName() + ": Renamed group '" + o + "' -> '" + n + "'");
    }
}
