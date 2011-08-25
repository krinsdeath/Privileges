package net.krinsoft.privileges;

import com.pneumaticraft.commandhandler.PermissionsInterface;
import java.util.List;
import org.bukkit.command.CommandSender;

/**
 *
 * @author krinsdeath
 */
class PermissionHandler implements PermissionsInterface {
    private Privileges plugin;

    public PermissionHandler(Privileges plugin) {
        this.plugin = plugin;
    }

    public boolean hasPermission(CommandSender sender, String node, boolean isOpRequired) {
        if (!sender.isOp() && isOpRequired) { return false; }
        if (sender.hasPermission(node)) {
            return true;
        } else if (sender.isPermissionSet(node) && !sender.hasPermission(node)) {
            return false;
        } else if (!sender.isPermissionSet(node) && sender.hasPermission("privileges.*")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasAnyPermission(CommandSender sender, List<String> allPermissionStrings, boolean opRequired) {
        if (!sender.isOp() && opRequired) { return false; }
        for (String node : allPermissionStrings) {
            if (hasPermission(sender, node, opRequired)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAllPermission(CommandSender sender, List<String> allPermissionStrings, boolean opRequired) {
        if (!sender.isOp() && opRequired) { return false; }
        for (String node : allPermissionStrings) {
            if (!hasPermission(sender, node, opRequired)) {
                return false;
            }
        }
        return true;
    }

}
