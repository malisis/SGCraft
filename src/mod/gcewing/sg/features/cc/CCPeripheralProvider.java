//------------------------------------------------------------------------------------------------
//
//   SG Craft - Computercraft tile entity peripheral provider
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.features.cc;

import net.minecraft.tileentity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.util.*;
import dan200.computercraft.api.peripheral.*;

public class CCPeripheralProvider implements IPeripheralProvider {

    @Override
    public IPeripheral getPeripheral(World world, BlockPos pos, EnumFacing side) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof CCInterfaceTE) {
            IPeripheral peripheral = ((CCInterfaceTE) te).getPeripheral();
            if (peripheral == null) {
                peripheral = new CCSGPeripheral(te);
                ((CCInterfaceTE) te).setPeripheral(peripheral);
            }
            return peripheral;
        } else {
            return null;
        }
    }
}
