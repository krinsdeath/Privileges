package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import net.krinsoft.privileges.groups.Group;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * @author krinsdeath
 */
public class UserResetCommand extends PrivilegesCommand {
    
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
        Player p = plugin.getServer().getPlayer(args.get(0));
        if (plugin.getGroupManager().getRank(p) >= plugin.getGroupManager().getRank(sender) && !sender.hasPermission("privileges.self.edit")) {
            sender.sendMessage(ChatColor.RED + "You cannot reset that user.");
            return;
        }
        plugin.getUserNode(p.getName()).set("group", plugin.getGroupManager().getDefaultGroup().getName());
        plugin.getUserNode(p.getName()).set("permissions", null);
        plugin.getUserNode(p.getName()).set("worlds", null);
        plugin.saveUsers();
        plugin.getPermissionManager().registerPlayer(p.getName());
        sender.sendMessage(ChatColor.GREEN + "The user '" + p.getName() + "' has been reset to default.");
        plugin.log(p.getName() + " has been reset by " + sender.getName());
    }
}
