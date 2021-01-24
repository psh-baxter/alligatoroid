package com.zarbosoft.merman.editor.visual;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Hoverable;
import com.zarbosoft.merman.editor.visual.tags.TagsChange;
import com.zarbosoft.merman.editor.visual.visuals.VisualAtom;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

public interface VisualLeaf {
	void getLeafPropertiesForTagsChange(
			Context context, TSList<ROPair<Brick, Brick.Properties>> brickProperties, TagsChange change
	);

	Hoverable hover(final Context context, final Vector point); // Should map to method in Visual

	VisualParent parent(); // Should map to method in Visual

	default VisualAtom atomVisual() {
		return parent().atomVisual();
	}
}
