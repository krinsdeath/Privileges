package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.io.File;
import java.util.List;

/**
 * @author krinsdeath
 */
public class RestoreCommand extends PrivilegesCommand {

    public RestoreCommand(Privileges plugin) {
        super(plugin);
        setName("Privileges: Restore");
        setCommandUsage("/priv restore [backup]");
        addCommandExample(ChatColor.GREEN + "/priv restore" + ChatColor.AQUA + " feb22 " + ChatColor.WHITE + "-- Restore the backup in the folder 'feb22'");
        setArgRange(0, 1);
        addKey("privileges restore");
        addKey("priv restore");
        addKey("prestore");
        setPermission("privileges.restore", "Allows users to restore past privileges backups.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        String backup = (args.size() == 0 ? "main" : args.get(0)) + "/";
        backup = backup.replaceAll("[\\s./\\\\]", "");
        File folder = new File("privbackups/" + backup);
        try {
            if (!folder.exists()) {
                sender.sendMessage("There is no backup with that name.");
                plugin.log(">> " + sender.getName() + ": Tried to restore a non-existent backup.");
                return;
            }
            plugin.getConfig().load(new File(folder, "config.yml"));
            plugin.getConfig().save(new File(plugin.getDataFolder(), "config.yml"));
            plugin.getUsers().load(new File(folder, "users.yml"));
            plugin.getUsers().save(new File(plugin.getDataFolder(), "users.yml"));
            plugin.getGroups().load(new File(folder, "groups.yml"));
            plugin.getGroups().save(new File(plugin.getDataFolder(), "groups.yml"));
            plugin.reload();
        } catch (Exception e) {
            plugin.warn("An error occurred when trying to restore a backup for Privileges.");
            plugin.warn(e.getLocalizedMessage());
            return;
        }
        sender.sendMessage("The backup for '" + folder.getPath() + "' has been restored.");
        plugin.log(">> " + sender.getName() + ": A Privileges backup has been restored.");
    }

}
