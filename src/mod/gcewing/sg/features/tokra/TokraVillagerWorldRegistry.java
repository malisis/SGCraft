package gcewing.sg.features.tokra;
import static com.google.common.base.Preconditions.checkNotNull;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.yaml.snakeyaml.DumperOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class TokraVillagerWorldRegistry {

    private static final Map<String, Boolean> tokraWorlds = new HashMap<>();
    private static final String WORLDS_NODE = "worlds";

    public static ConfigurationNode createRootNode(final Path path) throws IOException {
        final YAMLConfigurationLoader loader = YAMLConfigurationLoader.builder().setPath(path).setFlowStyle(DumperOptions.FlowStyle.BLOCK).build();

        if (Files.notExists(path)) {
            Files.createDirectories(path.getParent());
            Files.createFile(path);
            final ConfigurationNode node = loader.createEmptyNode(ConfigurationOptions.defaults());
            // Simply writing out default template, don't mind me
            node.getNode(WORLDS_NODE, "world").setValue(true);
            node.getNode(WORLDS_NODE, "world_2").setValue(false);
            loader.save(node);
        }

        return loader.load(ConfigurationOptions.defaults());
    }

    public static void populateTokraVillagerWorlds(final ConfigurationNode root) {
        checkNotNull(root);

        tokraWorlds.clear();

        final ConfigurationNode worldsRoot = root.getNode(WORLDS_NODE);
        for (final Map.Entry<Object, ? extends ConfigurationNode> worldEntry : worldsRoot.getChildrenMap().entrySet()) {
            final String world = worldEntry.getKey().toString().toLowerCase();
            final boolean value = worldEntry.getValue().getBoolean();
            tokraWorlds.put(world, value);
        }
    }

    public static Optional<Boolean> populateTokraVillagers(final String world) {
        checkNotNull(world.toLowerCase());

        return Optional.ofNullable(tokraWorlds.get(world.toLowerCase()));
    }
}

