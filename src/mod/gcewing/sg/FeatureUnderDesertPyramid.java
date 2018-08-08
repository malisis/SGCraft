//------------------------------------------------------------------------------------------------
//
//   SG Craft - Generate stargate under desert pyramid
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import gcewing.sg.ic2.zpm.ZPMItem;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.template.TemplateManager;

import java.util.Random;

public class FeatureUnderDesertPyramid extends StructureComponent {

    StructureComponent base;
    boolean generateStructure = false;

    @Override
    protected void readStructureFromNBT(NBTTagCompound compound, TemplateManager templateManager) {}

    @Override
    protected void writeStructureToNBT(NBTTagCompound compound) {}

    public FeatureUnderDesertPyramid() {
        //System.out.printf("SGCraft: FeatureUnderDesertPyramid instantiated with no arguments\n");
    }

    public FeatureUnderDesertPyramid(StructureComponent base) {
        super(0);
        this.base = base;
        Random rand = new Random();
        generateStructure = rand.nextInt(100) <= FeatureGeneration.structureAugmentationChance;

        if (FeatureGeneration.debugStructures) {
            System.out.println("SGCraft: Creating FeatureUnderDesertPyramid with GenerateStructure: " + generateStructure);
        }

        StructureBoundingBox baseBox = base.getBoundingBox();
        BlockPos boxCenter = new BlockPos(baseBox.minX + (baseBox.maxX - baseBox.minX + 1) / 2, baseBox.minY + (baseBox.maxY - baseBox.minY + 1) / 2, baseBox.minZ + (baseBox.maxZ - baseBox.minZ + 1) / 2);
        int cx = boxCenter.getX();
        int cz = boxCenter.getZ();
        int bottom = baseBox.minY - 7;
        boundingBox = new StructureBoundingBox(cx - 5, bottom, cz - 5, cx + 5, bottom + 7, cz + 8);
        setCoordBaseMode(EnumFacing.SOUTH);
    }

    @Override
    public boolean addComponentParts(World world, Random rand, StructureBoundingBox clip) {
        return generateStructure && addAugmentationParts(world, rand, clip);
    }

