//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate controller fuelling gui container
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.features.zpm;

import gcewing.sg.BaseContainer;
import gcewing.sg.tileentity.DHDTE;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ZpmConsoleContainer extends BaseContainer {

    static final int numFuelSlotColumns = 2;
    static final int zpmSlotsX = 120;
    static final int zpmSlotsY = 54;
    static final int playerSlotsX = 48;
    static final int playerSlotsY = 124;

    ZpmConsoleTE te;

    public static ZpmConsoleContainer create(EntityPlayer player, World world, BlockPos pos) {
        ZpmConsoleTE te = ZpmConsoleTE.at(world, pos);
        if (te != null) {
            return new ZpmConsoleContainer(player, te);
        } else {
            System.out.println("SHouldn't happen!!!");
            return null;
        }
    }

    public ZpmConsoleContainer(EntityPlayer player, ZpmConsoleTE te) {
        super(ZPMConsoleScreen.guiWidth, ZPMConsoleScreen.guiHeight);
        this.te = te;

        addZpmSlots();
        addPlayerSlots(player, playerSlotsX, playerSlotsY);
    }
    
    void addZpmSlots() {
        int b = ZpmConsoleTE.firstZpmSlot;
        int n = ZpmConsoleTE.numZpmSlots;
        for (int i = 0; i < n; i++) {
            int row = i / numFuelSlotColumns;
            int col = i % numFuelSlotColumns;
            int x = zpmSlotsX + col * 18;
            int y = zpmSlotsY + row * 18;
            System.out.println("Adding Slot: " + i);
            addSlotToContainer(new ZpmConsoleSlot(te, b + i, x, y));
        }
    }


    @Override
    protected SlotRange transferSlotRange(int srcSlotIndex, ItemStack stack) {
        SlotRange range = new SlotRange();
        range.firstSlot = 0;
        range.numSlots = 1;
        return range;
    }
}
