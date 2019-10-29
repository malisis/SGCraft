//------------------------------------------------------------------------------------------------
//
//   SG Craft - Generate stargate under desert pyramid
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.generator;

import gcewing.sg.BaseOrientation;
import gcewing.sg.SGCraft;
import gcewing.sg.block.SGRingBlock;
import gcewing.sg.features.zpm.ZPMItem;
import gcewing.sg.tileentity.DHDTE;
import gcewing.sg.tileentity.SGBaseTE;
import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import java.util.Random;

public class FeatureIgloo extends StructureComponent {

    StructureComponent base;
    boolean generateStructure = false;
    boolean generateChevronUpgrade = false;
    boolean generateZpmChest = false;
    boolean taintedZpm = false;
    BlockPos centerPos = null;
    int pass = 0;

    @Override
    protected void readStructureFromNBT(NBTTagCompound compound, TemplateManager templateManager) {}

    @Override
    protected void writeStructureToNBT(NBTTagCompound compound) {}

    public FeatureIgloo() {
        //System.out.printf("SGCraft: FeatureUnderDesertPyramid instantiated with no arguments\n");
    }

    public FeatureIgloo(StructureComponent base) {
        super(0);
        this.base = base;
        Random rand = new Random();
        generateStructure = rand.nextInt(100) <= FeatureGeneration.iglooAddonChance;
        generateChevronUpgrade = rand.nextInt(100) <= FeatureGeneration.iglooChevronUpgradeChance;
        generateZpmChest = rand.nextInt(100) <= FeatureGeneration.iglooZpmChestChance;
        taintedZpm = rand.nextInt(100) <= 10;

        if (FeatureGeneration.debugStructures) {
            //System.out.println("SGCraft: Creating FeatureUnderDesertPyramid with GenerateStructure: " + generateStructure);
        }

        StructureBoundingBox baseBox = base.getBoundingBox();
        BlockPos boxCenter = new BlockPos(baseBox.minX + (baseBox.maxX - baseBox.minX + 1) / 2, baseBox.minY + (baseBox.maxY - baseBox.minY + 1) / 2, baseBox.minZ + (baseBox.maxZ - baseBox.minZ + 1) / 2);
        int cx = boxCenter.getX()-15;
        int cz = boxCenter.getZ();
        int bottom = baseBox.minY;

        boundingBox = new StructureBoundingBox(cx - 5, bottom, cz - 5, cx + 5, bottom, cz + 8);

        setCoordBaseMode(EnumFacing.SOUTH);
    }

    @Override
    public boolean addComponentParts(World world, Random rand, StructureBoundingBox clip) {
        return generateStructure && addAugmentationParts(world, rand, clip);
    }

    protected boolean addAugmentationParts(World world, Random rand, StructureBoundingBox clip) {
        if (FeatureGeneration.debugStructures) {
            //System.out.printf("SGCraft: FeatureUnderDesertPyramid.addComponentParts in %s clipped to %s\n", getBoundingBox(), clip);
        }
        if (base == null) {
            //System.out.printf("SGCraft: FeatureUnderDesertPyramid.addComponentParts: no base\n");
            return false;
        }
        StructureBoundingBox box = getBoundingBox();

        BlockPos boxCenter = new BlockPos(box.minX + (box.maxX - box.minX + 1) / 2, box.minY + (box.maxY - box.minY + 1) / 2, box.minZ + (box.maxZ - box.minZ + 1) / 2);
        BlockPos newYPos = world.getTopSolidOrLiquidBlock(boxCenter);

        clip.minY = newYPos.getY()-1;
        box.minY = newYPos.getY()-1;

        IBlockState air = Blocks.AIR.getDefaultState();
        IBlockState dhd = SGCraft.sgControllerBlock.getDefaultState().withProperty(BaseOrientation.Orient4WaysByState.FACING, EnumFacing.NORTH);
        IBlockState sgBase = SGCraft.sgBaseBlock.getDefaultState().withProperty(BaseOrientation.Orient4WaysByState.FACING, EnumFacing.NORTH);
        IBlockState[] sgRings = new IBlockState[2];
        sgRings[0] = SGCraft.sgRingBlock.getDefaultState();
        sgRings[1] = sgRings[0].withProperty(SGRingBlock.VARIANT, 1);
        IBlockState chest = Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, EnumFacing.NORTH);
        IBlockState snow = Blocks.SNOW.getDefaultState();

