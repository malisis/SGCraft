package gcewing.sg.features.ic2.zpm.modulehub;

import ic2.api.energy.prefab.BasicSource;
import ic2.api.energy.tile.IEnergyAcceptor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public final class ZpmHubBasicSource extends BasicSource {

    private final TileEntity parent;

    public ZpmHubBasicSource(final TileEntity parent, final double capacity, final int tier) {
        super(parent, capacity, tier);
        this.parent = parent;
    }

    @Override
    public void drawEnergy(double amount) {
        ZpmHubTE te = ((ZpmHubTE) this.parent);
        if (te != null) {
            te.drawEnergy(amount);

            int zpmCount = ((ZpmHubTE) this.parent).getZpmSlotsloaded();

            double drawAmount = amount / zpmCount;
            int perZpmDrawAmount = (int) (drawAmount / zpmCount);

            if (te.zpmSlot0Energy > 0) {
                te.zpmSlot0Energy = te.zpmSlot0Energy - perZpmDrawAmount;
            }
            if (te.zpmSlot1Energy > 0) {
                te.zpmSlot1Energy = te.zpmSlot1Energy - perZpmDrawAmount;
            }
            if (te.zpmSlot2Energy > 0) {
                te.zpmSlot2Energy = te.zpmSlot2Energy - perZpmDrawAmount;
            }

            super.drawEnergy(drawAmount);

            te.markChanged();
            // Todo: fix this.
            /*
            if (((ZpmHubTE)this.parent).isTainted(((ZpmHubTE)this.parent).getStackInSlot(0))) {
                world.newExplosion(null, this.pos.getX(), this.pos.getY(), this.pos.getZ(), (float)250, true, true);
            } */
        }
    }

    @Override
    public boolean emitsEnergyTo(IEnergyAcceptor iEnergyAcceptor, EnumFacing direction) {
        // Todo: math needed to only output energy on one side of the zpm interface cart
        return true;
    }
}
