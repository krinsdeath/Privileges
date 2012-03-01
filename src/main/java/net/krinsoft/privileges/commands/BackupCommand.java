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
public class BackupCommand extends PrivilegesCommand {

    public BackupCommand(Privileges plugin) {
        super(plugin);
        setName("Privileges: Backup");
        setCommandUsage("/priv backup [name]");
        addCommandExample(ChatColor.GREEN + "/priv backup " + ChatColor.AQUA + "feb22 " + ChatColor.WHITE + "-- Backup to a folder called 'feb22'");
        setArgRange(0, 1);
        addKey("privileges backup");
        addKey("priv backup");
        addKey("pbackup");
        setPermission("privileges.backup", "Backs up Privileges' config files.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        String backup = (args.size() == 0 ? "main" : args.get(0)) + "/";
        backup = backup.replaceAll("[\\s./\\\\]", "");
        File folder = new File("privbackups/" + backup);
        try {
            if (!folder.exists()) {
                folder.mkdirs();
            }
            plugin.getConfig().save(new File(folder, "config.yml"));
            plugin.getUsers().save(new File(folder, "users.yml"));
            plugin.getGroups().save(new File(folder, "groups.yml"));
        } catch (Exception e) {
            plugin.warn("An error occurred while backing the config files up.");
            plugin.warn(e.getLocalizedMessage());
            return;
        }
        sender.sendMessage("Privileges has been backed up.");
        plugin.log(">> " + sender.getName() + ": Privileges config files have been backed up to '" + folder.getPath() + "'.");
    }

}
