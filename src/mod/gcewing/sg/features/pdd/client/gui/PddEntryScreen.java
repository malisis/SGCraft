package gcewing.sg.features.pdd.client.gui;

import static gcewing.sg.tileentity.SGBaseTE.sendErrorMsg;

import gcewing.sg.features.pdd.network.PddNetworkHandler;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class PddEntryScreen extends BasicScreen {
    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private BasicForm form;
    private UIButton localIrisOpenButton, localGateCloseButton, localIrisCloseButton, remoteIrisOpenButton, remoteGateCloseButton, remoteIrisCloseButton;
    private UITextField nameTextField, addressTextField, indexTextField;
    private EntityPlayer player;
    public boolean isLocked, delete;
    public String name;
    public String address;
    public int index, unid;

    public PddEntryScreen(BasicScreen parent, EntityPlayer player, String name, String address, int index, int unid, boolean isLocked, boolean delete) {
        super(parent, true);
        this.player = player;
        this.name = name;
        this.address = address;
        this.index = index;
        this.unid = unid;
        this.isLocked = isLocked;
        this.delete = delete;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void construct() {
        this.guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        // Master Panel
        this.form = new BasicForm(this, 250, 100, "");
        this.form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        this.form.setMovable(true);
        this.form.setClosable(true);
        this.form.setBorder(FontColors.WHITE, 1, 185);
        this.form.setBackgroundAlpha(255);
        this.form.setBottomPadding(3);
        this.form.setRightPadding(3);
        this.form.setTopPadding(20);
        this.form.setLeftPadding(3);

        final UILabel titleLabel = new UILabel(this, "PDD Entry");
        titleLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        titleLabel.setPosition(0, -15, Anchor.CENTER | Anchor.TOP);
        if (this.name.equalsIgnoreCase("name here")) {
            titleLabel.setText("Add PDD Entry");
        } else {
            if (!delete) {
                titleLabel.setText("Edit PDD Entry");
            } else {
                titleLabel.setText("Delete PDD Entry");
            }
        }

        final UILabel nameLabel = new UILabel(this, "Name:");
        nameLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        nameLabel.setPosition(0, 10, Anchor.LEFT | Anchor.TOP);

        this.nameTextField = new UITextField(this, "", false);
        this.nameTextField.setText(this.name);
        this.nameTextField.setEditable(!this.isLocked);
        this.nameTextField.setSize(180, 0);
        this.nameTextField.setPosition(60, 10, Anchor.LEFT | Anchor.TOP);
        this.nameTextField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        final UILabel addressLabel = new UILabel(this, "Address:");
        addressLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        addressLabel.setPosition(0, 25, Anchor.LEFT | Anchor.TOP);

        this.addressTextField = new UITextField(this, "", false);
        this.addressTextField.setText(this.address);
        this.addressTextField.setEditable(!this.isLocked);
        this.addressTextField.setSize(180, 0);
        this.addressTextField.setPosition(60, 25, Anchor.LEFT | Anchor.TOP);
        this.addressTextField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        final UILabel indexLabel = new UILabel(this, "Index:");
        indexLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        indexLabel.setPosition(0, 40, Anchor.LEFT | Anchor.TOP);

        this.indexTextField = new UITextField(this, "", false);
        this.indexTextField.setText(String.valueOf(this.index));
        this.indexTextField.setSize(180, 0);
        this.indexTextField.setPosition(60, 40, Anchor.LEFT | Anchor.TOP);
        this.indexTextField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        // ****************************************************************************************************************************

        // Delete Feature button
        final UIButton buttonDelete = new UIButtonBuilder(this)
            .width(40)
            .position(-50, 0)
            .visible(this.delete)
            .anchor(Anchor.BOTTOM | Anchor.RIGHT)
            .text("Delete")
            .onClick(() -> {
                PddNetworkHandler.sendPddEntryUpdateToServer(this.nameTextField.getText().trim(), this.addressTextField.getText().trim(), -1, this.unid, this.isLocked);
                this.close();

            })
            .listener(this)
            .build("button.delete");

        // Save Feature button
        final UIButton buttonSave = new UIButtonBuilder(this)
            .width(40)
            .position(-50, 0)
            .visible(!this.delete)
            .anchor(Anchor.BOTTOM | Anchor.RIGHT)
            .text("Save")
            .onClick(() -> {
                if (this.addressTextField.getText().length() == 11 && this.addressTextField.getText().substring(4,5).equalsIgnoreCase("-") && this.addressTextField.getText().substring(8,9).equalsIgnoreCase("-")) {
                    PddNetworkHandler.sendPddEntryUpdateToServer(this.nameTextField.getText().trim(), this.addressTextField.getText().trim(), Integer.valueOf(this.indexTextField.getText()), this.unid, this.isLocked);
                    this.close();
                } else {
                    sendErrorMsg(player, "invalidFormat");
                }
            })
            .listener(this)
            .build("button.save");

        // Close button
        final UIButton buttonClose = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.BOTTOM | Anchor.RIGHT)
            .text("Close")
            .onClick(() -> {
                this.close();
            })
            .listener(this)
            .build("button.close");

        this.form.add(titleLabel, nameLabel, nameTextField, addressLabel, addressTextField, indexLabel, indexTextField, buttonDelete, buttonSave, buttonClose);
        addToScreen(this.form);
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);
        if (unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
            unlockMouse = false; // Only unlock once per session.
        }

        if (++this.lastUpdate > 30) {
            if (delete) {
                this.nameTextField.setEditable(false);
                this.addressTextField.setEditable(false);
                this.indexTextField.setEditable(false);
            }
            this.lastUpdate = 0;
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
        if (keyCode == Keyboard.KEY_TAB) {
            if (this.nameTextField.isFocused()) {
                this.addressTextField.setFocused(true);
                return;
            }
            if (this.addressTextField.isFocused()) {
                this.indexTextField.setFocused(true);
                return;
            }
            if (this.indexTextField.isFocused()) {
                this.nameTextField.setFocused(true);
                return;
            }
        }
        super.keyTyped(keyChar, keyCode);
        this.lastUpdate = 0; // Reset the timer when key is typed.
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        this.lastUpdate = 0; // Reset the timer when mouse is pressed.
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false; // Can't stop the game otherwise the Sponge Scheduler also stops.
    }
}
