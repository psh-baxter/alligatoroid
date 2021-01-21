package com.zarbosoft.merman.editorcore.editing;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.merman.syntax.front.FrontArraySpecBase;
import com.zarbosoft.merman.syntax.front.FrontAtomSpec;
import com.zarbosoft.merman.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.syntax.front.FrontSpec;
import com.zarbosoft.merman.syntax.front.FrontSymbol;
import com.zarbosoft.merman.syntax.primitivepattern.Pattern;
import com.zarbosoft.merman.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.pidgoon.Grammar;
import com.zarbosoft.pidgoon.bytes.BytesHelper;
import com.zarbosoft.pidgoon.bytes.ParseBuilder;
import com.zarbosoft.pidgoon.bytes.Position;
import com.zarbosoft.pidgoon.nodes.Color;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.pidgoon.parse.Parse;
import com.zarbosoft.rendaw.common.Common;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.zarbosoft.rendaw.common.Common.iterable;

/**
 * Represents matchable text in an atom.  All matchable text may be bordered by array/atom slots
 *
 * Collects the matchable text to make matching against entered text easier
 */
public class GapKey {
  /**
   * Index of front atom/array preceeding this key
   *
   * <p>-1 if no atom/array preceeding this key
   *
   * <p>Atoms/arrays that can't accept the element to place are skipped (-1)
   */
  public int indexBefore;
  /** Front symbols separating atoms/arrays */
  public List<FrontSpec> keyParts = new ArrayList<>();
  /** Same as Before, but After */
  public int indexAfter;

  public static List<GapKey> gapKeys(final FreeAtomType type) {
    final List<GapKey> out = new ArrayList<>();
    final Common.Mutable<GapKey> top = new Common.Mutable<>(new GapKey());
    top.value.indexBefore = -1;
    for (int frontIndex0 = 0; frontIndex0 < type.front.size(); ++frontIndex0) {
      int frontIndex = frontIndex0;
      FrontSpec front = type.front.get(frontIndex);
      front.dispatch(
          new FrontSpec.DispatchHandler() {
            @Override
            public void handle(final FrontSymbol front) {
              if (front.condition != null && !front.condition.defaultOn()) return;
              top.value.keyParts.add(front);
            }

            @Override
            public void handle(final FrontArraySpecBase front) {
              for (FrontSymbol s : front.prefix) {
                s.dispatch(this);
              }
              flush();
              if (!front.separator.isEmpty()) {
                for (FrontSymbol s : front.separator) {
                  s.dispatch(this);
                }
                flush();
              }
              for (FrontSymbol s : front.suffix) {
                s.dispatch(this);
              }
            }

            private void flush() {
              if (!top.value.keyParts.isEmpty()) {
                top.value.indexAfter = frontIndex;
                out.add(top.value);
                top.value = new GapKey();
              }
              top.value.indexBefore = frontIndex;
            }

            @Override
            public void handle(final FrontAtomSpec front) {
              flush();
            }

            @Override
            public void handle(final FrontPrimitiveSpec front) {
              top.value.keyParts.add(front);
            }

            @Override
            public void handle(final FrontGapBase front) {
              throw new DeadCode();
            }
          });
    }
    if (!top.value.keyParts.isEmpty()) {
      top.value.indexAfter = -1;
      out.add(top.value);
    }
    return out;
  }

  public com.zarbosoft.pidgoon.Node matchGrammar(final Object color) {
    final Sequence out = new Sequence();
    for (final FrontSpec part : keyParts) {
      if (part instanceof FrontSymbol) {
        String text = ((FrontSymbol) part).gapKey;
        if (((FrontSymbol) part).type instanceof SymbolTextSpec)
          text = ((SymbolTextSpec) ((FrontSymbol) part).type).text;
        out.add(BytesHelper.stringSequence(text));
      } else if (part instanceof FrontPrimitiveSpec) {
        BaseBackPrimitiveSpec middle = ((FrontPrimitiveSpec) part).dataType;
        out.add((middle.pattern == null ? Pattern.repeatedAny : middle.pattern).build());
      } else throw new DeadCode();
    }
    return new Color(color, out);
  }

  public ParseResult parse(final Context context, final FreeAtomType type, final String string) {
    final ParseResult out = new ParseResult();
    final Iterator<FrontSpec> frontIterator = keyParts.iterator();

    // Parse string into primitive parts
    final Set<String> filled = new HashSet<>(type.fields.keySet());
    final TSMap<String, Value> data = new TSMap<>();
    int at = 0;
    for (final FrontSpec front : iterable(frontIterator)) {
      final Grammar grammar = new Grammar();
      if (front instanceof FrontSymbol) {
        String text = ((FrontSymbol) front).gapKey;
        if (text.isEmpty() && ((FrontSymbol) front).type instanceof SymbolTextSpec)
          text = ((SymbolTextSpec) ((FrontSymbol) front).type).text;
        grammar.add("root", BytesHelper.stringSequence(text));
      } else if (front instanceof FrontPrimitiveSpec) {
        final BaseBackPrimitiveSpec middle =
            (BaseBackPrimitiveSpec) type.fields.get(((FrontPrimitiveSpec) front).field);
        grammar.add(
            "root", (middle.pattern == null ? Pattern.repeatedAny : middle.pattern).build());
      } else throw new DeadCode();
      final Pair<Parse, Position> longest =
          new ParseBuilder<>()
              .grammar(grammar)
              .longestMatchFromStart(
                  new ByteArrayInputStream(string.substring(at).getBytes(StandardCharsets.UTF_8)));
      if (front instanceof FrontPrimitiveSpec) {
        data.putNew(
            front.field(),
            new ValuePrimitive(
                ((FrontPrimitiveSpec) front).dataType,
                string.substring(at, at + (int) longest.second.absolute)));
        filled.remove(front.field());
        out.nextPrimitive = front;
      } else out.nextPrimitive = null;
      at = at + (int) longest.second.absolute;
      if (at >= string.length()) break;
    }
    if (at < string.length()) out.nextPrimitive = null;
    filled.forEach(middle -> data.putNew(middle, type.fields.get(middle).create(context.syntax)));
    out.remainder = string.substring(at);
    out.atom = new Atom(type, data);

    // Look for the next place to enter text
    if (out.nextPrimitive == null)
      for (final FrontSpec part : iterable(frontIterator)) {
        if (!(part instanceof FrontPrimitiveSpec)) continue;
        out.nextPrimitive = part;
        break;
      }

    return out;
  }

  public static class ParseResult {
    public Atom atom;
    public FrontSpec nextPrimitive;
    public String remainder;
  }
}
