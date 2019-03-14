package gcewing.sg.features.pdd;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public final class AddressData {

    static final String ADDRESSES = "addresses";
    private static final String NAME = "name";
    private static final String ADDRESS = "address";
    private static final String LOCKED = "locked";
    private static final String INDEX = "index";
    private static final String UNID = "unid";

    private final String name;
    private final String address;
    private final boolean locked;
    private final int index;
    private final int unid;

    public AddressData(final String name, final String address, final boolean locked, final int index, final int unid) {
        this.name = name;
        this.address = address;
        this.locked = locked;
        this.index = index;
        this.unid = unid;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public boolean isLocked() {
        return locked;
    }

    public int getIndex() {
        return index;
    }

    public int getUnid() {
        return unid;
    }

    public static void updateAddress(EntityPlayer player, NBTTagCompound compound, int unid, String newName, String newAddress, int newIndex, boolean locked) {
        checkNotNull(compound);
        final List<AddressData> addresses = new ArrayList<>();
        final NBTTagList list = compound.getTagList(ADDRESSES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            final NBTTagCompound addressCompound = list.getCompoundTagAt(i);

            final int compoundEntryUnid = addressCompound.getInteger(UNID);
            if (compoundEntryUnid == unid) {
                addressCompound.setString(NAME, newName);
                addressCompound.setString(ADDRESS, newAddress);
                addressCompound.setInteger(INDEX, newIndex);
            }

            if (!(addressCompound.getInteger(INDEX) == -1)) { // -1 means delete the entry, so skip re-adding it to the list.
                addresses.add(new AddressData(addressCompound.getString(NAME), addressCompound.getString(ADDRESS), addressCompound.getBoolean(LOCKED), addressCompound.getInteger(INDEX), addressCompound.getInteger(UNID)));
            }
        }

        if (unid == 0) { // Indicates new request.
            addresses.add(new AddressData(newName, newAddress, locked, newIndex, 1));
        }

        writeAddresses(compound, addresses);
    }

    public static List<AddressData> getAddresses(final NBTTagCompound compound) {
        checkNotNull(compound);

        final List<AddressData> addresses = new ArrayList<>();
        final NBTTagList list = compound.getTagList(ADDRESSES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            final NBTTagCompound addressCompound = list.getCompoundTagAt(i);

            final String name = addressCompound.getString(NAME);
            final String address = addressCompound.getString(ADDRESS);
            final boolean locked = addressCompound.getBoolean(LOCKED);
            final int index = addressCompound.getInteger(INDEX);
            final int unid = addressCompound.getInteger(UNID);

            addresses.add(new AddressData(name, address, locked, index, unid));
        }

        return addresses;
    }

    public static NBTTagCompound writeAddresses(final NBTTagCompound compound, final List<AddressData> addresses) {
        checkNotNull(compound);
        checkNotNull(addresses);
        checkState(addresses.size() > 0, "Writing no addresses makes no sense!");

        //final NBTTagList list = compound.getTagList(ADDRESSES, Constants.NBT.TAG_COMPOUND);
        final NBTTagList list = new NBTTagList();

        for (final AddressData data : addresses) {
            final NBTTagCompound addressCompound = new NBTTagCompound();
            addressCompound.setString(NAME, data.getName());
            addressCompound.setString(ADDRESS, data.getAddress());
            addressCompound.setBoolean(LOCKED, data.isLocked());
            addressCompound.setInteger(INDEX, data.getIndex());
            addressCompound.setInteger(UNID, data.hashCode());

            list.appendTag(addressCompound);
        }

        compound.setTag(ADDRESSES, list);

        return compound;
    }
}
