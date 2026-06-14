package de.luckymcdev.foundryengine.common.event;

import de.luckymcdev.foundryengine.api.event.registry.RegistryEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppedEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;

public class BlueprintContexts {

    public static Map<String, Object> beginPlay() {
        var ctx = new HashMap<String, Object>();
        return ctx;
    }

    public static Map<String, Object> serverTick(ServerTickEvent.Post event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Server", event.getServer());
        ctx.put("CommandSource", event.getServer().createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> serverAboutToStart(ServerAboutToStartEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Server", event.getServer());
        ctx.put("CommandSource", event.getServer().createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> serverStarted(ServerStartedEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Server", event.getServer());
        ctx.put("CommandSource", event.getServer().createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> serverStarting(ServerStartingEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Server", event.getServer());
        ctx.put("CommandSource", event.getServer().createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> serverStopped(ServerStoppedEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Server", event.getServer());
        ctx.put("CommandSource", event.getServer().createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> serverStopping(ServerStoppingEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Server", event.getServer());
        ctx.put("CommandSource", event.getServer().createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> serverTags(TagsUpdatedEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("LookupProvider", event.getLookupProvider());
        ctx.put("UpdateCause", event.getUpdateCause());
        return ctx;
    }

    public static Map<String, Object> clientTick(ClientTickEvent.Post event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Client", Minecraft.getInstance());
        return ctx;
    }

    public static Map<String, Object> clientStopped(ClientStoppedEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Client", Minecraft.getInstance());
        return ctx;
    }

    public static Map<String, Object> clientStopping(ClientStoppingEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Client", Minecraft.getInstance());
        return ctx;
    }

    public static Map<String, Object> clientChat(ClientChatEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Message", event.getMessage());
        ctx.put("OriginalMessage", event.getOriginalMessage());
        return ctx;
    }

    public static Map<String, Object> clientLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Player", event.getPlayer());
        ctx.put("Connection", event.getConnection());
        return ctx;
    }

    public static Map<String, Object> clientLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Player", event.getPlayer());
        return ctx;
    }

    public static Map<String, Object> renderGui(RenderGuiEvent.Post event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("PartialTicks", event.getPartialTick());
        return ctx;
    }

    public static Map<String, Object> blockBroken(BreakBlockEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Level", event.getLevel());
        ctx.put("Player", event.getPlayer());
        ctx.put("Pos", Vec3.atCenterOf(event.getPos()));
        ctx.put("BlockState", event.getState());
        if (event.getPlayer() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> blockPlaced(BlockEvent.EntityPlaceEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Level", event.getLevel());
        ctx.put("Entity", event.getEntity());
        ctx.put("Pos", Vec3.atCenterOf(event.getPos()));
        ctx.put("BlockState", event.getState());
        if (event.getEntity() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> blockLeftClicked(PlayerInteractEvent.LeftClickBlock event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Level", event.getLevel());
        ctx.put("Entity", event.getEntity());
        ctx.put("Pos", Vec3.atCenterOf(event.getPos()));
        ctx.put("Direction", event.getFace());
        ctx.put("Action", event.getAction());
        if (event.getEntity() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> blockRightClicked(PlayerInteractEvent.RightClickBlock event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Level", event.getLevel());
        ctx.put("Entity", event.getEntity());
        ctx.put("Pos", Vec3.atCenterOf(event.getPos()));
        ctx.put("Direction", event.getFace());
        if (event.getEntity() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> blockFarmlandTrampled(BlockEvent.FarmlandTrampleEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Level", event.getLevel());
        ctx.put("Entity", event.getEntity());
        ctx.put("Pos", Vec3.atCenterOf(event.getPos()));
        ctx.put("FallDistance", event.getFallDistance());
        if (event.getEntity() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Player", event.getEntity());
        if (event.getEntity() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Player", event.getEntity());
        if (event.getEntity() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> playerTick(PlayerTickEvent.Post event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Player", event.getEntity());
        if (event.getEntity() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> playerChat(ServerChatEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Player", event.getPlayer());
        ctx.put("Username", event.getUsername());
        ctx.put("Message", event.getMessage());
        ctx.put("RawText", event.getRawText());
        ctx.put("CommandSource", event.getPlayer().createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> playerAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Player", event.getEntity());
        ctx.put("Advancement", event.getAdvancement());
        if (event.getEntity() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> chestClosed(PlayerContainerEvent.Close event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Player", event.getEntity());
        ctx.put("Container", event.getContainer());
        if (event.getEntity() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> chestOpened(PlayerContainerEvent.Open event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Player", event.getEntity());
        ctx.put("Container", event.getContainer());
        if (event.getEntity() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> playerRespawned(PlayerEvent.PlayerRespawnEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Player", event.getEntity());
        if (event.getEntity() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> decorateChat(ClientChatReceivedEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Message", event.getMessage());
        return ctx;
    }

    public static Map<String, Object> levelLoad(LevelEvent.Load event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Level", event.getLevel());
        return ctx;
    }

    public static Map<String, Object> levelUnload(LevelEvent.Unload event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Level", event.getLevel());
        return ctx;
    }

    public static Map<String, Object> levelSave(LevelEvent.Save event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Level", event.getLevel());
        return ctx;
    }

    public static Map<String, Object> levelTick(LevelTickEvent.Post event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Level", event.getLevel());
        return ctx;
    }

    public static Map<String, Object> beforeExplosion(ExplosionEvent.Start event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Level", event.getLevel());
        ctx.put("Explosion", event.getExplosion());
        return ctx;
    }

    public static Map<String, Object> afterExplosion(ExplosionEvent.Detonate event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Level", event.getLevel());
        ctx.put("Explosion", event.getExplosion());
        return ctx;
    }

    public static Map<String, Object> itemPickedUp(ItemEntityPickupEvent.Post event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Player", event.getPlayer());
        ctx.put("CurrentStack", event.getCurrentStack());
        ctx.put("OriginalStack", event.getOriginalStack());
        ctx.put("ItemEntity", event.getItemEntity());
        if (event.getPlayer() instanceof ServerPlayer player) {
            ctx.put("CommandSource", player.createCommandSourceStack());
        }
        return ctx;
    }

    public static Map<String, Object> itemDestroyed(PlayerDestroyItemEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Player", event.getEntity());
        ctx.put("ItemStack", event.getOriginal());
        ctx.put("Hand", event.getHand());
        if (event.getEntity() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> itemRightClicked(PlayerInteractEvent.RightClickItem event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Player", event.getEntity());
        ctx.put("Level", event.getLevel());
        ctx.put("Pos", Vec3.atCenterOf(event.getPos()));
        ctx.put("Hand", event.getHand());
        ctx.put("ItemStack", event.getItemStack());
        if (event.getEntity() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> itemCrafted(PlayerEvent.ItemCraftedEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Player", event.getEntity());
        ctx.put("Crafting", event.getCrafting());
        if (event.getEntity() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> itemDropped(ItemTossEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Player", event.getPlayer());
        ctx.put("ItemEntity", event.getEntity());
        if (event.getPlayer() instanceof ServerPlayer player) {
            ctx.put("CommandSource", player.createCommandSourceStack());
        }
        return ctx;
    }

    public static Map<String, Object> foodEaten(LivingEntityUseItemEvent.Finish event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("LivingEntity", event.getEntity());
        ctx.put("OriginalStack", event.getItem());
        ctx.put("ResultStack", event.getResultStack());
        ctx.put("Duration", event.getDuration());
        if (event.getEntity() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> itemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("LivingEntity", event.getEntity());
        ctx.put("ItemStack", event.getSmelting());
        ctx.put("AmountRemoved", event.getAmountRemoved());
        if (event.getEntity() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> itemTooltip(ItemTooltipEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Player", event.getEntity());
        ctx.put("TooltipContext", event.getToolTip());
        ctx.put("TooltipFlags", event.getFlags());
        ctx.put("ItemStack", event.getItemStack());
        if (event.getEntity() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> itemEntityInteract(PlayerInteractEvent.EntityInteract event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Entity", event.getEntity());
        ctx.put("Target", event.getTarget());
        ctx.put("Level", event.getLevel());
        ctx.put("ItemStack", event.getItemStack());
        if (event.getEntity() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> firstLeftClicked(PlayerInteractEvent.LeftClickEmpty event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Player", event.getEntity());
        ctx.put("Level", event.getLevel());
        ctx.put("Hand", event.getHand());
        ctx.put("ItemStack", event.getItemStack());
        if (event.getEntity() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> firstRightClicked(PlayerInteractEvent.RightClickEmpty event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Player", event.getEntity());
        ctx.put("Level", event.getLevel());
        ctx.put("Hand", event.getHand());
        ctx.put("ItemStack", event.getItemStack());
        if (event.getEntity() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        return ctx;
    }

    public static Map<String, Object> entityJoinLevel(EntityJoinLevelEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Entity", event.getEntity());
        ctx.put("Level", event.getLevel());
        if (event.getEntity() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        else if (event.getLevel() instanceof ServerLevel sl && event.getEntity() instanceof LivingEntity le)
            ctx.put("CommandSource", sl.getServer().createCommandSourceStack()
                    .withEntity(le).withPosition(le.position()).withRotation(le.getRotationVector()));
        return ctx;
    }

    public static Map<String, Object> entityDeath(LivingDeathEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Entity", event.getEntity());
        ctx.put("DamageSource", event.getSource());
        if (event.getEntity() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        else if (event.getEntity().level() instanceof ServerLevel sl && event.getEntity() instanceof LivingEntity le)
            ctx.put("CommandSource", sl.getServer().createCommandSourceStack()
                    .withEntity(le).withPosition(le.position()).withRotation(le.getRotationVector()));
        return ctx;
    }

    public static Map<String, Object> entityDrops(LivingDropsEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Entity", event.getEntity());
        ctx.put("DamageSource", event.getSource());
        if (event.getEntity() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        else if (event.getEntity().level() instanceof ServerLevel sl && event.getEntity() instanceof LivingEntity le)
            ctx.put("CommandSource", sl.getServer().createCommandSourceStack()
                    .withEntity(le).withPosition(le.position()).withRotation(le.getRotationVector()));
        return ctx;
    }

    public static Map<String, Object> entityHurt(LivingDamageEvent.Post event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Entity", event.getEntity());
        ctx.put("DamageSource", event.getSource());
        if (event.getEntity() instanceof ServerPlayer sp)
            ctx.put("CommandSource", sp.createCommandSourceStack());
        else if (event.getEntity().level() instanceof ServerLevel sl && event.getEntity() instanceof LivingEntity le)
            ctx.put("CommandSource", sl.getServer().createCommandSourceStack()
                    .withEntity(le).withPosition(le.position()).withRotation(le.getRotationVector()));
        return ctx;
    }

    public static Map<String, Object> bundleRegistry(RegistryEvent event) {
        var ctx = new HashMap<String, Object>();
        return ctx;
    }

    public static Map<String, Object> vanillaGame(VanillaGameEvent event) {
        var ctx = new HashMap<String, Object>();
        return ctx;
    }

    public static Map<String, Object> commonSetup(FMLCommonSetupEvent event) {
        var ctx = new HashMap<String, Object>();
        return ctx;
    }

    public static Map<String, Object> clientSetup(FMLClientSetupEvent event) {
        var ctx = new HashMap<String, Object>();
        return ctx;
    }

    public static Map<String, Object> dedicatedServerSetup(FMLDedicatedServerSetupEvent event) {
        var ctx = new HashMap<String, Object>();
        return ctx;
    }

    public static Map<String, Object> postInit(InterModProcessEvent event) {
        var ctx = new HashMap<String, Object>();
        return ctx;
    }

    public static Map<String, Object> recipesReceived(RecipesReceivedEvent event) {
        var ctx = new HashMap<String, Object>();
        return ctx;
    }

    public static Map<String, Object> registerCommands(RegisterCommandsEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Dispatcher", event.getDispatcher());
        return ctx;
    }

    public static Map<String, Object> registerClientCommands(RegisterClientCommandsEvent event) {
        var ctx = new HashMap<String, Object>();
        ctx.put("Dispatcher", event.getDispatcher());
        return ctx;
    }
}