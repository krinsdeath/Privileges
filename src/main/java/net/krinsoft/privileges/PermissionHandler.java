package net.krinsoft.privileges;

import com.pneumaticraft.commandhandler.PermissionsInterface;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author krinsdeath
 */
public class PermissionHandler implements PermissionsInterface {

    public boolean hasPermission(CommandSender sender, String node, boolean isOpRequired) {
        return !(sender instanceof Player) || sender.hasPermission(node);
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