package com.zarbosoft.merman.editorcore.helper;

import com.zarbosoft.merman.core.syntax.back.BackFixedArraySpec;
import com.zarbosoft.merman.core.syntax.back.BackSpec;

public class BackArrayBuilder {
	BackFixedArraySpec back = new BackFixedArraySpec();

	public BackArrayBuilder add(final BackSpec part) {
		back.elements.add(part);
		return this;
	}

	public BackSpec build() {
		return back;
	}
}
