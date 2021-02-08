package com.zarbosoft.merman.standalone;

import com.zarbosoft.merman.editor.ClipboardEngine;
import com.zarbosoft.merman.syntax.BackType;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class SimpleClipboardEngine extends ClipboardEngine {
	private final DataFormat dataFormat;
	Clipboard clipboard = Clipboard.getSystemClipboard();
	public final BackType backType;

	public SimpleClipboardEngine(BackType backType) {
		this.backType = backType;
		dataFormat = new DataFormat(backType.mime());
	}

	@Override
	public void set(final Object bytes) {
		final ClipboardContent content = new ClipboardContent();
		content.put(dataFormat, bytes);
		content.putString(new String((byte[])bytes, StandardCharsets.UTF_8));
		clipboard.setContent(content);
	}

	@Override
	public void setString(final String string) {
		final ClipboardContent content = new ClipboardContent();
		content.putString(string);
		clipboard.setContent(content);
	}

	@Override
	public void get(Consumer<Object> cb) {
		byte[] out = (byte[]) clipboard.getContent(dataFormat);
		if (out == null) {
			final String temp = clipboard.getString();
			if (temp != null) {
				out = temp.getBytes(StandardCharsets.UTF_8);
			}
		}
		cb.accept(out);
	}

	@Override
	public void getString(Consumer<String> cb) {
		return clipboard.getString();
	}
}
