package net.krinsoft.privileges.importer;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.krinsoft.privileges.Privileges;
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
            Configuration config = new Configuration(file);
            config.load();
            ConfigurationNode groups = config.getNode("groups");
            for (String group : groups.getKeys()) {
                long time = System.currentTimeMillis();
                // create the group if it doesn't exist
                plugin.buildGroup(group);
                plugin.getGroupNode(group).setProperty("rank", plugin.getGroupNode(group).getInt("rank", 0));
                List<String> globals = plugin.getGroups().getNode("groups." + group).getStringList("permissions", null);
                String permKey = group + ".permissions";
                // iterate through global 'permissions'
                for (Map.Entry<String, Object> globalNode : groups.getNode(permKey).getAll().entrySet()) {
                    if (!globals.contains(globalNode.getKey()) && !globals.contains("-" + globalNode.getKey())) {
                        if ((Boolean) globalNode.getValue()) {
                            globals.add(globalNode.getKey());
                        } else if (!(Boolean) globalNode.getValue()) {
                            globals.add("-" + globalNode.getKey());
                        }
                    }
                }
                plugin.getGroupNode(group).setProperty("permissions", globals);
                // iterate through worlds
                if (groups.getKeys(group + ".worlds") != null) {
                    for (String world : groups.getKeys(group + ".worlds")) {
                        String worldKey = group + ".worlds." + world;
                        List<String> worldNodes = plugin.getGroupNode(group).getStringList("worlds." + world, null);
                        // iterate through world keys for individual nodes
                        if (groups.getKeys(worldKey) != null) {
                            for (Map.Entry<String, Object> worldNode : groups.getNode(worldKey).getAll().entrySet()) {
                                if (!worldNodes.contains(worldNode.getKey()) && !worldNodes.contains("-" + worldNode.getKey())) {
                                    if ((Boolean)worldNode.getValue()) {
                                        worldNodes.add(worldNode.getKey());
                                    } else if (!(Boolean)worldNode.getValue()) {
                                        worldNodes.add("-" + worldNode.getKey());
                                    }
                                }
                            }
                        }
                        plugin.getGroupNode(group).setProperty("worlds." + world, worldNodes);
                    }
                }
                plugin.info("Imported " + group + " in " + (System.currentTimeMillis() - time) + "ms");
                plugin.getGroups().save();
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
                Configuration conf = new Configuration(gGroups);
                conf.load();
                if (conf.getKeys("groups") != null) {
                    for (String group : conf.getKeys("groups")) {
                        plugin.buildGroup(group);
                        ConfigurationNode NODE = conf.getNode("groups." + group);
                        List<String> globals = plugin.getGroupNode(group).getStringList("permissions", null);
                        List<String> nodes = NODE.getStringList("permissions", null);
                        if (NODE.getBoolean("default", false)) {
                            plugin.info("Setting default group to '" + group + "'");
                            plugin.getConfiguration().setProperty("default_group", group);
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
                        List<String> parents = plugin.getGroupNode(group).getStringList("inheritance", null);
                        List<String> inherits = NODE.getStringList("inheritance", null);
                        for (String parent : inherits) {
                            parents.remove(parent);
                            parents.add(parent);
                        }
                        plugin.getGroupNode(group).setProperty("permissions", globals);
                        plugin.getGroupNode(group).setProperty("inheritance", parents);
                        plugin.getGroupNode(group).setProperty("rank", NODE.getInt("info.rank", 0));
                    }
                }
            }
            for (File folder : files) {
                if (folder.isDirectory()) {
                    File gConfig = new File("plugins/Permissions/" + folder.getName() + "/groups.yml");
                    if (gConfig.exists()) {
                        Configuration conf = new Configuration(gConfig);
                        conf.load();
                        ConfigurationNode groups = conf.getNode("groups");
                        if (groups == null) { continue; }
                        for (String group : groups.getKeys()) {
                            long groupTIME = System.currentTimeMillis();
                            // create the group if it doesn't exist, and get the nodes for this world
                            plugin.buildGroup(group);
                            List<String> worldNodes = plugin.getGroupNode(group).getStringList("worlds." + folder.getName(), null);
                            // get the node for this group from the permissions 3 config
                            ConfigurationNode NODE = groups.getNode(group);
                            // get the list of permissions
                            List<String> nodes = NODE.getStringList("permissions", null);
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
                            List<String> parents = plugin.getGroupNode(group).getStringList("inheritance", null);
                            List<String> inherits = NODE.getStringList("inheritance", null);
                            for (String parent : inherits) {
                                parents.remove(parent);
                                parents.add(parent);
                            }
                            plugin.getGroupNode(group).setProperty("rank", NODE.getInt("info.rank", 0));
                            plugin.getGroupNode(group).setProperty("worlds." + folder.getName(), worldNodes);
                            plugin.getGroupNode(group).setProperty("inheritance", parents);
                            plugin.info("Imported '" + group + "' from Permissions 3 in " + (System.currentTimeMillis() - groupTIME) + "ms");
                        }
                    }
                }
            }
            file.renameTo(new File("plugins/Permissions_DEPRECATED"));
            plugin.info("Permissions 3 import finished in " + (System.currentTimeMillis() - imp) + "ms");
            plugin.info("Permissions 3 can now be deleted.");
            plugin.info("Please check plugin/Privileges/groups.yml. Some groups may have unset 'rank' keys.");
            plugin.getGroups().save();
            plugin.getConfiguration().save();
        }
    }


}
