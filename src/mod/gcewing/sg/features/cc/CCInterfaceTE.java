//------------------------------------------------------------------------------------------------
//
//   SG Craft - Computercraft Interface Tile Entity
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.features.cc;

import dan200.computercraft.api.peripheral.IComputerAccess;
import gcewing.sg.interfaces.IComputerInterface;
import gcewing.sg.tileentity.SGInterfaceTE;
import net.minecraft.tileentity.TileEntity;

import java.util.HashSet;
import java.util.Set;

public class CCInterfaceTE extends SGInterfaceTE implements IComputerInterface {

    Set<IComputerAccess> attachedComputers = new HashSet<IComputerAccess>();

    public void postEvent(TileEntity source, String name, Object... args) {
        //System.out.printf("CCInterfaceTE.postEvent: %s\n", name);
        for (IComputerAccess cpu : attachedComputers)
            cpu.queueEvent(name, prependArgs(cpu.getAttachmentName(), args));
    }
    
}
