package gcewing.sg.util;

import gcewing.sg.tileentity.DHDTE;
import gcewing.sg.tileentity.SGBaseTE;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GateUtil {

    public static SGBaseTE findGate(World world, EntityPlayer player, int radius) {
        BlockPos playerPos = new BlockPos(player.posX, player.posY, player.posZ);
        for (BlockPos.MutableBlockPos pos : BlockPos.getAllInBoxMutable(playerPos.add(-radius, -radius, -radius),
                playerPos.add(radius, radius, radius))) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof SGBaseTE) {
                return (SGBaseTE) te;
            }

            if (te instanceof DHDTE) {
                te = ((DHDTE) te).getLinkedStargateTE();
                if (te != null) {
                    return (SGBaseTE) te;
                }
            }
        }
        return null;
    }

    public static TileEntity locateLocalGate(World world, BlockPos pos, int radius, boolean debug) {
        for (final BlockPos.MutableBlockPos nearPos : BlockPos.getAllInBoxMutable(
                pos.add(-radius, -radius, -radius),
                pos.add(radius, radius, radius)
        )) {
            TileEntity gateTE = world.getTileEntity(nearPos);

            if (!(gateTE instanceof SGBaseTE)) {
                TileEntity dhdBaseTE = GateUtil.locateDHD(world, pos, radius, debug);
                if (dhdBaseTE instanceof DHDTE) {
                    DHDTE dhd = (DHDTE) dhdBaseTE;
                    if (dhd.isLinkedToStargate) {
                        gateTE = dhd.getLinkedStargateTE();
                    }
                }
            }

            if (gateTE instanceof SGBaseTE) {
                if (gateTE != null) {
                    if (debug) {
                        System.err.println("Found SGBaseTE at: " + gateTE.getPos());
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

    public static TileEntity locateDHD(World world, BlockPos pos, int radius, boolean debug) {
        for (final BlockPos.MutableBlockPos nearPos : BlockPos.getAllInBoxMutable(
                pos.add(-radius, -radius, -radius),
                pos.add(radius, radius, radius)
        )) {
            TileEntity dhdTE = world.getTileEntity(nearPos);

            if (dhdTE instanceof DHDTE) {
                if (dhdTE != null) {
                    if (debug) {
                        System.err.println("Found DHDTE at: " + dhdTE.getPos());
                    }
                    return dhdTE;
                }
            }
        }

        if (debug) {
            System.err.println("Failed to find DHDTE.");
        }
        return null;
    }
}
