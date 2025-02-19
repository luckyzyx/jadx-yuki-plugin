package com.luckyzyx.jadx.plugins;

import jadx.api.plugins.options.impl.BasePluginOptionsBuilder;

public class JadxPluginOptions extends BasePluginOptionsBuilder {

	private boolean enable;

	@Override
	public void registerOptions() {
		boolOption(JadxPluginInfo.PLUGIN_ID + ".enable")
				.description("启用插件")
				.defaultValue(true)
				.setter(v -> enable = v);
	}

	public boolean isEnable() {
		return enable;
	}
}
