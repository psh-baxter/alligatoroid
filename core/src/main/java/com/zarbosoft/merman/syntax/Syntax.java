package com.zarbosoft.merman.syntax;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.merman.document.Document;
import com.zarbosoft.merman.editor.serialization.Load;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.error.DuplicateAtomTypeIds;
import com.zarbosoft.merman.syntax.error.DuplicateAtomTypeIdsInGroup;
import com.zarbosoft.merman.syntax.error.GroupChildDoesntExist;
import com.zarbosoft.merman.syntax.error.NotTransverse;
import com.zarbosoft.merman.syntax.error.TypeCircularReference;
import com.zarbosoft.merman.syntax.error.UnsupportedDirections;
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
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.zarbosoft.rendaw.common.Common.last;
import static com.zarbosoft.rendaw.common.Common.sublist;

public class Syntax {
  public BackType backType = BackType.LUXEM;
  public ModelColor background = ModelColor.RGB.white;
  public Padding pad = new Padding();
  public String unprintable = "▢";
  public List<Style> styles = new ArrayList<>();
  public Padding bannerPad = new Padding();
  public Padding detailPad = new Padding();
  public int detailSpan = 300;
  public final List<AtomType> types;
  public final TSMap<String, Set<AtomType>> splayedTypes;
  public final RootAtomType root;
  public boolean animateCoursePlacement = false;
  public boolean animateDetails = false;
  public boolean startWindowed = false;
  public int ellipsizeThreshold = Integer.MAX_VALUE;
  public int layBrickBatchSize = 10;
  public double retryExpandFactor = 1.25;
  public double scrollFactor = 0.1;
  public double scrollAlotFactor = 0.8;
  public boolean prettySave = false;
  public Direction converseDirection = Direction.RIGHT;
  public Direction transverseDirection = Direction.DOWN;
  private Grammar grammar;

  public Syntax(
      List<AtomType> types, TSMap<String, Set<AtomType>> splayedTypes, RootAtomType root) {
    this.types = types;
    this.splayedTypes = splayedTypes;
    this.root = root;
  }

  /**
   * Turns a tree of group -> children into a flat map of id -> types
   *
   * @param errors
   * @param types
   * @param groups
   * @return
   */
  public static TSMap<String, Set<FreeAtomType>> splayGroups(
      List<Object> errors, List<FreeAtomType> types, TSMap<String, List<String>> groups) {
    TSMap<String, Set<FreeAtomType>> splayedTypes = new TSMap<>();

    TSMap<String, FreeAtomType> typeLookup = new TSMap<>();
    for (FreeAtomType entry : types) {
      typeLookup.put(entry.id, entry);
      splayedTypes.put(entry.id, new HashSet<>(Arrays.asList(entry)));
    }

    for (Map.Entry<String, List<String>> group : groups.entries()) {
      if (splayedTypes.contains(group.getKey())) {
        errors.add(new DuplicateAtomTypeIds(group.getKey()));
      }
      if (new HashSet<>(group.getValue()).size() != group.getValue().size()) {
        errors.add(new DuplicateAtomTypeIdsInGroup(group.getKey()));
      }
      final Deque<Pair<PVector<String>, Iterator<String>>> stack = new ArrayDeque<>();
      Iterator<String> seed = group.getValue().iterator();
      Set<FreeAtomType> out = new HashSet<>();
      if (seed.hasNext()) {
        stack.addLast(new Pair<>(TreePVector.singleton(group.getKey()), seed));
        while (!stack.isEmpty()) {
          Pair<PVector<String>, Iterator<String>> top = stack.peekLast();
          final String childKey = top.second.next();
          if (!top.second.hasNext()) stack.removeLast();

          int pathContainsChild = top.first.lastIndexOf(childKey);
          PVector<String> newPath = top.first.plus(childKey);
          if (pathContainsChild != -1) {
            errors.add(new TypeCircularReference(sublist(newPath, pathContainsChild)));
            continue;
          }

          final Set<FreeAtomType> splayed = splayedTypes.getOpt(childKey);
          if (splayed != null) {
            out.addAll(splayed);
            continue;
          }

          final List<String> childGroup = groups.getOpt(childKey);
          if (childGroup != null) {
            stack.addLast(new Pair<>(newPath, childGroup.iterator()));
            continue;
          }

          FreeAtomType gotType = typeLookup.getOpt(childKey);
          if (gotType != null) {
            out.add(gotType);
            continue;
          }

          errors.add(new GroupChildDoesntExist(last(top.first), childKey));
        }
      }
      splayedTypes.put(group.getKey(), out);
    }

    return splayedTypes;
  }

  public void finish(List<Object> errors) {
    // jfx, qt, and swing don't support vertical languages
    if (!ImmutableSet.of(Direction.LEFT, Direction.RIGHT).contains(converseDirection)
        || (transverseDirection != Direction.DOWN)) {
      errors.add(new UnsupportedDirections());
    }
    switch (converseDirection) {
      case LEFT:
      case RIGHT:
        switch (transverseDirection) {
          case LEFT:
          case RIGHT:
            errors.add(new NotTransverse(converseDirection, transverseDirection));
        }
        break;
      case UP:
      case DOWN:
        switch (transverseDirection) {
          case UP:
          case DOWN:
            errors.add(new NotTransverse(converseDirection, transverseDirection));
        }
        break;
    }

    splayedTypes.values().stream()
        .flatMap(s -> s.stream())
        .collect(Collectors.toSet())
        .forEach(
            t -> {
              t.finish(errors, this);
            });

    root.finish(errors, this);
  }

  public Grammar getGrammar() {
    if (grammar == null) {
      grammar = new Grammar();
      for (AtomType t : types) {
        grammar.add(t.id(), t.buildBackRule(this));
      }
      for (Map.Entry<String, Set<AtomType>> entry : splayedTypes.entries()) {
        final Union group = new Union();
        entry.getValue().forEach(n -> group.add(new Reference(n.id())));
        grammar.add(entry.getKey(), group);
      }
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
