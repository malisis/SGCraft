package gcewing.sg.features.zpm;

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

public final class ZPMMultiplierRegistry {

    private static final Map<String, Map<String, Double>> multipliers = new HashMap<>();
    private static final String WORLDS_NODE = "worlds";

    public static ConfigurationNode createRootNode(final Path path) throws IOException {
        final YAMLConfigurationLoader loader = YAMLConfigurationLoader.builder().setPath(path).setFlowStyle(DumperOptions.FlowStyle.BLOCK).build();

        if (Files.notExists(path)) {
            Files.createDirectories(path.getParent());
            Files.createFile(path);
            final ConfigurationNode node = loader.createEmptyNode(ConfigurationOptions.defaults());
            // Simply writing out default template, don't mind me
            node.getNode(WORLDS_NODE, "world", "DIM-1").setValue(0);
            node.getNode(WORLDS_NODE, "world", "DIM1").setValue(1);
            loader.save(node);
        }

        return loader.load(ConfigurationOptions.defaults());
    }

    public static void populateMultipliers(final ConfigurationNode root) {
        checkNotNull(root);

        multipliers.clear();

        final ConfigurationNode worldsRoot = root.getNode(WORLDS_NODE);
        for (final Map.Entry<Object, ? extends ConfigurationNode> fromEntry : worldsRoot.getChildrenMap().entrySet()) {
            final String from = fromEntry.getKey().toString().toLowerCase();
            final ConfigurationNode fromNode = fromEntry.getValue();

            final Map<String, Double> fromMultipliers = multipliers.computeIfAbsent(from, k -> new HashMap<>());

            for (final Map.Entry<Object, ? extends ConfigurationNode> toEntry : fromNode.getChildrenMap().entrySet()) {
                final String to = toEntry.getKey().toString().toLowerCase();
                final double multiplier = toEntry.getValue().getDouble(0.0);

                fromMultipliers.put(to, multiplier);
            }
        }
    }

    public static Optional<Double> getMultiplierFrom(final String from, final String to) {
        checkNotNull(from);
        checkNotNull(to);

        final Map<String, Double> toMultipliers = multipliers.get(from.toLowerCase());
        if (toMultipliers == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(toMultipliers.get(to.toLowerCase()));
    }
}