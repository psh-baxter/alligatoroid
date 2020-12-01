package com.zarbosoft.merman.syntax;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.alignments.AlignmentDefinition;
import com.zarbosoft.merman.syntax.back.BackSpec;
import com.zarbosoft.merman.syntax.front.FrontSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RootAtomType extends AtomType {
  public List<FrontSpec> front = new ArrayList<>();
  public List<BackSpec> back = new ArrayList<>();
  public Map<String, AlignmentDefinition> alignments = new HashMap<>();

  @Override
  public Map<String, AlignmentDefinition> alignments() {
    return alignments;
  }

  @Override
  public int precedence() {
    return -Integer.MAX_VALUE;
  }

  @Override
  public boolean associateForward() {
    return false;
  }

  @Override
  public int depthScore() {
    return 0;
  }

  @Override
  public List<FrontSpec> front() {
    return front;
  }

  @Override
  public List<BackSpec> back() {
    return back;
  }

  @Override
  public String id() {
    return "root";
  }

  @Override
  public String name() {
    return "root array";
  }

  public Atom create(final Syntax syntax) {
    final TSMap<String, Value> data = new TSMap<>();
    fields.entrySet().stream().forEach(e -> data.put(e.getKey(), e.getValue().create(syntax)));
    return new Atom(this, data);
  }
}
