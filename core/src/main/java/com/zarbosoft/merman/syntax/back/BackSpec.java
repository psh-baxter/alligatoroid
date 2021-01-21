package com.zarbosoft.merman.syntax.back;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.serialization.Write;
import com.zarbosoft.merman.misc.MultiError;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Node;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public abstract class BackSpec {
  public Parent parent = null;

  public static void walk(BackSpec root, Function<BackSpec, Boolean> consumer) {
    Deque<Iterator<BackSpec>> stack = new ArrayDeque<>();
    stack.addLast(ImmutableList.of(root).iterator());
    while (!stack.isEmpty()) {
      Iterator<BackSpec> top = stack.getLast();
      BackSpec next = top.next();
      if (!top.hasNext()) {
        stack.removeLast();
      }
      boolean cont = consumer.apply(next);
      if (cont) {
        Iterator<BackSpec> children = next.walkStep();
        if (children != null && children.hasNext()) {
          stack.addLast(children);
        }
      }
    }
  }

  protected abstract Iterator<BackSpec> walkStep();

  public abstract Node buildBackRule(Syntax syntax);

  public void finish(
          MultiError errors,
          final Syntax syntax,
          final Path typePath,
          /** Null if this is nested under an array and thus will only be consumed by that array */
          /** If immediate child is an atom, all candidates must be singular values */
          boolean singularRestriction,
          boolean typeRestriction) {}

  /**
   * @param stack
   * @param data map of ids to data (StringBuilder, Atom, List of Atom)
   * @param writer
   */
  public abstract void write(
      Deque<Write.WriteState> stack, TSMap<String, Object> data, Write.EventConsumer writer);

  /**
   * Can this represent a single value (non key/type) field in back type Subarrays can represent a
   * single value but this cannot be checked up front and may change during operation, so false
   *
   * @return
   */
  protected abstract boolean isSingularValue();

  /**
   * Does this always represent a type
   *
   * @return
   */
  protected abstract boolean isTypedValue();

  public abstract static class Parent {}

  public abstract static class PartParent extends Parent {
    public abstract BackSpec part();

    public abstract String pathSection();
  }
}
