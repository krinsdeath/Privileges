package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * @author krinsdeath
 */
public class SaveCommand extends PrivilegesCommand {
    
    public SaveCommand(Privileges plugin) {
        super(plugin);
        setName("Privileges: Save");
        setCommandUsage("/priv save");
        setArgRange(0, 0);
        addKey("privileges save");
        addKey("priv save");
        addKey("psave");
        setPermission("privileges.save", "Saves Privileges' config files to disk immediately.", PermissionDefault.OP);
    }
    
    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        plugin.saveUsers();
        plugin.saveGroups();
        plugin.saveConfig();
        sender.sendMessage(ChatColor.GREEN + "All config files saved.");
        plugin.log(">> " + sender.getName() + ": All config files saved.");
    }
}
