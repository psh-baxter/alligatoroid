package com.zarbosoft.pidgoon.model;

import com.zarbosoft.pidgoon.AmbiguitySample;
import com.zarbosoft.pidgoon.BranchingStack;
import com.zarbosoft.pidgoon.Stats;
import com.zarbosoft.rendaw.common.Pair;

import java.util.ArrayList;
import java.util.List;

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

  public abstract static class State {
    protected State() {}

    /**
     * The current color of this branch, as set by a Color node
     *
     * @param <T>
     * @return
     */
    public abstract <T> T color();

    public abstract void parse(Parse step, Position position);
  }
}
