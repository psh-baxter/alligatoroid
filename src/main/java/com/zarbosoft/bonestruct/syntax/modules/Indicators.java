package com.zarbosoft.bonestruct.syntax.modules;

import com.zarbosoft.bonestruct.display.DisplayNode;
import com.zarbosoft.bonestruct.display.Group;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.syntax.symbol.Symbol;
import com.zarbosoft.interface1.Configuration;
import org.pcollections.HashTreePSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration(description = "Displays a row of symbols in either gutter based on tags.")
public class Indicators extends Module {
	@Configuration
	public static class Indicator {
		@Configuration(description = "Used as type tag for styling this element.")
		public String id;

		@Configuration
		public Set<Visual.Tag> tags = new HashSet<>();

		@Configuration
		public Symbol symbol;

		DisplayNode node;
	}

	@Configuration
	public List<Indicator> indicators = new ArrayList<>();

	@Configuration(optional = true, description = "If true, show the symbols in the near gutter.  Otherwise, the far.")
	public boolean converseStart = true;

	@Configuration
	public int conversePadding = 0;

	@Configuration(optional = true,
			description = "If true, show the symbols at the start of the gutter.  Otherwise, the end.")
	boolean transverseStart = true;
	@Configuration
	public int transversePadding = 0;

	private Group group;

	private final Context.ContextIntListener resizeListener = new Context.ContextIntListener() {
		@Override
		public void changed(final Context context, final int oldValue, final int newValue) {
			updatePosition(context);
		}
	};

	public void update(final Context context, final Set<Visual.Tag> tags) {
		int transverse = 0;
		int offset = 0;
		for (final Indicator indicator : indicators) {
			if (tags.containsAll(indicator.tags)) {
				DisplayNode node = indicator.node;
				if (node == null) {
					node = indicator.node = indicator.symbol.createDisplay(context);
					group.add(offset, node);
				}
				indicator.symbol.style(
						context,
						node,
						context.getStyle(HashTreePSet
								.from(tags)
								.plus(new Visual.TypeTag(indicator.id))
								.plus(new Visual.PartTag("indicator")))
				);
				node.setTransverse(context, transverse);
				transverse += node.transverseSpan(context);
				if (!converseStart) {
					node.setConverse(context, -node.converseSpan(context));
				}
				offset += 1;
			} else {
				if (indicator.node != null) {
					group.remove(offset);
					indicator.node = null;
				}
			}
		}
	}

	public void updatePosition(final Context context) {
		group.setPosition(context, new Vector(
				converseStart ? conversePadding : (context.edge - conversePadding - group.converseSpan(context)),
				transverseStart ? transversePadding : (context.edge - transversePadding)
		), false);
	}

	@Override
	public State initialize(final Context context) {
		context.addTagsChangeListener(new Context.TagsListener() {
			@Override
			public void tagsChanged(final Context context, final Set<Visual.Tag> tags) {
				update(context, tags);
			}
		});
		context.addConverseEdgeListener(resizeListener);
		context.addTransverseEdgeListener(resizeListener);
		group = context.display.group();
		context.midground.add(group);
		update(context, context.globalTags);
		updatePosition(context);
		return new State() {
			@Override
			public void destroy(final Context context) {
				context.midground.remove(group);
				context.removeConverseEdgeListener(resizeListener);
				context.removeTransverseEdgeListener(resizeListener);
			}
		};
	}
}
