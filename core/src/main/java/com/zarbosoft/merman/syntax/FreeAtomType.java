package com.zarbosoft.merman.syntax;

import com.zarbosoft.merman.syntax.alignments.AlignmentDefinition;
import com.zarbosoft.merman.syntax.back.BackSubArraySpec;
import com.zarbosoft.merman.syntax.back.BackSpec;
import com.zarbosoft.merman.syntax.front.FrontSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FreeAtomType extends AtomType {
  public String id;
  public String name;
  public int depthScore = 0;
  public List<FrontSpec> front = new ArrayList<>();
  public List<BackSpec> back = new ArrayList<>();
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
  public List<FrontSpec> front() {
    return front;
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
}
