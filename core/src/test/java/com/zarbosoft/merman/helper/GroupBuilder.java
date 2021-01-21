package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.misc.ROList;
import com.zarbosoft.merman.misc.TSList;
import com.zarbosoft.merman.syntax.FreeAtomType;

import java.util.ArrayList;
import java.util.List;

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
