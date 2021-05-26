package com.zarbosoft.merman.core.syntax;

import com.zarbosoft.merman.core.AtomKey;
import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.backevents.EArrayCloseEvent;
import com.zarbosoft.merman.core.backevents.EArrayOpenEvent;
import com.zarbosoft.merman.core.backevents.EKeyEvent;
import com.zarbosoft.merman.core.backevents.EObjectCloseEvent;
import com.zarbosoft.merman.core.backevents.EObjectOpenEvent;
import com.zarbosoft.merman.core.backevents.EPrimitiveEvent;
import com.zarbosoft.merman.core.backevents.ETypeEvent;
import com.zarbosoft.merman.core.backevents.JSpecialPrimitiveEvent;
import com.zarbosoft.merman.core.serialization.WriteStateDeepDataArray;
import com.zarbosoft.merman.core.syntax.error.DuplicateAtomTypeIds;
import com.zarbosoft.merman.core.syntax.error.DuplicateAtomTypeIdsInGroup;
import com.zarbosoft.merman.core.syntax.error.GroupChildDoesntExist;
import com.zarbosoft.merman.core.syntax.error.NotTransverse;
import com.zarbosoft.merman.core.syntax.error.TypeCircularReference;
import com.zarbosoft.merman.core.syntax.primitivepattern.Digits;
import com.zarbosoft.merman.core.syntax.primitivepattern.PatternSequence;
import com.zarbosoft.merman.core.syntax.primitivepattern.PatternString;
import com.zarbosoft.merman.core.syntax.style.ModelColor;
import com.zarbosoft.merman.core.syntax.style.Padding;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.HomogenousSequence;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.rendaw.common.Pair;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.ROOrderedMap;
import com.zarbosoft.rendaw.common.ROOrderedSetRef;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;
import com.zarbosoft.rendaw.common.TSOrderedSet;
import com.zarbosoft.rendaw.common.TSSet;

import java.util.Iterator;
import java.util.Map;

public class Syntax {
  public static final Reference.Key<Object> GRAMMAR_WILDCARD_KEY = new Reference.Key<>();
  public static final Reference.Key<Object> GRAMMAR_WILDCARD_KEY_UNTYPED = new Reference.Key<>();
  public final BackType backType;
  public final ModelColor background;
  public final Padding pad;
  public final String unprintable;
  public final ROMap<String, ROOrderedSetRef<AtomType>> splayedTypes;
  public final RootAtomType root;
  public final GapAtomType gap;
  public final SuffixGapAtomType suffixGap;
  public final Direction converseDirection;
  public final Direction transverseDirection;
  public final DisplayUnit displayUnit;
  public final Style cursorStyle;
  public final Style primitiveCursorStyle;
  public final Style hoverStyle;
  public final Style primitiveHoverStyle;
  public final String gapInRecordKeyPrefix;
  public final PatternSequence gapInRecordKeyPrefixPattern;
  public final double courseTransverseStride;
  private final Grammar grammar;

