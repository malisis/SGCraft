//------------------------------------------------------------------------------------------------
//
//   SG Craft - Naquadah ore block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.util.*;

// import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.*;
import net.minecraft.item.*;
// import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class NaquadahOreBlock extends BaseOreBlock {

    public NaquadahOreBlock() {
        super();
        setHardness(5.0F);
        setResistance(10.0F);
        setStepSound(soundTypeStone);
        setHarvestLevel("pickaxe", 3);
        setCreativeTab(CreativeTabs.tabBlock);
    }
    
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return SGCraft.naquadah;
    }
    
    @Override
    public int quantityDropped(Random random) {
        return 2 + random.nextInt(5);
    }
    
    // Almura: added this method to override BaseBlock.java to fix infinite loops within getBlock() and TileEntityChest.
    @Override
    public void onBlockAdded(World world, int x, int y, int z) {}
    
    // Almura: end.
}
