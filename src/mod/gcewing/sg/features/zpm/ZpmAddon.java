package gcewing.sg.features.zpm;

import gcewing.sg.features.ic2.zpm.ZpmInterfaceCartTE;
import gcewing.sg.interfaces.ISGEnergySource;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.text.DecimalFormat;
import java.util.Optional;

public class ZpmAddon {

    final static boolean debugAddon = true;
    final static DecimalFormat dFormat = new DecimalFormat("###,###,###,##0");

    public static boolean routeRequiresZPM(World origin, World destination) {
        return routeRequiresZpm(origin.getWorldInfo().getWorldName().toLowerCase(), destination.getWorldInfo().getWorldName().toLowerCase());
    }

    public static boolean routeRequiresZpm(String origin, String destination) {
        Optional<Double> multiplier = ZPMMultiplierRegistry.getMultiplierFrom(origin, destination);
        if (!multiplier.isPresent()) {
            return false;
        }

        if (multiplier.get() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static int routeZpmMultiplier(String origin, String destination) {
        Optional<Double> multiplier = ZPMMultiplierRegistry.getMultiplierFrom(origin, destination);
        if (!multiplier.isPresent()) {
            return 0;
        }

        return multiplier.get().intValue();
    }

    public static double zpmPowerAvailable(World world, BlockPos pos, int radius, boolean debug) {
        double zpmPower = 0.0;
        for (final BlockPos.MutableBlockPos nearPos : BlockPos.getAllInBoxMutable(
            pos.add(-radius, -radius, -radius),
            pos.add(radius, radius, radius)
        )) {
            TileEntity nte = world.getTileEntity(nearPos);
            if (nte != null) {
                if (nte instanceof ZpmInterfaceCartTE) {
                    if (debugAddon) {
                        System.out.printf("SGBaseTE.zpmInterfaceCartNear: %s at %s\n", nte, nearPos);
                    }
                    if (((ZpmInterfaceCartTE) nte).isEmpty()) {
                        if (debugAddon) {
                            System.out.println("ZPM cart is empty");
                        }
                        return 0;
                    }
                    zpmPower += ((ISGEnergySource) nte).availableEnergy();
                }
                if (nte instanceof ZpmConsoleTE) {
                    if (debugAddon) {
                        System.out.printf("SGBaseTE.zpmConsoleNear: %s at %s\n", nte, nearPos);
                    }
                    if (((ZpmConsoleTE) nte).isEmpty()) {
                        if (debugAddon) {
                            System.out.println("ZPM cart is empty");
                        }
                        return 0;
                    }
                    zpmPower += ((ISGEnergySource) nte).availableEnergy();
                }
            }
        }
        if (debugAddon) {
            System.out.println("SGCraft:ZpmAddon - Power Available: " + dFormat.format(zpmPower));
        }
        return zpmPower;
    }
}
