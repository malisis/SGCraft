package gcewing.sg.features.pdd.client.gui;

import gcewing.sg.features.configurator.network.ConfiguratorNetworkHandler;
import gcewing.sg.features.pdd.AddressData;
import gcewing.sg.features.pdd.network.PddNetworkHandler;
import gcewing.sg.network.SGChannel;
import gcewing.sg.tileentity.SGBaseTE;
import gcewing.sg.util.GateUtil;
import gcewing.sg.util.SGAddressing;
import gcewing.sg.util.SGState;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.BasicContainer;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.container.BasicList;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
    private UIButton addAddressButton, editAddressButton, deleteAddressButton, dialAddressButton, closeButton, buttonDial, buttonDisconnect;
    private UILabel localGateAddressLabel, remoteGateAddressLabel;
    private BlockPos location;
    private World world;
    private EntityPlayer player;
    private boolean delayedUpdate = true;

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

        final UILabel availableAddressesLabel = new UILabel(this, TextFormatting.WHITE + "Available Addresses:");
        availableAddressesLabel.setPosition(5, 1, Anchor.LEFT | Anchor.TOP);

        localGateAddressLabel = new UILabel(this, "gateAddress");
        localGateAddressLabel.setFontOptions(FontOptions.builder().from(FontColors.BLUE_FO).shadow(true).scale(1.0F).build());
        localGateAddressLabel.setPosition(-5, 1, Anchor.RIGHT | Anchor.TOP);

        this.addressList = new BasicList<>(this, UIComponent.INHERITED, this.addressContainer.getHeight() - 14);
        this.addressList.setPosition(0, BasicScreen.getPaddedY(availableAddressesLabel, 2));
        this.addressList.setItemComponentFactory(AddressItemComponent::new);
        this.addressList.setItemComponentSpacing(1);
        this.addressList.setPadding(2);
        this.addressList.setBorder(FontColors.WHITE, 1, 185);
        this.addressList.setBorders(FontColors.WHITE, 185, 0, 1, 0, 0);

        this.addressContainer.add(availableAddressesLabel, localGateAddressLabel, this.addressList);

        // ****************************************************************************************************************************

        this.addAddressButton = new UIButtonBuilder(this)
            .text(TextFormatting.GREEN + "+")
            .onClick(() -> new PddEntryScreen(this, player, "Name Here", "Address Here", 10, 0, false, false).display())
            .anchor(Anchor.BOTTOM | Anchor.LEFT)
            .position(0, 0)
            //.visible(this.canAdd)
            .build("button.add");

        this.deleteAddressButton = new UIButtonBuilder(this)
            .text(TextFormatting.RED + "-")
            .onClick(() -> {
                if (addressList.getSelectedItem() != null && !addressList.getSelectedItem().isLocked()) {
                    new PddEntryScreen(this, player, addressList.getSelectedItem().getName(), addressList.getSelectedItem().getAddress(), addressList.getSelectedItem().getIndex(), addressList.getSelectedItem().getUnid(), addressList.getSelectedItem().isLocked(), true).display();
                }
            })
            .anchor(Anchor.BOTTOM | Anchor.LEFT)
            .position(BasicScreen.getPaddedX(this.addAddressButton, 2), 0)
            //.visible(this.canRemove)
            .build("button.remove");

        this.editAddressButton = new UIButtonBuilder(this)
            .text(TextFormatting.YELLOW + "?")
            .onClick(() -> new PddEntryScreen(this, player, addressList.getSelectedItem().getName(), addressList.getSelectedItem().getAddress(), addressList.getSelectedItem().getIndex(), addressList.getSelectedItem().getUnid(), addressList.getSelectedItem().isLocked(), false).display())
            .anchor(Anchor.BOTTOM | Anchor.LEFT)
            .position(BasicScreen.getPaddedX(this.deleteAddressButton, 2), 0)
            //.visible(this.canModify)
            .build("button.details");

        buttonDial = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.BOTTOM | Anchor.CENTER)
            .text("Dial Selected Address")
            .visible(false)
            .onClick(() -> {
                if (this.addressList.getSize() > 0) {
                    dialSelectedAddress();
                }
            })
            .build("button.dial");

        buttonDisconnect = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.BOTTOM | Anchor.CENTER)
            .text("Disconnect")
            .visible(false)
            .onClick(() -> {
                final TileEntity localGate = GateUtil.locateLocalGate(this.world, this.location, 6, true);
                if (localGate != null) {
                    if (!(localGate instanceof SGBaseTE)) {
                        return;
                    }
                    PddNetworkHandler.sendPddInputToServer((SGBaseTE) localGate, 2, "");
                }
            })
            .build("button.disconnect");

        final UIButton buttonClose = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.BOTTOM | Anchor.RIGHT)
            .text("Close")
            .onClick(this::close)
            .build("button.close");

        this.form.add(this.addressContainer, addAddressButton, editAddressButton, deleteAddressButton, buttonDial, buttonDisconnect, buttonClose);
        addToScreen(this.form);
        this.readAddresses(player);
        this.refresh();
    }

    public void readAddresses(EntityPlayer player) {
        final ItemStack stack = player.getHeldItemMainhand();
        NBTTagCompound compound = stack.getTagCompound();

        if (compound != null) {
            this.addressList.clearItems();
            this.addressList.addItems(AddressData.getAddresses(compound));
        }
    }

    public void delayedUpdate() {
        this.delayedUpdate = true;
        this.lastUpdate = 0;
    }

    private void refresh() {
        final TileEntity localGateEntity = GateUtil.locateLocalGate(this.world, this.location, 6, false);
        if (localGateEntity instanceof SGBaseTE) {
            final SGBaseTE localGate = (SGBaseTE) localGateEntity;
            if (localGate != null) {
                this.localGateAddressLabel.setText(SGAddressing.formatAddress(((SGBaseTE) localGate).homeAddress, "-", "-"));

               // System.out.println("State: " + localGate.state);

                if (localGate.state == SGState.Idle && !localGate.isConnected()) {
                    this.buttonDisconnect.setVisible(false);
                    this.buttonDial.setVisible(true);
                }

                if (localGate.isConnected()) {
                    this.buttonDial.setVisible(false);
                    this.buttonDisconnect.setVisible(true);
                    this.buttonDisconnect.setEnabled(localGate.isConnected());
                }

            }
        } else {
            this.buttonDisconnect.setEnabled(false);
            this.localGateAddressLabel.setVisible(false);
        }
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);
        if (unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
            unlockMouse = false; // Only unlock once per session.
        }

        if (this.lastUpdate == 50) {
            this.refresh();
            if (delayedUpdate) {
                this.readAddresses(player);
                if (this.addressList.getSize() > 0) {
                    this.addressList.setSelectedItem(this.addressList.getItem(0));
                    this.delayedUpdate = false; // Leave this in the loop in case it takes longer for some users, it will force it to keep auto-refreshing until it gets something.
                }

            }
        }

        if (++this.lastUpdate > 60) {
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

    private void dialSelectedAddress() {
        final TileEntity localGate = GateUtil.locateLocalGate(this.world, this.location, 6, true);
        if (localGate != null) {
            if (!(localGate instanceof SGBaseTE)) {
                return;
            }
            if (this.addressList.getSelectedItem() != null) {
                String address = this.addressList.getSelectedItem().getAddress().replaceAll("-", "");
                PddNetworkHandler.sendPddInputToServer((SGBaseTE) localGate, 1, address);
                this.close();
            }
        }
    }

    private class AddressItemComponent extends BasicList.ItemComponent<AddressData> {

      private UILabel addressLabel, nameLabel;
      private BasicContainer lockedStatusContainer;

      public AddressItemComponent(final MalisisGui gui, final BasicList<AddressData> parent, final AddressData item) {
        super(gui, parent, item);
          this.setOnDoubleClickConsumer(i -> dialSelectedAddress());
      }

      @Override
      protected void construct(final MalisisGui gui) {
        super.construct(gui);

        this.setHeight(28);
        this.setPadding(1);

        this.nameLabel = new UILabel(this.getGui(), TextFormatting.WHITE + this.item.getName());
        this.nameLabel.setPosition(4, 3);

        this.addressLabel = new UILabel(this.getGui(), TextFormatting.BLUE + this.item.getAddress());
        this.addressLabel.setPosition(4, BasicScreen.getPaddedY(this.nameLabel, 2));

        this.lockedStatusContainer = new BasicContainer(this.getGui(), 5, UIComponent.INHERITED);
        this.lockedStatusContainer.setPosition(0, 0, Anchor.MIDDLE | Anchor.RIGHT);

        this.update();

        this.add(this.nameLabel, this.addressLabel, this.lockedStatusContainer);
      }

      public void update() {
        this.lockedStatusContainer.setColor(this.item.isLocked() ? FontColors.GRAY : FontColors.GREEN);
      }
    }
}
