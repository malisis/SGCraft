//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate base tile entity
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.tileentity;

import static gcewing.sg.BaseBlockUtils.getWorldTileEntity;
import static gcewing.sg.BaseMod.isModLoaded;
import static gcewing.sg.BaseUtils.max;
import static gcewing.sg.BaseUtils.min;

import com.google.common.collect.Sets;
import gcewing.sg.BaseBlockUtils;
import gcewing.sg.BaseConfiguration;
import gcewing.sg.BaseTileInventory;
import gcewing.sg.BaseUtils;
import gcewing.sg.util.SGAddressing;
import gcewing.sg.SGCraft;
import gcewing.sg.util.SGLocation;
import gcewing.sg.util.SGState;
import gcewing.sg.Trans3;
import gcewing.sg.Vector3;
import gcewing.sg.block.SGBaseBlock;
import gcewing.sg.client.renderer.SGBaseTERenderer;
import gcewing.sg.entity.EntityStargateIris;
import gcewing.sg.features.cc.CCInterfaceTE;
import gcewing.sg.features.ic2.IC2PowerTE;
import gcewing.sg.features.ic2.zpm.ZpmAddon;
import gcewing.sg.features.ic2.zpm.ZpmInterfaceCartTE;
import gcewing.sg.features.oc.OCIntegration;
import gcewing.sg.features.oc.OCInterfaceTE;
import gcewing.sg.features.oc.OCWirelessEndpoint;
import gcewing.sg.features.rf.RFPowerTE;
import gcewing.sg.interfaces.IComputerInterface;
import gcewing.sg.interfaces.ISGEnergySource;
import gcewing.sg.util.IrisState;
import gcewing.sg.interfaces.LoopingSoundSource;
import gcewing.sg.util.FakeTeleporter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class SGBaseTE extends BaseTileInventory implements ITickable, LoopingSoundSource {

    static boolean debugState = false;
    public static boolean debugEnergyUse = false;
    static boolean debugConnect = false;
    static boolean debugTransientDamage = false;
    static boolean debugTeleport = false;
    static boolean debugZPM = false;
    final static DecimalFormat dFormat = new DecimalFormat("###,###,###,##0");

    public static SoundEvent
            dialFailSound,
            connectSound,
            disconnectSound,
            irisOpenSound,
            irisCloseSound,
            irisHitSound,
            dhdPressSound,
            dhdDialSound,
            chevronOutgoingSound,
            chevronIncomingSound,
            lockOutgoingSound,
            lockIncomingSound,
            gateRollSound,
            eventHorizonSound,
            teleportSound;

    public static void registerSounds(SGCraft mod) {
        dialFailSound = mod.newSound("dial_fail");
        connectSound = mod.newSound("gate_open");
        disconnectSound = mod.newSound("gate_close");
        irisOpenSound = mod.newSound("iris_open");
        irisCloseSound = mod.newSound("iris_close");
        irisHitSound = mod.newSound("iris_hit");
        dhdPressSound = mod.newSound("dhd_press");
        dhdDialSound = mod.newSound("dhd_dial");
        chevronOutgoingSound = mod.newSound("chevron_outgoing");
        chevronIncomingSound = mod.newSound("chevron_incoming");
        lockOutgoingSound = mod.newSound("lock_outgoing");
        lockIncomingSound = mod.newSound("lock_incoming");
        gateRollSound = mod.newSound("gate_roll");
        eventHorizonSound = mod.newSound("event_horizon");
        teleportSound = mod.newSound("teleport");
    }

    public final static String symbolChars = SGAddressing.symbolChars;
    public final static int numRingSymbols = SGAddressing.numSymbols;
    public final static double ringSymbolAngle = 360.0 / numRingSymbols;
    public final static double irisZPosition = 0.1;
    public final static double irisThickness = 0.2; //0.1;
    public final static DamageSource irisDamageSource = new DamageSource("sgcraft:iris");
    public final static float irisDamageAmount = 1000000;
    public static int minutesOpenPerFuelItem = 80;


    final static int interDiallingTime = 10; // ticks
    public int syncAwaitTime = 10; // ticks  // 10 - withinWorld, 30 - dimensional
    final static int transientDuration = 20; // ticks
    final static int disconnectTime = 40; // ticks

    final static double openingTransientIntensity = 1.3;
    final static double openingTransientRandomness = 0.25;
    final static double closingTransientRandomness = 0.25;
    final static double transientDamageRate = 50;

    final static int maxIrisPhase = 70; // ticks

    public static int firstCamouflageSlot = 0;
    public static int numCamouflageSlots = 5;
    public static int numInventorySlots = numCamouflageSlots;

    static float defaultChevronAngle = 40f;
    static float chevronAngles[][] = {
            //     0    1    2    <-- Base camouflage level
            { 45f, 45f, 40f }, // 7 chevrons
            { 36f, 33f, 30f }  // 9 chevrons
    };

    // Static options
    static int chunkLoadingRange = 1;
    static boolean logStargateEvents = false;
    static float soundVolume = 1F;
    static boolean variableChevronPositions = true;
    public static double energyToOpen;
    static double energyUsePerTick;
    public static boolean transparency = true;
    static Random random = new Random();
    static DamageSource transientDamage = new DamageSource("sgcraft:transient");
    public static BaseConfiguration cfg;

    // Instanced options
    public boolean isMerged;
    public SGState state = SGState.Idle;
    public double startRingAngle, ringAngle, lastRingAngle, targetRingAngle; // degrees
    public int numEngagedChevrons;
    public String dialledAddress = "";
    public boolean isLinkedToController;
    public BlockPos linkedPos = new BlockPos(0, 0, 0);
    public boolean hasChevronUpgrade;
    public boolean hasIrisUpgrade;
    public IrisState irisState = IrisState.Open;
    public int irisPhase = maxIrisPhase; // 0 = fully closed, maxIrisPhase = fully open
    public int lastIrisPhase = maxIrisPhase;
    public OCWirelessEndpoint ocWirelessEndpoint; //[OC]
    public boolean debugCCInterface = false;

    public SGLocation connectedLocation;
    public boolean isInitiator, redstoneInput, loaded;
    int timeout, maxTimeout;
    public double energyInBuffer, distanceFactor; // all energy use is multiplied by this
    public String homeAddress, addressError;
    private int updated = 0;

    IInventory inventory = new InventoryBasic("Stargate", false, numInventorySlots);

    // ZPM Implementation
    public boolean zpmPowered = false;
    public boolean destinationRequiresZPM = false;

    // Retain current irisState prior to opening
    public boolean wasIrisClosed = false;

    // Unique Gate Configurator Options
    public int gateType = 1; //1 = Milky way 2 = Pegasus
    public int gateOrientation = 1;
    public int secondsToStayOpen = 5 * 60;
    public int ticksToStayOpen = 20 * secondsToStayOpen;
    public boolean oneWayTravel = true;
    public double ringRotationSpeed = 2.0;
    public double maxEnergyBuffer = 1000;
    public double energyPerFuelItem = 96000;
    public double distanceFactorMultiplier = 1.0;
    public double interDimensionMultiplier = 4.0;
    public int gateOpeningsPerFuelItem = 24;
    public boolean reverseWormholeKills = false;
    public boolean closeFromEitherEnd = true;
    public boolean preserveInventory = false;
    public boolean acceptIncomingConnections = true;
    public boolean chevronsLockOnDial = false;
    public boolean returnToPreviousIrisState = false;
    public boolean allowOnlySpecifiedDestination = false;
    public String onlySpecifiedAddress = "";
    public int facingDirectionOfBase = 0;

    double ehGrid[][][];
    private static Set<UUID> messagesQueue = Sets.newHashSet();

    public SGBaseTE() {
        this.hasIrisUpgrade = cfg.getBoolean("stargate", "irisUpgrade", this.hasIrisUpgrade);
        this.hasChevronUpgrade = cfg.getBoolean("stargate", "chevronUpgrade", this.hasChevronUpgrade);
        this.gateType = cfg.getInteger("stargate", "gateType", this.gateType);
        this.secondsToStayOpen = cfg.getInteger("stargate", "secondsToStayOpen", this.secondsToStayOpen);
        this.oneWayTravel = cfg.getBoolean("stargate", "oneWayTravel", this.oneWayTravel);
        this.ringRotationSpeed = cfg.getDouble("stargate", "ringRotationSpeed", this.ringRotationSpeed);
        this.maxEnergyBuffer = cfg.getDouble("stargate", "maxEnergyBuffer", this.maxEnergyBuffer);
        this.energyPerFuelItem = cfg.getDouble("stargate", "energyPerFuelItem", this.energyPerFuelItem);
        this.gateOpeningsPerFuelItem = cfg.getInteger("stargate", "gateOpeningsPerFuelItem", this.gateOpeningsPerFuelItem);
        this.distanceFactorMultiplier = cfg.getDouble("stargate", "distanceFactorMultiplier", this.distanceFactorMultiplier);
        this.interDimensionMultiplier = cfg.getDouble("stargate", "interDimensionMultiplier", this.interDimensionMultiplier);
        this.reverseWormholeKills = cfg.getBoolean("stargate", "reverseWormholeKills", this.reverseWormholeKills);
        this.closeFromEitherEnd = cfg.getBoolean("stargate", "closeFromEitherEnd", this.closeFromEitherEnd);
        this.preserveInventory = cfg.getBoolean("iris", "preserveInventory", this.preserveInventory);
        this.chevronsLockOnDial = cfg.getBoolean("stargate", "chevronsLockOnDial", this.chevronsLockOnDial);
        this.returnToPreviousIrisState = cfg.getBoolean("stargate", "returnToPreviousIrisState", this.returnToPreviousIrisState);
    }

    public static void configure(BaseConfiguration cfg) {
        SGBaseTE.cfg = cfg;
        // Instanced config values
        // Note: allowing these methods to be hit creates the config in the file.
        cfg.getDouble("stargate", "energyPerFuelItem", 96000);
        cfg.getInteger("stargate", "gateOpeningsPerFuelItem", 24);
        cfg.getInteger("stargate", "secondsToStayOpen", 5 * 60);
        cfg.getBoolean("stargate", "oneWayTravel", true);
        cfg.getBoolean("stargate", "reverseWormholeKills", false);
        cfg.getBoolean("stargate", "closeFromEitherEnd", true);
        cfg.getDouble("stargate", "maxEnergyBuffer", 1000);
        cfg.getDouble("stargate", "distanceFactorMultiplier", 1.0);
        cfg.getDouble("stargate", "interDimensionMultiplier", 4.0);
        cfg.getBoolean("iris", "preserveInventory", false);

        // New instance config values
        cfg.getInteger("stargate", "gateType", 1);
        cfg.getBoolean("stargate", "chevronsLockOnDial", false);
        cfg.getBoolean("stargate", "returnToPreviousIrisState", false);
        cfg.getDouble("stargate", "ringRotationSpeed", 2.0);
        cfg.getBoolean("stargate", "irisUpgrade", false);
        cfg.getBoolean("stargate", "chevronUpgrade", false);

        // Global static config values
        minutesOpenPerFuelItem = cfg.getInteger("stargate", "minutesOpenPerFuelItem", minutesOpenPerFuelItem);
        chunkLoadingRange = cfg.getInteger("options", "chunkLoadingRange", chunkLoadingRange);
        transparency = cfg.getBoolean("stargate", "transparency", transparency);
        logStargateEvents = cfg.getBoolean("options", "logStargateEvents", logStargateEvents);
        soundVolume = (float)cfg.getDouble("stargate", "soundVolume", soundVolume);
        variableChevronPositions = cfg.getBoolean("stargate", "variableChevronPositions", variableChevronPositions);
    }

    public static SGBaseTE get(IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof SGBaseTE) {
            return (SGBaseTE)te;
        } else if (te instanceof SGRingTE) {
            return ((SGRingTE)te).getBaseTE();
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("SGBaseTE(%s,%s)", pos, world.provider.getDimension());
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        return new AxisAlignedBB(x - 2, y, z - 2, x + 3, y + 5, z + 3);
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 32768.0;
    }

    @Override
    public void onAddedToWorld() {
        if (SGBaseBlock.debugMerge) {
            System.out.print("SGBaseTE.onAddedToWorld\n");
        }
        updateChunkLoadingStatus();
    }

    @Override
    public float getSoundVolume(SoundEvent sound) {
        return soundVolume;
    }

    @Override
    public World getSoundWorld() {
        return this.world;
    }

    @Override
    public BlockPos getSoundPos() {
        return this.pos;
    }

    @Override
    public boolean isSoundActive(SoundEvent sound) {
        if (this.isInvalid() || !this.world.isBlockLoaded(this.pos) || this.world.getTileEntity(this.pos) != this) {
            return false;
        }
        if (sound == gateRollSound) {
            return state == SGState.Dialling;
        } else if (sound == irisOpenSound) {
            return irisState == IrisState.Opening;
        } else if (sound == irisCloseSound) {
            return irisState == IrisState.Closing;
        } else if (sound == eventHorizonSound) {
            return state == SGState.Connected;
        } else {
            return false;
        }
    }

    void updateChunkLoadingStatus() {
        if (state != SGState.Idle || state == SGState.attemptToDial) {
            int n = chunkLoadingRange;
            if (n >= 0) {
                SGCraft.chunkManager.setForcedChunkRange(this, -n, -n, n, n);
            }
        } else {
            SGCraft.chunkManager.clearForcedChunkRange(this);
        }
    }

    public static SGBaseTE at(IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        return te instanceof SGBaseTE ? (SGBaseTE) te : null;
    }

    public static SGBaseTE at(SGLocation loc) {
        if (loc != null) {
            World world = SGAddressing.getWorld(loc.dimension);
            if (world != null) {
                return SGBaseTE.at(world, loc.pos);
            }
        }
        return null;
    }

    public static SGBaseTE at(IBlockAccess world, NBTTagCompound nbt) {
        BlockPos pos = new BlockPos(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z"));
        return SGBaseTE.at(world, pos);
    }

    public void setMerged(boolean state) {
        if (isMerged != state) {
            isMerged = state;
            markBlockChanged();

            String address = tryToGetHomeAddress();
            if (address != null) {
                Logger log = LogManager.getLogger();
                String action = isMerged ? "ADDED" : "REMOVED";
                String name = getWorld().getWorldInfo().getWorldName();
                if (isMerged) {
                    this.homeAddress = address;
                }
                if (logStargateEvents) {
                    log.info(String.format("STARGATE %s %s %s %s", action, name, pos, address));
                }
            }
            updateIrisEntity();
        }
    }

    String tryToGetHomeAddress() {
        try {
            return getHomeAddress();
        } catch (SGAddressing.AddressingError e) {
            return null;
        }
    }

    public int dimension() {
        return this.world != null ? world.provider.getDimension() : -999;
    }

    @Override
    protected void setWorldCreate(World world) {
        this.world = world;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.isMerged = nbt.getBoolean("isMerged");
        this.state = SGState.values()[nbt.getInteger("state")];
        SGState oldState = state;
        this.ringAngle = nbt.getDouble("ringAngle");
        this.startRingAngle = nbt.getDouble("startRingAngle");
        this.targetRingAngle = nbt.getDouble("targetRingAngle");
        this.numEngagedChevrons = nbt.getInteger("numEngagedChevrons");
        this.dialledAddress = nbt.getString("dialledAddress");
        this.isLinkedToController = nbt.getBoolean("isLinkedToController");
        int x = nbt.getInteger("linkedX");
        int y = nbt.getInteger("linkedY");
        int z = nbt.getInteger("linkedZ");
        this.linkedPos = new BlockPos(x, y, z);
        if (nbt.hasKey("connectedLocation")) {
            connectedLocation = new SGLocation(nbt.getCompoundTag("connectedLocation"));
        }
        this.isInitiator = nbt.getBoolean("isInitiator");
        this.timeout = nbt.getInteger("timeout");
        this.maxTimeout = nbt.getInteger("maxTimeout");
        this.energyInBuffer = nbt.hasKey("energyInBuffer") ? nbt.getDouble("energyInBuffer") : nbt.getInteger("fuelBuffer");
        this.distanceFactor = nbt.getDouble("distanceFactor");
        this.irisState = IrisState.values()[nbt.getInteger("irisState")];
        this.irisPhase = nbt.getInteger("irisPhase");
        this.redstoneInput = nbt.getBoolean("redstoneInput");
        this.homeAddress = getStringOrNull(nbt, "address");
        this.addressError = nbt.getString("addressError");

        if (oldState != this.state && this.state == SGState.Connected && this.world.isRemote) {
            SGCraft.playSound(this, eventHorizonSound);
        }

        if (nbt.hasKey("hasIrisUpgrade") && !SGCraft.forceSGBaseTEUpdate) {
            this.hasIrisUpgrade = nbt.getBoolean("hasIrisUpgrade");
        } else {
            this.hasIrisUpgrade = cfg.getBoolean("stargate", "irisUpgrade", this.hasIrisUpgrade);
        }

        if (nbt.hasKey("hasChevronUpgrade") && !SGCraft.forceSGBaseTEUpdate) {
            this.hasChevronUpgrade = nbt.getBoolean("hasChevronUpgrade");
        } else {
            this.hasChevronUpgrade = cfg.getBoolean("stargate", "chevronUpgrade", this.hasChevronUpgrade);
        }

        if (nbt.hasKey("gateType") && !SGCraft.forceSGBaseTEUpdate) {
            this.gateType = nbt.getInteger("gateType");
        } else {
            this.gateType = cfg.getInteger("stargate", "gateType", this.gateType);
        }

        if (nbt.hasKey("secondsToStayOpen") && !SGCraft.forceSGBaseTEUpdate) {
            this.secondsToStayOpen = nbt.getInteger("secondsToStayOpen");
        } else {
            this.secondsToStayOpen = cfg.getInteger("stargate", "secondsToStayOpen", this.secondsToStayOpen);
        }

        if (nbt.hasKey("oneWayTravel") && !SGCraft.forceSGBaseTEUpdate) {
            this.oneWayTravel = nbt.getBoolean("oneWayTravel");
        } else {
            this.oneWayTravel = cfg.getBoolean("stargate", "oneWayTravel", this.oneWayTravel);
        }

        if (nbt.hasKey("ringRotationSpeed") && !SGCraft.forceSGBaseTEUpdate) {
            this.ringRotationSpeed = nbt.getDouble("ringRotationSpeed");
        } else {
            this.ringRotationSpeed = cfg.getDouble("stargate", "ringRotationSpeed", this.ringRotationSpeed);
        }

        if (nbt.hasKey("maxEnergyBuffer") && !SGCraft.forceSGBaseTEUpdate) {
            this.maxEnergyBuffer = nbt.getDouble("maxEnergyBuffer");
        } else {
            this.maxEnergyBuffer = cfg.getDouble("stargate", "maxEnergyBuffer", this.maxEnergyBuffer);
        }

        if (nbt.hasKey("energyPerFuelItem") && !SGCraft.forceSGBaseTEUpdate) {
            this.energyPerFuelItem = nbt.getDouble("energyPerFuelItem");
        } else {
            this.energyPerFuelItem = cfg.getDouble("stargate", "energyPerFuelItem", this.energyPerFuelItem);
        }

        if (nbt.hasKey("gateOpeningsPerFuelItem") && !SGCraft.forceSGBaseTEUpdate) {
            this.gateOpeningsPerFuelItem = nbt.getInteger("gateOpeningsPerFuelItem");
        } else {
            this.gateOpeningsPerFuelItem = cfg.getInteger("stargate", "gateOpeningsPerFuelItem", this.gateOpeningsPerFuelItem);
        }

        if (nbt.hasKey("distanceFactorMultiplier") && !SGCraft.forceSGBaseTEUpdate) {
            this.distanceFactorMultiplier = nbt.getDouble("distanceFactorMultiplier");
        } else {
            this.distanceFactorMultiplier = cfg.getDouble("stargate", "distanceFactorMultiplier", this.distanceFactorMultiplier);
        }

        if (nbt.hasKey("interDimensionalMultiplier") && !SGCraft.forceSGBaseTEUpdate) {
            this.interDimensionMultiplier = nbt.getDouble("interDimensionalMultiplier");
        } else {
            this.interDimensionMultiplier = cfg.getDouble("stargate", "interDimensionMultiplier", this.interDimensionMultiplier);
        }

        if (nbt.hasKey("reverseWormholeKills") && !SGCraft.forceSGBaseTEUpdate) {
            this.reverseWormholeKills = nbt.getBoolean("reverseWormholeKills");
        } else {
            reverseWormholeKills = cfg.getBoolean("stargate", "reverseWormholeKills", this.reverseWormholeKills);
        }

        if (nbt.hasKey("closeFromEitherEnd") && !SGCraft.forceSGBaseTEUpdate) {
            this.closeFromEitherEnd = nbt.getBoolean("closeFromEitherEnd");
        } else {
            this.closeFromEitherEnd = cfg.getBoolean("stargate", "closeFromEitherEnd", this.closeFromEitherEnd);
        }

        if (nbt.hasKey("preserveInventory") && !SGCraft.forceSGBaseTEUpdate) {
            this.preserveInventory = nbt.getBoolean("preserveInventory");
        } else {
            this.preserveInventory = cfg.getBoolean("iris", "preserveInventory", this.preserveInventory);
        }

        if (nbt.hasKey("acceptIncomingConnections") && !SGCraft.forceSGBaseTEUpdate) {
            this.acceptIncomingConnections = nbt.getBoolean("acceptIncomingConnections");
        } else {
            this.acceptIncomingConnections = true;
        }

        if (nbt.hasKey("chevronsLockOnDial") && !SGCraft.forceSGBaseTEUpdate) {
            this.chevronsLockOnDial = nbt.getBoolean("chevronsLockOnDial");
        } else {
            this.chevronsLockOnDial = cfg.getBoolean("stargate", "chevronsLockOnDial", this.chevronsLockOnDial);
        }

        if (nbt.hasKey("returnToPreviousIrisState") && !SGCraft.forceSGBaseTEUpdate) {
            this.returnToPreviousIrisState = nbt.getBoolean("returnToPreviousIrisState");
        } else {
            this.returnToPreviousIrisState = cfg.getBoolean("stargate", "returnToPreviousIrisState", this.returnToPreviousIrisState);
        }

        if (nbt.hasKey("allowOnlySpecifiedDestination") && !SGCraft.forceSGBaseTEUpdate) {
            this.allowOnlySpecifiedDestination = nbt.getBoolean("allowOnlySpecifiedDestination");
        } else {
            this.allowOnlySpecifiedDestination = false;
        }

        if (nbt.hasKey("onlySpecifiedAddress") && !SGCraft.forceSGBaseTEUpdate) {
            this.onlySpecifiedAddress = nbt.getString("onlySpecifiedAddress");
        } else {
            this.onlySpecifiedAddress = "";
        }

        if (nbt.hasKey("gateOrientation")) {
            this.gateOrientation = nbt.getInteger("gateOrientation");
        } else {
            this.gateOrientation = 1;
        }
        this.facingDirectionOfBase = nbt.getInteger("facingDirectionOfBase");

        // Set values after NBT load
        this.ticksToStayOpen = 20 * this.secondsToStayOpen;
        this.energyToOpen = this.energyPerFuelItem / this.gateOpeningsPerFuelItem;
        this.energyUsePerTick = this.energyPerFuelItem / (this.minutesOpenPerFuelItem * 60 * 20);
    }

    protected String getStringOrNull(NBTTagCompound nbt, String name) {
        return nbt.hasKey(name) ? nbt.getString(name) : null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("isMerged", isMerged);
        nbt.setInteger("state", state.ordinal());
        nbt.setDouble("ringAngle", ringAngle);
        nbt.setDouble("startRingAngle", startRingAngle);
        nbt.setDouble("targetRingAngle", targetRingAngle);
        nbt.setInteger("numEngagedChevrons", numEngagedChevrons);
        nbt.setString("dialledAddress", dialledAddress);
        nbt.setBoolean("isLinkedToController", isLinkedToController);
        nbt.setInteger("linkedX", linkedPos.getX());
        nbt.setInteger("linkedY", linkedPos.getY());
        nbt.setInteger("linkedZ", linkedPos.getZ());
        nbt.setBoolean("hasChevronUpgrade", hasChevronUpgrade);
        nbt.setBoolean("isInitiator", isInitiator);
        nbt.setInteger("timeout", timeout);
        nbt.setInteger("maxTimeout", maxTimeout);
        nbt.setDouble("energyInBuffer", energyInBuffer);
        nbt.setDouble("distanceFactor", distanceFactor);
        nbt.setBoolean("hasIrisUpgrade", hasIrisUpgrade);
        nbt.setInteger("irisState", irisState.ordinal());
        nbt.setInteger("irisPhase", irisPhase);
        nbt.setBoolean("redstoneInput", redstoneInput);
        if (connectedLocation != null) {
            nbt.setTag("connectedLocation", connectedLocation.toNBT());
        }
        if (homeAddress != null) {
            nbt.setString("address", homeAddress);
        }
        if (addressError != null) {
            nbt.setString("addressError", addressError);
        }

        // Configurator Options
        nbt.setInteger("gateType", gateType);
        nbt.setInteger("secondsToStayOpen", secondsToStayOpen);
        nbt.setBoolean("oneWayTravel", oneWayTravel);
        nbt.setDouble("ringRotationSpeed", ringRotationSpeed);
        nbt.setDouble("maxEnergyBuffer", maxEnergyBuffer);
        nbt.setDouble("energyPerFuelItem", energyPerFuelItem);
        nbt.setInteger("gateOpeningsPerFuelItem", gateOpeningsPerFuelItem);
        nbt.setDouble("distanceFactorMultiplier", distanceFactorMultiplier);
        nbt.setDouble("interDimensionMultiplier", interDimensionMultiplier);
        nbt.setBoolean("reverseWormholeKills", reverseWormholeKills);
        nbt.setBoolean("closeFromEitherEnd", closeFromEitherEnd);
        nbt.setBoolean("preserveInventory", preserveInventory);
        nbt.setBoolean("acceptIncomingconnections", acceptIncomingConnections);
        nbt.setBoolean("chevronsLockOnDial", chevronsLockOnDial);
        nbt.setBoolean("returnToPreviousIrisState", returnToPreviousIrisState);
        nbt.setBoolean("allowOnlySpecifiedDestination", allowOnlySpecifiedDestination);
        nbt.setString("onlySpecifiedAddress", onlySpecifiedAddress);
        nbt.setInteger("gateOrientation", gateOrientation);
        nbt.setInteger("facingDirectionOfBase", facingDirectionOfBase);
        return nbt;
    }

    public boolean isActive() {
        return state != SGState.Idle && state != SGState.Disconnecting;
    }

    static boolean isValidSymbolChar(String c) {
        return SGAddressing.isValidSymbolChar(c);
    }

    public static char symbolToChar(int i) {
        return SGAddressing.symbolToChar(i);
    }

    public static int charToSymbol(char c) {
        return SGAddressing.charToSymbol(c);
    }

    static int charToSymbol(String c) {
        return SGAddressing.charToSymbol(c);
    }

    public EnumActionResult applyChevronUpgrade(ItemStack stack, EntityPlayer player) {
        if (!getWorld().isRemote && !hasChevronUpgrade && stack.getCount() > 0) {
            //System.out.printf("SGBaseTE.applyChevronUpgrade: Installing chevron upgrade\n");
            hasChevronUpgrade = true;
            stack.shrink(1);
            markChanged();
        }
        return EnumActionResult.SUCCESS;
    }

    public EnumActionResult applyIrisUpgrade(ItemStack stack, EntityPlayer player) {
        if (!getWorld().isRemote && !hasIrisUpgrade && stack.getCount() > 0) {
            //System.out.printf("SGBaseTE.applyIrisUpgrade: Installing iris upgrade\n");
            hasIrisUpgrade = true;
            stack.shrink(1);
            markChanged();
            updateIrisEntity();
        }
        return EnumActionResult.SUCCESS;
    }

    public int getNumChevrons() {
        return hasChevronUpgrade ? 9 : 7;
    }

    public boolean chevronIsEngaged(int i) {
        return i < numEngagedChevrons;
    }

    public float angleBetweenChevrons() {
        if (variableChevronPositions) {
            int c9 = getNumChevrons() > 7 ? 1 : 0;
            int bc = baseCornerCamouflage();
            return chevronAngles[c9][bc];
        } else {
            return defaultChevronAngle;
        }
    }

    public String getHomeAddress() throws SGAddressing.AddressingError {
        return SGAddressing.addressForLocation(new SGLocation(this));
    }

    public SGBaseBlock getBlock() {
        return (SGBaseBlock)getBlockType();
    }

    public double interpolatedRingAngle(double partialTicks) {
        return isInitiator ? lastRingAngle + (ringAngle - lastRingAngle) * partialTicks : 0;
    }

    @Override
    public void update() {
        if (world.isRemote) {
            clientUpdate();
        } else {
            serverUpdate();
            checkForEntitiesInPortal();
        }
        irisUpdate();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (!world.isRemote && ocWirelessEndpoint != null) {  //[OC]
            ocWirelessEndpoint.remove();
        }
    }

    String side() {
        return world.isRemote ? "Client" : "Server";
    }

    void enterState(SGState newState, int newTimeout) {
        if (debugState) {
            System.out.printf("SGBaseTE: at %s in dimension %s entering state %s with timeout %s\n", pos, world.provider.getDimension(), newState, newTimeout);
        }
        SGState oldState = state;
        state = newState;
        startRingAngle = ringAngle;
        maxTimeout = newTimeout;
        timeout = newTimeout;
        markChanged();
        if ((oldState == SGState.Idle) != (newState == SGState.Idle)) {
            updateChunkLoadingStatus();
            world.notifyNeighborsOfStateChange(pos, getBlockType(), true);
            //Update: may not need observer update here.
        }
        String oldDesc = sgStateDescription(oldState);
        String newDesc = sgStateDescription(newState);
        if (!oldDesc.equals(newDesc))
            postEvent("sgStargateStateChange", newDesc, oldDesc);
    }

    public boolean isConnected() {
        return state == SGState.SyncAwait
                || state == SGState.Transient
                || state == SGState.Connected
                || state == SGState.Disconnecting;
    }

    public DHDTE getLinkedControllerTE() {
        //System.out.printf("SGBaseTE.getLinkedControllerTE: isLinkedToController = %s, linkedPos = %s\n",
        //    isLinkedToController, linkedPos);
        if (isLinkedToController) {
            TileEntity cte = world.getTileEntity(linkedPos);
            if (cte instanceof DHDTE)
                return (DHDTE)cte;
        }
        return null;
    }

    public void checkForLink() {
        int rangeXY = max(DHDTE.linkRangeX, DHDTE.linkRangeY);
        int rangeZ = DHDTE.linkRangeZ;
        if (SGBaseBlock.debugMerge) {
            System.out.printf("SGBaseTE.checkForLink: in range +/-(%d,%d,%d) of %s\n", rangeXY, rangeZ, rangeXY, pos);
        }
        for (int i = -rangeXY; i <= rangeXY; i++)
            for (int j = -rangeZ; j <= rangeZ; j++)
                for (int k = -rangeXY; k <= rangeXY; k++) {
                    TileEntity te = world.getTileEntity(pos.add(i, j, k));
                    if (te instanceof DHDTE)
                        ((DHDTE)te).checkForLink();
                }
    }

    public void unlinkFromController() {
        if (isLinkedToController) {
            DHDTE cte = getLinkedControllerTE();
            if (cte != null)
                cte.clearLinkToStargate();
            clearLinkToController();
        }
    }

    public void clearLinkToController() {
        if (SGBaseBlock.debugMerge) {
            System.out.printf("SGBaseTE: Unlinking stargate at %d from controller\n", pos);
        }
        isLinkedToController = false;
        markDirty();
    }

    //------------------------------------   Server   --------------------------------------------

    public void connectOrDisconnect(String address, EntityPlayer player) {
        if (debugConnect) {
            System.out.printf("SGBaseTE: %s: connectOrDisconnect('%s') in state %s by %s\n", side(), address, state, player);
        }
        if (address.length() > 0) {
            DHDTE te = getLinkedControllerTE();
            if (te != null) {
                if (connect(address, player) != null) {
                    numEngagedChevrons = 0;
                    markChanged();
                }
            }
        } else {
            disconnect(player);
        }

    }

    public String disconnect(EntityPlayer player) {
        boolean canDisconnect = disconnectionAllowed();
        SGBaseTE dte = getConnectedStargateTE();
        boolean validConnection = dte != null && !dte.isInvalid() && dte.getConnectedStargateTE() == this;

        if (canDisconnect || !validConnection) {
            if (state != SGState.Disconnecting)
                disconnect();
            return null;
        } else {
            return operationFailure(player, "incomingConnection");
        }
    }

    public boolean disconnectionAllowed() {
        return isInitiator || closeFromEitherEnd;
    }

    public String connect(String address, EntityPlayer player) {
        if (state != SGState.Idle) {
            return diallingFailure(player, "selfBusy");
        }
        String homeAddress = findHomeAddress();
        if (homeAddress.equals("")) {
            return diallingFailure(player, "selfOutOfRange");
        }
        if (this.allowOnlySpecifiedDestination) {
            if (!this.onlySpecifiedAddress.isEmpty()) {
                if (!address.equalsIgnoreCase(this.onlySpecifiedAddress));
                    return diallingFailure(player, "dialspecificaddress");
            }
        }
        SGBaseTE targetGate;
        try {
            targetGate = SGAddressing.findAddressedStargate(address, world);
        } catch (SGAddressing.AddressingError e) {
            return diallingFailure(player, e.getMessage());
        }
        if (targetGate == null || !targetGate.isMerged) {
            return diallingFailure(player, "unknownAddress", address);
        }
        if (getWorld() == targetGate.getWorld()) {
            address = SGAddressing.localAddress(address);
            homeAddress = SGAddressing.localAddress(homeAddress);
        }
        if (!targetGate.acceptIncomingConnections) {
            return diallingFailure(player, "cannotDialThisGate", address);
        }
        if (address.length() > getNumChevrons()) {
            return diallingFailure(player, "selfLackChevrons", address);
        }
        if (targetGate == this) {
            return diallingFailure(player, "diallingItself");
        }
        if (debugConnect) {
            System.out.printf("SGBaseTE.connect: to %s in dimension %d with state %s\n", targetGate.getPos(), targetGate.getWorld().provider.getDimension(), targetGate.state);
        }
        if (targetGate.getNumChevrons() < homeAddress.length()) {
            return diallingFailure(player, "targetLackChevrons");
        }
        if (targetGate.state != SGState.Idle) {
            return diallingFailure(player, "targetBusy", address);
        }
        distanceFactor = distanceFactorForCoordDifference(this, targetGate);
        if (debugEnergyUse) {
            System.out.printf("SGBaseTE: distanceFactor = %s\n", distanceFactor);
        }

        //Reset this value:
        energyToOpen = energyPerFuelItem / gateOpeningsPerFuelItem;

        if (isModLoaded("ic2")) {
            // Zpm
            String originName = this.getWorld().getWorldInfo().getWorldName().toLowerCase();
            String destinationName = targetGate.getWorld().getWorldInfo().getWorldName().toLowerCase();

            if (ZpmAddon.routeRequiresZpm(originName, destinationName)) {
                long power = (long) ZpmAddon.zpmPowerAvailable(world, this.pos, 4, false);
                if (!(power > 0)) {
                    return diallingFailure(player, "zpmNotFound");
                } else {
                    if (!(power > energyToOpen * distanceFactor)) {
                        return diallingFailure(player, "zpmLowPower");
                    }
                }
                this.syncAwaitTime = 30; // Sets longer connect delay due to distance
                this.destinationRequiresZPM = true;

            } else {
                this.syncAwaitTime = 10; // Sets short connect delay for in-world Stargates
                this.destinationRequiresZPM = false;
            }

            if (debugEnergyUse || debugZPM) {
                System.out.println("-------------------   Power Usage Debug   --------------------------");
                System.out.println("EnergyPerFuelItem: " + dFormat.format(energyPerFuelItem));
                System.out.println("Gate Openings Per Fuel: " + gateOpeningsPerFuelItem);
                System.out.println("SGPU Energy to Open with Distance Factor: " + dFormat.format(energyToOpen * distanceFactor));
                System.out.println("--------------------------------------------------------------------");
                System.out.println("IC2 Energy to Open with Distance Factor: " + dFormat.format((energyToOpen * distanceFactor) * SGCraft.Ic2euPerSGEnergyUnit));
                System.out.println("--------------------------------------------------------------------");
                System.out.println("ZPM Required: " + this.destinationRequiresZPM);
                System.out.println("ZPM Multiplier: " + ZpmAddon.routeZpmMultiplier(originName, destinationName));
                System.out.println("--------------------------------------------------------------------");
                System.out.println("Energy to Open: " + energyToOpen);
                System.out.println("Distance Factor: " + distanceFactor);
                System.out.println("--------------------------------------------------------------------");
                System.out.println("Energy Available: " + availableEnergy());
                System.out.println("Energy Required: " + (energyToOpen * distanceFactor));
                System.out.println("Energy per Tick: " + energyUsePerTick);
                System.out.println("Energy Used per Tick: " + (energyUsePerTick * distanceFactor));
                System.out.println("--------------------------------------------------------------------");
            }
        }

        // Final Power check before dial
        if (!energyIsAvailable(energyToOpen * distanceFactor)) {
            return diallingFailure(player, "insufficientEnergy");
        }

        if (this.hasChevronUpgrade) {
            this.wasIrisClosed = this.irisIsClosed();
        }

        if (targetGate.hasChevronUpgrade) {
            targetGate.wasIrisClosed = targetGate.irisIsClosed();
        }

        startDiallingStargate(address, targetGate, true, this.chevronsLockOnDial);
        targetGate.enterState(SGState.attemptToDial, 0); // Force remote gate immediate change state to help chunk stay loaded
        targetGate.startDiallingStargate(homeAddress, this, false, this.chevronsLockOnDial);

        return null;
    }

    public double distanceFactorForCoordDifference(TileEntity te1, TileEntity te2) {
        double dx = te1.getPos().getX() - te2.getPos().getX();
        double dz = te1.getPos().getZ() - te2.getPos().getZ();
        double d = Math.sqrt(dx * dx + dz * dz);

        if (debugEnergyUse) {
            System.out.printf("SGBaseTE: Connection distance = %s\n", d);
        }

        double ld = Math.log(0.05 * d + 1);
        double lm = Math.log(0.05 * 16 * SGAddressing.coordRange);
        double lr = ld / lm;
        double f = 1 + 14 * distanceFactorMultiplier * lr * lr;
        if (te1.getWorld() != te2.getWorld()) {
            f *= distanceFactorMultiplier;
            if (isModLoaded("ic2")) {
                String originName = te1.getWorld().getWorldInfo().getWorldName().toLowerCase();
                String destinationName = te2.getWorld().getWorldInfo().getWorldName().toLowerCase();

                if (ZpmAddon.routeRequiresZpm(te1.getWorld().getWorldInfo().getWorldName().toLowerCase(), te2.getWorld().getWorldInfo().getWorldName().toLowerCase())) {
                    f += ZpmAddon.routeZpmMultiplier(originName, destinationName);
                }
            }
        }
        return f;
    }

    public void playSGSoundEffect(SoundEvent se, float volume, float pitch) {
        playSoundEffect(se, volume * soundVolume, pitch);
    }

    public String diallingFailure(EntityPlayer player, String msg, Object... args) {
        if (player != null) {
            if (state == SGState.Idle)
                playSGSoundEffect(dialFailSound, 1F, 1F);
        }
        return operationFailure(player, msg, args);
    }

    public String operationFailure(EntityPlayer player, String msg, Object... args) {
        if (player != null)
            sendErrorMsg(player, msg, args);
        return msg;
    }

    public static void sendErrorMsg(EntityPlayer player, String msg, Object... args) {
        ITextComponent component = new TextComponentTranslation("message.sgcraft:" + msg, args);
        component.getStyle().setColor(TextFormatting.RED);
        player.sendMessage(component);
    }

    String findHomeAddress() {
        try {
            return getHomeAddress();
        } catch (SGAddressing.AddressingError e) {
            //System.out.printf("SGBaseTE.findHomeAddress: %s\n", e);
            return "";
        }
    }

    public void disconnect() {
        if (debugConnect) {
            System.out.printf("SGBaseTE: %s: disconnect()\n", side());
        }
        SGBaseTE dte = SGBaseTE.at(connectedLocation);

        if (dte != null)
            dte.clearConnection();
        clearConnection();
    }

    public void clearConnection() {
        if (state != SGState.Idle || connectedLocation != null) {
            dialledAddress = "";
            connectedLocation = null;
            isInitiator = false;
            markChanged();
            if (state == SGState.Connected) {
                enterState(SGState.Disconnecting, disconnectTime);
                //sendClientEvent(SGEvent.StartDisconnecting, 0);
                playSGSoundEffect(disconnectSound, 1F, 1F);

            } else {
                numEngagedChevrons = 0;
                if (state != SGState.Idle && state != SGState.Disconnecting) {
                    playSGSoundEffect(dialFailSound, 1F, 1F);
                } else {
                    playSGSoundEffect(chevronOutgoingSound, 1F, 1F);
                }
                enterState(SGState.Idle, 0);
                //sendClientEvent(SGEvent.FinishDisconnecting, 0);
            }
        }
    }

    void startDiallingStargate(String address, SGBaseTE dte, boolean initiator, boolean immediate) {
        //System.out.printf("SGBaseTE.startDiallingStargate %s, initiator = %s\n",
        //  dte, initiator);
        dialledAddress = address;
        connectedLocation = new SGLocation(dte);
        isInitiator = initiator;
        markDirty();
        if (isInitiator && !immediate) {
            startDiallingNextSymbol();
        }
        postEvent(initiator ? "sgDialOut" : "sgDialIn", address);
        if (immediate) {
            numEngagedChevrons = dialledAddress.length();
            if (!initiator) {
                playSGSoundEffect(chevronIncomingSound, 1F, 1F);
            }
            enterState(SGState.SyncAwait, syncAwaitTime);
        } else {
            numEngagedChevrons = 0;
        }

    }

    void serverUpdate() {
        if (!loaded) {
            loaded = true;
            try {
                homeAddress = getHomeAddress();
                addressError = "";
            } catch (SGAddressing.AddressingError e) {
                homeAddress = null;
                addressError = e.getMessage();
            }
            if (SGCraft.ocIntegration != null) { //[OC]
                ((OCIntegration)SGCraft.ocIntegration).onSGBaseTEAdded(this);
            }
        }
        if (isMerged) {
            if (debugState && state != SGState.Connected && timeout > 0) {
                int dimension = world.provider.getDimension();
                System.out.printf("SGBaseTE.serverUpdate at %s in dimension %d: state %s, timeout %s\n", pos, dimension, state, timeout);
            }
            tickEnergyUsage();
            if (timeout > 0) {
                switch (state) {
                    case Transient:
                        if (!irisIsClosed()) {
                            performTransientDamage();
                        }
                        break;
                    case Dialling:
                        double step = (double) (maxTimeout - timeout) / (double) maxTimeout;
                        ringAngle = startRingAngle + (targetRingAngle - startRingAngle) * step;
                        break;

                    case EstablishingConnection:
                        if (timeout == 25) {
                            playSGSoundEffect(connectSound, 1F, 1F); // Play sound before gate actually opens.
                        }
                        break;
                }
                --timeout;
            } else {
                switch(state) { // Next Stage
                    case Idle:
                        if (symbolsRemaining(false) && isInitiator) {
                            startDiallingNextSymbol();
                        }
                        break;
                    case Dialling:
                        if (isInitiator) {
                            char charTargetSymbol = dialledAddress.charAt(numEngagedChevrons);
                            char charOwnSymbol = homeAddress.charAt(numEngagedChevrons);
                            String targetSymbol = Character.toString(charTargetSymbol);
                            String ownSymbol = Character.toString(charOwnSymbol);
                            // Note:  CC interfaces can't use CHAR!
                            finishDiallingSymbol(targetSymbol, true, true, !symbolsRemaining(true));
                            SGBaseTE targetGate = SGBaseTE.at(connectedLocation);
                            targetGate.finishDiallingSymbol(ownSymbol, false, true, !targetGate.symbolsRemaining(true));
                        }
                        break;
                    case InterDialling:
                        if (isInitiator) {
                            startDiallingNextSymbol();
                        }
                        break;
                    case SyncAwait:
                        attemptToLockStargate();
                        break;
                    case EstablishingConnection:
                        openStargate();
                        break;
                    case Transient:
                        enterState(SGState.Connected, isInitiator ? ticksToStayOpen : 0);
                        break;
                    case Connected:
                        if (isInitiator && ticksToStayOpen > 0) {
                            disconnect();
                        }
                        break;
                    case Disconnecting:
                        numEngagedChevrons = 0;
                        enterState(SGState.Idle, 0);

                        if (this.hasIrisUpgrade && this.returnToPreviousIrisState && this.wasIrisClosed) {
                            this.closeIris();
                            // Note: this is fired at both the origin and destination gates.
                        }

                        break;
                }
            }
        }
    }

    void tickEnergyUsage() {
        if (state == SGState.Connected && isInitiator) {
            if (!useEnergy(energyUsePerTick * distanceFactor)) {
                disconnect();
            }
        }
    }

    public double availableEnergy() {
        List<ISGEnergySource> sources = findEnergySources(this.destinationRequiresZPM);
        return energyInBuffer + energyAvailableFrom(sources);
    }

    public boolean energyIsAvailable(double amount) {
        double energy = availableEnergy();
        if (debugEnergyUse) {
            System.out.printf("SGBaseTE.energyIsAvailable: need %s, have %s\n", amount, energy);
        }
        return energy >= amount;
    }

    public boolean useEnergy(double amount) {
        if (debugEnergyUse) {
            System.out.printf("SGBaseTE.useEnergy: %s; buffered: %s\n", amount, energyInBuffer);
        }

        if (amount <= energyInBuffer) {
            energyInBuffer -= amount;

            if (updated++ > 10) {
                // Send energy update to client for diag/gui purposes
                markChanged();
                updated = 0;
            }
            return true;
        }

        List<ISGEnergySource> sources = findEnergySources(this.destinationRequiresZPM);
        double energyAvailable = energyInBuffer + energyAvailableFrom(sources);

        if (debugEnergyUse) {
            System.out.printf("SGBaseTE.useEnergy: %s available\n", energyAvailable);
        }

        if (amount > energyAvailable) {
            if (debugEnergyUse) {
                System.out.print("SGBaseTE: Not enough energy available\n");
            }
            return false;
        }

        double desiredEnergy = max(amount, maxEnergyBuffer);
        double targetEnergy = min(desiredEnergy, energyAvailable);
        double energyRequired = targetEnergy - energyInBuffer;

        if (debugEnergyUse) {
            System.out.printf("SGBaseTE.useEnergy: another %s required\n", energyRequired);
        }

        double energyOnHand = energyInBuffer + drawEnergyFrom(sources, energyRequired);

        if (debugEnergyUse) {
            System.out.printf("SGBaseTE.useEnergy: %s now on hand, need %s\n", energyOnHand, amount);
        }

        if (amount - 0.0001 > energyOnHand) {
            if (debugEnergyUse) {
                System.out.printf("SGBaseTE: Energy sources only delivered %s of promised %s\n", energyOnHand - energyInBuffer, energyAvailable);
            }
            return false;
        }

        setEnergyInBuffer(energyOnHand - amount);

        if (debugEnergyUse) {
            System.out.printf("SGBaseTE.useEnergy: %s left over in buffer\n", energyInBuffer);
        }

        return true;
    }

    private List<ISGEnergySource> findEnergySources(boolean requireZPM) {

        boolean ccLoaded = isModLoaded("computercraft");
        boolean ocLoaded = isModLoaded("opencomputers");
        boolean ic2Loaded = isModLoaded("ic2");
        boolean rsfLoaded = isModLoaded("redstoneflux");
        boolean useDHD = true;
        DHDTE te = getLinkedControllerTE();

        if (debugEnergyUse) {
            System.out.printf("SGBaseTe.findEnergySources: for %s\n", getSoundPos());
        }

        List<ISGEnergySource> result = new ArrayList<>();
        Trans3 t = localToGlobalTransformation();
        //for (int i = -5; i <= 2; i++) {
        //BlockPos bp = t.p(i, -1, 0).blockPos();

        int radius = 2;

        for (final BlockPos.MutableBlockPos nearPos : BlockPos.getAllInBoxMutable(
                pos.add(-radius, -radius, -radius),
                pos.add(radius, radius, radius)
        )) {

            TileEntity nte = world.getTileEntity(nearPos);
            if (debugEnergyUse) {
                //System.out.printf("SGBaseTE.findEnergySources: %s at %s\n", nte, nearPos);
            }

            // make a another energy source required instead of DHD if a computer interface is attached.
            if ((ccLoaded && nte instanceof CCInterfaceTE) || (ocLoaded && nte instanceof OCInterfaceTE)) {
                if (isModLoaded("ic2") || isModLoaded("redstoneflux")) {
                    useDHD = false;
                }
            }

            if (nte instanceof ISGEnergySource) { // Specifically exclude the ZPM Interface.
                if (ic2Loaded && nte instanceof IC2PowerTE) {
                    result.add((ISGEnergySource) nte);
                    if (debugEnergyUse) {
                        System.out.println("Found IC2PowerTE at: " + nte.getPos());
                    }
                }

                if(ic2Loaded && nte instanceof ZpmInterfaceCartTE) {
                    if (debugEnergyUse) {
                        System.out.println("Found ZpmInterfaceCartTE at: " + nte.getPos() + " but not added as source");
                    }
                }

                if (rsfLoaded && nte instanceof RFPowerTE) {
                    result.add((ISGEnergySource) nte);
                    if (debugEnergyUse) {
                        System.out.println("Found RFPowerTE at: " + nte.getPos());
                    }
                }

                if (ocLoaded && nte instanceof OCInterfaceTE) {
                    result.add((ISGEnergySource) nte);
                    if (debugEnergyUse) {
                        System.out.println("Found OCInterfaceTE at: " + nte.getPos());
                    }
                }
            }
        }

        if (te != null) {
            if (useDHD) {
                if (!requireZPM) {
                    result.add(te);
                    if (debugEnergyUse) {
                        System.out.println("Found DHDTE at: " + te.getPos());
                    }
                } else {
                    if (debugEnergyUse) {
                        System.out.println("Found DHDTE at: " + te.getPos() + " but was not added because destination requires ZPM");
                    }
                }
            } else {
                if (debugEnergyUse) {
                    System.out.println("Found DHDTE at: " + te.getPos() + " but was not added because a computer interface is attached to the gate");
                }
            }
        }

        return result;
    }

    private double energyAvailableFrom(List<ISGEnergySource> sources) {
        double energy = 0;
        for (ISGEnergySource source : sources) {
            double e = source.availableEnergy();
            if (debugEnergyUse) {
                System.out.printf("SGBaseTe.energyAvailableFrom: %s can supply %s\n", source, e);
            }
            energy += e;
        }
        return energy;
    }

    double energyTotalAvailableFrom(List<ISGEnergySource> sources) {
        double energy = 0;
        for (ISGEnergySource source : sources) {
            double e = source.totalAvailableEnergy();
            if (debugEnergyUse) {
                System.out.printf("SGBaseTe.energyAvailableFrom: %s can supply %s\n", source, e);
            }
            energy += e;
        }
        return energy;
    }

    private double drawEnergyFrom(List<ISGEnergySource> sources, double amount) {
        double total = 0;
        for (ISGEnergySource source : sources) {
            if (total >= amount)
                break;
            double e = source.drawEnergyDouble(amount - total);
            if (debugEnergyUse) {
                System.out.printf("SGBaseTe.drawEnergyFrom: %s supplied %s\n", source, e);
            }
            total += e;
        }
        if (total < amount) {
            if (debugEnergyUse) {
                System.out.printf("SGCraft: Warning: Energy sources did not deliver promised energy (%s requested, %s delivered)\n", amount, total);
            }
        }

        return total;
    }

    private void setEnergyInBuffer(double amount) {
        if (energyInBuffer != amount) {
            energyInBuffer = amount;
            markDirty();
        }
    }

    private void performTransientDamage() {
        Trans3 t = localToGlobalTransformation();
        Vector3 p0 = t.p(-1.5, 0.5, 0.5);
        Vector3 p1 = t.p(1.5, 3.5, 5.5);
        Vector3 q0 = p0.min(p1);
        Vector3 q1 = p0.max(p1);
        AxisAlignedBB box = new AxisAlignedBB(q0.x, q0.y, q0.z, q1.x, q1.y, q1.z);
        if (debugTransientDamage) {
            System.out.print("SGBaseTE.performTransientDamage: players in world:\n");
            for (Entity ent : world.loadedEntityList)
                if (ent instanceof EntityPlayer) {
                    System.out.printf("--- %s\n", ent);
                }
            System.out.printf("SGBaseTE.performTransientDamage: box = %s\n", box);
        }
        List<EntityLivingBase> ents = world.getEntitiesWithinAABB(EntityLivingBase.class, box);
        for (EntityLivingBase ent : ents) {
            Vector3 ep = new Vector3(ent.posX, ent.posY, ent.posZ);
            Vector3 gp = t.p(0, 2, 0.5);
            double dist = ep.distance(gp);
            if (debugTransientDamage) {
                System.out.printf("SGBaseTE.performTransientDamage: found %s\n", ent);
            }
            if (dist > 1.0) {
                dist = 1.0;
            }
            int damage = (int)Math.ceil(dist * transientDamageRate);
            if (debugTransientDamage) {
                System.out.printf("SGBaseTE.performTransientDamage: distance = %s, damage = %s\n", dist, damage);
            }
            ent.attackEntityFrom(transientDamage, damage);
        }
    }

    private boolean symbolsRemaining(boolean before) {
        int n = numEngagedChevrons;
        return n < dialledAddress.length() - (before ? 1 : 0);
    }

    private void startDiallingNextSymbol() {
        if (debugState) {
            System.out.printf("SGBaseTE.startDiallingNextSymbol: %s of %s\n", numEngagedChevrons, dialledAddress);
        }
        startDiallingSymbol(dialledAddress.charAt(numEngagedChevrons));
    }

    private void startDiallingSymbol(char c) {
        int i = SGAddressing.charToSymbol(c);
        if (debugState) {
            System.out.printf("SGBaseTE.startDiallingSymbol: %s\n", i);
        }
        if (i >= 0 && i < numRingSymbols) {
            double targetAngle = i * ringSymbolAngle;
            double diff = targetAngle - ringAngle;
            if (Math.abs(diff) < 180) {
                targetAngle -= Math.copySign(360.0, ringAngle);
                diff = targetAngle - ringAngle;
            }

            int delay = (int)Math.abs(diff / ringRotationSpeed);
            targetRingAngle = targetAngle;
            //System.out.println(homeAddress + " -> Delay: " + delay + " (From angle " + ringAngle + " to angle " + targetAngle + ")");
            enterState(SGState.Dialling, delay);
        } else {
            System.out.printf("SGCraft: Stargate jammed trying to dial symbol %s\n", c);
            dialledAddress = "";
            enterState(SGState.Idle, 0);
        }
    }

    public void unsetSymbol(char symbol) {
        --numEngagedChevrons; // Needs to be pre-event
        postEvent("sgChevronUnset", numEngagedChevrons, symbol);
    }

    public void finishDiallingSymbol(String symbol, boolean outgoing, boolean changeState, boolean lastOne) {
        ++numEngagedChevrons;
        postEvent("sgChevronEngaged", numEngagedChevrons, symbol);

        if (lastOne) {
            if (changeState) {
                enterState(SGState.SyncAwait, syncAwaitTime);
            }
            if (!world.isRemote) {
                playSGSoundEffect(outgoing ? lockOutgoingSound : lockIncomingSound, 1F, 1F);
            }
        } else {
            if (changeState) {
                enterState(SGState.InterDialling, interDiallingTime);
            }
            if (!world.isRemote) {
                playSGSoundEffect(outgoing ? chevronOutgoingSound : chevronIncomingSound, 1F, 1F);
            }
        }
    }

    private void attemptToLockStargate() {
        if (!isInitiator || useEnergy(energyToOpen * distanceFactor)) {
            enterState(SGState.EstablishingConnection, 30);
        } else {
            disconnect();
        }
    }

    private void openStargate() {
        if (debugConnect) {
            System.out.printf("SGBaseTE: Connecting to '%s'\n", dialledAddress);
        }
        enterState(SGState.Transient, transientDuration);
    }

    public boolean canTravelFromThisEnd() {
        return isInitiator || !oneWayTravel;
    }

    String repr(Entity entity) {
        if (entity != null) {
            String s = String.format("%s#%s", entity.getClass().getSimpleName(), entity.getEntityId());
            if (entity.isDead) {
                s += "(dead)";
            }
            return s;
        } else {
            return "null";
        }
    }

    class TrackedEntity {
        public Entity entity;
        public Vector3 lastPos;

        public TrackedEntity(Entity entity) {
            this.entity = entity;
            this.lastPos = new Vector3(entity.posX, entity.posY, entity.posZ);
        }
    }

    List<TrackedEntity> trackedEntities = new ArrayList<>();

    void checkForEntitiesInPortal() {
        if (state == SGState.Connected) {
            for (TrackedEntity trk : trackedEntities) {
                entityInPortal(trk.entity, trk.lastPos);
            }
            trackedEntities.clear();
            Vector3 p0 = new Vector3(-1.5, 0.5, -1.5);
            Vector3 p1 = new Vector3(1.5, 3.5, 1.5);
            Trans3 t = localToGlobalTransformation();
            AxisAlignedBB box = t.box(p0, p1);
            //System.out.printf("SGBaseTE.checkForEntitiesInPortal: %s\n", box);
            List<Entity> ents = world.getEntitiesWithinAABB(Entity.class, box);
            for (Entity entity : ents) {
                if (entity instanceof EntityFishHook) {
                    continue;
                }
                if (!entity.isDead && entity.getRidingEntity() == null) {
                    //if (!(entity instanceof EntityPlayer))
                    //  System.out.printf("SGBaseTE.checkForEntitiesInPortal: Tracking %s\n", repr(entity));
                    trackedEntities.add(new TrackedEntity(entity));
                }
            }
        } else {
            trackedEntities.clear();
        }
    }

    public void entityInPortal(Entity entity, Vector3 prevPos) {
        if (!entity.isDead && state == SGState.Connected) {
            Trans3 t = localToGlobalTransformation();
            double vx = entity.posX - prevPos.x;
            double vy = entity.posY - prevPos.y;
            double vz = entity.posZ - prevPos.z;
            Vector3 p1 = t.ip(entity.posX, entity.posY, entity.posZ);
            Vector3 p0 = t.ip(2 * prevPos.x - entity.posX, 2 * prevPos.y - entity.posY, 2 * prevPos.z - entity.posZ);
            //if (!(entity instanceof EntityPlayer))
            //  System.out.printf("SGBaseTE.entityInPortal: z0 = %.3f z1 = %.3f\n", p0.z, p1.z);
            double z0 = 0.0;
            if (p0.z >= z0 && p1.z < z0 && p1.z > z0 - 5.0) {
                //System.out.printf("SGBaseTE.entityInPortal: %s passed through event horizon of stargate at (%d,%d,%d) in %s\n",
                //  repr(entity), xCoord, yCoord, zCoord, world);
                entity.motionX = vx;
                entity.motionY = vy;
                entity.motionZ = vz;
                //System.out.printf("SGBaseTE.entityInPortal: %s pos (%.2f, %.2f, %.2f) prev (%.2f, %.2f, %.2f) motion (%.2f, %.2f, %.2f)\n",
                //  repr(entity),
                //  entity.posX, entity.posY, entity.posZ,
                //  prevPos.x, prevPos.y, prevPos.z,
                //  entity.motionX, entity.motionY, entity.motionZ);

                SGBaseTE dte = getConnectedStargateTE();
                if (dte != null) {
                    Trans3 dt = dte.localToGlobalTransformation();
                    while (entity.getRidingEntity() != null)
                        entity = entity.getRidingEntity();
                    teleportEntityAndRiders(entity, t, dt, connectedLocation.dimension, dte.irisIsClosed());
                }
            }
        }
    }

    Entity teleportEntityAndRiders(Entity entity, Trans3 t1, Trans3 t2, int dimension, boolean destBlocked) {
        if (debugTeleport) {
            System.out.printf("SGBaseTE.teleportEntityAndRiders: destBlocked = %s\n", destBlocked);
        }

        List<Entity> riders = entity.getPassengers();
        for (int i = 0; i < riders.size(); i++) {
            Entity rider = riders.get(i);
            rider.dismountRidingEntity();
            rider = teleportEntityAndRiders(rider, t1, t2, dimension, destBlocked);
            riders.set(i, rider);
        }
        unleashEntity(entity);
        entity = teleportEntity(entity, t1, t2, dimension, destBlocked);
        if (entity != null && !entity.isDead) {
            for (Entity rider : riders) {
                if (rider != null && !rider.isDead) {
                    rider.startRiding(entity, true);
                }
            }
        }
        return entity;
    }

    // Break any leash connections to or from the given entity. That happens anyway
    // when the entity is teleported, but without this it drops an extra leash item.
    protected void unleashEntity(Entity entity) {
        if (entity instanceof EntityLiving) {
            ((EntityLiving) entity).clearLeashed(true, false);
        }
        for (EntityLiving entity2 : entitiesWithinLeashRange(entity)) {
            if (entity2.getLeashed() && entity2.getLeashHolder() == entity) {
                entity2.clearLeashed(true, false);
            }
        }
    }

    protected List<EntityLiving> entitiesWithinLeashRange(Entity entity) {
        AxisAlignedBB box = new AxisAlignedBB(
                entity.posX - 7.0D, entity.posY - 7.0D, entity.posZ - 7.0D,
                entity.posX + 7.0D, entity.posY + 7.0D, entity.posZ + 7.0D);
        return entity.world.getEntitiesWithinAABB(EntityLiving.class, box);
    }

    Entity teleportEntity(Entity entity, Trans3 t1, Trans3 t2, int dimension, boolean destBlocked) {
        Entity newEntity = null;
        if (debugTeleport) {
            System.out.printf("SGBaseTE.teleportEntity: %s (in dimension %d)  to dimension %d\n",
                    repr(entity), entity.dimension, dimension);
            System.out.printf("SGBaseTE.teleportEntity: pos (%.2f, %.2f, %.2f) prev (%.2f, %.2f, %.2f) last (%.2f, %.2f, %.2f) pitch %.2f yaw %.2f\n",
                    entity.posX, entity.posY, entity.posZ,
                    entity.prevPosX, entity.prevPosY, entity.prevPosZ,
                    entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ,
                    entity.rotationPitch, entity.rotationYaw);
        }

        Vector3 p = t1.ip(entity.posX, entity.posY, entity.posZ); // local position
        Vector3 v = t1.iv(entity.motionX, entity.motionY, entity.motionZ); // local velocity
        Vector3 r = t1.iv(yawVector(entity)); // local facing
        Vector3 q = t2.p(-p.x, p.y, -p.z); // new global position
        Vector3 u = t2.v(-v.x, v.y, -v.z); // new global velocity
        Vector3 s = t2.v(r.mul(-1)); // new global facing
        if (debugTeleport) {
            System.out.printf("SGBaseTE.teleportEntity: Facing old %s new %s\n", r, s);
        }

        double a = yawAngle(s, entity); // new global yaw angle

        if (debugTeleport) {
            System.out.printf("SGBaseTE.teleportEntity: new yaw %.2f\n", a);
        }

        if (!canTravelFromThisEnd() && !isInitiator) {
            if (reverseWormholeKills) { // Player attempting to reverse travel through one-way wormhole.
                terminateEntityByReverseWormhole(entity);
                return entity;
            } else {
                // Player attempted to reverse travel but config option for reverseWormholleKills is disabled, return entity, display nothing.
                return entity;
            }
        }

        if (!destBlocked) { // destBlocked == closed iris.
            // Play sound from point of origin gate.
            playTeleportSound(entity.getEntityWorld(), new Vector3(entity.getPositionVector()), entity);
            if (entity.dimension == dimension) {
                newEntity = teleportWithinDimension(entity, q, u, a, destBlocked);
            } else {
                newEntity = teleportToOtherDimension(entity, q, u, a, dimension, destBlocked);
            }
        } else {
            terminateEntityByIrisImpact(entity);
            playIrisHitSound(worldForDimension(dimension), q, entity);
        }
        // Play sound at destination gate.
        playTeleportSound(entity.getEntityWorld(), new Vector3(entity.getPositionVector()), entity);
        return newEntity;
    }

    public void terminateEntityByIrisImpact(Entity entity) {
        if (entity instanceof EntityPlayer) {
            terminatePlayerByIrisImpact((EntityPlayer)entity);
        } else {
            entity.setDead();
        }
    }

    public void terminatePlayerByIrisImpact(EntityPlayer player) {
        if (player.capabilities.isCreativeMode) {
            sendErrorMsg(player, "irisAtDestination");
        } else {
            if (!(preserveInventory || player.world.getGameRules().getBoolean("keepInventory")))
                player.inventory.clear();
            player.attackEntityFrom(irisDamageSource, irisDamageAmount);
        }
    }

    public void terminateEntityByReverseWormhole(Entity entity) {
        if (entity instanceof EntityPlayer) {
            terminatePlayerByReverseWormhole((EntityPlayer)entity);
        } else {
            entity.setDead();
        }
    }

    public void terminatePlayerByReverseWormhole(EntityPlayer player) {
        if (player.capabilities.isCreativeMode) {
            sendErrorMsg(player, "reverseWormhole");
        } else {
            if (!(preserveInventory || player.world.getGameRules().getBoolean("keepInventory")))
                player.inventory.clear();
            player.attackEntityFrom(transientDamage, irisDamageAmount);
            if (!player.isDead) {
                player.setHealth(0.0F); // Backup player death to prevent grief prevention from saving player.
            }
        }
    }

    static WorldServer worldForDimension(int dimension) {
        return SGAddressing.getWorld(dimension);
    }

    void playIrisHitSound(World world, Vector3 pos, Entity entity) {
        float volume = (float) min(entity.width * entity.height, 1.0);
        float pitch = 2F - volume;
        if (debugTeleport) {
            System.out.printf("SGBaseTE.playIrisHitSound: at (%.3f,%.3f,%.3f) volume %.3f pitch %.3f\n", pos.x, pos.y, pos.z, volume, pitch);
        }
        playSoundEffect(world, pos.x, pos.y, pos.z, irisHitSound, volume * soundVolume, pitch);
    }

    void playTeleportSound(World world, Vector3 pos, Entity entity) {
        float volume = (float) min(entity.width * entity.height, 1.0);
        float pitch = 2F - volume;
        if (debugTeleport) {
            System.out.printf("SGBaseTE.playTeleportSound: at (%.3f,%.3f,%.3f) volume %.3f pitch %.3f\n", pos.x, pos.y, pos.z, volume, pitch);
        }
        playSoundEffect(world, pos.x, pos.y, pos.z, teleportSound, volume * soundVolume, pitch);
    }

    Entity teleportWithinDimension(Entity entity, Vector3 p, Vector3 v, double a, boolean destBlocked) {
        if (entity instanceof EntityPlayerMP) {
            System.out.println("AAA");
            return teleportPlayerWithinDimension((EntityPlayerMP) entity, p, v, a);
        } else {
            return teleportEntityToWorld(entity, p, v, a, (WorldServer) entity.world, destBlocked);
        }
    }

    Entity teleportPlayerWithinDimension(EntityPlayerMP entity, Vector3 p, Vector3 v, double a) {
        entity.rotationYaw = (float)a;
        entity.setPositionAndUpdate(p.x, p.y, p.z);
        entity.world.updateEntityWithOptionalForce(entity, false);
        entity.velocityChanged = true; // Have to mark entity velocity changed.

        return entity;
    }

    Entity teleportToOtherDimension(Entity entity, Vector3 p, Vector3 v, double a, int dimension, boolean destBlocked) {
        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)entity;
            Vector3 q = p.add(yawVector(a));
            transferPlayerToDimension(player, dimension, q, a);
            return player;
        } else {
            setVelocity(entity,v); // Make sure riding entity exits at the same speed they entered otherwise event horizon will kill them.
            return teleportEntityToDimension(entity, p, v, a, dimension, destBlocked);
        }
    }

    void transferPlayerToDimension(EntityPlayerMP player, int newDimension, Vector3 p, double a) {
        FakeTeleporter fakeTeleporter = new FakeTeleporter();
        double x = player.motionX;
        double y = player.motionY;
        double z = player.motionZ;
        player.changeDimension(newDimension, fakeTeleporter);

        // Now check to see if the player made it through the above server method, if it did, then update their location.
        if (player.dimension == newDimension) {
            player.connection.setPlayerLocation(p.x, p.y, p.z, (float) a, player.rotationPitch);
            player.setVelocity(x, y, z);
            player.velocityChanged = true;
        }
    }

    Entity teleportEntityToDimension(Entity entity, Vector3 p, Vector3 v, double a, int dimension, boolean destBlocked) {
        MinecraftServer server = BaseUtils.getMinecraftServer();
        WorldServer world = server.getWorld(dimension);
        return teleportEntityToWorld(entity, p, v, a, world, destBlocked);
    }

    Entity teleportEntityToWorld(Entity oldEntity, Vector3 p, Vector3 v, double a, WorldServer newWorld, boolean destBlocked) {
        if (destBlocked) {
            if (!(oldEntity instanceof EntityLivingBase))
                return null;
        }

        FakeTeleporter fakeTeleporter = new FakeTeleporter();
        Entity newEntity = oldEntity.changeDimension(newWorld.provider.getDimension(), fakeTeleporter);

        // Now check to see if the entity made it through the above server method, if it did, then update their location.
        if (newEntity.dimension == newWorld.provider.getDimension()) {
            newEntity.setLocationAndAngles(p.x, p.y, p.z, (float) a, oldEntity.rotationPitch);
            setVelocity(newEntity,v); //Set velocity so that items exist at the same rate they did when they entered the event horizon.
            newEntity.velocityChanged = true;
        }

        return newEntity;
    }

    protected static int yawSign(Entity entity) {
        return entity instanceof EntityArrow ? -1 : 1;
    }

    Vector3 yawVector(Entity entity) {
        return yawVector(yawSign(entity) * entity.rotationYaw);
    }

    Vector3 yawVector(double yaw) {
        double a = Math.toRadians(yaw);
        Vector3 v = new Vector3(-Math.sin(a), 0, Math.cos(a));
        //System.out.printf("SGBaseTE.yawVector: %.2f --> (%.3f, %.3f)\n", yaw, v.x, v.z);
        return v;
    }

    double yawAngle(Vector3 v, Entity entity) {
        double a = Math.atan2(-v.x, v.z);
        double d = Math.toDegrees(a);
        //System.out.printf("SGBaseTE.yawAngle: (%.3f, %.3f) --> %.2f\n", v.x, v.z, d);
        return yawSign(entity) * d;
    }

    public SGBaseTE getConnectedStargateTE() {
        return isConnected() && connectedLocation != null ? connectedLocation.getStargateTE() : null;
    }

    static void copyMoreEntityData(EntityLiving oldEntity, EntityLiving newEntity) {
        float s = oldEntity.getAIMoveSpeed();
        if (s != 0)
            newEntity.setAIMoveSpeed(s);
    }

    static void setVelocity(Entity entity, Vector3 v) {
        entity.motionX = v.x;
        entity.motionY = v.y;
        entity.motionZ = v.z;
    }

    //------------------------------------   Client   --------------------------------------------

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        //System.out.printf("SGBaseTE.onDataPacket: with state %s numEngagedChevrons %s\n",
        //  SGState.valueOf(pkt.customParam1.getInteger("state")),
        //  pkt.customParam1.getInteger("numEngagedChevrons"));
        IrisState oldIrisState = irisState;
        SGState oldState = state;
        super.onDataPacket(net, pkt);
        if (isMerged) {
            if (state != oldState) {
                switch (state) {
                    case Transient:
                        initiateOpeningTransient();
                        break;
                    case Disconnecting:
                        initiateClosingTransient();
                        break;
                    case Dialling:
                        if (isInitiator) {
                            if (timeout > 0) {
                                SGCraft.playSound(this, gateRollSound);
                            }
                        }
                        break;
                    //case Connected:
                    //    SGCraft.playSound(this, eventHorizonSound);
                    //    break;
                }
            }
            if (irisState != oldIrisState) {
                switch (irisState) {
                    case Opening:
                        SGCraft.playSound(this, irisOpenSound);
                        break;
                    case Closing:
                        SGCraft.playSound(this, irisCloseSound);
                        break;
                }
            }
        }
    }

    void clientUpdate() {
        lastRingAngle = ringAngle;
        switch (state) {
            case Dialling:
                if (timeout > 0) {
                    double step = (double)(maxTimeout - timeout) / (double)maxTimeout;
                    ringAngle = startRingAngle + (targetRingAngle - startRingAngle) * step;
                    --timeout;
                }
                break;
            case Transient:
            case Connected:
            case Disconnecting:
                applyRandomImpulse();
                updateEventHorizon();
                break;
        }
    }

    public double[][][] getEventHorizonGrid() {
        if (ehGrid == null) {
            int m = SGBaseTERenderer.ehGridRadialSize;
            int n = SGBaseTERenderer.ehGridPolarSize;
            ehGrid = new double[2][n + 2][m + 1];
            for (int i = 0; i < 2; i++) {
                ehGrid[i][0] = ehGrid[i][n];
                ehGrid[i][n + 1] = ehGrid[i][1];
            }
        }
        return ehGrid;
    }

    void initiateOpeningTransient() {
        double v[][] = getEventHorizonGrid()[1];
        int n = SGBaseTERenderer.ehGridPolarSize;
        for (int j = 0; j <= n+1; j++) {
            v[j][0] = openingTransientIntensity;
            v[j][1] = v[j][0] + openingTransientRandomness * random.nextGaussian();
        }
    }

    void initiateClosingTransient() {
        //numEngagedChevrons = 0;
        double v[][] = getEventHorizonGrid()[1];
        int m = SGBaseTERenderer.ehGridRadialSize;
        int n = SGBaseTERenderer.ehGridPolarSize;
        for (int i = 1; i < m; i++)
            for (int j = 1; j <= n; j++)
                v[j][i] += closingTransientRandomness * random.nextGaussian();
    }

    void applyRandomImpulse() {
        double v[][] = getEventHorizonGrid()[1];
        int m = SGBaseTERenderer.ehGridRadialSize;
        int n = SGBaseTERenderer.ehGridPolarSize;
        int i = random.nextInt(m - 1) + 1;
        int j = random.nextInt(n) + 1;
        v[j][i] += 0.05 * random.nextGaussian();
    }

    void updateEventHorizon() {
        double grid[][][] = getEventHorizonGrid();
        double u[][] = grid[0];
        double v[][] = grid[1];
        int m = SGBaseTERenderer.ehGridRadialSize;
        int n = SGBaseTERenderer.ehGridPolarSize;
        double dt = 1.0;
        double asq = 0.03;
        double d = 0.95;
        for (int i = 1; i < m; i++)
            for (int j = 1; j <= n; j++) {
                double du_dr = 0.5 * (u[j][i+1] - u[j][i-1]);
                double d2u_drsq = u[j][i+1] - 2 * u[j][i] + u[j][i-1];
                double d2u_dthsq = u[j+1][i] - 2 * u[j][i] + u[j-1][i];
                v[j][i] = d * v[j][i] + (asq * dt) * (d2u_drsq + du_dr / i + d2u_dthsq / (i * i));
            }
        for (int i = 1; i < m; i++)
            for (int j = 1; j <= n; j++)
                u[j][i] += v[j][i] * dt;
        double u0 = 0, v0 = 0;
        for (int j = 1; j <= n; j++) {
            u0 += u[j][1];
            v0 += v[j][1];
        }
        u0 /= n;
        v0 /= n;
        for (int j = 1; j <= n; j++) {
            u[j][0] = u0;
            v[j][0] = v0;
        }
        //dumpGrid("u", u);
        //dumpGrid("v", v);
    }

    void dumpGrid(String label, double g[][]) {
        System.out.printf("SGBaseTE: %s:\n", label);
        int m = SGBaseTERenderer.ehGridRadialSize;
        int n = SGBaseTERenderer.ehGridPolarSize;
        for (int j = 0; j <= n+1; j++) {
            for (int i = 0; i <= m; i++)
                System.out.printf(" %6.3f", g[j][i]);
            System.out.print("\n");
        }
    }

    @Override
    public IInventory getInventory() {
        return inventory;
    }

    public boolean irisIsClosed() {
        //System.out.printf("SGBaseTE.irisIsClosed: irisPhase = %s\n", irisPhase);
        return hasIrisUpgrade && irisPhase <= maxIrisPhase / 2;
    }

    public double getIrisAperture(double partialTicks) {
        return (lastIrisPhase * (1 - partialTicks) + irisPhase * partialTicks) / maxIrisPhase;
    }

    void irisUpdate() {
        lastIrisPhase = irisPhase;
        switch (irisState) {
            case Opening:
                if (irisPhase < maxIrisPhase)
                    ++irisPhase;
                else
                    enterIrisState(IrisState.Open);
                break;
            case Closing:
                if (irisPhase > 0)
                    --irisPhase;
                else
                    enterIrisState(IrisState.Closed);
                break;
        }
    }

    void enterIrisState(IrisState newState) {
        if (irisState != newState) {
            String oldDesc = irisStateDescription(irisState);
            String newDesc = irisStateDescription(newState);
            irisState = newState;
            markChanged();
            if (!oldDesc.equals(newDesc))
                //postEvent("sgIrisStateChange", "oldState", oldDesc, "newState", newDesc);
                postEvent("sgIrisStateChange", newDesc, oldDesc);
        }
    }

    public void openIris() {
        if (isMerged && hasIrisUpgrade && irisState != IrisState.Open) {
            enterIrisState(IrisState.Opening);
        }
    }

    public void closeIris() {
        if (isMerged && hasIrisUpgrade && irisState != IrisState.Closed) {
            enterIrisState(IrisState.Closing);
        }
    }

    public void onNeighborBlockChange() {
        if (!world.isRemote) {
            boolean newInput = BaseBlockUtils.blockIsGettingExternallyPowered(world, pos);
            if (redstoneInput != newInput) {
                redstoneInput = newInput;
                markDirty();
                if (redstoneInput) {
                    closeIris();
                } else {
                    openIris();
                }
            }
        }
    }

    void updateIrisEntity() {
        if (isMerged && hasIrisUpgrade) {
            if (!hasIrisEntity()) {
                EntityStargateIris ent = new EntityStargateIris(this);
                world.spawnEntity(ent);
            }
        } else {
            for (EntityStargateIris ent : findIrisEntities()) {
                world.removeEntity(ent);
            }
        }
    }

    boolean hasIrisEntity() {
        return findIrisEntities().size() != 0;
    }

    List<EntityStargateIris> findIrisEntities() {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        AxisAlignedBB box = new AxisAlignedBB(x, y, z, x + 1, y + 2, z + 1);
        //System.out.printf("SGBaseTE.findIrisEntities: in %s\n", box);
        return world.getEntitiesWithinAABB(EntityStargateIris.class, box);
    }

    public ItemStack getCamouflageStack(BlockPos cpos) {
        //System.out.printf("SGBaseTE.getCamouflageStack: for %s from base at %s\n", cpos, pos);
        Trans3 t = localToGlobalTransformation();
        Vector3 p = t.ip(Vector3.blockCenter(cpos));
        //System.out.printf("SGBaseTE.getCamouflageStack: p = %s\n", p);
        if (p.y == 0) {
            int i = 2 + p.roundX();
            //System.out.printf("SGBaseTE.getCamouflageStack: i = %s\n", i);
            if (i >= 0 && i < 5)
                return getStackInSlot(firstCamouflageSlot + i);
        }
        return null;
    }

    boolean isCamouflageSlot(int slot) {
        return slot >= firstCamouflageSlot && slot < firstCamouflageSlot + numCamouflageSlots;
    }

    @Override
    protected void onInventoryChanged(int slot) {
        //System.out.printf("SGBaseTE.onInventoryChanged: %s\n", slot);
        super.onInventoryChanged(slot);
        if (isCamouflageSlot(slot)) {
            //System.out.printf("SGBaseTE.onInventoryChanged: Camouflage slot changed\n");
            for (int dx = -2; dx <= 2; dx++)
                for (int dz = -2; dz <= 2; dz++)
                    BaseBlockUtils.markBlockForUpdate(world, pos.add(dx, 0, dz));
        }
    }

    protected int baseCornerCamouflage() {
        return max(baseCamouflageAt(0), baseCamouflageAt(4));
    }

    @SuppressWarnings("deprecation")
    protected int baseCamouflageAt(int i) {
        ItemStack stack = getStackInSlot(i);
        if (stack != null) {
            Item item = stack.getItem();
            Block block = Block.getBlockFromItem(stack.getItem());
            if (block != Blocks.AIR) {
                if (block instanceof BlockSlab)
                    return 1;
                int meta = item.getMetadata(stack);
                IBlockState state = block.getStateFromMeta(meta);
                if (state.isFullCube()) {
                    return 2;
                }
            }
        }
        return 0;
    }

    static int rdx[] = {1, 0, -1, 0};
    static int rdz[] = {0, -1, 0, 1};

    // Find locations of tile entities that could connect to the stargate ring.
    public Collection<TileEntity> adjacentTiles() {
        Collection<TileEntity> result = new ArrayList<>();
        Trans3 t = localToGlobalTransformation();
        for (int i = -2; i <= 2; i++) {
            BlockPos bp = t.p(i, -1, 0).blockPos();
            TileEntity te = getWorldTileEntity(world, bp);
            if (te != null)
                result.add(te);
        }
        return result;
    }

    //------------------------------------ Computer interface ----------------------------------

    public void forwardNetworkPacket(Object packet) {
        SGBaseTE dte = getConnectedStargateTE();
        if (dte != null)
            dte.rebroadcastNetworkPacket(packet);
    }

    void rebroadcastNetworkPacket(Object packet) {
        for (TileEntity te : adjacentTiles()) {
            if (te instanceof SGInterfaceTE)
                ((SGInterfaceTE)te).rebroadcastNetworkPacket(packet);
        }
    }

    public String sendMessage(Object[] args) {
        SGBaseTE dte = getConnectedStargateTE();
        if (dte != null) {
            dte.postEvent("sgMessageReceived", args);
            return null;
        }
        else
            return "Stargate not connected";
    }

    void postEvent(String name, Object... args) {
        for (TileEntity te : adjacentTiles()) {
            if (te instanceof IComputerInterface) {
                ((IComputerInterface)te).postEvent(this, name, args);
            }
        }
        this.debugCCInterface = false;
    }

    public String sgStateDescription() {
        return sgStateDescription(state);
    }

    String sgStateDescription(SGState state) {
        switch (state) {
            case Idle: return "Idle";
            case Dialling:
            case InterDialling: return "Dialling";
            case SyncAwait:
            case Transient: return "Opening";
            case Connected: return "Connected";
            case Disconnecting: return "Closing";
            default: return "Unknown";
        }
    }

    public String irisStateDescription() {
        return irisStateDescription(irisState);
    }

    static String irisStateDescription(IrisState state) {
        return state.toString();
    }

    public static SGBaseTE getBaseTE(SGInterfaceTE ite) {
        return SGBaseTE.get(ite.getWorld(), ite.getPos().add(0, 1, 0));
    }

    public double getMaxEnergyBuffer() {
        return this.maxEnergyBuffer;
    }

    public double getBaseMaxEnergyBuffer() {
        return this.maxEnergyBuffer;
    }
}