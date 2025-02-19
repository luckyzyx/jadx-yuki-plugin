package com.luckyzyx.jadx.plugins

import jadx.api.metadata.ICodeNodeRef
import jadx.api.plugins.JadxPlugin
import jadx.api.plugins.JadxPluginContext
import jadx.api.plugins.JadxPluginInfo
import jadx.api.plugins.JadxPluginInfoBuilder
import java.util.function.Function

class JadxPluginInfo : JadxPlugin {

	private val options = JadxPluginSettings()

	companion object {
		const val PLUGIN_ID: String = "jadx-yuki-plugin"
	}

	override fun getPluginInfo(): JadxPluginInfo {
		return JadxPluginInfoBuilder.pluginId(PLUGIN_ID)
			.name("Jadx YukiHookAPI Plugin")
			.description("为 Jadx 添加 YukiHookAPI 支持")
			.homepage("https://github.com/luckyzyx/jadx-yuki-plugin")
			.build()
	}

	private fun canGen(nodeRef: ICodeNodeRef?): Boolean {
		return true
	}

	override fun init(context: JadxPluginContext) {
		context.registerOptions(options)
		if (options.isEnable) {
			val decompiler = context.decompiler
			val guiContext = context.guiContext
			if (guiContext != null) {
				val codeGenerator = CodeGenerator(guiContext, decompiler)
				guiContext.addPopupMenuAction(
					"复制为 YukiHookAPI 片段",
					Function { nodeRef: ICodeNodeRef? -> this.canGen(nodeRef) }, null, codeGenerator
				)
			}
		}
	}
}
