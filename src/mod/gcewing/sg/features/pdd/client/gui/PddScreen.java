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
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.BasicContainer;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.container.BasicList;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.BasicLine;
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
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class PddScreen extends BasicScreen {
    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private boolean isAdmin;
    private BasicForm form;
    private BasicContainer<?> addressContainer;
    private UIButton addAddressButton, editAddressButton, deleteAddressButton, dialAddressButton, closeButton;
    private UILabel localGateAddressLabel, remoteGateAddressLabel;
    private BlockPos location;
    private World world;
    private EntityPlayer player;

    private BasicList<AddressData> addressList;

    public PddScreen(EntityPlayer player, World worldIn, boolean isAdmin) {
        this.player = player;
        this.isAdmin = isAdmin;
        this.world = worldIn;
        this.location = new BlockPos(player.posX, player.posY, player.posZ);
    }

    @Override
    public void construct() {
        this.guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        // Master Panel
        this.form = new BasicForm(this, 300, 225, "Personal Dialer Device");

        // ****************************************************************************************************************************
        // Address List Area
        // ****************************************************************************************************************************
        this.addressContainer = new BasicContainer(this, 295, 185);
        this.addressContainer.setPosition(0, 0, Anchor.LEFT | Anchor.MIDDLE);
        this.addressContainer.setBorder(FontColors.WHITE, 1, 185);
        this.addressContainer.setPadding(0, 3);
        this.addressContainer.setBackgroundAlpha(0);

        final UILabel availableAddressesLabel = new UILabel(this, TextFormatting.WHITE + "Available Addresses");
        availableAddressesLabel.setPosition(0, 0, Anchor.CENTER | Anchor.TOP);

        this.addressList = new BasicList<>(this, UIComponent.INHERITED, this.addressContainer.getHeight() - 14);
        this.addressList.setPosition(0, BasicScreen.getPaddedY(availableAddressesLabel, 2));
        this.addressList.setItemComponentFactory(AddressItemComponent::new);
        this.addressList.setItemComponentSpacing(1);
        this.addressList.setPadding(2);
        this.addressList.setBorder(FontColors.WHITE, 1, 185);
        this.addressList.setBorders(FontColors.WHITE, 185, 0, 1, 0, 0);

        this.addressList.addItem(new AddressData("Banana1", "12345-12345-12345-12345", false));
        this.addressList.addItem(new AddressData("Banana2", "12345-12345-12345-12345", false));
        this.addressList.addItem(new AddressData("Banana3", "12345-12345-12345-12345", false));
        this.addressList.addItem(new AddressData("Banana4", "12345-12345-12345-12345", false));
        this.addressList.addItem(new AddressData("Banana5", "12345-12345-12345-12345", false));
        this.addressList.addItem(new AddressData("Banana6", "12345-12345-12345-12345", false));
        this.addressList.addItem(new AddressData("Banana7", "12345-12345-12345-12345", false));

        this.addressContainer.add(availableAddressesLabel, this.addressList);

        // ****************************************************************************************************************************

        // Test Feature button
        final UIButton buttonTest = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.BOTTOM | Anchor.LEFT)
            .text("TEST")
            .onClick(() -> {
              final TileEntity localGate = GateUtil.locateLocalGate(this.world, this.location, 6, true);
              if (!(localGate instanceof SGBaseTE)) {
                return;
              }
              SGChannel.sendGdoInputToServer((SGBaseTE) localGate, 7);
            })
            .build("button.test");

        // Close button
        final UIButton buttonClose = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.BOTTOM | Anchor.RIGHT)
            .text("Close")
            .onClick(this::close)
            .build("button.close");

        this.form.add(this.addressContainer, buttonTest, buttonClose);
        addToScreen(this.form);
        this.refresh();
    }

    private void refresh() {
        final TileEntity localGateEntity = GateUtil.locateLocalGate(this.world, this.location, 6, false);
        if (localGateEntity instanceof SGBaseTE) {
            final SGBaseTE localGate = (SGBaseTE) localGateEntity;

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

    private class AddressData {
      private final String name;
      private final String address;
      private final boolean locked;

      private AddressData(final String name, final String address, final boolean locked) {
        this.name = name;
        this.address = address;
        this.locked = locked;
      }
    }

    private class AddressItemComponent extends BasicList.ItemComponent<AddressData> {

      private UILabel addressLabel, nameLabel;
      private BasicContainer lockedStatusContainer;

      public AddressItemComponent(final MalisisGui gui, final BasicList<AddressData> parent, final AddressData item) {
        super(gui, parent, item);
      }

      @Override
      protected void construct(final MalisisGui gui) {
        super.construct(gui);

        this.setHeight(28);
        this.setPadding(1);

        this.nameLabel = new UILabel(this.getGui(), TextFormatting.WHITE + this.item.name);
        this.nameLabel.setPosition(4, 3);

        this.addressLabel = new UILabel(this.getGui(), TextFormatting.WHITE + this.item.address);
        this.addressLabel.setPosition(4, BasicScreen.getPaddedY(this.nameLabel, 2));

        this.lockedStatusContainer = new BasicContainer(this.getGui(), 5, UIComponent.INHERITED);
        this.lockedStatusContainer.setPosition(0, 0, Anchor.MIDDLE | Anchor.RIGHT);

        this.update();

        this.add(this.nameLabel, this.addressLabel, this.lockedStatusContainer);
      }

      public void update() {
        this.lockedStatusContainer.setColor(this.item.locked ? FontColors.GREEN : FontColors.RED);
      }
    }
}
