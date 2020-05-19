package gcewing.sg.features.pdd;

import com.google.common.collect.Maps;
import net.minecraft.nbt.NBTTagCompound;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.yaml.snakeyaml.DumperOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;

public class Address {

    private static final Path configPath = Paths.get(".", "config", "SGCraft", "pdd.yml");
    //NBT tag names
    public static final String ADDRESSES = "addresses";//public because used from PddItem
    private static final String NAME = "name";
    private static final String ADDRESS = "address";
    private static final String LOCKED = "locked";
    private static final String INDEX = "index";

    private static final Map<String, Address> defaultAddresses = Maps.newHashMap();

    private String address;
    private String name;
    private int index;
    private boolean locked;


    public Address(String address, String name, int index, boolean locked) {
        this.address = address.toUpperCase();
        this.name = name;
        this.index = index;
        this.locked = locked;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public boolean isLocked() {
        return locked;
    }

    public static Address fromNBT(NBTTagCompound compound) {
        String address = compound.getString(ADDRESS);
        String name = compound.getString(NAME);
        int index = compound.getInteger(INDEX);
        boolean locked = compound.getBoolean(LOCKED);

        return new Address(address, name, index, locked);
    }

    public NBTTagCompound toNBT(NBTTagCompound compound) {
        compound.setString(ADDRESS, address);
        compound.setString(NAME, name);
        compound.setInteger(INDEX, index);
        compound.setBoolean(LOCKED, locked);
        return compound;
    }

    public static Collection<Address> getDefaultAddresses() {
        return defaultAddresses.values();
    }


    public static void loadDefaultAddresses() {
        try {
            YAMLConfigurationLoader loader =
                    YAMLConfigurationLoader.builder().setPath(configPath).setFlowStyle(DumperOptions.FlowStyle.BLOCK).build();

            //create default config for first time run
            if (Files.notExists(configPath)) {
                Files.createDirectories(configPath.getParent());
                Files.createFile(configPath);
                final ConfigurationNode node = loader.createEmptyNode(ConfigurationOptions.defaults());
                // Simply writing out default template, don't mind me
                node.getNode(ADDRESSES, "ABCD-EFG-HI", "name").setValue("Descriptive Name Here");
                node.getNode(ADDRESSES, "ABCD-EFG-HI", "locked").setValue(false);
                loader.save(node);
            }


            //load default address from config
            ConfigurationNode addresses = loader.load(ConfigurationOptions.defaults()).getNode(ADDRESSES);
            for (Map.Entry<Object, ? extends ConfigurationNode> addressEntry : addresses.getChildrenMap().entrySet()) {
                final String address = addressEntry.getKey().toString().toLowerCase();
                final String name = addressEntry.getValue().getNode("name").getString("");
                final boolean locked = addressEntry.getValue().getNode("locked").getBoolean(true);
                if (name.isEmpty()) {
                    System.err.println("Unable to add default PDD entry, missing name!");
                    continue;
                }

                defaultAddresses.put(address, new Address(address, name, 0, locked));
            }

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
