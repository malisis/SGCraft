package gcewing.sg.features.pdd;

import static com.google.common.base.Preconditions.checkNotNull;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.yaml.snakeyaml.DumperOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class AddressNameRegistry {

    private static final Map<String, AddressDataEntry> entries = new HashMap<>();
    private static final String ADDRESSES_NODE = "addresses";

    public static ConfigurationNode createRootNode(final Path path) throws IOException {
        final YAMLConfigurationLoader loader = YAMLConfigurationLoader.builder().setPath(path).setFlowStyle(DumperOptions.FlowStyle.BLOCK).build();

        if (Files.notExists(path)) {
            Files.createDirectories(path.getParent());
            Files.createFile(path);
            final ConfigurationNode node = loader.createEmptyNode(ConfigurationOptions.defaults());
            // Simply writing out default template, don't mind me
            node.getNode(ADDRESSES_NODE, "ABCD-EFG-HI", "name").setValue("Descriptive Name Here");
            node.getNode(ADDRESSES_NODE, "ABCD-EFG-HI", "locked").setValue(false);
            loader.save(node);
        }

        return loader.load(ConfigurationOptions.defaults());
    }

    public static void populateNames(final ConfigurationNode root) {
        checkNotNull(root);
        entries.clear();

        final ConfigurationNode addressesRoot = root.getNode(ADDRESSES_NODE);
        for (final Map.Entry<Object, ? extends ConfigurationNode> addressEntry : addressesRoot.getChildrenMap().entrySet()) {
            final String address = addressEntry.getKey().toString().toLowerCase();
            final String name = addressEntry.getValue().getNode("name").getString("");
            final boolean locked = addressEntry.getValue().getNode("locked").getBoolean(true);
            if (name.isEmpty()) {
                System.err.println("Unable to add default PDD entry, missing name!");
                continue;
            }

            entries.put(address, new AddressDataEntry(name, locked));
        }
    }

    public static Optional<AddressDataEntry> getEntry(final String address) {
        checkNotNull(address);

        return Optional.ofNullable(entries.get(address));
    }

    public static Collection<Map.Entry<String, AddressDataEntry>> getEntries() {
        return Collections.unmodifiableCollection(entries.entrySet());
    }

    public static List<AddressData> getDefaultPDDEntries() {
        int index = 0;
        final List<AddressData> addresses = new ArrayList<>();
        for (Map.Entry<String, AddressDataEntry> entry : getEntries()) {
            final String address = entry.getKey();
            final String name = entry.getValue().getName();
            final boolean locked = entry.getValue().isLocked();

            if (address.length() == 11 && address.substring(4,5).equalsIgnoreCase("-") && address.substring(8,9).equalsIgnoreCase("-")) {
                addresses.add(new AddressData(name, address.toUpperCase(), locked, index, 0));
                index++;
            } else {
                System.err.println("Unable to add default PDD entry, invalid address format!");
            }

        }
        return addresses;
    }
}