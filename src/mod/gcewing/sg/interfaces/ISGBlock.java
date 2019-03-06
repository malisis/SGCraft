//------------------------------------------------------------------------------------------------
//
//   SG Craft - Interface for stargate ring and base blocks
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.interfaces;

import gcewing.sg.tileentity.SGBaseTE;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public interface ISGBlock {

    public SGBaseTE getBaseTE(IBlockAccess world, BlockPos pos);
    public boolean isMerged(IBlockAccess world, BlockPos pos);

}
