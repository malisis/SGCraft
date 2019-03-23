//------------------------------------------------------------------------------------------------
//
//   SG Craft - RF Stargate Power Unit Tile Entity
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.tileentity;

import gcewing.sg.SGCraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public class SGPowerTE extends PowerTE implements IEnergyStorage {

    private int maxInput = 10000;
    private int maxOutput = 10000;
    private EnergyStorage storage = new EnergyStorage(SGCraft.FPMaxEnergyBuffer, maxInput, maxOutput);

    public SGPowerTE() {
        super(SGCraft.FPMaxEnergyBuffer, SGCraft.FPPerSGEnergyUnit);
    }

    @Override
    public String getScreenTitle() {
        return "SGPU";
    }

    @Override
    public String getUnitName() {
        return "SG";
    }

    @Override
    public void readContentsFromNBT(NBTTagCompound nbttagcompound) {
        super.readContentsFromNBT(nbttagcompound);
        if (nbttagcompound.hasKey("capacity")) {
            int capacity = nbttagcompound.getInteger("capacity");
            int energy = nbttagcompound.getInteger("energy");
            storage = new EnergyStorage(capacity, this.maxInput, this.maxOutput, energy);
        }

        if (SGCraft.forceFPCfgUpdate) {
            energyMax = SGCraft.FPMaxEnergyBuffer;
            energyPerSGEnergyUnit = SGCraft.FPPerSGEnergyUnit;
            storage = new EnergyStorage((int)energyMax, this.maxInput, maxOutput );
        }
    }

    @Override
    public void writeContentsToNBT(NBTTagCompound nbttagcompound) {
        super.writeContentsToNBT(nbttagcompound);
        nbttagcompound.setInteger("capacity", storage.getMaxEnergyStored());
        nbttagcompound.setInteger("energy", storage.getEnergyStored());
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability.equals(CapabilityEnergy.ENERGY) || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (hasCapability(capability, facing))
            return CapabilityEnergy.ENERGY.cast(this);
        return super.getCapability(capability, facing);
    }

    //------------------------ IEnergyStorage ---------------------------

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int result = storage.receiveEnergy(maxReceive, simulate);
        energyBuffer = storage.getEnergyStored();
        markChanged();

        return result;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int result = storage.extractEnergy(maxExtract, simulate);
        energyBuffer = storage.getEnergyStored();
        markChanged();

        return result;
    }

    @Override
    public int getEnergyStored() {
        return storage.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return storage.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return storage.canExtract();
    }

    @Override
    public boolean canReceive() {
        return storage.canReceive();
    }

    @Override public double totalAvailableEnergy() {
        return energyBuffer;
    }
}
