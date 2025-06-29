package com.luckyzyx.jadx.plugins

import jadx.api.plugins.options.impl.BasePluginOptionsBuilder

class JadxPluginOptions : BasePluginOptionsBuilder() {

	var isEnable: Boolean = true
	var addMethodClass: Boolean = true
	var addFieldClass: Boolean = true
	var addModifiers: Boolean = true

	override fun registerOptions() {
		boolOption(JadxPluginInfo.PLUGIN_ID + ".enable")
			.description("启用插件 | Enable Plugin")
			.defaultValue(true)
			.setter { v: Boolean -> isEnable = v }

		boolOption(JadxPluginInfo.PLUGIN_ID + ".method.add.class")
			.description("复制Method时添加Class | Add Class when copying Method")
			.defaultValue(true)
			.setter { v: Boolean -> addMethodClass = v }

		boolOption(JadxPluginInfo.PLUGIN_ID + ".field.add.class")
			.description("复制Field时添加Class | Add Class when copying Field")
			.defaultValue(true)
			.setter { v: Boolean -> addFieldClass = v }

		boolOption(JadxPluginInfo.PLUGIN_ID + ".add.modifiers")
			.description("复制Member时添加Modifiers | Add Modifiers when copying Member")
			.defaultValue(true)
			.setter { v: Boolean -> addModifiers = v }
	}
}
