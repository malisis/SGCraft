package gcewing.sg.features.pdd.client.gui;

import gcewing.sg.SGCraft;
import gcewing.sg.features.ego.SGComponent;
import gcewing.sg.features.pdd.Address;
import net.malisis.ego.font.FontOptions;
import net.malisis.ego.gui.EGOGui;
import net.malisis.ego.gui.component.container.UIContainer;
import net.malisis.ego.gui.component.control.UIMoveHandle;
import net.malisis.ego.gui.component.decoration.UILabel;
import net.malisis.ego.gui.component.interaction.UIButton;
import net.malisis.ego.gui.component.interaction.UITextField;
import net.malisis.ego.gui.element.Padding;
import net.malisis.ego.gui.element.position.Position;
import net.malisis.ego.gui.element.size.Size;
import net.malisis.ego.gui.render.shape.GuiShape;
import net.minecraft.util.text.TextFormatting;

public class AddressEditComponent extends UIContainer {


    public AddressEditComponent(Address add) {

        setPosition(Position.middleCenter(this));
        setSize(Size.of(250, 100));
        setPadding(Padding.of(22, 3, 3, 3));
        setBackground(SGComponent.titleBackground(this, add != null ? "sgcraft.gui.pdd.label.editPddEntry" : "sgcraft.gui.pdd.label.addPddEntry"));
        addControlComponent(UIMoveHandle.builder().size(250, 20).build());


        FontOptions options = FontOptions.builder().color(TextFormatting.WHITE)
                .scale(1.1F)
                .shadow()
                .build();

        UILabel nameLabel = UILabel.builder()
                .parent(this)
                .text("{sgcraft.gui.pdd.label.name} :")
                .fontOptions(options)
                .build();

        UITextField name = UITextField.builder()
                .parent(this)
                .rightAligned()
                .width(180)
                .background(this::tfBackground)
                .text(add != null ? add.getName() : "")
                .textColor(TextFormatting.WHITE)
                .shadow()
                .build();


        UILabel indexLabel = UILabel.builder()
                .parent(this)
                .below(name, 2)
                .text("{sgcraft.gui.pdd.label.index} :")
                .fontOptions(options)
                .build();

        UITextField index = UITextField.builder()
                .parent(this)
                .rightAligned()
                .below(name, 2)
                .width(180)
                .background(this::tfBackground)
                .text(add != null ? "" + add.getIndex() : "")
                .textColor(TextFormatting.WHITE)
                .shadow()
                .build();

        UILabel addressLabel = UILabel.builder()
                .parent(this)
                .below(index, 2)
                .text("{sgcraft.gui.pdd.label.address} :")
                .fontOptions(options)
                .build();

        UITextField address = UITextField.builder()
                .parent(this)
                .rightAligned()
                .below(index, 2)
                .width(180)
                .height(24)
                .background(this::tfBackground)
                .text(add != null ? add.getAddress() : "")
                .textColor(TextFormatting.WHITE)
                .font(SGCraft.GLYPHS_FONT)
                .scale(2.5F)
                .build();


        UIButton close = UIButton.builder()
                .parent(this)
                .bottomRight()
                .width(40)
                .text("sgcraft.gui.button.close")
                .onClick(EGOGui::closeModal)
                .build();

        UIButton delete = UIButton.builder()
                .parent(this)
                .leftOfCenter(2)
                .bottomAligned()
                .width(40)
                .text("sgcraft.gui.button.delete")
                .onClick(this::delete)
                .build();

        UIButton save = UIButton.builder()
                .parent(this)
                .rightOfCenter(2)
                .bottomAligned()
                .width(40)
                .text("sgcraft.gui.button.save")
                .onClick(this::save)
                .build();

        EGOGui.current().displayModal(this);
    }

    private GuiShape tfBackground(UITextField textField) {
        return GuiShape.builder(textField)
                .color(() -> textField.isFocused() ? 0x414141 : 0x000000)
                .border(1, 0xFFFFFF, 200)
                .alpha(200)
                .build();
    }

    public void save() {

    }

    public void delete() {

    }
}
