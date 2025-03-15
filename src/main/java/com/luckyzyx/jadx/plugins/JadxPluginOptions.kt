package com.luckyzyx.jadx.plugins

import jadx.api.plugins.options.impl.BasePluginOptionsBuilder

class JadxPluginOptions : BasePluginOptionsBuilder() {

	var isEnable: Boolean = false
	var addMethodClass: Boolean = false
	var addFieldClass: Boolean = false

	override fun registerOptions() {
		boolOption(JadxPluginInfo.PLUGIN_ID + ".enable")
			.description("启用插件")
			.defaultValue(true)
			.setter { v: Boolean -> isEnable = v }

		boolOption(JadxPluginInfo.PLUGIN_ID + ".method.add.class")
			.description("复制Method时添加Class")
			.defaultValue(true)
			.setter { v: Boolean -> addMethodClass = v }

		boolOption(JadxPluginInfo.PLUGIN_ID + ".field.add.class")
			.description("复制Field时添加Class")
			.defaultValue(true)
			.setter { v: Boolean -> addFieldClass = v }
	}
}
