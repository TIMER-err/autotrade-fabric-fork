package com.github.timererr.autotrade.compat.modmenu;

import com.github.timererr.autotrade.gui.GuiConfigs;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuImpl implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return (screen) -> {
			GuiConfigs gui = new GuiConfigs();
			gui.setParent(screen);
			return gui;
		};
	}
}
