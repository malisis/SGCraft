package gcewing.sg.features.pdd.client.gui;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glTexCoord2d;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glVertex3d;

import gcewing.sg.SGCraft;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
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
    private UIButton addAddressButton, editAddressButton, deleteAddressButton, buttonReset, closeButton, buttonDial, buttonDisconnect;
    private UILabel localGateAddressLabel, gateStatusLabel, availableAddressesLabel;
    private BlockPos location;
    private World world;
    private EntityPlayer player;
    private boolean delayedUpdate = true;
    private int digit = 0;
    private boolean dialling = false;
    private String enteredAddress = "";
    private GState gstate = new GState();
    private String testAddress = "ZFDDUR8";
    private int xStart = 0;
    private SGBaseTE localGate = null;
    private boolean last = false;
    public boolean firstOpen = true;
    public boolean error = false;
    private boolean showError = false;


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
        final TileEntity localGateTE = GateUtil.locateLocalGate(this.world, this.location, 6, true);
        if (localGateTE instanceof SGBaseTE) {
            localGate = (SGBaseTE) localGateTE;
        }

        // Master Panel
        this.form = new BasicForm(this, 300, 225, "Personal Dialer Device");
        this.form.setMovable(false);

        // ****************************************************************************************************************************
        // Address List Area
        // ****************************************************************************************************************************
        this.addressContainer = new BasicContainer(this, 295, 185);
        this.addressContainer.setPosition(0, 0, Anchor.LEFT | Anchor.MIDDLE);
        this.addressContainer.setBorder(FontColors.WHITE, 1, 185);
        this.addressContainer.setPadding(0, 3);
        this.addressContainer.setBackgroundAlpha(0);

        availableAddressesLabel = new UILabel(this, TextFormatting.WHITE + "Available Addresses:");
        availableAddressesLabel.setPosition(5, 1, Anchor.LEFT | Anchor.TOP);

        localGateAddressLabel = new UILabel(this, "gateAddress");
        localGateAddressLabel.setFontOptions(FontOptions.builder().from(FontColors.BLUE_FO).shadow(true).scale(1.0F).build());
        localGateAddressLabel.setPosition(-5, 1, Anchor.RIGHT | Anchor.TOP);

        gateStatusLabel = new UILabel(this, "... Dialling ...");
        gateStatusLabel.setFontOptions(FontOptions.builder().from(FontColors.BLUE_FO).shadow(true).scale(1.8F).build());
        gateStatusLabel.setPosition(-5, 50, Anchor.CENTER | Anchor.TOP);
        gateStatusLabel.setVisible(true);

        this.addressList = new BasicList<>(this, UIComponent.INHERITED, this.addressContainer.getHeight() - 14);
        this.addressList.setPosition(0, BasicScreen.getPaddedY(availableAddressesLabel, 2));
        this.addressList.setItemComponentFactory(AddressItemComponent::new);
        this.addressList.setItemComponentSpacing(1);
        this.addressList.setPadding(2);
        this.addressList.setBorder(FontColors.WHITE, 1, 185);
        this.addressList.setBorders(FontColors.WHITE, 185, 0, 1, 0, 0);

        this.addressContainer.add(availableAddressesLabel, localGateAddressLabel, gateStatusLabel, this.addressList);

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
                if (this.addressList.getSize() > 0 && !this.addressList.getSelectedItem().getAddress().isEmpty()) {
                    this.enteredAddress = "";
                    this.dialling = true;
                    this.gateStatusLabel.setText("... Dialling ...");
                    //dialSelectedAddress(); // Immediate Dial all chevrons
                    this.digit = 0;
                }
            })
            .build("button.dial");

        buttonReset = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.CENTER)
                .text("Reset")
                .visible(false)
                .onClick(() -> {
                    resetGui();
                })
                .build("button.reset");

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

        this.form.add(this.addressContainer, addAddressButton, editAddressButton, deleteAddressButton, buttonDial, buttonReset, buttonDisconnect, buttonClose);
        addToScreen(this.form);
        this.readAddresses(player);
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);
        if (unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
            unlockMouse = false; // Only unlock once per session.
        }

        this.refresh();

        if (this.lastUpdate == 50 || this.lastUpdate == 100) {
            if (delayedUpdate) {
                if (this.addressList.getSize() == 0) {
                    System.out.println("List size = 0");
                    this.readAddresses(player);
                    if (this.addressList.getSize() > 0) {
                        this.addressList.setSelectedItem(this.addressList.getItem(0));
                        this.delayedUpdate = false; // Leave this in the loop in case it takes longer for some users, it will force it to keep auto-refreshing until it gets something.
                    }
                }
            }
        }

        if (this.lastUpdate == 100) {
            this.dialAddress();
        }

        if (++this.lastUpdate > 100) {
            this.lastUpdate = 0;
        }
    }

    private void refresh() {
        if (localGate != null) {
            this.localGateAddressLabel.setText(SGAddressing.formatAddress(((SGBaseTE) localGate).homeAddress, "-", "-"));
            if (localGate != null) {
                if ((this.dialling || this.last || localGate.state == SGState.SyncAwait || localGate.state == SGState.Transient)) {
                    this.drawAddressSymbols(xStart, this.gateStatusLabel.screenY() + 20, this.enteredAddress);
                    this.buttonDial.setVisible(false);
                    this.addressList.setVisible(false);
                    this.availableAddressesLabel.setVisible(false);
                    this.localGateAddressLabel.setVisible(false);
                    if (localGate.state == SGState.SyncAwait || localGate.state == SGState.Transient) {
                        this.gateStatusLabel.setText("... Establishing ...");
                    }
                } else {
                    if (localGate.state == SGState.Idle && !localGate.errorState && !dialling) {
                        this.addressList.setVisible(true);
                        this.buttonDial.setVisible(true);
                        this.localGateAddressLabel.setVisible(true);
                        this.availableAddressesLabel.setVisible(true);
                    }
                    if (localGate.state == SGState.Disconnecting) {
                        this.addressList.setVisible(false);
                        this.gateStatusLabel.setVisible(true);
                        this.gateStatusLabel.setText(" ... Disconnecting ...");
                        this.drawAddressSymbols(xStart, this.gateStatusLabel.screenY() + 20, this.enteredAddress);
                    }
                    if (localGate.state == SGState.Connected) {
                        if (this.enteredAddress.isEmpty()) {
                            this.enteredAddress = localGate.dialledAddress;
                            xStart = (this.gateStatusLabel.screenX() - 30) + (enteredAddress.length() * 12);
                        }
                        this.addressList.setVisible(false);
                        this.gateStatusLabel.setVisible(true);
                        this.buttonDisconnect.setVisible(true);
                        this.gateStatusLabel.setText(" ... Connected ...");
                        this.drawAddressSymbols(xStart, this.gateStatusLabel.screenY() + 20, this.enteredAddress);
                    }
                }

                if (localGate.errorState) {
                    this.addressList.setVisible(false);
                    this.gateStatusLabel.setVisible(true);
                    this.buttonDisconnect.setVisible(true);
                    this.gateStatusLabel.setText(" ... Error ...");
                    dialling = false;
                    this.buttonDial.setVisible(false);
                    this.buttonReset.setVisible(true);
                }


                if (localGate.isConnected() && localGate.state == SGState.Connected) {
                    this.last = false;
                    this.buttonDial.setVisible(false);
                    this.buttonDisconnect.setVisible(true);
                    this.buttonDisconnect.setEnabled(true);
                } else {
                    this.buttonDisconnect.setVisible(false);
                }
                this.addAddressButton.setVisible(this.addressList.isVisible());
                this.deleteAddressButton.setVisible(this.addressList.isVisible());
                this.editAddressButton.setVisible(this.addressList.isVisible());
                this.availableAddressesLabel.setVisible(this.addressList.isVisible());
                this.localGateAddressLabel.setVisible(this.addressList.isVisible());
            }
        }
    }

    private void resetGui() {
        System.out.println("Reset GUI");
        PddNetworkHandler.sendPddInputToServer(localGate, 3, "");
        dialling = false;
        firstOpen = true;
        showError = false;
        last = false;
        this.addressList.setVisible(true);
        this.buttonReset.setVisible(false);
        this.buttonDial.setVisible(true);
        localGate.dialledAddress = "";
        localGate.errorState = false; // Force this upon the local gate because the server/client is out of sync at this point.
        localGate.resetStargate();
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

// ******************************************************************************************************************************

    final static String symbolTextureFile = "symbols48.png";
    final static int symbolsPerRowInTexture = 10;
    final static int symbolWidthInTexture = 48;
    final static int symbolHeightInTexture = 48;
    final static int symbolTextureWidth = 512;
    final static int symbolTextureHeight = 256;
    final static int frameWidth = 236;
    final static int frameHeight = 44;
    final static int borderSize = 6;
    final static int cellSize = 24;

    private void readAddresses(EntityPlayer player) {
        final ItemStack stack = player.getHeldItemMainhand();
        NBTTagCompound compound = stack.getTagCompound();

        if (compound != null) {
            this.addressList.clearItems();
            this.addressList.addItems(AddressData.getAddresses(compound));
        }
    }

    public void delayedUpdate() { // public, called from pddNetworkHandler.
        this.delayedUpdate = true;
        this.lastUpdate = 0;
    }

    private void dialSelectedAddress() {
        final TileEntity localGate = GateUtil.locateLocalGate(this.world, this.location, 6, true);
        if (localGate != null) {
            if (!(localGate instanceof SGBaseTE)) {
                return;
            }
            if (this.addressList.getSelectedItem() != null) {
                String address = this.addressList.getSelectedItem().getAddress().toUpperCase().replaceAll("-", "");
                PddNetworkHandler.sendPddInputToServer((SGBaseTE) localGate, 1, address); // Immediate dial all chevrons.
                this.close();
            }
        }
    }

    private void dialAddress() {
        if (dialling && !localGate.errorState) {
            firstOpen = false;
            String address = this.addressList.getSelectedItem().getAddress().toUpperCase().replaceAll("-", "");
            if (address.length() > localGate.getNumChevrons()) {
                player.sendMessage(new TextComponentString("Gate does not have 9 chevrons."));
                dialling = false;
                return;
            }
            this.addressList.setVisible(false);
            final TileEntity localGateTE = GateUtil.locateLocalGate(this.world, this.location, 6, true);
            if (localGateTE instanceof SGBaseTE) {
                SGBaseTE localGate = (SGBaseTE) localGateTE;
                this.last = false;
                PddNetworkHandler.sendEnterSymbolToServer(localGate, address, this.digit);
                char currentSymbol = address.charAt(digit);
                enteredAddress += currentSymbol;
                if (address.length() == 7) {
                    xStart = (this.gateStatusLabel.screenX() - 30) + (enteredAddress.length() * 12);
                } else {
                    xStart = (this.gateStatusLabel.screenX() - 50) + (enteredAddress.length() * 12);
                }
                this.digit += 1;
            }
            if (this.digit >= address.length()) {
                this.dialling = false;
                this.last = true;
                this.digit = 0;
            }
        }
    }

    public void stopDialing() {
        this.dialling = false;
    }

    private void drawAddressSymbols(int x, int y, String address) {
        int x0 = x - address.length() * cellSize / 2;
        int y0 = y + frameHeight / 2 - cellSize / 2;
        bindSGTexture(symbolTextureFile,
                symbolTextureWidth * cellSize / symbolWidthInTexture,
                symbolTextureHeight * cellSize / symbolHeightInTexture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        int n = address.length();
        for (int i = 0; i < n; i++) {
            int s = SGBaseTE.charToSymbol(address.charAt(i));
            int row = s / symbolsPerRowInTexture;
            int col = s % symbolsPerRowInTexture;

            drawTexturedRect(x0 + i * cellSize, y0, cellSize, cellSize,col * cellSize, row * cellSize);
        }
    }

    private void drawTexturedRect(double x, double y, double w, double h, double u, double v) {
        drawTexturedRect(x, y, w, h, u, v, w, h);
    }

    private void drawTexturedRect(double x, double y, double w, double h, double u, double v, double us, double vs) {
        drawTexturedRectUV(x, y, w, h, u * gstate.uscale, v * gstate.vscale, us * gstate.uscale, vs * gstate.vscale);
    }

    private void drawTexturedRectUV(double x, double y, double w, double h, double u, double v, double us, double vs) {
        glBegin(GL_QUADS);
        glColor3f(gstate.red, gstate.green, gstate.blue);
        glTexCoord2d(u, v+vs);
        glVertex3d(x, y+h, 1000); //zLevel
        glTexCoord2d(u+us, v+vs);
        glVertex3d(x+w, y+h, 1000); //zLevel
        glTexCoord2d(u+us, v);
        glVertex3d(x+w, y, 1000); //zLevel
        glTexCoord2d(u, v);
        glVertex3d(x, y, 1000);//zLevel
        glEnd();
    }

    private void bindSGTexture(String name, int usize, int vsize) {
        bindTexture(SGCraft.mod.resourceLocation("textures/gui/" + name), usize, vsize);
    }

    private void bindTexture(ResourceLocation rsrc, int usize, int vsize) {
        gstate.texture = rsrc;
        mc.getTextureManager().bindTexture(rsrc);
        gstate.uscale = 1.0 / usize;
        gstate.vscale = 1.0 / vsize;
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
