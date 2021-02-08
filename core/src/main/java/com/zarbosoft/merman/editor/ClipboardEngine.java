package com.zarbosoft.merman.editor;

import java.util.function.Consumer;

/**
 * Raw data type depends on environment.  In JS string, bytes in most other environments probably.
 */
public abstract class ClipboardEngine {
	public abstract void set(Object bytes);

	public abstract void setString(String string);

	public abstract void get(Consumer<Object> cb);

	public abstract void getString(Consumer<String> cb);
}
