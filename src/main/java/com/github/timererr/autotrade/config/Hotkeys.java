package com.github.timererr.autotrade.config;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import java.util.List;

public class Hotkeys {
	public static final ConfigHotkey TOGGLE_KEY = new ConfigHotkey("autotrade.config.hotkeys.toggletrading.name", "",
			"autotrade.config.hotkeys.toggletrading.comment");
	public static final ConfigHotkey SET_SELL_KEY = new ConfigHotkey("autotrade.config.hotkeys.setsellitem.name", "",
			"autotrade.config.hotkeys.setsellitem.comment");
	public static final ConfigHotkey SET_BUY_KEY = new ConfigHotkey("autotrade.config.hotkeys.setbuyitem.name", "",
			"autotrade.config.hotkeys.setbuyitem.comment");
	public static final ConfigHotkey SET_INPUT_KEY = new ConfigHotkey("autotrade.config.hotkeys.setinputcontainer.name", "",
			"autotrade.config.hotkeys.setinputcontainer.comment");
	public static final ConfigHotkey SET_OUTPUT_KEY = new ConfigHotkey("autotrade.config.hotkeys.setoutputcontainer.name", "",
			"autotrade.config.hotkeys.setoutputcontainer.comment");
	public static final ConfigHotkey OPEN_GUI_SETTINGS = new ConfigHotkey("autotrade.config.hotkeys.openguisettings.name", "RIGHT_SHIFT,T",
			"autotrade.config.hotkeys.openguisettings.comment");

	public static final List<ConfigHotkey> HOTKEY_LIST = ImmutableList.of(TOGGLE_KEY, SET_SELL_KEY, SET_BUY_KEY,
			SET_INPUT_KEY, SET_OUTPUT_KEY, OPEN_GUI_SETTINGS);
}
