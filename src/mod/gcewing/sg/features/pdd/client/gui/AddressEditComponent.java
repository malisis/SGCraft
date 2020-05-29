package gcewing.sg.features.pdd.client.gui;

import gcewing.sg.features.ego.SGAddressComponent;
import gcewing.sg.features.ego.SGComponent;
import gcewing.sg.features.pdd.Address;
import gcewing.sg.features.pdd.network.PddMessage;
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
import org.apache.commons.lang3.StringUtils;

import java.util.function.Predicate;

public class AddressEditComponent extends UIContainer {

    private final String key;
    private final UITextField address;
    private final UITextField name;
    private final UITextField index;

    private String nameError, addressError;

    public AddressEditComponent(String address, int maxIndex) {
        this(new Address(address != null ? address : "", "", maxIndex, false));
    }

    public AddressEditComponent(Address add) {
        key = add.getAddress();
        setPosition(Position.middleCenter(this));
        setSize(Size.of(280, 120));
        setPadding(Padding.of(22, 3, 3, 3));
        setBackground(SGComponent.titleBackground(this, StringUtils.isEmpty(key) ? "sgcraft.gui.pdd.label.editPddEntry" : "sgcraft.gui.pdd.label"
                + ".addPddEntry"));
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

        name = UITextField.builder()
                .parent(this)
                .rightAligned()
                .width(180)
                .background(this::tfBackground)
                .text(add.getName())
                .textColor(TextFormatting.WHITE)
                .editable(!add.isLocked())
                .shadow()
                .validator(tf -> !StringUtils.isEmpty(tf.getText()))
                .onEnter(this::save)
                .build();

        UILabel nameErrorLabel = UILabel.builder()
                .parent(this)
                .below(nameLabel, -1)
                .text("Name must not be empty") //TODO: lang file
                .textColor(0xFF6666)
                .scale(2 / 3F)
                .visible(name::isInvalid)
                .build();

        UILabel addressLabel = UILabel.builder()
                .parent(this)
                .below(name, 5)
                .text("{sgcraft.gui.pdd.label.address} :")
                .fontOptions(options)
                .build();


        address = UITextField.builder()
                .parent(this)
                .rightAligned()
                .below(name, 5)
                .width(180)
                .background(this::tfBackground)
                .text(add.getAddress())
                .textColor(TextFormatting.WHITE)
                .editable(!add.isLocked())
                .filter(String::toUpperCase)
                .allowedInput((tf, c) -> Address.baseChars.indexOf(c) != -1 && tf.getText().length() < 9)
                .shadow()
                .validator(tf -> tf.getText().length() == 9)
                .onEnter(this::save)
                .build();


        UILabel addressErrorLabel = UILabel.builder()
                .parent(this)
                .below(addressLabel, -1)
                .text("message.sgcraft:invalidFormat")
                .textColor(0xFF6666)
                .scale(2 / 3F)
                .visible(address::isInvalid)
                .build();


        UILabel indexLabel = UILabel.builder()
                .parent(this)
                .below(address, 5)
                .text("{sgcraft.gui.pdd.label.index} :")
                .fontOptions(options)
                .build();

        //noinspection RedundantCast (compiler finds ambiguity without Predicate<Character> cast)
        index = UITextField.builder()
                .parent(this)
                .rightAligned()
                .below(address, 5)
                .width(180)
                .background(this::tfBackground)
                .text("" + add.getIndex())
                .textColor(TextFormatting.WHITE)
                .editable(!add.isLocked())
                .allowedInput((Predicate<Character>) Character::isDigit)
                .shadow()
                .onEnter(this::save)
                .build();

        SGAddressComponent addressComponent = SGAddressComponent.builder(address::getText)
                .parent(this)
                .centered()
                .below(indexLabel, 10)
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
                .visible(!add.isLocked())
                .onClick(this::delete)
                .build();

        UIButton save = UIButton.builder()
                .parent(this)
                .rightOfCenter(2)
                .bottomAligned()
                .width(40)
                .text("sgcraft.gui.button.save")
                .onClick(this::save)
                .visible(() -> !add.isLocked() && name.isValid() && address.isValid())
                .build();

        EGOGui.current().displayModal(this);
        if (!add.isLocked()) {
            EGOGui.setFocusedComponent(name);
        }
    }

    private GuiShape tfBackground(UITextField textField) {
        return GuiShape.builder(textField)
                .color(() -> textField.isInvalid() ? 0x990000 : (textField.isFocused() ? 0x414141 : 0x000000))
                .border(1, 0xFFFFFF, 200)
                .alpha(200)
                .build();
    }

    public void save() {
        if (!name.isValid() || !address.isValid()) {
            return;
        }

        if (StringUtils.isEmpty(index.getText())) {
            index.setText("-1");
        }
        Address newAddress = new Address(address.getText(), name.getText(), index.getTextAsInt(), false);
        PddMessage.save(key, newAddress);
        EGOGui.closeModal();
    }

    public void delete() {
        PddMessage.delete(key);
        EGOGui.closeModal();
    }
}
