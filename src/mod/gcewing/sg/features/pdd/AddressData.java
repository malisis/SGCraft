package gcewing.sg.features.pdd;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

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

    private final String name;
    private final String address;
    private final boolean locked;

    public AddressData(final String name, final String address, final boolean locked) {
        this.name = name;
        this.address = address;
        this.locked = locked;
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

    public static List<AddressData> getAddresses(final NBTTagCompound compound) {
        checkNotNull(compound);

        final List<AddressData> addresses = new ArrayList<>();
        final NBTTagList list = compound.getTagList(ADDRESSES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            final NBTTagCompound addressCompound = list.getCompoundTagAt(i);
            final String name = addressCompound.getString(NAME);
            final String address = addressCompound.getString(ADDRESS);
            final boolean locked = addressCompound.getBoolean(LOCKED);

            addresses.add(new AddressData(name, address, locked));
        }

        return addresses;
    }

    public static NBTTagCompound writeAddresses(final NBTTagCompound compound, final List<AddressData> addresses) {
        checkNotNull(compound);
        checkNotNull(addresses);
        checkState(addresses.size() > 0, "Writing no addresses makes no sense!");

        final NBTTagList list = compound.getTagList(ADDRESSES, Constants.NBT.TAG_COMPOUND);
        for (final AddressData data : addresses) {
            final NBTTagCompound addressCompound = new NBTTagCompound();
            compound.setString(NAME, data.getName());
            compound.setString(ADDRESS, data.getAddress());
            compound.setBoolean(LOCKED, data.isLocked());

            list.appendTag(addressCompound);
        }

        compound.setTag(ADDRESSES, list);

        return compound;
    }
}
