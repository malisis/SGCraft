package gcewing.sg.ic2.zpm;

import gcewing.sg.ISGEnergySource;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

import java.text.DecimalFormat;

public class ZpmAddon {

    // Note: The following code has been added for support with specific added methods within SGBaseTE.
    final static boolean debugAddon = false;
    final static DecimalFormat dFormat = new DecimalFormat("###,###,###,##0");

    // Todo:  make this entire lookup configurable via Config instead of this static lookup.
    public static boolean isAlmuraLoaded () {
        return Loader.isModLoaded("almura");
    }

    public static boolean worldRequiresZPM(World origin, World destination) {
        return worldRequiresZPM(origin.getWorldInfo().getWorldName().toLowerCase(), destination.getWorldInfo().getWorldName().toLowerCase());
    }

    public static boolean worldRequiresZPM(String origin, String destination) {
        if (origin.equalsIgnoreCase(destination)) {
            return false;
        }

        if (isAlmuraLoaded()) {
            if (origin.equalsIgnoreCase("dakara") || origin.equalsIgnoreCase("nether") || origin.equalsIgnoreCase("the_end") || origin.equalsIgnoreCase("asgard") || origin.equalsIgnoreCase("orilla")) {
                if (destination.equalsIgnoreCase("dakara") || destination.equalsIgnoreCase("nether") || destination.equalsIgnoreCase("the_end") || destination.equalsIgnoreCase("asgard") || destination.equalsIgnoreCase("orilla")) {
                    if (debugAddon) {
                        System.out.println("SGCraft:ZpmAddon - Origin: [" + origin + "] -> Destination: [" + destination + "] - Zpm Required: false");
                    }
                    return false;
                }
            }
        }

        if (debugAddon) {
            System.out.println("SGCraft:ZpmAddon - Origin: [" + origin + "] -> Destination: [" + destination + "] - Zpm Required: true");
        }
        return true;
    }

    public static int worldZpmMultiplier(String origin, String destination) {
        if (isAlmuraLoaded()) {
            if (origin.equalsIgnoreCase("dakara") || origin.equalsIgnoreCase("nether") || origin.equalsIgnoreCase("the_end") || origin.equalsIgnoreCase("asgard") || origin.equalsIgnoreCase("orilla")) {
                if (destination.equalsIgnoreCase("dakara") || destination.equalsIgnoreCase("nether") || destination.equalsIgnoreCase("the_end") || destination.equalsIgnoreCase("asgard") || destination.equalsIgnoreCase("orilla")) {
                    if (debugAddon) {
                        System.out.println("SGCraft:ZpmAddon - Origin: [" + origin + "] -> Destination: [" + destination + "] - Zpm Power Multiplier: " + 0);
                    }
                    return 0;
                }
            }

            if (origin.equalsIgnoreCase("keystone") || origin.equalsIgnoreCase("cemaria") || origin.equalsIgnoreCase("atlantis") || origin.equalsIgnoreCase("zeal")) {
                if (debugAddon) {
                    System.out.println("SGCraft:ZpmAddon - Origin: [" + origin + "] -> Destination: [" + destination + "] - Zpm Power Multiplier: " + 10);
                }
                return 25;
            }

            if (destination.equalsIgnoreCase("keystone") || destination.equalsIgnoreCase("cemaria") || destination.equalsIgnoreCase("atlantis") || destination.equalsIgnoreCase("zeal")) {
                if (debugAddon) {
                    System.out.println("SGCraft:ZpmAddon - Origin: [" + origin + "] -> Destination: [" + destination + "] - Zpm Power Multiplier: " + 10);
                }
                return 25;
            }
        }

        return 10;
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
                        zpmPower = ((ISGEnergySource) nte).totalAvailableEnergy();
                    }
                    break;
                }
            }
        }
        if (debugAddon) {
            System.out.println("SGCraft:ZpmAddon - Power Available: " + dFormat.format(zpmPower));
        }
        return zpmPower;
    }
}
