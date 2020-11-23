package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.syntax.back.BackFixedArraySpec;
import com.zarbosoft.merman.syntax.back.BackSpec;

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
