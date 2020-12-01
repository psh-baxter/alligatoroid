package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.syntax.front.FrontPrimitiveSpec;

import java.util.HashSet;
import java.util.Set;

public class FrontDataPrimitiveBuilder {
	private final FrontPrimitiveSpec front;
	private final Set<String> tags = new HashSet<>();

	public FrontDataPrimitiveBuilder(final String middle) {
		this.front = new FrontPrimitiveSpec();
		front.field = middle;
		front.tags(tags);
	}

	public FrontPrimitiveSpec build() {
		return front;
	}

	public FrontDataPrimitiveBuilder tag(final String tag) {
		tags.add(tag);
		return this;
	}
}
