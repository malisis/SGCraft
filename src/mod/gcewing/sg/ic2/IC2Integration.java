//------------------------------------------------------------------------------------------------
//
//   SG Craft - IC2 Integration Module
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.ic2;

import gcewing.sg.ic2.zpm.ZeroPointModuleBlock;
import gcewing.sg.ic2.zpm.ZeroPointModuleItem;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.init.*;
import net.minecraft.item.*;
import gcewing.sg.*;

import ic2.api.item.*; //[IC2]
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class IC2Integration extends BaseSubsystem<SGCraft, SGCraftClient> {

    public static ItemStack getIC2Item(String name) {
        return getIC2Item(name, null);
    }

    public static ItemStack getIC2Item(String name, String variant) {
        ItemStack stack = IC2Items.getItem(name, variant);
        if (stack == null)
            throw new RuntimeException(String.format("IC2 item %s.%s not found", name, variant));
        return stack;
    }
    
    @Override
    public void registerBlocks() {
        mod.ic2PowerUnit = mod.newBlock("ic2PowerUnit", IC2PowerBlock.class, IC2PowerItem.class);

        SGCraft.zeroPointModuleBlock = new ZeroPointModuleBlock(Material.ROCK).setHardness(1.5f);
        SGCraft.zeroPointModuleBlock.setRegistryName(new ResourceLocation(Info.modID, "zero_point_module"));
        SGCraft.zeroPointModuleBlock.setUnlocalizedName(Info.modID + ".zero_point_module");
        ForgeRegistries.BLOCKS.register(SGCraft.zeroPointModuleBlock);
    }

    @Override
    protected void registerItems() {
        SGCraft.zeroPointModuleItem = new ZeroPointModuleItem(SGCraft.zeroPointModuleBlock);
        SGCraft.zeroPointModuleItem.setRegistryName(new ResourceLocation(Info.modID, "zero_point_module"));
        ForgeRegistries.ITEMS.register(SGCraft.zeroPointModuleItem);
    }

    @Override
    public void registerRecipes() {
        ItemStack rubber = getIC2Item("crafting", "rubber");
        ItemStack copperPlate = getIC2Item("plate", "copper");
        ItemStack machine = getIC2Item("resource", "machine");
        ItemStack wire = getIC2Item("cable", "type:copper,insulation:0");
        ItemStack circuit = getIC2Item("crafting", "circuit");
        mod.newRecipe("ic2Capacitor",mod.ic2Capacitor, 1, "ppp", "rrr", "ppp", 'p', copperPlate, 'r', rubber);
        mod.newRecipe("ic2Powerunit", mod.ic2PowerUnit,  1, "cwc", "wMw", "cec", 'c', mod.ic2Capacitor, 'w', wire, 'M', machine, 'e', circuit);
    }
    
}
