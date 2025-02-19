package com.luckyzyx.jadx.plugins;

import jadx.api.JadxDecompiler;
import jadx.api.metadata.ICodeNodeRef;
import jadx.api.plugins.gui.JadxGuiContext;

import java.util.function.Consumer;

public class CodeGenerator implements Consumer<ICodeNodeRef> {

	private final JadxGuiContext guiContext;
	private final JadxDecompiler decompiler;
	private final JadxPluginOptions options;

	public CodeGenerator(JadxGuiContext guiContext, JadxDecompiler decompiler, JadxPluginOptions options) {
		this.guiContext = guiContext;
		this.decompiler = decompiler;
		this.options = options;
	}

	@Override
	public void accept(ICodeNodeRef iCodeNodeRef) {

	}

}
