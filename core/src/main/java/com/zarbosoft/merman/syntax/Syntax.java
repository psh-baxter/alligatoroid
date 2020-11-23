package com.zarbosoft.merman.syntax;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.merman.document.Document;
import com.zarbosoft.merman.editor.serialization.Load;
import com.zarbosoft.merman.modules.Module;
import com.zarbosoft.merman.syntax.style.BoxStyle;
import com.zarbosoft.merman.syntax.style.ModelColor;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.merman.syntax.symbol.Symbol;
import com.zarbosoft.merman.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.pidgoon.Grammar;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.zarbosoft.rendaw.common.Common.stream;

public class Syntax {

  public BackType backType = BackType.LUXEM;
  public ModelColor background = ModelColor.RGB.white;
  public Padding pad = new Padding();
  public String placeholder = "▢";
  public List<Style> styles = new ArrayList<>();
  public Padding bannerPad = new Padding();
  public Padding detailPad = new Padding();
  public int detailSpan = 300;
  public List<FreeAtomType> types = new ArrayList<>();
  public Map<String, List<String>> groups = new HashMap<>();
  public RootAtomType root;
  public GapAtomType gap = new GapAtomType();
  public PrefixGapAtomType prefixGap = new PrefixGapAtomType();
  public SuffixGapAtomType suffixGap = new SuffixGapAtomType();
  public Symbol gapPlaceholder = new SymbolTextSpec("•");
  public BoxStyle gapChoiceStyle = new BoxStyle();
  public List<Module> modules = new ArrayList<>();
  public boolean animateCoursePlacement = false;
  public boolean animateDetails = false;
  public boolean startWindowed = false;
  public int ellipsizeThreshold = Integer.MAX_VALUE;
  public int layBrickBatchSize = 10;
  public double retryExpandFactor = 1.25;
  public double scrollFactor = 0.1;
  public double scrollAlotFactor = 0.8;
  public boolean prettySave = false;
  public String id; // Fake final - don't modify (set in loadSyntax)
  public Direction converseDirection = Direction.RIGHT;
  public Direction transverseDirection = Direction.DOWN;
  Grammar grammar;

  public void finish() {
    // jfx, qt, and swing don't support vertical languages
    if (!ImmutableSet.of(Direction.LEFT, Direction.RIGHT).contains(converseDirection)
        || (transverseDirection != Direction.DOWN))
      throw new InvalidSyntax(
          "Currently only converse directions left/right and transverse down are supported.");
    switch (converseDirection) {
      case LEFT:
      case RIGHT:
        switch (transverseDirection) {
          case LEFT:
          case RIGHT:
            throw new InvalidSyntax("Secondary direction must cross converse direction axis.");
        }
        break;
      case UP:
      case DOWN:
        switch (transverseDirection) {
          case UP:
          case DOWN:
            throw new InvalidSyntax("Secondary direction must cross converse direction axis.");
        }
        break;
    }

    {
      final Deque<Pair<PSet<String>, Iterator<String>>> stack = new ArrayDeque<>();
      stack.addLast(new Pair<>(HashTreePSet.empty(), groups.keySet().iterator()));
      while (!stack.isEmpty()) {
        final Pair<PSet<String>, Iterator<String>> top = stack.pollLast();
        if (!top.second.hasNext()) continue;
        final String childKey = top.second.next();
        final List<String> child = groups.get(childKey);
        if (child == null) continue;
        if (top.first.contains(childKey))
          throw new InvalidSyntax(String.format("Circular reference in group [%s].", childKey));
        stack.addLast(top);
        stack.addLast(new Pair<>(top.first.plus(childKey), child.iterator()));
      }
    }

    final Set<String> scalarTypes = new HashSet<>(); // Types that only have one back element
    final Set<String> allTypes = new HashSet<>();
    for (final FreeAtomType t : types) {
      if (t.back.isEmpty())
        throw new InvalidSyntax(String.format("Type [%s] has no back parts.", t.id()));
      if (allTypes.contains(t.id()))
        throw new InvalidSyntax(String.format("Multiple types with id [%s].", t.id()));
      allTypes.add(t.id());
      if (t.back.size() == 1) scalarTypes.add(t.id());
    }
    final Map<String, Set<String>> groupsThatContainType = new HashMap<>();
    final Set<String> potentiallyScalarGroups = new HashSet<>();
    for (final Map.Entry<String, List<String>> pair : groups.entrySet()) {
      final String group = pair.getKey();
      if (new HashSet<>(pair.getValue()).size() != pair.getValue().size())
        throw new InvalidSyntax(String.format("Duplicate type ids in group [%s].", group));
      for (final String child : pair.getValue()) {
        if (!allTypes.contains(child) && !groups.containsKey(child))
          throw new InvalidSyntax(
              String.format("Group [%s] refers to non-existant member [%s].", group, child));
        groupsThatContainType.putIfAbsent(child, new HashSet<>());
        groupsThatContainType.get(child).add(pair.getKey());
      }
      if (allTypes.contains(group))
        throw new InvalidSyntax(String.format("Group id [%s] already used.", group));
      allTypes.add(group);
      potentiallyScalarGroups.add(group);
    }
    for (final FreeAtomType t : types) {
      if (t.back.size() == 1) continue;
      final Deque<Iterator<String>> stack = new ArrayDeque<>();
      stack.add(groupsThatContainType.getOrDefault(t.id(), ImmutableSet.of()).iterator());
      while (!stack.isEmpty()) {
        final Iterator<String> top = stack.pollLast();
        if (top.hasNext()) {
          stack.addLast(top);
          final String notScalarGroup = top.next();
          if (potentiallyScalarGroups.contains(notScalarGroup)) {
            stack.add(
                groupsThatContainType.getOrDefault(notScalarGroup, ImmutableSet.of()).iterator());
            potentiallyScalarGroups.remove(notScalarGroup);
          }
        }
      }
    }
    scalarTypes.addAll(potentiallyScalarGroups);
    for (final FreeAtomType t : types) {
      t.finish(this, allTypes, scalarTypes);
    }
    root.finish(this, allTypes, scalarTypes);
    gap.finish(this, allTypes, scalarTypes);
    prefixGap.finish(this, allTypes, scalarTypes);
    suffixGap.finish(this, allTypes, scalarTypes);
  }

