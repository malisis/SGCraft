package gcewing.sg.features.pdd.client.gui;

import gcewing.sg.SGCraft;
import gcewing.sg.features.ego.SGAddressComponent;
import gcewing.sg.features.ego.SGComponent;
import gcewing.sg.features.pdd.Address;
import gcewing.sg.features.pdd.network.PddMessage;
import gcewing.sg.tileentity.SGBaseTE;
import gcewing.sg.util.GateUtil;
import net.malisis.ego.gui.EGOGui;
import net.malisis.ego.gui.component.container.UIContainer;
import net.malisis.ego.gui.component.container.UIListContainer;
import net.malisis.ego.gui.component.decoration.UILabel;
import net.malisis.ego.gui.component.interaction.UIButton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class PddScreenEGO extends EGOGui {

    private final SGBaseTE localGate;
    private final boolean showLocalAddress;
    private ItemStack itemStack;
    private UIListContainer<Address> listAddresses;
    private List<Address> addresses;
    private int maxIndex;

    public PddScreenEGO() {
        localGate = GateUtil.findGate(world(), player(), 6);
        showLocalAddress = localGate != null && localGate.displayGateAddress;
        itemStack = player().getHeldItemMainhand();
        load(itemStack);
    }

    private List<Address> load(ItemStack itemStack) {
        this.itemStack = itemStack;
        addresses = SGCraft.pdd.getAddresses(itemStack);
        maxIndex = addresses.get(addresses.size() - 1).getIndex() + 1;
        return addresses;
    }

    @Override
    public void construct() {
        UIContainer window = SGComponent.window("sgcraft.gui.pdd.label.personalDialerDevice")
                .middleCenter()
                .size(300, 225)
                .build();


        SGAddressComponent local = SGAddressComponent.builder(localGate != null ? localGate.homeAddress : "")
                .parent(window)
                .topRight()
                .obfuscated(!showLocalAddress)
                .tooltip(showLocalAddress ? "Double click to add to PDD" : "Address hidden")
                .onDoubleClick(c -> {
                    if (localGate != null && showLocalAddress) {
                        new AddressEditComponent(localGate.homeAddress, maxIndex);
                    }
                    return true;
                })
                .build();
        UILabel localLbl = UILabel.builder()
                .parent(window)
                .topAligned(2)
                .leftOf(local, 3)
                .text("Local address :")
                .textColor(0xFFFFFF)
                .build();

        listAddresses = UIListContainer.builder(addresses)
                .parent(window)
                .below(local, 3)
                .fillWidth()
                .fillHeight(18)
                .padding(2)
                .background(SGComponent::defaultBackground)
                .factory(ItemAddress::new)
                .selectable()
                .deselectable()
                .build();

        UIButton add = UIButton.builder()
                .parent(window)
                .bottomLeft()
                .textColor(TextFormatting.GREEN)
                .text("+")
                .tooltip("Add")
                .onClick(() -> new AddressEditComponent("", maxIndex))
                .build();
        UIButton delete = UIButton.builder()
                .parent(window)
                .rightOf(add, 1)
                .bottomAligned()
                .textColor(TextFormatting.RED)
                .text("-")
                .tooltip("Delete")
                .enabled(() -> listAddresses.selected() != null && !listAddresses.selected().isLocked())
                .onClick(() -> PddMessage.delete(listAddresses.selected().getAddress()))
                .build();
        UIButton edit = UIButton.builder()
                .parent(window)
                .rightOf(delete, 1)
                .bottomAligned()
                .textColor(TextFormatting.YELLOW)
                .text("?")
                .tooltip("Edit")
                .enabled(() -> listAddresses.selected() != null)
                .onClick(() -> new AddressEditComponent(listAddresses.selected()))
                .build();


        UIButton dial = UIButton.builder()
                .parent(window)
                .bottomCenter()
                .text("sgcraft.gui.button.dialSelectedAddress")
                .visible(() -> listAddresses.selected() != null)
                .enabled(() -> localGate != null && !listAddresses.selected().getAddress().equals(localGate.homeAddress))
                .onClick(null)
                .build();

        UIButton close = UIButton.builder()
                .parent(window)
                .bottomRight()
                .text("sgcraft.gui.button.close")
                .onClick(this::close)
                .build();

        addToScreen(window);
    }

    @Override
    public void update() {
        if (itemStack.equals(player().getHeldItemMainhand())) {
            return;
        }

        itemStack = player().getHeldItemMainhand();
        listAddresses.setElements(load(itemStack));
    }
}
