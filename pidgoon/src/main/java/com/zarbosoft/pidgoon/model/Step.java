package com.zarbosoft.pidgoon.model;

import com.zarbosoft.rendaw.common.TSList;

/**
 * This manages the state of a parse. This can be used without a Parse object for manually driven
 * parses.
 */
public class Step<R> {
  /** An error for each branch that failed in the previous step of the parse. */
  public final TSList<MismatchCause> errors = new TSList<>();

  /** This represents the tip node of each branch. */
  public final TSList<Leaf> leaves = new TSList<>();

  /** This is the results of any leaves that completed from the previous step. */
  public final TSList<R> completed = new TSList<>();
}
