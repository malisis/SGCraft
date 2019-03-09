package gcewing.sg.features.pdd.client.gui;

import com.google.common.eventbus.Subscribe;
import gcewing.sg.util.IrisState;
import gcewing.sg.tileentity.SGBaseTE;
import gcewing.sg.network.SGChannel;
import gcewing.sg.SGCraft;
import gcewing.sg.util.SGAddressing;
import gcewing.sg.util.SGState;
import gcewing.sg.util.GateUtil;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.container.BasicList;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UISeparator;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class PddScreen extends BasicScreen {
    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private boolean isAdmin;
    private BasicForm form, addressArea, buttonsArea;
    private UIButton addAddressButton, editAddressButton, deleteAddressButton, dialAddressButton, closeButton;
    private UILabel localGateAddressLabel, remoteGateAddressLabel;
    private BlockPos location;
    private World world;
    private EntityPlayer player;

    //private BasicList<AddressItem> addressList;  // ?


    public PddScreen(EntityPlayer player, World worldIn,  boolean isAdmin) {
        this.player = player;
        this.isAdmin = isAdmin;
        this.world = worldIn;
        this.location = new BlockPos(player.posX, player.posY, player.posZ);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void construct() {
        this.guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        // Master Panel
        this.form = new BasicForm(this, 300, 225, "");
        this.form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        this.form.setMovable(true);
        this.form.setClosable(true);
        this.form.setBorder(FontColors.WHITE, 1, 185);
        this.form.setBackgroundAlpha(215);
        this.form.setBottomPadding(3);
        this.form.setRightPadding(3);
        this.form.setTopPadding(20);
        this.form.setLeftPadding(3);

        final UILabel titleLabel = new UILabel(this, "Personal Dialer Device");
        titleLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        titleLabel.setPosition(0, -15, Anchor.CENTER | Anchor.TOP);
        // ****************************************************************************************************************************
        // Address List Area
        // ****************************************************************************************************************************
        addressArea = new BasicForm(this, 295, 185, "");
        addressArea.setPosition(0, 0, Anchor.LEFT | Anchor.MIDDLE);
        addressArea.setMovable(false);
        addressArea.setClosable(false);
        addressArea.setBorder(FontColors.WHITE, 1, 185);
        addressArea.setBackgroundAlpha(215);
        addressArea.setBottomPadding(3);
        addressArea.setRightPadding(3);
        addressArea.setTopPadding(3);
        addressArea.setLeftPadding(3);

        final UILabel availableAddressesLabel = new UILabel(this, "Available Addresses");
        availableAddressesLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        availableAddressesLabel.setPosition(0, 0, Anchor.CENTER | Anchor.TOP);

        final UISeparator valuesSeparator = new UISeparator(this);
        valuesSeparator.setSize(this.addressArea.getWidth() - 15, 1);
        valuesSeparator.setPosition(0, 10, Anchor.TOP | Anchor.CENTER);

        // Address List Here

        addressArea.add(availableAddressesLabel, valuesSeparator);

        // ****************************************************************************************************************************

        // Test Feature button
        final UIButton buttonTest = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.BOTTOM | Anchor.LEFT)
            .text("TEST")
            .listener(this)
            .build("button.test");

        // Close button
        final UIButton buttonClose = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.BOTTOM | Anchor.RIGHT)
            .text("Close")
            .listener(this)
            .build("button.close");

        this.form.add(titleLabel, addressArea, buttonTest, buttonClose);
        addToScreen(this.form);
        this.refresh();
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        TileEntity localGate = GateUtil.locateLocalGate(this.world, this.location, 6, true);

        if (localGate == null) {
            return;
        }

        if (!(localGate instanceof SGBaseTE)) {
            return;
        }

        switch (event.getComponent().getName().toLowerCase()) {

            case "button.test":
                SGChannel.sendGdoInputToServer((SGBaseTE)localGate, 7);
                break;

            case "button.close":
                this.close();
                break;
        }
    }

    private void refresh() {
        TileEntity localGate = GateUtil.locateLocalGate(this.world, this.location, 6, false);
        if (localGate != null) {
            if (localGate instanceof SGBaseTE) {
               // Do things, all the things.
            }
        }
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);
        if (unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
            unlockMouse = false; // Only unlock once per session.
        }

        if (this.lastUpdate == 100) {
            TileEntity localGate = GateUtil.locateLocalGate(this.world, this.location, 6, true);
            if (localGate != null) {
                //SGChannel.sendGuiRequestToServer((SGBaseTE) localGate, player, 2);
            }
            this.refresh();
        }

        if (++this.lastUpdate > 120) {
            this.lastUpdate = 0;
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
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
