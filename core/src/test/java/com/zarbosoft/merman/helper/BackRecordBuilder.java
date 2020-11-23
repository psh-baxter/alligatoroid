package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.syntax.back.BackSpec;
import com.zarbosoft.merman.syntax.back.BackFixedRecordSpec;

public class BackRecordBuilder {
	BackFixedRecordSpec back = new BackFixedRecordSpec();

	public BackRecordBuilder add(final String key, final BackSpec part) {
		back.pairs.put(key, part);
		return this;
	}

	public BackSpec build() {
		return back;
	}
}
