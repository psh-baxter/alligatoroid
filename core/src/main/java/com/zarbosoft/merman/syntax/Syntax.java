package com.zarbosoft.merman.syntax;

import com.zarbosoft.merman.editor.I18nEngine;
import com.zarbosoft.merman.editor.backevents.EArrayCloseEvent;
import com.zarbosoft.merman.editor.backevents.EArrayOpenEvent;
import com.zarbosoft.merman.editor.backevents.EKeyEvent;
import com.zarbosoft.merman.editor.backevents.EObjectCloseEvent;
import com.zarbosoft.merman.editor.backevents.EObjectOpenEvent;
import com.zarbosoft.merman.editor.backevents.EPrimitiveEvent;
import com.zarbosoft.merman.editor.backevents.ETypeEvent;
import com.zarbosoft.merman.editor.backevents.JSpecialPrimitiveEvent;
import com.zarbosoft.merman.misc.MultiError;
import com.zarbosoft.merman.syntax.error.DuplicateAtomTypeIds;
import com.zarbosoft.merman.syntax.error.DuplicateAtomTypeIdsInGroup;
import com.zarbosoft.merman.syntax.error.GroupChildDoesntExist;
import com.zarbosoft.merman.syntax.error.NotTransverse;
import com.zarbosoft.merman.syntax.error.TypeCircularReference;
import com.zarbosoft.merman.syntax.error.UnsupportedDirections;
import com.zarbosoft.merman.syntax.style.ModelColor;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.Pair;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;
import com.zarbosoft.rendaw.common.TSSet;

import java.util.Iterator;
import java.util.Map;

public class Syntax {
  public static final Object GRAMMAR_WILDCARD_KEY = new Object();
  public static final Object GRAMMAR_WILDCARD_KEY_UNTYPED = new Object();
  public static final Object GRAMMAR_ROOT = new Object();
  public final BackType backType;
  public final ModelColor background;
  public final Padding pad;
  public final String unprintable;
  public final ROList<Style.Spec> styles;
  public final Padding bannerPad;
  public final Padding detailPad;
  public final int detailSpan;
  public final ROList<AtomType> types;
  public final ROMap<String, ROSet<AtomType>> splayedTypes;
  public final RootAtomType root;
  public final GapAtomType gap;
  public final SuffixGapAtomType suffixGap;
  public final Direction converseDirection;
  public final Direction transverseDirection;
  private final Grammar grammar;

