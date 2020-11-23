package com.zarbosoft.merman.syntax;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.syntax.alignments.AlignmentDefinition;
import com.zarbosoft.merman.syntax.back.BackRootArraySpec;
import com.zarbosoft.merman.syntax.back.BackSpec;
import com.zarbosoft.merman.syntax.front.FrontSpec;
import com.zarbosoft.merman.syntax.middle.MiddleSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FreeAtomType extends AtomType {
  public String id;
  public String name;
  public int depthScore = 0;
  public List<FrontSpec> front = new ArrayList<>();
  public List<BackSpec> back = new ArrayList<>();
  public Map<String, MiddleSpec> middle = new HashMap<>();
  public Map<String, AlignmentDefinition> alignments = new HashMap<>();
  public int precedence = Integer.MAX_VALUE;
  public boolean associateForward = false;
  public int autoChooseAmbiguity = 1;

  @Override
  public Map<String, AlignmentDefinition> alignments() {
    return alignments;
  }

  @Override
  public int precedence() {
    return precedence;
  }

  @Override
  public boolean associateForward() {
    return associateForward;
  }

  @Override
  public int depthScore() {
    return depthScore;
  }

  @Override
  public void finish(
      final Syntax syntax, final Set<String> allTypes, final Set<String> scalarTypes) {
    super.finish(syntax, allTypes, scalarTypes);
    back.forEach(
        backPart -> {
          if (backPart instanceof BackRootArraySpec) {
            throw new InvalidSyntax(
                String.format(
                    "[%s] has back parts of type [%s] which may only be used in the root atom type.",
                    name, BackRootArraySpec.class.getName()));
          }
        });
  }

  @Override
  public List<FrontSpec> front() {
    return front;
  }

  @Override
  public Map<String, MiddleSpec> middle() {
    return middle;
  }

  @Override
  public List<BackSpec> back() {
    return back;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String name() {
    return name;
  }

  public Atom create(final Syntax syntax) {
    final Map<String, Value> data = new HashMap<>();
    middle.entrySet().stream().forEach(e -> data.put(e.getKey(), e.getValue().create(syntax)));
    return new Atom(this, data);
  }
}
