package gcewing.sg.util;

import gcewing.sg.tileentity.SGBaseTE;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GateUtil {
    public static TileEntity locateLocalGate(World world, BlockPos pos, int radius, boolean debug) {
        for (final BlockPos.MutableBlockPos nearPos : BlockPos.getAllInBoxMutable(
            pos.add(-radius, -radius, -radius),
            pos.add(radius, radius, radius)
        )) {
            TileEntity gateTE = world.getTileEntity(nearPos);

            if (gateTE instanceof SGBaseTE) {
                if (gateTE != null) {
                    if (debug) {
                        //System.err.println("Found SGBaseTE at: " + gateTE.getPos());
                    }
                    return gateTE;
                }
            }
        }

        if (debug) {
            System.err.println("Failed to find SGBaseTE.");
        }
        return null;
    }
}
