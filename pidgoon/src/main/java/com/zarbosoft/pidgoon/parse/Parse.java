package com.zarbosoft.pidgoon.parse;

import com.zarbosoft.pidgoon.Grammar;
import com.zarbosoft.pidgoon.Position;
import com.zarbosoft.pidgoon.State;
import com.zarbosoft.pidgoon.Store;
import com.zarbosoft.pidgoon.errors.GrammarTooUncertain;
import com.zarbosoft.pidgoon.errors.InvalidStream;
import com.zarbosoft.pidgoon.internal.BranchingStack;
import com.zarbosoft.pidgoon.internal.Parent;
import com.zarbosoft.rendaw.common.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This manages the state of a parse. This can be used without a Parse object for manually driven
 * parses.
 */
public class Parse {
  public final Grammar grammar;
  /** An error for each branch that failed in the previous step of the parse. */
  public final List<Object> errors = new ArrayList<>();

  public final int errorHistoryLimit;
  public final int uncertaintyLimit;
  /** This represents the tip node of each branch. */
  public final List<State> leaves = new ArrayList<>();

  public final Stats stats;
  /**
   * This represents the top value of the stack of branches that matched in the previous step of the
   * parse.
   */
  public final List<Object> results = new ArrayList<>();
  /** The error from steps before the previous (controlled by errorHistoryLimit). */
  public List<Pair<Position, List<Object>>> errorHistory;

  public BranchingStack<AmbiguitySample> ambiguityHistory;

  public Parse(
      final Grammar grammar,
      final int errorHistoryLimit,
      final int uncertaintyLimit,
      final boolean dumpAmbiguity) {
    this.grammar = grammar;
    this.errorHistoryLimit = errorHistoryLimit;
    this.uncertaintyLimit = uncertaintyLimit;
    if (dumpAmbiguity) this.ambiguityHistory = new BranchingStack<>(new AmbiguitySample());
    stats = new Stats();
  }

  public Parse(final Parse previous) {
    this.grammar = previous.grammar;
    this.stats = new Stats(previous.stats);
    stats.totalLeaves += previous.leaves.size();
    stats.maxLeaves = Math.max(stats.maxLeaves, previous.leaves.size());
    stats.steps += 1;
    this.errorHistoryLimit = previous.errorHistoryLimit;
    this.uncertaintyLimit = previous.uncertaintyLimit;
    this.ambiguityHistory = previous.ambiguityHistory;
  }

  public static Parse prepare(
      final Grammar grammar,
      final Object root,
      final Store initialStore,
      final int errorHistoryLimit,
      final int uncertaintyLimit,
      final boolean dumpAmbiguity) {
    final Parse context = new Parse(grammar, errorHistoryLimit, uncertaintyLimit, dumpAmbiguity);
    context.errorHistory = new ArrayList<>();
    grammar
        .getNode(root)
        .context(
            context,
            initialStore,
            new Parent() {
              @Override
              public void advance(final Parse step, final Store store, final Object cause) {
                if (store.hasResult()) step.results.add(store.result());
              }

              @Override
              public void error(final Parse step, final Store store, final Object cause) {
                step.errors.add(cause);
              }

              @Override
              public long size(final Parent stopAt, final long start) {
                throw new UnsupportedOperationException();
              }
            },
            "<SOF>");
    return context;
  }

  /**
   * Advance the parse through the next position
   *
   * @param position the next event to parse
   * @return parse after consuming current position, or null if EOF reached
   */
  public Parse step(final Position position) {
    if (position.isEOF()) throw new RuntimeException("Cannot step; end of file reached.");

    if (leaves.isEmpty()) return null;

    final Parse nextStep = new Parse(this);

    for (final State leaf : leaves) leaf.parse(nextStep, position);

    if (errorHistoryLimit > 0) {
      if (nextStep.errors.isEmpty()) {
        nextStep.errorHistory = errorHistory;
        if (nextStep.errorHistory == null) nextStep.errorHistory = new ArrayList<>();
      } else {
        nextStep.errorHistory = new ArrayList<>();
        nextStep.errorHistory.add(new Pair<>(position, nextStep.errors));
        for (Pair<Position, List<Object>> s : errorHistory) {
          if (nextStep.errorHistory.size() >= errorHistoryLimit) break;
          nextStep.errorHistory.add(s);
        }
      }
    }
    if (nextStep.ambiguityHistory != null) {
      int dupeCount = 0;
      final Set<String> unique = new HashSet<>();
      for (final State leaf : nextStep.leaves) {
        if (unique.contains(leaf.toString())) {
          dupeCount += 1;
        } else {
          unique.add(leaf.toString());
        }
      }
      nextStep.ambiguityHistory =
          nextStep.ambiguityHistory.push(
              new AmbiguitySample(
                  nextStep.ambiguityHistory.top().step + 1,
                  nextStep.leaves.size(),
                  position,
                  dupeCount));
    }
    if (nextStep.leaves.size() > nextStep.uncertaintyLimit)
      throw new GrammarTooUncertain(nextStep, position);
    if (nextStep.leaves.isEmpty() && nextStep.errors.size() == leaves.size())
      throw new InvalidStream(nextStep, position);

    return nextStep;
  }
}
