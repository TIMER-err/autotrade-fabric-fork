package com.github.timererr.autotrade.event;

import com.github.timererr.autotrade.AutoTrade;
import com.github.timererr.autotrade.config.Configs;
import com.github.timererr.autotrade.config.Hotkeys;
import com.github.timererr.autotrade.gui.GuiConfigs;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.interfaces.IClientTickHandler;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.malilib.util.InfoUtils;

import java.util.HashMap;
import java.util.Vector;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.SelectMerchantTradeC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;

public class KeybindCallbacks implements IHotkeyCallback, IClientTickHandler {
    private static final KeybindCallbacks INSTANCE = new KeybindCallbacks();

    private Vector<Entity> villagersInRange = new Vector<Entity>();
    private int villagerActive = 0;

    private boolean state = false;
    private boolean inputInRange = false;
    private boolean inputOpened = false;
    private boolean outputInRange = false;
    private boolean outputOpened = false;
    private int tickCount = 0;
    private int voidDelay = 0;
    private int containerDelay = 0;

    public static KeybindCallbacks getInstance() {
        return INSTANCE;
    }

    private KeybindCallbacks() {
    }

    public void setCallbacks() {
        for (ConfigHotkey hotkey : Hotkeys.HOTKEY_LIST) {
            hotkey.getKeybind().setCallback(this);
        }
    }

    public boolean functionalityEnabled() {
        return Configs.Generic.ENABLED.getBooleanValue();
    }

    @Override
    public boolean onKeyAction(KeyAction action, IKeybind key) {
        return this.onKeyActionImpl(action, key);
    }

    private void processOutput(ScreenHandler handler) {
        outputOpened = false;

        String itemToPlace = "minecraft:emerald";
        if (Configs.Generic.ENABLE_BUY.getBooleanValue()) {
            itemToPlace = Configs.Generic.BUY_ITEM.getStringValue();
        }

        for (int i = 0; i < handler.slots.size(); i++) {
            if (handler.getSlot(i).inventory instanceof PlayerInventory) {
                if (Registries.ITEM.getId(handler.getSlot(i).getStack().getItem()).toString().equals(itemToPlace)) {
                    try {
                        MinecraftClient.getInstance().interactionManager.clickSlot(handler.syncId,
                                handler.getSlot(i).id, 0, SlotActionType.QUICK_MOVE,
                                MinecraftClient.getInstance().player);
                    } catch (Exception e) {
                        System.out.println("err " + e);
                    }
                }
            }
        }

    }

    private void processInput(ScreenHandler handler) {
        inputOpened = false;

        HashMap<String, Integer> inventory = new HashMap<String, Integer>();

        for (int i = 0; i < handler.slots.size(); i++) {
            if (handler.getSlot(i).inventory instanceof PlayerInventory) {
                inventory.put(Registries.ITEM.getId(handler.getSlot(i).getStack().getItem()).toString(),
                        handler.getSlot(i).getStack().getCount() + inventory.getOrDefault(
                                Registries.ITEM.getId(handler.getSlot(i).getStack().getItem()).toString(), 0));
            }
        }

        String itemToTake = "minecraft:emerald";
        if (Configs.Generic.ENABLE_SELL.getBooleanValue()) {
            itemToTake = Configs.Generic.SELL_ITEM.getStringValue();
        }

        int inputCount = inventory.getOrDefault(itemToTake, 0);

        for (int i = 0; i < handler.slots.size(); i++) {
            if (!(handler.getSlot(i).inventory instanceof PlayerInventory)) {
                if (Registries.ITEM.getId(handler.getSlot(i).getStack().getItem()).toString().equals(itemToTake)) {
                    if (inputCount < (Configs.Generic.MAX_INPUT_ITEMS.getIntegerValue() * 64)) {
                        inputCount += handler.getSlot(i).getStack().getCount();
                        try {
                            MinecraftClient.getInstance().interactionManager.clickSlot(handler.syncId,
                                    handler.getSlot(i).id, 0, SlotActionType.QUICK_MOVE,
                                    MinecraftClient.getInstance().player);
                        } catch (Exception e) {
                            System.out.println("err " + e);
                        }
                    }
                }
            }
        }

    }