    protected boolean addAugmentationParts(World world, Random rand, StructureBoundingBox clip) {
        if (FeatureGeneration.debugStructures) {
            System.out.printf("SGCraft: FeatureUnderDesertPyramid.addComponentParts in %s clipped to %s\n", getBoundingBox(), clip);
        }
        if (base == null) {
            System.out.printf("SGCraft: FeatureUnderDesertPyramid.addComponentParts: no base\n");
            return false;
        }
        StructureBoundingBox box = getBoundingBox();
        IBlockState air = Blocks.AIR.getDefaultState();
        IBlockState sandstone = Blocks.SANDSTONE.getDefaultState().withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.SMOOTH);
        IBlockState orange = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.ORANGE);
        IBlockState stairs = Blocks.SANDSTONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST);
        IBlockState ladder = Blocks.LADDER.getDefaultState();
        IBlockState dhd = SGCraft.sgControllerBlock.getDefaultState().withProperty(BaseOrientation.Orient4WaysByState.FACING, EnumFacing.NORTH);
        IBlockState sgBase = SGCraft.sgBaseBlock.getDefaultState().withProperty(BaseOrientation.Orient4WaysByState.FACING, EnumFacing.NORTH);
        IBlockState[] sgRings = new IBlockState[2];
        sgRings[0] = SGCraft.sgRingBlock.getDefaultState();
        sgRings[1] = sgRings[0].withProperty(SGRingBlock.VARIANT, 1);
        IBlockState chest = Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, EnumFacing.NORTH);
        //System.out.printf("SGCraft: FeatureUnderDesertPyramid.addComponentParts: " +
        //  "Filling (%d,%d,%d)-(%d,%d,%d)\n", box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
        // Main chamber
        fillWithBlocks(world, clip, 0, 0, 0, 10, 7, 10, sandstone, air, false);
        // Stairwell
        fillWithBlocks(world, clip, 4, 0, 11, 13, 7, 13, sandstone, air, false);
        // Stairwell entrance
        fillWithAir(world, clip, 12, 7, 12, 12, 9, 12);
        // Stairwell exit
        fillWithAir(world, clip, 5, 1, 10, 5, 2, 11);
        // Stairs
        setBlockState(world, sandstone, 12, 4, 12, clip);
        for (int i = 0; i < 4; i++)
            setBlockState(world, stairs, 8+i, 1+i, 12, clip);
        for (int i = 0; i < 3; i++)
            setBlockState(world, ladder, 12, 5+i, 12, clip);
        // Wall decorations
        fillWithBlocks(world, clip, 0, 3, 0, 10, 3, 10, orange, air, true);
        fillWithBlocks(world, clip, 3, 4, 10, 7, 4, 10, orange, air, true);
        // Floor decorations
        fillWithBlocks(world, clip, 3, 0, 4, 3, 0, 6, orange, air, true);
        fillWithBlocks(world, clip, 7, 0, 4, 7, 0, 6, orange, air, true);
        fillWithBlocks(world, clip, 4, 0, 3, 6, 0, 3, orange, air, true);
        fillWithBlocks(world, clip, 4, 0, 7, 6, 0, 7, orange, air, true);
        setBlockState(world, orange, 5, 0, 5, clip);
        // Door frame
        fillWithBlocks(world, clip, 4, 1, 10, 6, 3, 10, sandstone, air, true);
        // Stargate
        for (int i = -2; i <= 2; i++)
            for (int j = 0; j <= 4; j++) {
                IBlockState id;
                if (i == 0 && j == 0) {
                    id = sgBase;
                }
                else if (i == -2 || i == 2 || j == 0 || j == 4) {
                    id = sgRings[(i + j + 1) & 1];
                }
                else {
                    id = air;
                }
                setBlockState(world, id, 5+i, 1+j, 2, clip);
            }

        int baseX = box.minX + 5, baseY = box.minY + 1, baseZ = box.minZ + 2;
        SGBaseTE te = (SGBaseTE)world.getTileEntity(new BlockPos(baseX, baseY, baseZ));
        if (FeatureGeneration.debugStructures) {
            System.out.println("Stargate built at: " + baseX + "/" + baseY + "/" + baseZ);
        }

        if (te != null) {
            // Randomly give stargates the chevron upgrade.
            if (rand.nextInt(100) <= FeatureGeneration.chevronUpgradeChance) {
                te.hasChevronUpgrade = true;
                if (FeatureGeneration.debugStructures) {
                    System.out.println("Stargate at: [" + baseX + "/" + baseY + "/" + baseZ + "] granted chevron upgrade.");
                }
            }

            // Set sandstone base so Stargate doesn't appear to float.
            ItemStack sandStoneSlab = new ItemStack(Blocks.STONE_SLAB, 1, BlockStoneSlab.EnumType.SAND.getMetadata());
            te.getInventory().setInventorySlotContents(0, sandStoneSlab.copy());
            te.getInventory().setInventorySlotContents(1, sandStoneSlab.copy());
            te.getInventory().setInventorySlotContents(2, sandStoneSlab.copy());
            te.getInventory().setInventorySlotContents(3, sandStoneSlab.copy());
            te.getInventory().setInventorySlotContents(4, sandStoneSlab.copy());
        }

        // DHD
        setBlockState(world, dhd, 5, 1, 7, clip);

        // ZPM Chest Placement
        if (rand.nextInt(100) <= FeatureGeneration.zpmChestChance) {
            if (SGCraft.zpm == null) {
                return true; // ZPM Item not found thus cant continue.
            }

            int chestX = box.minX + 8, chestY = box.minY + 1, chestZ = box.minZ + 2;
            BlockPos chestPos = new BlockPos(chestX, chestY, chestZ);

            if (world.getBlockState(chestPos).getBlock() != Blocks.CHEST) {

                setBlockState(world, chest, 8, 1, 2, clip);  // Expects offset location.

                TileEntityChest chestTE = (TileEntityChest) world.getTileEntity(chestPos);

                if (chestTE != null) {
                    if (FeatureGeneration.debugStructures) {
                        System.out.println("Generating ZPM Chest at: " + chestPos);
                    }

                    ItemStack zpm = new ItemStack(SGCraft.zpm, 1);

                    if (zpm != null) {
                        NBTTagCompound tag = zpm.getTagCompound();
                        if (tag == null) {
                            tag = new NBTTagCompound();
                        }

                        zpm.setTagCompound(tag);
                        tag.setDouble(ZPMItem.ENERGY, Integer.MAX_VALUE);
                        tag.setBoolean(ZPMItem.LOADED, false);
                    }
                    chestTE.getSingleChestHandler().insertItem(0, zpm, false);
                }
            }
        }

        return true;
    }
}
