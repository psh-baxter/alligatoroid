package com.zarbosoft.pidgoon;

import com.zarbosoft.pidgoon.errors.GrammarTooUncertain;
import com.zarbosoft.pidgoon.errors.InvalidStream;
import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.Parent;
import com.zarbosoft.pidgoon.model.Parse;
import com.zarbosoft.pidgoon.model.Position;
import com.zarbosoft.pidgoon.model.Store;
import com.zarbosoft.rendaw.common.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Pidgoon {
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
     * @param parse
     * @param position the next event to parse
     * @return parse after consuming current position, or null if EOF reached
     */
    public static Parse step(Parse parse, final Position position) {
      if (position.isEOF()) throw new RuntimeException("Cannot step; end of file reached.");

      if (parse.leaves.isEmpty()) return null;

      final Parse nextStep = new Parse(parse);

      for (final State leaf : parse.leaves) leaf.parse(nextStep, position);

      if (parse.errorHistoryLimit > 0) {
        if (nextStep.errors.isEmpty()) {
          nextStep.errorHistory = parse.errorHistory;
          if (nextStep.errorHistory == null) nextStep.errorHistory = new ArrayList<>();
        } else {
          nextStep.errorHistory = new ArrayList<>();
          nextStep.errorHistory.add(new Pair<>(position, nextStep.errors));
          for (Pair<Position, List<Object>> s : parse.errorHistory) {
            if (nextStep.errorHistory.size() >= parse.errorHistoryLimit) break;
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
      if (nextStep.leaves.isEmpty() && nextStep.errors.size() == parse.leaves.size())
        throw new InvalidStream(nextStep, position);

      return nextStep;
    }
}
