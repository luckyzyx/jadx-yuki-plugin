package com.luckyzyx.jadx.plugins

import jadx.api.plugins.options.impl.BasePluginOptionsBuilder

class JadxPluginSettings : BasePluginOptionsBuilder() {

	var isEnable: Boolean = false

	override fun registerOptions() {
		boolOption(JadxPluginInfo.PLUGIN_ID + ".enable")
			.description("启用插件")
			.defaultValue(true)
			.setter { v: Boolean -> isEnable = v }
	}
}
