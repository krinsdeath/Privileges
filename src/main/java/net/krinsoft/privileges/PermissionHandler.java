package net.krinsoft.privileges;

import com.pneumaticraft.commandhandler.PermissionsInterface;
import java.util.List;
import org.bukkit.command.CommandSender;

/**
 *
 * @author krinsdeath
 */
public class PermissionHandler implements PermissionsInterface {

    public PermissionHandler() {}

    public boolean hasPermission(CommandSender sender, String node, boolean isOpRequired) {
        boolean hasPermission = sender.hasPermission(node);
        boolean isPermissionSet = sender.isPermissionSet(node);
        boolean globalPermission = sender.hasPermission("privileges.*");
        if (hasPermission) {
            return true;
        } else if (isPermissionSet && !hasPermission) {
            return false;
        } else if (!isPermissionSet && globalPermission) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasAnyPermission(CommandSender sender, List<String> allPermissionStrings, boolean opRequired) {
        for (String node : allPermissionStrings) {
            if (hasPermission(sender, node, opRequired)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAllPermission(CommandSender sender, List<String> allPermissionStrings, boolean opRequired) {
        for (String node : allPermissionStrings) {
            if (!hasPermission(sender, node, opRequired)) {
                return false;
            }
        }
        return true;
    }

}