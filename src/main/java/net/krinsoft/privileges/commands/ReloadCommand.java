package net.krinsoft.privileges.commands;

import java.util.List;
import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

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
        plugin.saveGroups();
        plugin.saveUsers();
        plugin.saveConfig();
        plugin.reload();
        String msg = "Privileges has been reloaded.";
        sender.sendMessage(ChatColor.GREEN + msg);
        if (sender instanceof Player) {
            plugin.log(">> " + sender.getName() + ": " + msg);
        }
    }

}
