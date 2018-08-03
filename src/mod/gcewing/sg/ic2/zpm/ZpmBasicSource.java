package gcewing.sg.ic2.zpm;

import ic2.api.energy.prefab.BasicSource;
import net.minecraft.tileentity.TileEntity;

public final class ZpmBasicSource extends BasicSource {

    private final TileEntity parent;

    public ZpmBasicSource(final TileEntity parent, final double capacity, final int tier) {
        super(parent, capacity, tier);
        this.parent = parent;
    }

    @Override
    public void drawEnergy(double amount) {
        super.drawEnergy(amount);
        this.parent.markDirty();
    }
}
