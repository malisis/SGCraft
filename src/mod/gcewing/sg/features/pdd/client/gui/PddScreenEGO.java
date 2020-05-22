package gcewing.sg.features.pdd.client.gui;

import com.google.common.collect.Lists;
import gcewing.sg.SGCraft;
import gcewing.sg.features.ego.SGComponent;
import gcewing.sg.features.pdd.Address;
import gcewing.sg.tileentity.SGBaseTE;
import gcewing.sg.util.GateUtil;
import net.malisis.ego.gui.EGOGui;
import net.malisis.ego.gui.component.container.UIContainer;
import net.malisis.ego.gui.component.container.UIListContainer;
import net.malisis.ego.gui.component.decoration.UILabel;
import net.malisis.ego.gui.component.interaction.UIButton;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class PddScreenEGO extends EGOGui {

    private SGBaseTE localGate;
    private boolean showLocalAddress;
    private List<Address> addresses = Lists.newArrayList();

    public PddScreenEGO() {
        localGate = GateUtil.findGate(world(), player(), 6);
        showLocalAddress = localGate != null && localGate.displayGateAddress;
        addresses = SGCraft.pdd.getAddresses(player().getHeldItemMainhand());
    }

    @Override
    public void construct() {


        UIContainer window = SGComponent.window("sgcraft.gui.pdd.label.personalDialerDevice")
                .middleCenter()
                .size(300, 225)
                .build();

        UILabel localLbl = UILabel.builder()
                .parent(window)
                .topLeft()
                .text("{sgcraft.gui.pdd.label.availableAddresses} :")
                .textColor(0xFFFFFF)
                .visible(showLocalAddress)
                .build();

        UILabel localAddress = UILabel.builder()
                .parent(window)
                .topRight()
                .text(showLocalAddress ? localGate.homeAddress : "")
                .visible(showLocalAddress)
                .textColor(0x5555FF)
                .build();

        UIListContainer<Address> listAddresses = UIListContainer.builder(addresses)
                .parent(window)
                .below(localLbl, 3)
                .fillWidth()
                .fillHeight(18)
                .padding(2)
                .background(SGComponent::defaultBackground)
                .factory(AddressComponent::new)
                .selectable()
                .deselectable()
                .build();

        UIButton add = UIButton.builder()
                .parent(window)
                .bottomLeft()
                .textColor(TextFormatting.GREEN)
                .text("+")
                .tooltip("Add")
                .onClick(null)
                .build();
        UIButton delete = UIButton.builder()
                .parent(window)
                .rightOf(add, 1)
                .bottomAligned()
                .textColor(TextFormatting.RED)
                .text("-")
                .tooltip("Delete")
                .enabled(() -> listAddresses.selected() != null && !listAddresses.selected().isLocked())
                .onClick(null)
                .build();
        UIButton edit = UIButton.builder()
                .parent(window)
                .rightOf(delete, 1)
                .bottomAligned()
                .textColor(TextFormatting.YELLOW)
                .text("?")
                .tooltip("Edit")
                .enabled(() -> listAddresses.selected() != null && !listAddresses.selected().isLocked())
                .onClick(null)
                .build();


        UIButton dial = UIButton.builder()
                .parent(window)
                .bottomCenter()
                .text("sgcraft.gui.button.dialSelectedAddress")
                .visible(() -> listAddresses.selected() != null)
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
}
