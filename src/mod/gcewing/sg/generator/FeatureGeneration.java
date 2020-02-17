//------------------------------------------------------------------------------------------------
//
//   SG Craft - Map feature generation
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.generator;

import java.util.*;
import java.lang.reflect.Field;

import gcewing.sg.BaseConfiguration;
import gcewing.sg.BaseReflectionUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.structure.*;

import net.minecraftforge.fml.common.registry.VillagerRegistry;

import net.minecraftforge.event.terraingen.*;

public class FeatureGeneration {

    // Generic
    public static boolean debugStructures = false;
    public static int genericTaintedZpm = 10;

    // Village
    public static boolean villageAddon = true;
    public static int villageAddonChance = 25;
    public static int villageChevronUpgradeChance = 25;
    public static boolean villageSpawnTokra = true;
    public static int villageZpmChestChance = 15;

    // Desert Temple
    public static boolean pyramidAddon = true;
    public static int pyramidAddonChance = 25;
    public static int pyramidChevronUpgradeChance = 25;
    public static boolean pyramidSpawnTokra = true;
    public static int pyramidZpmChestChance = 15;

    // Igloo
    public static boolean iglooAddon = true;
    public static int iglooAddonChance = 25;
    public static int iglooChevronUpgradeChance = 25;
    public static boolean iglooSpawnTokra = true;
    public static int iglooZpmChestChance = 15;

    // Jungle Temple
    public static boolean jungleAddon = true;
    public static int jungleAddonChance = 25;
    public static int jungleChevronUpgradeChance = 25;
    public static boolean jungleSpawnTokra = true;
    public static int jungleZpmChestChance = 15;

    // Swamp Hut
    public static boolean swampAddon = true;
    public static int swampAddonChance = 25;
    public static int swampChevronUpgradeChance = 25;
    public static boolean swampSpawnTokra = true;
    public static int swampZpmChestChance = 15;
    public static int swampAddonRuinedChance = 50;

    private static final VillagerRegistry VILLAGER_REGISTRY = VillagerRegistry.instance();


    static Field structureMap = BaseReflectionUtils.getFieldDef(MapGenStructure.class,
        "structureMap", "field_75053_d");
    
    public static void initVillageStargate() {
        VILLAGER_REGISTRY.registerVillageCreationHandler(new FeatureVillageStargate.VillageManager());
        MapGenStructureIO.registerStructureComponent(FeatureVillageStargate.class, "sgcraft:FeatureVillageStargate");
                                            }

    public static void configure(BaseConfiguration config) {
        // Generic
        debugStructures = config.getBoolean("debug", "debugStructures", debugStructures);
        genericTaintedZpm = config.getInteger("generaton", "generic_tainted_zpm", genericTaintedZpm);

        // Villages
        villageAddon = config.getBoolean("generaton", "village_addon", villageAddon);
        villageAddonChance = config.getInteger("generaton", "village_addon_chance", villageAddonChance);
        villageZpmChestChance = config.getInteger("generaton", "village_zpm_chest_chance", villageZpmChestChance);
        villageChevronUpgradeChance = config.getInteger("generaton", "village_chevron_upgrade_chance", villageChevronUpgradeChance);
        villageSpawnTokra = config.getBoolean("generaton", "village_spawn_tokra", villageSpawnTokra);

        // Pyramids
        pyramidAddon = config.getBoolean("generaton", "pyramid_addon", pyramidAddon);
        pyramidAddonChance = config.getInteger("generaton", "pyramid_addon_chance", pyramidAddonChance);
        pyramidZpmChestChance = config.getInteger("generaton", "pyramid_zpm_chest_chance", pyramidZpmChestChance);
        pyramidChevronUpgradeChance = config.getInteger("generaton", "pyramid_chevron_upgrade_chance", pyramidChevronUpgradeChance);
        pyramidSpawnTokra = config.getBoolean("generaton", "pyramid_spawn_tokra", pyramidSpawnTokra);

        // Igloo
        iglooAddon = config.getBoolean("generaton", "igloo_addon", iglooAddon);
        iglooAddonChance = config.getInteger("generaton", "igloo_addon_chance", iglooAddonChance);
        iglooZpmChestChance = config.getInteger("generaton", "igloo_zpm_chest_chance", iglooZpmChestChance);
        iglooChevronUpgradeChance = config.getInteger("generaton", "igloo_chevron_upgrade_chance", iglooChevronUpgradeChance);
        iglooSpawnTokra = config.getBoolean("generaton", "igloo_spawn_tokra", iglooSpawnTokra);

        // Jungle
        jungleAddon = config.getBoolean("generaton", "jungle_addon", jungleAddon);
        jungleAddonChance = config.getInteger("generaton", "jungle_addon_chance", jungleAddonChance);
        jungleZpmChestChance = config.getInteger("generaton", "jungle_zpm_chest_chance", jungleZpmChestChance);
        jungleChevronUpgradeChance = config.getInteger("generaton", "jungle_chevron_upgrade_chance", jungleChevronUpgradeChance);
        jungleSpawnTokra = config.getBoolean("generaton", "jungle_spawn_tokra", jungleSpawnTokra);

        // Swamp
        swampAddon = config.getBoolean("generaton", "swamp_addon", swampAddon);
        swampAddonChance = config.getInteger("generaton", "swamp_addon_chance", swampAddonChance);
        swampZpmChestChance = config.getInteger("generaton", "swamp_zpm_chest_chance", swampZpmChestChance);
        swampChevronUpgradeChance = config.getInteger("generaton", "swamp_chevron_upgrade_chance", swampChevronUpgradeChance);
        swampSpawnTokra = config.getBoolean("generaton", "swamp_spawn_tokra", swampSpawnTokra);
    }

