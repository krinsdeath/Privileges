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
                plugin.getGroups().setProperty("groups." + group + ".rank", plugin.getGroupNode(group).getInt("rank", 0));
                List<String> globals = plugin.getGroupNode(group).getStringList("permissions", null);
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
                Collections.sort(globals, new Comparator<String>() {
                    public int compare(String a, String b) {
                        return a.compareTo(b);
                    }
                });
                plugin.getGroups().setProperty("groups." + group + ".permissions", globals);
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
                        Collections.sort(worldNodes, new Comparator<String>() {
                            public int compare(String a, String b) {
                                return a.compareTo(b);
                            }
                        });
                        plugin.getGroups().setProperty("groups." + group + ".worlds." + world, worldNodes);
                    }
                }
                plugin.info("Imported " + group + " in " + (System.currentTimeMillis() - time) + "ms");
                plugin.getGroups().save();
            }
            plugin.info("PermissionsBukkit import finished in " + (System.currentTimeMillis() - imp) + "ms");
            plugin.info("You can now delete PermissionsBukkit.");
        }
    }


}
