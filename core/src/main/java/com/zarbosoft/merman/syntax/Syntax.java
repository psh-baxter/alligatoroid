package com.zarbosoft.merman.syntax;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.merman.document.Document;
import com.zarbosoft.merman.editor.serialization.Load;
import com.zarbosoft.merman.misc.MultiError;
import com.zarbosoft.merman.misc.ROList;
import com.zarbosoft.merman.misc.ROMap;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.misc.TSSet;
import com.zarbosoft.merman.syntax.error.DuplicateAtomTypeIds;
import com.zarbosoft.merman.syntax.error.DuplicateAtomTypeIdsInGroup;
import com.zarbosoft.merman.syntax.error.GroupChildDoesntExist;
import com.zarbosoft.merman.syntax.error.NotTransverse;
import com.zarbosoft.merman.syntax.error.TypeCircularReference;
import com.zarbosoft.merman.syntax.error.UnsupportedDirections;
import com.zarbosoft.merman.syntax.style.ModelColor;
import com.zarbosoft.merman.syntax.style.Style;
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
  public final BackType backType;
  public final ModelColor background;
  public final Padding pad;
  public final String unprintable;
  public final ROList<Style.Spec> styles;
  public final Padding bannerPad;
  public final Padding detailPad;
  public final int detailSpan;
  public final ROList<AtomType> types;
  public final ROMap<String, Set<AtomType>> splayedTypes;
  public final RootAtomType root;
  public final GapAtomType gap;
  public final SuffixGapAtomType suffixGap;
  public final Direction converseDirection;
  public final Direction transverseDirection;
  private Grammar grammar;

  public static class Config {
    public BackType backType = BackType.LUXEM;
    public ModelColor background = ModelColor.RGB.white;
    public Padding pad = Padding.empty;
    public String unprintable = "▢";
    public ROList<Style.Spec> styles = ROList.empty;
    public Padding bannerPad = Padding.empty;
    public Padding detailPad = Padding.empty;
    public int detailSpan = 300;
    public final ROList<AtomType> types;
    public final ROMap<String, Set<AtomType>> splayedTypes;
    public final RootAtomType root;
    public GapAtomType gap = new GapAtomType(new GapAtomType.Config());
    public SuffixGapAtomType suffixGap = new SuffixGapAtomType(new SuffixGapAtomType.Config());
    public Direction converseDirection = Direction.RIGHT;
    public Direction transverseDirection = Direction.DOWN;

    public Config(
        ROList<AtomType> types, ROMap<String, Set<AtomType>> splayedTypes, RootAtomType root) {
      this.types = types;
      this.splayedTypes = splayedTypes;
      this.root = root;
    }

    public Config(
        BackType backType,
        ModelColor background,
        Padding pad,
        String unprintable,
        ROList<Style.Spec> styles,
        Padding bannerPad,
        Padding detailPad,
        int detailSpan,
        ROList<AtomType> types,
        ROMap<String, Set<AtomType>> splayedTypes,
        RootAtomType root,
        GapAtomType gap,
        SuffixGapAtomType suffixGap,
        Direction converseDirection,
        Direction transverseDirection) {
      this.backType = backType;
      this.background = background;
      this.pad = pad;
      this.unprintable = unprintable;
      this.styles = styles;
      this.bannerPad = bannerPad;
      this.detailPad = detailPad;
      this.detailSpan = detailSpan;
      this.types = types;
      this.splayedTypes = splayedTypes;
      this.root = root;
      this.gap = gap;
      this.suffixGap = suffixGap;
      this.converseDirection = converseDirection;
      this.transverseDirection = transverseDirection;
    }
  }

  public Syntax(Config config) {
    MultiError errors = new MultiError();
    // jfx, qt, and swing don't support vertical languages
    if (!ImmutableSet.of(Direction.LEFT, Direction.RIGHT).contains(config.converseDirection)
        || (config.transverseDirection != Direction.DOWN)) {
      errors.add(new UnsupportedDirections());
    }
    switch (config.converseDirection) {
      case LEFT:
      case RIGHT:
        switch (config.transverseDirection) {
          case LEFT:
          case RIGHT:
            errors.add(new NotTransverse(config.converseDirection, config.transverseDirection));
        }
        break;
      case UP:
      case DOWN:
        switch (config.transverseDirection) {
          case UP:
          case DOWN:
            errors.add(new NotTransverse(config.converseDirection, config.transverseDirection));
        }
        break;
    }
    this.backType = config.backType;
    this.background = config.background;
    this.pad = config.pad;
    this.unprintable = config.unprintable;
    this.styles = config.styles;
    this.bannerPad = config.bannerPad;
    this.detailPad = config.detailPad;
    this.detailSpan = config.detailSpan;
    this.types = config.types;
    this.splayedTypes = config.splayedTypes;
    this.root = config.root;
    this.gap = config.gap;
    this.suffixGap = config.suffixGap;
    this.converseDirection = config.converseDirection;
    this.transverseDirection = config.transverseDirection;
    errors.raise();
  }

  /**
   * Turns a tree of group -> children into a flat map of id -> types
   *
   * @param errors
   * @param types
   * @param groups
   * @return
   */
  public static TSMap<String, Set<AtomType>> splayGroups(
      MultiError errors, ROList<AtomType> types, ROMap<String, ROList<String>> groups) {
    TSMap<String, Set<AtomType>> splayedTypes = new TSMap<>();

    TSMap<String, AtomType> typeLookup = new TSMap<>();
    for (AtomType entry : types) {
      typeLookup.put(entry.id(), entry);
      splayedTypes.put(entry.id(), new HashSet<>(Arrays.asList(entry)));
    }

    for (Map.Entry<String, ROList<String>> group : groups) {
      if (splayedTypes.contains(group.getKey())) {
        errors.add(new DuplicateAtomTypeIds(group.getKey()));
      }
      if (group.getValue().toSet().size() != group.getValue().size()) {
        errors.add(new DuplicateAtomTypeIdsInGroup(group.getKey()));
      }
      final Deque<Pair<PVector<String>, Iterator<String>>> stack = new ArrayDeque<>();
      Iterator<String> seed = group.getValue().iterator();
      Set<AtomType> out = new HashSet<>();
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

          final Set<AtomType> splayed = splayedTypes.getOpt(childKey);
          if (splayed != null) {
            out.addAll(splayed);
            continue;
          }

          final ROList<String> childGroup = groups.getOpt(childKey);
          if (childGroup != null) {
            stack.addLast(new Pair<>(newPath, childGroup.iterator()));
            continue;
          }

          AtomType gotType = typeLookup.getOpt(childKey);
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

  public void finish(MultiError errors) {
    TSSet<AtomType> seen = new TSSet<>();
    for (Map.Entry<String, Set<AtomType>> splayedType : splayedTypes) {
      for (AtomType atomType : splayedType.getValue()) {
        if (!seen.addNew(atomType)) continue;
        atomType.finish(errors, this);
      }
    }
    root.finish(errors, this);
    gap.finish(errors, this);
    suffixGap.finish(errors, this);
  }

  public Node backRuleRef(final String type) {
    final Union out = new Union();
    out.add(new Reference(type));
    out.add(new Reference(gap.backType));
    out.add(new Reference(suffixGap.backType));
    return out;
  }

  public Grammar getGrammar() {
    if (grammar == null) {
      grammar = new Grammar();
      for (Map.Entry<String, Set<AtomType>> entry : splayedTypes) {
        Set<AtomType> types = entry.getValue();
        String key = entry.getKey();
        AtomType firstType = types.iterator().next();
        if (types.size() == 1 && key.equals(firstType.id())) {
          AtomType type = firstType;
          grammar.add(type.id(), type.buildBackRule(this));
        } else {
          final Union group = new Union();
          for (AtomType type : types) {
            group.add(new Reference(type.id()));
          }
          grammar.add(key, group);
        }
      }
      grammar.add("root", root.buildBackRule(this));
    }
    return grammar;
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
}