    public static void onInitMapGen(InitMapGenEvent e) {
        // if (debugStructures)
        //  System.out.printf("SGCraft: FeatureGeneration.onInitMapGen: %s\n", e.getType());
        switch (e.getType()) {
            case SCATTERED_FEATURE:
                MapGenBase newGen = e.getNewGen();
                if (newGen instanceof MapGenStructure) {
                    e.setNewGen(modifyScatteredFeatureGen((MapGenStructure)newGen));
                    // if (FeatureGeneration.debugStructures)
                    //   System.out.printf("SGCraft: FeatureGeneration: Installed SGStructureMap\n");
                }
                else
                    System.out.printf("SGCraft: FeatureGeneration: SCATTERED_FEATURE generator is not a MapGenStructure, cannot customise\n");
                break;
        }

    }

    static MapGenStructure modifyScatteredFeatureGen(MapGenStructure gen) {
        BaseReflectionUtils.setField(gen, structureMap, new SGStructureMap());
        return gen;
    }
}

class SGStructureMap extends Long2ObjectOpenHashMap {

    public SGStructureMap() {
        super(1024);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object put(final long key, final Object value) {
        if (value instanceof StructureStart)
            augmentStructureStart((StructureStart)value);
        return super.put(key, value);
    }
    
    void augmentStructureStart(StructureStart start) {
        List<StructureComponent> oldComponents = start.getComponents();
        List<StructureComponent> newComponents = new ArrayList<StructureComponent>();
        StructureComponent newComp = null;
        for (Object comp : oldComponents) {
            newComp = null;
            if (comp instanceof ComponentScatteredFeaturePieces.DesertPyramid && FeatureGeneration.pyramidAddon) {
                newComp = new FeatureUnderDesertPyramid((StructureComponent)comp);
            } else if (comp instanceof ComponentScatteredFeaturePieces.SwampHut && FeatureGeneration.swampAddon) {
                newComp = new FeatureSwampHut((StructureComponent)comp);
            } else if (comp instanceof ComponentScatteredFeaturePieces.Igloo && FeatureGeneration.iglooAddon) {
                newComp = new FeatureIgloo((StructureComponent)comp);
            } else if (comp instanceof ComponentScatteredFeaturePieces.JunglePyramid && FeatureGeneration.jungleAddon) {
                newComp = new FeatureJungleTemple((StructureComponent)comp);
            }

            if (newComp != null) {
                StructureBoundingBox box = ((StructureComponent)comp).getBoundingBox();
                start.getBoundingBox().expandTo(newComp.getBoundingBox());
                newComponents.add (newComp);
            }
        }
        oldComponents.addAll(newComponents);
    }
}