        fillWithBlocks(world, clip, 2, 0, 2, 10, 0, 10, snow, air, false);

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
                setBlockState(world, id, 5+i, j, 2, clip);
            }

        int baseX = box.minX + 5, baseY = box.minY, baseZ = box.minZ + 2;
        SGBaseTE te = (SGBaseTE)world.getTileEntity(new BlockPos(baseX, baseY, baseZ));
        if (FeatureGeneration.debugStructures) {
            System.out.println("Igloo Stargate built at: " + baseX + "/" + baseY + "/" + baseZ);
        }

        if (te != null) {
            // Randomly give stargates the chevron upgrade.
            if (generateChevronUpgrade) {
                te.hasChevronUpgrade = true;
                if (FeatureGeneration.debugStructures) {
                    System.out.println("Igloo Stargate at: [" + baseX + "/" + baseY + "/" + baseZ + "] granted chevron upgrade.");
                }
            }

            // Set sandstone base so Stargate doesn't appear to float.
            ItemStack snowBlock = new ItemStack(Blocks.SNOW, 1, 0);
            te.getInventory().setInventorySlotContents(0, snowBlock.copy());
            te.getInventory().setInventorySlotContents(1, snowBlock.copy());
            te.getInventory().setInventorySlotContents(2, snowBlock.copy());
            te.getInventory().setInventorySlotContents(3, snowBlock.copy());
            te.getInventory().setInventorySlotContents(4, snowBlock.copy());
            te.gateType = 2; // Pegasus Gate
            te.markChanged();
            if (te.homeAddress == null) {
                // Attempt to fix TE?
                te.setMerged(true);
            }
            if (te.homeAddress != null) {
                GeneratorAddressRegistry.addAddress(te.getWorld(), te.homeAddress);
                te.canPlayerBreakGate = SGBaseTE.cfg.getBoolean("stargate", "canPlayerBreakGate", true);
            } else {
                System.err.println("Something bad happened!!! please report to Dockter:  unable to assign home address during generation");
            }
        }

        // DHD
        setBlockState(world, dhd, 5, 1, 7, clip);
        int dhdX = box.minX + 5, dhdY = box.minY+1, dhdZ = box.minZ + 7;
        DHDTE dhdte = (DHDTE)world.getTileEntity(new BlockPos(dhdX, dhdY, dhdZ));
        if (dhdte != null) {
            ItemStack naquadahPieces = new ItemStack(SGCraft.naquadah, 3);
            dhdte.getInventory().setInventorySlotContents(0, naquadahPieces);
        }

        int chestX = box.minX + 8, chestY = box.minY + 1, chestZ = box.minZ + 2;
        BlockPos chestPos = new BlockPos(chestX, chestY, chestZ);

        // ZPM Chest Placement
        if (generateZpmChest) {
            if (SGCraft.zpm == null) {
                return true; // ZPM Item not found thus cant continue.
            }

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
                    if (taintedZpm) {
                        zpm.addEnchantment(Enchantment.getEnchantmentByID(51), 1);
                    }
                    chestTE.getSingleChestHandler().insertItem(0, zpm, false);
                }
            }
        }

        if (FeatureGeneration.iglooSpawnTokra && pass == 0) { // pass = 0 prevents more than 1 entity from spawning.
            EntityVillager entityvillager = new EntityVillager(world);
            entityvillager.setLocationAndAngles((double)chestX + 0.5D, (double)chestY + 2, (double)chestZ + 0.5D, 0.0F, 0.0F);
            entityvillager.setProfession(VillagerRegistry.getId(SGCraft.tokraProfession));
            entityvillager.finalizeMobSpawn(world.getDifficultyForLocation(new BlockPos(entityvillager)), (IEntityLivingData)null, false);
            world.spawnEntity(entityvillager);
        }

        pass++;  // Reminder: this entire method is called 4 times during world generation.

        return true;
    }
}