  public Syntax(Environment env, Config config) {
    MultiError errors = new MultiError();
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
    this.displayUnit = config.displayUnit;
    this.backType = config.backType;
    this.background = config.background;
    this.pad = config.pad;
    this.unprintable = config.unprintable;
    this.splayedTypes = config.splayedTypes;
    this.root = config.root;
    this.gap = config.gap;
    this.suffixGap = config.suffixGap;
    this.converseDirection = config.converseDirection;
    this.transverseDirection = config.transverseDirection;
    this.cursorStyle =
        config.cursorStyle == null ? new Style(new Style.Config()) : config.cursorStyle;
    this.primitiveCursorStyle =
        config.primitiveCursorStyle == null
            ? new Style(new Style.Config())
            : config.primitiveCursorStyle;
    this.hoverStyle = config.hoverStyle == null ? new Style(new Style.Config()) : config.hoverStyle;
    this.primitiveHoverStyle =
        config.primitiveHoverStyle == null
            ? new Style(new Style.Config())
            : config.primitiveHoverStyle;
    this.gapInRecordKeyPrefix = config.gapInRecordKeyPrefix;
    this.gapInRecordKeyPrefixPattern =
        new PatternSequence(
            TSList.of(
                new PatternString(env, WriteStateDeepDataArray.INDEX_KEY_PREFIX), new Digits()));
    courseTransverseStride = config.courseTransverseStride;

    TSSet<AtomType> seen = new TSSet<>();
    for (Map.Entry<String, ROOrderedSetRef<AtomType>> splayedType : splayedTypes) {
      for (AtomType atomType : splayedType.getValue()) {
        if (!seen.addNew(atomType)) continue;
        atomType.finish(errors, this);
      }
    }
    root.finish(errors, this);
    gap.finish(errors, this);
    suffixGap.finish(errors, this);

    grammar = new Grammar();
    grammar.add(
        GRAMMAR_WILDCARD_KEY_UNTYPED,
        new Union()
            .add(new MatchingEventTerminal<>(new EPrimitiveEvent()))
            .add(new MatchingEventTerminal<>(new JSpecialPrimitiveEvent()))
            .add(
                new HomogenousSequence()
                    .add(new MatchingEventTerminal<>(new EArrayOpenEvent()))
                    .add(new Repeat(new Reference(GRAMMAR_WILDCARD_KEY)))
                    .add(new MatchingEventTerminal<>(new EArrayCloseEvent())))
            .add(
                new HomogenousSequence()
                    .add(new MatchingEventTerminal<>(new EObjectOpenEvent()))
                    .add(
                        new Repeat(
                            new HomogenousSequence()
                                .add(new MatchingEventTerminal<>(new EKeyEvent()))
                                .add(new Reference(GRAMMAR_WILDCARD_KEY))))
                    .add(new MatchingEventTerminal<>(new EObjectCloseEvent()))));
    grammar.add(
        GRAMMAR_WILDCARD_KEY,
        new Union()
            .add(new Reference(GRAMMAR_WILDCARD_KEY_UNTYPED))
            .add(
                new HomogenousSequence()
                    .add(new MatchingEventTerminal<>(new ETypeEvent()))
                    .add(new Reference(GRAMMAR_WILDCARD_KEY_UNTYPED))));
    Union<AtomType.AtomParseResult> any = new Union<>();
    for (Map.Entry<String, ROOrderedSetRef<AtomType>> entry : splayedTypes) {
      ROOrderedSetRef<AtomType> types = entry.getValue();
      String key = entry.getKey();
      AtomType firstType = types.iterator().next();
      if (types.size() == 1 && key.equals(firstType.id())) {
        AtomType type = firstType;
        grammar.add(type.key, type.buildBackRule(env, this));
        any.add(new Reference<>(type.key));
      } else {
        final Union<AtomType.AtomParseResult> group = new Union<>();
        for (AtomType type : types) {
          Reference<AtomType.AtomParseResult> ref = new Reference<>(type.key);
          group.add(ref);
          any.add(ref);
        }
        grammar.add(new AtomKey(key), group);
      }
    }
    grammar.add(root.key, root.buildBackRule(env, this));
    grammar.add(new AtomKey(null), any);

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
  public static TSMap<String, ROOrderedSetRef<AtomType>> splayGroups(
      MultiError errors,
      ROList<AtomType> types,
      GapAtomType gap,
      SuffixGapAtomType suffixGap,
      ROOrderedMap<String, ROList<String>> groups) {
    TSMap<String, ROOrderedSetRef<AtomType>> splayedTypes = new TSMap<>();

    TSMap<String, AtomType> typeLookup = new TSMap<>();

    splayedTypes.putReplace(gap.id, TSOrderedSet.of(gap));
    splayedTypes.putReplace(suffixGap.id, TSOrderedSet.of(suffixGap));

    for (AtomType entry : types) {
      if (typeLookup.putReplace(entry.id(), entry) != null) {
        errors.add(new DuplicateAtomTypeIds(entry.id()));
      }
      splayedTypes.putReplace(entry.id(), TSOrderedSet.of(entry));
    }

    for (ROPair<String, ROList<String>> group : groups) {
      if (splayedTypes.contains(group.first)) {
        errors.add(new DuplicateAtomTypeIds(group.first));
      }
      if (group.second.toSet().size() != group.second.size()) {
        errors.add(new DuplicateAtomTypeIdsInGroup(group.first));
      }
      final TSList<Pair<ROList<String>, Iterator<String>>> stack = new TSList<>();
      Iterator<String> seed = group.second.iterator();
      TSOrderedSet<AtomType> out = new TSOrderedSet<>();
      if (seed.hasNext()) {
        stack.add(new Pair<ROList<String>, Iterator<String>>(TSList.of(group.first), seed));
        while (!stack.isEmpty()) {
          Pair<ROList<String>, Iterator<String>> top = stack.last();
          final String childKey = top.second.next();
          if (!top.second.hasNext()) stack.removeLast();

          int pathContainsChild = top.first.lastIndexOf(childKey);
          ROList<String> newPath = top.first.mut().add(childKey);
          if (pathContainsChild != -1) {
            errors.add(new TypeCircularReference(newPath.subFrom(pathContainsChild)));
            continue;
          }

          final ROOrderedSetRef<AtomType> splayed = splayedTypes.getOpt(childKey);
          if (splayed != null) {
            out.addAll(splayed);
            continue;
          }

          final ROList<String> childGroup = groups.getOpt(childKey);
          if (childGroup != null) {
            stack.add(new Pair<>(newPath, childGroup.iterator()));
            continue;
          }

          AtomType gotType = typeLookup.getOpt(childKey);
          if (gotType != null) {
            out.add(gotType);
            continue;
          }

          errors.add(new GroupChildDoesntExist(top.first.last(), childKey));
        }
      }
      splayedTypes.put(group.first, out);
    }

    return splayedTypes;
  }

  public Node<AtomType.AtomParseResult> backRuleRef(final String type) {
    final Union<AtomType.AtomParseResult> out = new Union<>();
    out.add(new Reference<>(new AtomKey(gap.backType)));
    out.add(new Reference<>(new AtomKey(suffixGap.backType)));
    out.add(new Reference<>(new AtomKey(type)));
    return out;
  }

  public Grammar getGrammar() {
    return grammar;
  }

  public static enum DisplayUnit {
    PX,
    MM
  }

  public static class Config {
    public final ROMap<String, ROOrderedSetRef<AtomType>> splayedTypes;
    public final RootAtomType root;
    public final GapAtomType gap;
    public final SuffixGapAtomType suffixGap;
    public BackType backType = BackType.LUXEM;
    public ModelColor background = ModelColor.RGB.white;
    public Padding pad = Padding.empty;
    public String unprintable = "▢";
    public Direction converseDirection = Direction.RIGHT;
    public Direction transverseDirection = Direction.DOWN;
    public DisplayUnit displayUnit = DisplayUnit.MM;
    public Style cursorStyle;
    public Style primitiveCursorStyle;
    public Style hoverStyle;
    public Style primitiveHoverStyle;
    public String gapInRecordKeyPrefix = "__gap_pair_";
    public double courseTransverseStride;

    public Config(
        ROMap<String, ROOrderedSetRef<AtomType>> splayedTypes,
        RootAtomType root,
        GapAtomType gap,
        SuffixGapAtomType suffixGap) {
      this.splayedTypes = splayedTypes;
      this.root = root;
      this.gap = gap;
      this.suffixGap = suffixGap;
    }

    /**
     * When a gap is in a record before it has been resolved into a key/value pair, generate a
     * standin key with this prefix to use for serialization
     *
     * @param prefix
     * @return
     */
    public Config gapInRecordKeyPrefix(String prefix) {
      this.gapInRecordKeyPrefix = prefix;
      return this;
    }

    public Config backType(BackType backType) {
      this.backType = backType;
      return this;
    }

    public Config courseTransverseStride(double stride) {
      this.courseTransverseStride = stride;
      return this;
    }

    public Config displayUnit(DisplayUnit unit) {
      this.displayUnit = unit;
      return this;
    }

    public Config background(ModelColor color) {
      this.background = color;
      return this;
    }

    public Config cursorStyle(Style style) {
      this.cursorStyle = style;
      return this;
    }

    public Config primitiveCursorStyle(Style style) {
      this.primitiveCursorStyle = style;
      return this;
    }

    public Config hoverStyle(Style style) {
      this.hoverStyle = style;
      return this;
    }

    public Config primitiveHoverStyle(Style style) {
      this.primitiveHoverStyle = style;
      return this;
    }

    public Config pad(Padding padding) {
      this.pad = padding;
      return this;
    }
  }
}