    private boolean onKeyActionImpl(KeyAction action, IKeybind key) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player == null || mc.world == null) {
            return false;
        }

        if (key == Hotkeys.TOGGLE_KEY.getKeybind()) {
            Configs.Generic.ENABLED.toggleBooleanValue();
            String msg = this.functionalityEnabled()
                    ? "autotrade.message.toggled_mod_on"
                    : "autotrade.message.toggled_mod_off";
            InfoUtils.showGuiOrInGameMessage(Message.MessageType.INFO, msg);
            if (this.functionalityEnabled()) {
                AutoTrade.sold = 0;
                AutoTrade.bought = 0;
                AutoTrade.sessionStart = System.currentTimeMillis() / 1000L;
            }
        } else if (key == Hotkeys.OPEN_GUI_SETTINGS.getKeybind()) {
            GuiBase.openGui(new GuiConfigs());
            return true;
        } else if (key == Hotkeys.SET_INPUT_KEY.getKeybind()) {
            HitResult result = mc.player.raycast(20.0D, 0.0F, false);
            if (result.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult) result;
                Configs.Generic.INPUT_CONTAINER_X.setIntegerValue(blockHit.getBlockPos().getX());
                Configs.Generic.INPUT_CONTAINER_Y.setIntegerValue(blockHit.getBlockPos().getY());
                Configs.Generic.INPUT_CONTAINER_Z.setIntegerValue(blockHit.getBlockPos().getZ());
                InfoUtils.showGuiOrInGameMessage(Message.MessageType.INFO, "autotrade.message.input_container_set",
                        blockHit.getBlockPos().getX(), blockHit.getBlockPos().getY(), blockHit.getBlockPos().getZ());
            }
        } else if (key == Hotkeys.SET_OUTPUT_KEY.getKeybind()) {
            HitResult result = mc.player.raycast(20.0D, 0.0F, false);
            if (result.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult) result;
                Configs.Generic.OUTPUT_CONTAINER_X.setIntegerValue(blockHit.getBlockPos().getX());
                Configs.Generic.OUTPUT_CONTAINER_Y.setIntegerValue(blockHit.getBlockPos().getY());
                Configs.Generic.OUTPUT_CONTAINER_Z.setIntegerValue(blockHit.getBlockPos().getZ());
                InfoUtils.showGuiOrInGameMessage(Message.MessageType.INFO, "autotrade.message.output_container_set",
                        blockHit.getBlockPos().getX(), blockHit.getBlockPos().getY(), blockHit.getBlockPos().getZ());
            }
        } else if (key == Hotkeys.SET_BUY_KEY.getKeybind()) {
            String buyItem = Registries.ITEM.getId(mc.player.getInventory().getSelectedStack().getItem()).toString();
            InfoUtils.showGuiOrInGameMessage(Message.MessageType.INFO, "autotrade.message.buy_item_set", buyItem);
            Configs.Generic.BUY_ITEM.setValueFromString(buyItem);
        } else if (key == Hotkeys.SET_SELL_KEY.getKeybind()) {

            String sellItem = Registries.ITEM.getId(mc.player.getInventory().getSelectedStack().getItem()).toString();
            InfoUtils.showGuiOrInGameMessage(Message.MessageType.INFO, "autotrade.message.sell_item_set", sellItem);
            Configs.Generic.SELL_ITEM.setValueFromString(sellItem);
        }

        return false;
    }

    @Override
    public void onClientTick(MinecraftClient mc) {
        if (mc.player == null) return;
        if (voidDelay > 0) {

            if (Configs.Generic.VOID_TRADING_DELAY_AFTER_TELEPORT.getBooleanValue()) {
                boolean found = false;
                for (Entity entity : mc.player.clientWorld.getEntities()) {
                    if (entity.getId() == villagerActive) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    voidDelay--;
                }
            } else {
                voidDelay--;
            }

            return;
        }

        if (containerDelay > 0) {

            containerDelay--;
        }

        if (!this.functionalityEnabled()) {
            return;
        }

        if (GuiUtils.getCurrentScreen() instanceof MerchantScreen screen) {
            if (!state) {
                String sellItemStr = Configs.Generic.SELL_ITEM.getStringValue();
                String buyItemStr = Configs.Generic.BUY_ITEM.getStringValue();
                state = true;
                MerchantScreenHandler handler = screen.getScreenHandler();
                TradeOfferList offers = handler.getRecipes();
                for (int i = 0; i < offers.size(); i++) {
                    TradeOffer offer = offers.get(i);
                    ItemStack sellItem = offer.getSellItem();//todo sellItem getter
                    ItemStack buyItem = offer.getFirstBuyItem().itemStack();//todo buyItem getter
                    String sellId = Registries.ITEM.getId(sellItem.getItem()).toString();
                    String buyId = Registries.ITEM.getId(buyItem.getItem()).toString();

                    if (sellId.equals(buyItemStr) && Configs.Generic.ENABLE_BUY.getBooleanValue()
                            && buyItem.getCount() <= Configs.Generic.BUY_LIMIT.getIntegerValue()) {
                        Slot slot = handler.getSlot(2);
                        handler.switchTo(i);
                        handler.setRecipeIndex(i);
                        mc.getNetworkHandler().sendPacket(new SelectMerchantTradeC2SPacket(i));
                        AutoTrade.sold += offer.getMaxUses();
                        try {
                            /*
                             * if (slot.hasStack()) { System.out.println("buy " +
                             * slot.getStack().getCount()); }
                             */
                            mc.interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE,
                                    mc.player);
                        } catch (Exception e) {
                            System.out.println("err " + e);
                        }
                    }
                    if (buyId.equals(sellItemStr) && Configs.Generic.ENABLE_SELL.getBooleanValue()
                            && buyItem.getCount() <= Configs.Generic.SELL_LIMIT.getIntegerValue()) {
                        Slot slot = handler.getSlot(2);
                        handler.switchTo(i);
                        handler.setRecipeIndex(i);
                        AutoTrade.bought += offer.getMaxUses();
                        mc.getNetworkHandler().sendPacket(new SelectMerchantTradeC2SPacket(i));
                        try {
                            /*
                             * if (slot.hasStack()) { System.out.println("sell " +
                             * slot.getStack().getCount()); }
                             */
                            mc.interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE,
                                    mc.player);
                        } catch (Exception e) {
                            System.out.println("err " + e);
                        }
                    }
                }
            }
            screen.close();
            inputInRange = false;
            outputInRange = false;
            return;
        }

        if (GuiUtils.getCurrentScreen() instanceof ShulkerBoxScreen screen) {
            ShulkerBoxScreenHandler handler = screen.getScreenHandler();
            if ((containerDelay == 0) && inputOpened) {
                processInput(handler);
                screen.close();
            }
            if ((containerDelay == 0) && outputOpened) {
                processOutput(handler);
                screen.close();
            }
        }
        if (GuiUtils.getCurrentScreen() instanceof GenericContainerScreen screen) {
            GenericContainerScreenHandler handler = screen.getScreenHandler();
            if ((containerDelay == 0) && inputOpened) {
                processInput(handler);
                screen.close();
            }
            if ((containerDelay == 0) && outputOpened) {
                processOutput(handler);
                screen.close();
            }
        }

        boolean found = false;

        Vector<Entity> newVillagersInRange = new Vector<Entity>(villagersInRange);

        for (Entity entity : mc.player.clientWorld.getEntities()) {
            if (entity instanceof VillagerEntity || entity instanceof WanderingTraderEntity) {
                if (entity.getPos().distanceTo(mc.player.getPos()) < 2.5f) {
                    if (!found) {
                        if (!newVillagersInRange.contains(entity)) {
                            found = true;
                            newVillagersInRange.add(entity);
                            mc.interactionManager.interactEntity(mc.player, entity, Hand.MAIN_HAND);
                            voidDelay = Configs.Generic.VOID_TRADING_DELAY.getIntegerValue();
                            villagerActive = entity.getId();
                            state = false;
                            break;
                        }
                    }
                }
            }
        }

        for (Entity entity : villagersInRange) {
            if (!(entity.getPos().distanceTo(mc.player.getPos()) < 4)) {
                newVillagersInRange.remove(entity);
            }
        }
        villagersInRange = newVillagersInRange;
        if (found) {
            return;
        }

        BlockPos input = new BlockPos(Configs.Generic.INPUT_CONTAINER_X.getIntegerValue(),
                Configs.Generic.INPUT_CONTAINER_Y.getIntegerValue(),
                Configs.Generic.INPUT_CONTAINER_Z.getIntegerValue());

        BlockPos output = new BlockPos(Configs.Generic.OUTPUT_CONTAINER_X.getIntegerValue(),
                Configs.Generic.OUTPUT_CONTAINER_Y.getIntegerValue(),
                Configs.Generic.OUTPUT_CONTAINER_Z.getIntegerValue());

        if ((input.toCenterPos().distanceTo(mc.player.getPos()) < 4) && (!inputInRange)) {
            inputInRange = true;
            ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                    new BlockHitResult(input.toCenterPos(), Direction.UP, input, false));
            containerDelay = Configs.Generic.CONTAINER_CLOSE_DELAY.getIntegerValue();
            inputOpened = true;
            return;
        }
        if ((output.toCenterPos().distanceTo(mc.player.getPos()) < 4) && (!outputInRange)) {
            outputInRange = true;
            ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                    new BlockHitResult(output.toCenterPos(), Direction.UP, output, false));
            containerDelay = Configs.Generic.CONTAINER_CLOSE_DELAY.getIntegerValue();
            outputOpened = true;
            return;
        }

        if (input.toCenterPos().distanceTo(mc.player.getPos()) > 5) {
            inputOpened = false;
            inputInRange = false;
        }
        if (output.toCenterPos().distanceTo(mc.player.getPos()) > 5) {
            outputOpened = false;
            outputInRange = false;
        }

        tickCount++;
        if (tickCount > 200) {
            tickCount = 0;
            villagersInRange = new Vector<Entity>();
            inputInRange = false;
            outputInRange = false;
            if (GuiUtils.getCurrentScreen() instanceof MerchantScreen) {
                GuiUtils.getCurrentScreen().close();
            }
            if (GuiUtils.getCurrentScreen() instanceof ShulkerBoxScreen) {
                GuiUtils.getCurrentScreen().close();
            }
            if (GuiUtils.getCurrentScreen() instanceof GenericContainerScreen) {
                GuiUtils.getCurrentScreen().close();
            }
        }

    }
}
