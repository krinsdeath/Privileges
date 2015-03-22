package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * @author krinsdeath
 */
public class UserResetCommand extends UserPermCommand {
    
    public UserResetCommand(Privileges plugin) {
        super(plugin);
        setName("Privileges: User Reset");
        setCommandUsage("/priv user reset [user]");
        setArgRange(1, 1);
        addKey("privileges user reset");
        addKey("priv user reset");
        addKey("pu reset");
        addKey("pur");
        setPermission("privileges.user.reset", "Resets a user to default group with no additional permissions.", PermissionDefault.OP);
    }
    
    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        OfflinePlayer p = plugin.getServer().getOfflinePlayer(args.get(0));
        if (plugin.getGroupManager().getRank(p) >= plugin.getGroupManager().getRank(sender) && !sender.hasPermission("privileges.self.edit")) {
            sender.sendMessage(ChatColor.RED + "You cannot reset that user.");
            return;
        }
        try {
            plugin.getUserNode(p.getUniqueId()).set("group", plugin.getGroupManager().getDefaultGroup().getName());
            plugin.getUserNode(p.getUniqueId()).set("permissions", null);
            plugin.getUserNode(p.getUniqueId()).set("worlds", null);
            plugin.getPlayerManager().register(p.getName());
            sender.sendMessage(ChatColor.GREEN + "The user '" + p.getName() + "' has been reset to default.");
            plugin.log(">> " + sender.getName() + ": " + p.getUniqueId() + " has been reset.");
            reload(sender);
        } catch (NullPointerException e) {
            plugin.warn(">> " + sender.getName() + ": Couldn't reset '" + p.getUniqueId() + "'... ");
            plugin.warn(">> " + e.getLocalizedMessage());
        }
    }
}
