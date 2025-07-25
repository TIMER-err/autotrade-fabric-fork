package com.github.timererr.autotrade.config;

import com.github.timererr.autotrade.Reference;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.IConfigValue;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigString;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import java.io.File;

public class Configs implements IConfigHandler {
	private static final String CONFIG_FILE_NAME = Reference.MOD_ID + ".json";

	public static class Generic {
		public static final ConfigBoolean ENABLED = new ConfigBoolean("autotrade.config.configs.enabled.name", false,
				"autotrade.config.configs.enabled.comment");
		public static final ConfigBoolean ENABLE_SELL = new ConfigBoolean("autotrade.config.configs.enablesell.name", false,
				"autotrade.config.configs.enablesell.comment");
		public static final ConfigString SELL_ITEM = new ConfigString("autotrade.config.configs.sellitem.name", "minecraft:gold_ingot",
				"autotrade.config.configs.sellitem.comment");
		public static final ConfigInteger SELL_LIMIT = new ConfigInteger("autotrade.config.configs.selllimit.name", 64, 1, 64,
				"autotrade.config.configs.selllimit.comment");
		public static final ConfigBoolean ENABLE_BUY = new ConfigBoolean("autotrade.config.configs.enablebuy.name", false,
				"autotrade.config.configs.enablebuy.comment");
		public static final ConfigString BUY_ITEM = new ConfigString("autotrade.config.configs.buyitem.name", "minecraft:redstone",
				"autotrade.config.configs.buyitem.comment");
		public static final ConfigInteger BUY_LIMIT = new ConfigInteger("autotrade.config.configs.buylimit.name", 64, 1, 64, "autotrade.config.configs.buylimit.comment");
		public static final ConfigInteger MAX_INPUT_ITEMS = new ConfigInteger("autotrade.config.configs.maxinputstacks.name", 9, 1, 35,
				"autotrade.config.configs.maxinputstacks.comment");
		public static final ConfigInteger INPUT_CONTAINER_X = new ConfigInteger("autotrade.config.configs.inputcontainerx.name", 0, -30000000,
				30000000, "autotrade.config.configs.inputcontainerx.comment");
		public static final ConfigInteger INPUT_CONTAINER_Y = new ConfigInteger("autotrade.config.configs.inputcontainery.name", 0, -64, 320,
				"autotrade.config.configs.inputcontainery.comment");
		public static final ConfigInteger INPUT_CONTAINER_Z = new ConfigInteger("autotrade.config.configs.inputcontainerz.name", 0, -30000000,
				30000000, "autotrade.config.configs.inputcontainerz.comment");
		public static final ConfigInteger OUTPUT_CONTAINER_X = new ConfigInteger("autotrade.config.configs.outputcontainerx.name", 0, -30000000,
				30000000, "autotrade.config.configs.outputcontainerx.comment");
		public static final ConfigInteger OUTPUT_CONTAINER_Y = new ConfigInteger("autotrade.config.configs.outputcontainery.name", 0, -64, 320,
				"autotrade.config.configs.outputcontainery.comment");
		public static final ConfigInteger OUTPUT_CONTAINER_Z = new ConfigInteger("autotrade.config.configs.outputcontainerz.name", 0, -30000000,
				30000000, "autotrade.config.configs.outputcontainerz.comment");
		public static final ConfigInteger VOID_TRADING_DELAY = new ConfigInteger("autotrade.config.configs.voidtradingdelay.name", 0, 0, 30000000,
				"autotrade.config.configs.voidtradingdelay.comment");
		public static final ConfigBoolean VOID_TRADING_DELAY_AFTER_TELEPORT = new ConfigBoolean("autotrade.config.configs.delayafterteleport.name",
				true,
				"autotrade.config.configs.delayafterteleport.comment");
		public static final ConfigInteger CONTAINER_CLOSE_DELAY = new ConfigInteger("autotrade.config.configs.containerclosedelay.name", 0, 0,
				30000000, "autotrade.config.configs.containerclosedelay.comment");

		public static final ImmutableList<IConfigValue> OPTIONS = ImmutableList.of(ENABLED,
				ENABLE_SELL, SELL_ITEM, SELL_LIMIT, ENABLE_BUY, BUY_ITEM, BUY_LIMIT, MAX_INPUT_ITEMS,
				INPUT_CONTAINER_X, INPUT_CONTAINER_Y, INPUT_CONTAINER_Z, OUTPUT_CONTAINER_X, OUTPUT_CONTAINER_Y,
				OUTPUT_CONTAINER_Z, VOID_TRADING_DELAY, VOID_TRADING_DELAY_AFTER_TELEPORT, CONTAINER_CLOSE_DELAY);
	}

	public static void loadFromFile() {
		File configFile = new File(FileUtils.getConfigDirectoryAsPath().toFile(), CONFIG_FILE_NAME);

		if (configFile.exists() && configFile.isFile() && configFile.canRead()) {
			JsonElement element = JsonUtils.parseJsonFile(configFile);

			if (element != null && element.isJsonObject()) {
				JsonObject root = element.getAsJsonObject();

				ConfigUtils.readConfigBase(root, "Generic", Generic.OPTIONS);
				ConfigUtils.readConfigBase(root, "Hotkeys", Hotkeys.HOTKEY_LIST);
			}
		}
	}

	public static void saveToFile() {
		File dir = FileUtils.getConfigDirectoryAsPath().toFile();

		if ((dir.exists() && dir.isDirectory()) || dir.mkdirs()) {
			JsonObject root = new JsonObject();

			ConfigUtils.writeConfigBase(root, "Generic", Generic.OPTIONS);
			ConfigUtils.writeConfigBase(root, "Hotkeys", Hotkeys.HOTKEY_LIST);

			JsonUtils.writeJsonToFile(root, new File(dir, CONFIG_FILE_NAME));
		}
	}

	@Override
	public void load() {
		loadFromFile();
	}

	@Override
	public void save() {
		saveToFile();
	}
}