  public Node backRuleRef(final String type) {
    final Union out = new Union();
    out.add(new Reference(type));
    out.add(new Reference("__gap"));
    out.add(new Reference("__prefix_gap"));
    out.add(new Reference("__suffix_gap"));
    return out;
  }

  public Grammar getGrammar() {
    if (grammar == null) {
      grammar = new Grammar();
      types.forEach(t -> grammar.add(t.id(), t.buildBackRule(this)));
      grammar.add(gap.id(), gap.buildBackRule(this));
      grammar.add(prefixGap.id(), prefixGap.buildBackRule(this));
      grammar.add(suffixGap.id(), suffixGap.buildBackRule(this));
      groups.forEach(
          (k, v) -> {
            final Union group = new Union();
            v.forEach(n -> group.add(new Reference(n)));
            grammar.add(k, group);
          });
      grammar.add("root", root.buildBackRule(this));
    }
    return grammar;
  }

  public Document create() {
    return new Document(this, root.create(this));
  }

  public Document load(final Path path) throws FileNotFoundException, IOException {
    try (InputStream data = Files.newInputStream(path)) {
      return load(data);
    }
  }

  public Document load(final InputStream data) {
    return Load.load(this, data);
  }

  public Document load(final String string) {
    return load(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
  }

  public Stream<FreeAtomType> getLeafTypes(final String type) {
    if (type == null) return types.stream(); // Gap types
    final List<String> group = groups.get(type);
    if (group == null) return Stream.of(getType(type));
    final Deque<Iterator<String>> stack = new ArrayDeque<>();
    stack.addLast(group.iterator());
    // TODO deduplicate to prevent loops?
    return stream(
            new Iterator<FreeAtomType>() {
              @Override
              public boolean hasNext() {
                return !stack.isEmpty();
              }

              @Override
              public FreeAtomType next() {
                final Iterator<String> top = stack.pollLast();
                if (!top.hasNext()) return null;
                final String childKey = top.next();
                if (top.hasNext()) stack.addLast(top);
                final List<String> child = groups.get(childKey);
                if (child == null) {
                  return getType(childKey);
                } else {
                  stack.addLast(child.iterator());
                }
                return null;
              }
            })
        .filter(x -> x != null);
  }

  public FreeAtomType getType(final String type) {
    return types.stream().filter(t -> t.id().equals(type)).findFirst().get();
  }

  public static enum BackType {
    LUXEM,
    JSON
  }

  public enum Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT
    // TODO boustrophedon
  }
}
