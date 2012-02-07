package net.krinsoft.privileges.commands;

import java.util.List;
import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

/**
 *
 * @author krinsdeath
 */
public class ReloadCommand extends PrivilegesCommand {

    public ReloadCommand(Privileges plugin) {
        super(plugin);
        this.setName("Privileges: Reload");
        this.setCommandUsage("/priv reload");
        this.setArgRange(0, 0);
        this.addKey("privileges reload");
        this.addKey("priv reload");
        this.setPermission("privileges.reload", "Allows this user to access '/priv reload'", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        long t = System.currentTimeMillis();
        plugin.registerConfiguration(true);
        plugin.registerPermissions();
        sender.sendMessage(ChatColor.GREEN + "Privileges has been reloaded. (" + (System.currentTimeMillis() - t) + "ms)");
    }

}
