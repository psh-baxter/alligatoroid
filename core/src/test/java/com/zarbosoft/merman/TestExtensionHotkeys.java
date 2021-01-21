package com.zarbosoft.merman;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.hid.HIDEvent;
import com.zarbosoft.merman.extensions.hotkeys.HotkeyRule;
import com.zarbosoft.merman.extensions.hotkeys.HotkeysExtension;
import com.zarbosoft.merman.extensions.hotkeys.Key;
import com.zarbosoft.merman.extensions.hotkeys.grammar.Node;
import com.zarbosoft.merman.extensions.hotkeys.grammar.Terminal;
import com.zarbosoft.merman.helper.GeneralTestWizard;
import com.zarbosoft.merman.helper.GroupBuilder;
import com.zarbosoft.merman.helper.Helper;
import com.zarbosoft.merman.helper.StyleBuilder;
import com.zarbosoft.merman.helper.SyntaxBuilder;
import com.zarbosoft.merman.helper.TreeBuilder;
import com.zarbosoft.merman.helper.TypeBuilder;
import com.zarbosoft.merman.misc.ROList;
import com.zarbosoft.merman.misc.ROSet;
import com.zarbosoft.merman.misc.TSList;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestExtensionHotkeys {
  @Test
  public void testInitialHotkeys() {
    final FreeAtomType one =
        new TypeBuilder("one").back(Helper.buildBackPrimitive("one")).frontMark("3_1").build();
    final FreeAtomType two =
        new TypeBuilder("two").back(Helper.buildBackPrimitive("two")).frontMark("3_2").build();
    final FreeAtomType arr =
        new TypeBuilder("arr")
            .back(Helper.buildBackDataArray("arr", "any"))
            .frontDataArray("arr")
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(arr)
            .group("any", new GroupBuilder().type(one).build())
            .style(new StyleBuilder().split(true).build())
            .build();
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(arr)
                .addArray("arr", new TreeBuilder(one).build(), new TreeBuilder(two).build())
                .build())
        .run(
            c -> {
              final HotkeysExtension hotkeys = new HotkeysExtension(c);
              hotkeys.rules.add(
                  new HotkeyRule(
                      ROSet.empty,
                      ROSet.empty,
                      new TSMap<String, ROList<Node>>()
                          .putChain("enter", TSList.of(new Terminal(Key.Q, true, ROSet.empty))),
                      false));
            })
        .select("value", "0")
        .sendHIDEvent(new HIDEvent(Key.Q, true, ROSet.empty))
        .checkCursorPath("value", "0", "arr", "0");
  }
}
