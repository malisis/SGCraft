package gcewing.sg.features.zpm;

import gcewing.sg.features.ic2.zpm.ZpmInterfaceCartTE;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ZpmConsoleSlot extends Slot {

    public ZpmConsoleSlot(IInventory inv, int i, int x, int y) {
        super(inv, i, x, y);
    }

    public boolean isItemValid(ItemStack stack) {
        return ZpmConsoleTE.isValidFuelItem(stack);
    }

}
