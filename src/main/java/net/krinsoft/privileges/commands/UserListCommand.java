package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.FancyPage;
import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.List;

/**
 * @author krinsdeath
 */
public class UserListCommand extends UserCommand {

    public UserListCommand(Privileges plugin) {
        super(plugin);
        setName("Privileges: User List");
        setCommandUsage("/priv user list");
        setArgRange(0, 1);
        addKey("privileges user list");
        addKey("priv user list");
        addKey("pu list");
        setPermission("privileges.user.list", "Shows a list of online users, groups and display names", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        List<String> online = new ArrayList<String>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            online.add(
                    // user group
                    ChatColor.GREEN + "[" + ChatColor.GOLD + plugin.getGroupManager().getGroup((OfflinePlayer) player).getName() + ChatColor.GREEN + "] " +
                    // user name
                    ChatColor.GREEN + player.getName() + " (~" + ChatColor.GOLD + player.getDisplayName() + ChatColor.GREEN + ")");
        }
        FancyPage page = new FancyPage(online);
        int pageNum = 0;
        if (args.size() > 0) {
            try {
                pageNum = Integer.parseInt(args.get(0));
            } catch (NumberFormatException e) {
                pageNum = 0;
            }
        }
        sender.sendMessage(ChatColor.GREEN + "=== " + ChatColor.GOLD + "User List" + ChatColor.GREEN + " ===");
        if (sender instanceof Player) {
            for (String line : page.getPage(pageNum)) {
                sender.sendMessage(line);
            }
        } else {
            for (String line : online) {
                sender.sendMessage(line);
            }
        }
    }
}
