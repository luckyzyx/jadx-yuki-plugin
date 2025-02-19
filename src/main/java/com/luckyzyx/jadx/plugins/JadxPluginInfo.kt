package com.luckyzyx.jadx.plugins;

import jadx.api.JadxDecompiler;
import jadx.api.metadata.ICodeNodeRef;
import jadx.api.plugins.JadxPlugin;
import jadx.api.plugins.JadxPluginContext;
import jadx.api.plugins.JadxPluginInfoBuilder;
import jadx.api.plugins.gui.JadxGuiContext;

public class JadxPluginInfo implements JadxPlugin {
	public static final String PLUGIN_ID = "jadx-yuki-plugin";

	private final JadxPluginOptions options = new JadxPluginOptions();

	@Override
	public jadx.api.plugins.JadxPluginInfo getPluginInfo() {
		return JadxPluginInfoBuilder.pluginId(PLUGIN_ID)
				.name("Jadx Yuki Plugin")
				.description("为Jadx添加Yuki API支持")
				.homepage("https://github.com/luckyzyx/jadx-yuki-plugin")
				.build();
	}

	public Boolean canGen(ICodeNodeRef nodeRef) {
		// todo 检查是否可以生成代码，貌似不必判断
		return true;
	}

	@Override
	public void init(JadxPluginContext context) {
		context.registerOptions(options);
		if (options.isEnable()) {
			JadxDecompiler decompiler = context.getDecompiler();
			JadxGuiContext guiContext = context.getGuiContext();
			if (guiContext != null) {
				CodeGenerator codeGenerator = new CodeGenerator(guiContext, decompiler, options);
				guiContext.addPopupMenuAction("复制为 YukiAPI 片段", this::canGen, null, codeGenerator);
			}
		}
	}
}
