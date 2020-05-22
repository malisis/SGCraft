package gcewing.sg.features.pdd.client.gui;

import gcewing.sg.features.pdd.Address;
import net.malisis.ego.gui.EGOGui;
import net.malisis.ego.gui.component.UIComponent;
import net.malisis.ego.gui.component.container.UIContainer;
import net.malisis.ego.gui.component.container.UIListContainer;
import net.malisis.ego.gui.component.decoration.UILabel;
import net.malisis.ego.gui.element.Padding;
import net.malisis.ego.gui.element.size.Size;
import net.malisis.ego.gui.element.size.Sizes;
import net.malisis.ego.gui.render.shape.GuiShape;

public class AddressComponent extends UIContainer {

    public AddressComponent(UIListContainer<Address> parent, Address address) {

        GuiShape bg = GuiShape.builder(this)
                .color(() -> parent.isSelected(address) ? 0x414141 : isHovered() ? 0x282828 : 0x000000)
                .border(1, 0x808080)
                .alpha(200)
                .build();

        setBackground(bg);
        setSize(Size.of(Sizes.parentWidth(this, 1.0F, 0), 28));
        setPadding(Padding.of(2));


        UILabel name = UILabel.builder()
                .parent(this)
                .text(address::getName)
                .topLeft(2, 1)
                .textColor(0xFFFFFF)
                .when(this::isHovered)
                .textColor(0xFFFFA0)
                .build();

        UILabel addressLabel = UILabel.builder()
                .parent(this)
                .text(address::getAddress)
                .x(4)
                .below(name, 2)
                .textColor(0x5555FF)
                .build();


        UIComponent locked = UIComponent.base()
                .parent(this)
                .topRight()
                .width(5)
                .fillHeight()
                .foreground((UIComponent c) -> GuiShape.builder(c).color(() -> address.isLocked() ? 0xAAAAAA : 0x55FF55).build())
                .build();
    }

    @Override
    public boolean isHovered() {
        UIComponent comp = EGOGui.getHoveredComponent();
        return comp != null && (comp == this || comp.getParent() == this);
    }
}
