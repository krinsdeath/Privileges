package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
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
        setCommandUsage("/priv reload [player]");
        setArgRange(0, 1);
        addKey("privileges reload");
        addKey("priv reload");
        addKey("preload");
        setPermission("privileges.reload", "Writes the current config file to disk, and then reloads all files and permissions.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        long t = System.currentTimeMillis();
        String msg;
        if (args.size() == 1) {
            if (plugin.getPermissionManager().registerPlayer(args.get(0))) {
                msg = "Privileges has reloaded: " + args.get(0);
            } else {
                msg = "Privileges couldn't reload: " + args.get(0);
            }
        } else {
            plugin.saveGroups();
            plugin.saveUsers();
            plugin.saveConfig();
            plugin.registerConfiguration(true);
            plugin.registerPermissions();
            msg = "Privileges has been reloaded.";
        }
        t = System.currentTimeMillis() - t;
        sender.sendMessage(ChatColor.GREEN + msg + " (" + t + "ms)");
        plugin.log(">> " + sender.getName() + ": " + msg + " (" + t + "ms)");
    }

}
