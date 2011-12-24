package net.krinsoft.privileges.importer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.krinsoft.privileges.Privileges;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

/**
 *
 * @author krinsdeath
 */
public class ImportManager {
    private Privileges plugin;

    public ImportManager(Privileges plugin) {
        this.plugin = plugin;
        locatePermissionsBukkit();
        locatePermissions3();
    }

    private void locatePermissionsBukkit() {
        File file = new File("plugins/PermissionsBukkit/config.yml");
        if (file.exists()) {
            long imp = System.currentTimeMillis();
            plugin.info("Beginning PermissionsBukkit configuration import...");
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection groups = config.getConfigurationSection("groups");
            for (String group : groups.getKeys(false)) {
                long time = System.currentTimeMillis();
                // create the group if it doesn't exist
                plugin.buildGroup(group);
                plugin.getGroupNode(group).set("rank", plugin.getGroupNode(group).getInt("rank", 0));
                List<String> globals = plugin.getGroupNode(group).getStringList("permissions");
                String permKey = group + ".permissions";
                System.out.println(permKey);
                // iterate through global 'permissions'
                for (Map.Entry<String, Object> key : groups.getConfigurationSection(permKey).getValues(true).entrySet()) {
                    if (key.getValue() instanceof Boolean) {
                        globals.add(((Boolean) key.getValue() ? key.getKey() : "-" + key.getKey()));
                        plugin.debug(key.getKey() + ": " + key.getValue());
                    }
                }
                plugin.getGroupNode(group).set("permissions", globals);
                // iterate through worlds
                if (groups.getConfigurationSection(group + ".worlds") != null) {
                    for (String world : groups.getConfigurationSection(group + ".worlds").getKeys(false)) {
                        String worldKey = group + ".worlds." + world;
                        List<String> worldNodes = plugin.getGroupNode(group).getStringList("worlds." + world);
                        if (worldNodes == null) { worldNodes = new ArrayList<String>(); }
                        // iterate through world keys for individual nodes
                        for (Map.Entry<String, Object> key : groups.getConfigurationSection(worldKey).getValues(true).entrySet()) {
                            if (key.getValue() instanceof Boolean) {
                                worldNodes.add(((Boolean) key.getValue() ? key.getKey() : "-" + key.getKey()));
                                plugin.debug(key.getKey() + ": " + key.getValue());
                            }
                        }
                        plugin.getGroupNode(group).set("worlds." + world, worldNodes);
                    }
                }
                plugin.info("Imported " + group + " in " + (System.currentTimeMillis() - time) + "ms");
                plugin.saveGroups();
            }
            file.getParentFile().renameTo(new File("plugins/PermissionsBukkit_DEPRECATED"));
            plugin.info("PermissionsBukkit import finished in " + (System.currentTimeMillis() - imp) + "ms");
            plugin.info("You can now delete PermissionsBukkit.");
            plugin.info("Please view plugins/Privileges/groups.yml, and edit the ranks of the imported groups.");
        }
    }

    private void locatePermissions3() {
        File file = new File("plugins/Permissions");
        if (file.exists()) {
            File[] files = file.listFiles();
            long imp = System.currentTimeMillis();
            plugin.info("Beginning Permissions 3 configuration import...");
            File gGroups = new File("plugins/Permissions/globalGroups.yml");
            if (gGroups.exists()) {
                FileConfiguration conf = YamlConfiguration.loadConfiguration(gGroups);
                if (conf.getKeys(false) != null) {
                    for (String group : conf.getKeys(false)) {
                        plugin.buildGroup(group);
                        ConfigurationSection NODE = conf.getConfigurationSection("groups." + group);
                        List<String> globals = plugin.getGroupNode(group).getStringList("permissions");
                        List<String> nodes = NODE.getStringList("permissions");
                        if (NODE.getBoolean("default", false)) {
                            plugin.info("Setting default group to '" + group + "'");
                            plugin.getConfig().set("default_group", group);
                        }
                        for (String node : nodes) {
                            globals.remove(node);
                            globals.remove("-" + node);
                            globals.add(node);
                        }
                        if (NODE.getBoolean("info.build", false)) {
                            globals.remove("privileges.build");
                            globals.remove("-privileges.build");
                            globals.add("privileges.build");
                        } else {
                            globals.remove("privileges.build");
                            globals.remove("-privileges.build");
                            globals.add("-privileges.build");
                        }
                        List<String> parents = plugin.getGroupNode(group).getStringList("inheritance");
                        List<String> inherits = NODE.getStringList("inheritance");
                        for (String parent : inherits) {
                            parents.remove(parent);
                            parents.add(parent);
                        }
                        plugin.getGroupNode(group).set("permissions", globals);
                        plugin.getGroupNode(group).set("inheritance", parents);
                        plugin.getGroupNode(group).set("rank", NODE.getInt("info.rank", 0));
                    }
                }
            }
            for (File folder : files) {
                if (folder.isDirectory()) {
                    File gConfig = new File("plugins/Permissions/" + folder.getName() + "/groups.yml");
                    if (gConfig.exists()) {
                        FileConfiguration conf = YamlConfiguration.loadConfiguration(gConfig);
                        ConfigurationSection groups = conf.getConfigurationSection("groups");
                        if (groups == null) { continue; }
                        for (String group : groups.getKeys(false)) {
                            long groupTIME = System.currentTimeMillis();
                            // create the group if it doesn't exist, and get the nodes for this world
                            plugin.buildGroup(group);
                            List<String> worldNodes = plugin.getGroupNode(group).getStringList("worlds." + folder.getName());
                            // get the node for this group from the permissions 3 config
                            ConfigurationSection NODE = groups.getConfigurationSection(group);
                            // get the list of permissions
                            List<String> nodes = NODE.getStringList("permissions");
                            for (String node : nodes) {
                                worldNodes.remove(node);
                                worldNodes.remove("-" + node);
                                worldNodes.add(node);
                            }
                            if (NODE.getBoolean("info.build", false)) {
                                worldNodes.remove("privileges.build");
                                worldNodes.remove("-privileges.build");
                                worldNodes.add("privileges.build");
                            } else {
                                worldNodes.remove("privileges.build");
                                worldNodes.remove("-privileges.build");
                                worldNodes.add("-privileges.build");
                            }
                            // get parent groups for this group
                            List<String> parents = plugin.getGroupNode(group).getStringList("inheritance");
                            List<String> inherits = NODE.getStringList("inheritance");
                            for (String parent : inherits) {
                                parents.remove(parent);
                                parents.add(parent);
                            }
                            plugin.getGroupNode(group).set("rank", NODE.getInt("info.rank", 0));
                            plugin.getGroupNode(group).set("worlds." + folder.getName(), worldNodes);
                            plugin.getGroupNode(group).set("inheritance", parents);
                            plugin.info("Imported '" + group + "' from Permissions 3 in " + (System.currentTimeMillis() - groupTIME) + "ms");
                        }
                    }
                }
            }
            file.renameTo(new File("plugins/Permissions_DEPRECATED"));
            plugin.info("Permissions 3 import finished in " + (System.currentTimeMillis() - imp) + "ms");
            plugin.info("Permissions 3 can now be deleted.");
            plugin.info("Please check plugin/Privileges/groups.yml. Some groups may have unset 'rank' keys.");
            plugin.saveGroups();
            plugin.saveConfig();
        }
    }


}
