package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;
import java.util.UUID;

/**
 * @author krinsdeath
 */
public class UserCleanCommand extends UserCommand {

    public UserCleanCommand(Privileges plugin) {
        super(plugin);
        setName("Privileges: User Clean");
        setCommandUsage("/priv user clean");
        addCommandExample("/priv user clean --confirm");
        setArgRange(1, 1);
        addKey("privileges user clean");
        addKey("priv user clean");
        addKey("pu clean");
        setPermission("privileges.user.clean", "Deletes all users in the default group and with no custom permissions.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        if (args.get(0).equals("--confirm")) {
            String default_group = plugin.getConfig().getString("default_group", "default");
            for (String key : plugin.getUsers().getConfigurationSection("users").getKeys(false)) {
                boolean success = false;
                ConfigurationSection node = plugin.getUserNode(UUID.fromString(key));
                List<String> perms = node.getStringList("permissions");
                if (node.getString("group").equals(default_group) && (perms == null || perms.size() == 0)) {
                    for (World world : plugin.getServer().getWorlds()) {
                        perms = node.getStringList("worlds." + world.getName());
                        success = perms == null || perms.size() == 0;
                    }
                }
                if (success) {
                    plugin.getUsers().set("users." + key + ".group", null);
                    plugin.getUsers().set("users." + key + ".permissions", null);
                    plugin.getUsers().set("users." + key + ".worlds", null);
                    plugin.getUsers().set("users." + key, null);
                }
            }
            plugin.saveUsers();
            sender.sendMessage(ChatColor.GREEN + "Users cleaned.");
        } else {
            sender.sendMessage(ChatColor.RED + "In order to clean all users, you must supply the argument '" + ChatColor.AQUA + "--confirm" + ChatColor.RED + "'");
        }
    }
}
