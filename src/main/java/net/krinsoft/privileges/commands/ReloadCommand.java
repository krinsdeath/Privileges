package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 *
 * @author krinsdeath
 */
public class ReloadCommand extends PrivilegesCommand {

    public ReloadCommand(Privileges plugin) {
        super(plugin);
        setName("Privileges: Reload");
        setCommandUsage("/priv reload");
        setArgRange(0, 0);
        addKey("privileges reload");
        addKey("priv reload");
        addKey("preload");
        setPermission("privileges.reload", "Writes the current config file to disk, and then reloads all files and permissions.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        long t = System.currentTimeMillis();
        plugin.saveGroups();
        plugin.saveUsers();
        plugin.saveConfig();
        plugin.registerConfiguration(true);
        plugin.registerPermissions();
        plugin.updatePermissions();
        String msg = "Privileges has been reloaded.";
        t = System.currentTimeMillis() - t;
        sender.sendMessage(ChatColor.GREEN + msg + " (" + t + "ms)");
        if (!(sender instanceof ConsoleCommandSender)) {
            plugin.log(">> " + sender.getName() + ": " + msg + " (" + t + "ms)");
        }
    }

}
