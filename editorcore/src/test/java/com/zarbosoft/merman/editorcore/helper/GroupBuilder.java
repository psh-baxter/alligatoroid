package com.zarbosoft.merman.editorcore.helper;

import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.merman.syntax.FreeAtomType;

public class GroupBuilder {
	TSList<String> subtypes = new TSList<>();

	public GroupBuilder type(final FreeAtomType type) {
		subtypes.add(type.id());
		return this;
	}

	public ROList<String> build() {
		return subtypes;
	}

	public GroupBuilder group(final String group) {
		subtypes.add(group);
		return this;
	}
}