  public Syntax(Config config) {
    MultiError errors = new MultiError();
    // jfx, qt, and swing don't support vertical languages
    if (!TSSet.of(Direction.LEFT, Direction.RIGHT).contains(config.converseDirection)
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

    TSSet<AtomType> seen = new TSSet<>();
    for (Map.Entry<String, ROSet<AtomType>> splayedType : splayedTypes) {
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
            .add(new MatchingEventTerminal(new EPrimitiveEvent()))
            .add(new MatchingEventTerminal(new JSpecialPrimitiveEvent()))
            .add(
                new Sequence()
                    .add(new MatchingEventTerminal(new EArrayOpenEvent()))
                    .add(new Repeat(new Reference(GRAMMAR_WILDCARD_KEY)))
                    .add(new MatchingEventTerminal(new EArrayCloseEvent())))
            .add(
                new Sequence()
                    .add(new MatchingEventTerminal(new EObjectOpenEvent()))
                    .add(
                        new Reference(
                            new Sequence()
                                .add(new MatchingEventTerminal(new EKeyEvent()))
                                .add(new Reference(GRAMMAR_WILDCARD_KEY))))
                    .add(new MatchingEventTerminal(new EObjectCloseEvent()))));
    grammar.add(
        GRAMMAR_WILDCARD_KEY,
        new Union()
            .add(new Reference(GRAMMAR_WILDCARD_KEY_UNTYPED))
            .add(
                new Sequence()
                    .add(new MatchingEventTerminal(new ETypeEvent()))
                    .add(new Reference(GRAMMAR_WILDCARD_KEY_UNTYPED))));
    for (Map.Entry<String, ROSet<AtomType>> entry : splayedTypes) {
      ROSet<AtomType> types = entry.getValue();
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
    grammar.add(RootAtomType.ROOT_TYPE_ID, root.buildBackRule(this));

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
  public static TSMap<String, ROSet<AtomType>> splayGroups(
      MultiError errors, ROList<AtomType> types, ROMap<String, ROList<String>> groups) {
    TSMap<String, ROSet<AtomType>> splayedTypes = new TSMap<>();

    TSMap<String, AtomType> typeLookup = new TSMap<>();
    for (AtomType entry : types) {
      if (typeLookup.putReplace(entry.id(), entry) != null) {
        errors.add(new DuplicateAtomTypeIds(entry.id()));
      }
      ;
      splayedTypes.putReplace(entry.id(), TSSet.of(entry).ro());
    }

    for (Map.Entry<String, ROList<String>> group : groups) {
      if (splayedTypes.contains(group.getKey())) {
        errors.add(new DuplicateAtomTypeIds(group.getKey()));
      }
      if (group.getValue().toSet().size() != group.getValue().size()) {
        errors.add(new DuplicateAtomTypeIdsInGroup(group.getKey()));
      }
      final TSList<Pair<ROList<String>, Iterator<String>>> stack = new TSList<>();
      Iterator<String> seed = group.getValue().iterator();
      TSSet<AtomType> out = new TSSet<>();
      if (seed.hasNext()) {
        stack.add(new Pair<ROList<String>, Iterator<String>>(TSList.of(group.getKey()), seed));
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

          final ROSet<AtomType> splayed = splayedTypes.getOpt(childKey);
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
      splayedTypes.put(group.getKey(), out.ro());
    }

    return splayedTypes;
  }

  public Node backRuleRef(final String type) {
    final Union out = new Union();
    out.add(new Reference(type));
    out.add(new Reference(gap.backType));
    out.add(new Reference(suffixGap.backType));
    return out;
  }

  public Grammar getGrammar() {
    if (grammar == null) throw new Assertion("finish() never called on syntax");
    return grammar;
  }

  public static class Config {
    public final ROList<AtomType> types;
    public final ROMap<String, ROSet<AtomType>> splayedTypes;
    public final RootAtomType root;
    public BackType backType = BackType.LUXEM;
    public ModelColor background = ModelColor.RGB.white;
    public Padding pad = Padding.empty;
    public String unprintable = "▢";
    public ROList<Style.Spec> styles = ROList.empty;
    public Padding bannerPad = Padding.empty;
    public Padding detailPad = Padding.empty;
    public int detailSpan = 300;
    public GapAtomType gap;
    public SuffixGapAtomType suffixGap;
    public Direction converseDirection = Direction.RIGHT;
    public Direction transverseDirection = Direction.DOWN;

    public Config(
        I18nEngine i18n,
        ROList<AtomType> types,
        ROMap<String, ROSet<AtomType>> splayedTypes,
        RootAtomType root) {
      this.types = types;
      this.splayedTypes = splayedTypes;
      this.root = root;
      gap = new GapAtomType(i18n, new GapAtomType.Config());
      suffixGap = new SuffixGapAtomType(i18n, new SuffixGapAtomType.Config());
    }

    public Config(
        I18nEngine i18n,
        BackType backType,
        ModelColor background,
        Padding pad,
        String unprintable,
        ROList<Style.Spec> styles,
        Padding bannerPad,
        Padding detailPad,
        int detailSpan,
        ROList<AtomType> types,
        ROMap<String, ROSet<AtomType>> splayedTypes,
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
      this.gap = new GapAtomType(i18n, new GapAtomType.Config());
      this.gap = gap;
      this.suffixGap = new SuffixGapAtomType(i18n, new SuffixGapAtomType.Config());
      this.suffixGap = suffixGap;
      this.converseDirection = converseDirection;
      this.transverseDirection = transverseDirection;
    }
  }
}
