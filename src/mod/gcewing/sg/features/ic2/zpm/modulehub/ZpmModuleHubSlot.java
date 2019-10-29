package gcewing.sg.features.ic2.zpm.modulehub;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ZpmModuleHubSlot extends Slot {

    public ZpmModuleHubSlot(IInventory inv, int i, int x, int y) {
        super(inv, i, x, y);
    }

    public boolean isItemValid(ItemStack stack) {
        return ZpmModuleHubTE.isValidFuelItem(stack);
    }

}
