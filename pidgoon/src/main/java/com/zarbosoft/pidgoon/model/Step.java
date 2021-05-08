package com.zarbosoft.pidgoon.model;

import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

/**
 * This manages the state of a parse. This can be used without a Parse object for manually driven
 * parses.
 */
public class Step<R> {
  /** An error for each branch that failed in the previous step of the parse. */
  public final TSList<MismatchCause> errors = new TSList<>();

  /** This represents the tip node of each branch. */
  public final TSList<Branch> branches = new TSList<>();

  public final TSList<R> completed = new TSList<>();
  /**
   * This represents the top value of the stack of branches that matched in the previous step of the
   * parse.
   */
  public final TSList<Object> results = new TSList<>();

  public abstract static class Branch<E> {
    protected Branch() {}

    /**
     * The current color of this branch, as set by a Color node
     *
     * @param <T>
     * @return
     */
    public abstract <T> T color();

    public abstract void parse(Grammar grammar, Step step, E event);
  }
}
