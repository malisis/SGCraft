package gcewing.sg.features.pdd;

public class AddressDataEntry {

    private final String name;
    private final boolean locked;

    public AddressDataEntry(String name, boolean locked) {
        this.name = name;
        this.locked = locked;
    }

    public String getName() {
        return name;
    }

    public boolean isLocked() {
        return locked;
    }
}
