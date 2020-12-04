package com.zarbosoft.merman.document;

import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.serialization.Write;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.editor.visual.tags.TypeTag;
import com.zarbosoft.merman.editor.visual.visuals.VisualAtom;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.rendaw.common.Assertion;
import org.pcollections.PSet;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class Atom {
  public final TSMap<String, Value> fields;
  public Value.Parent parent;
  public AtomType type;
  public VisualAtom visual;
  public PSet<Tag> tags;

  public Atom(final AtomType type, final TSMap<String, Value> fields) {
    this.type = type;
    this.fields = fields;
    tags = Context.asFreeTags(type.tags).plus(new TypeTag(type.id()));
    for (Map.Entry<String, Value> entry : fields.entries()) {
      Value v = entry.getValue();
      v.setParent(
          new Parent() {
            @Override
            public Atom atom() {
              return Atom.this;
            }

            @Override
            public boolean selectUp(final Context context) {
              if (parent == null) return false;
              return Atom.this.parent.selectUp(context);
            }

            @Override
            public Path getSyntaxPath() {
              Path out;
              if (Atom.this.parent == null) out = new Path();
              else out = Atom.this.parent.getSyntaxPath();
              return out.add(entry.getKey());
            }
          });
    }
  }

  public Path getSyntaxPath() {
    if (parent == null) return new Path();
    else return parent.path();
  }

  public Visual createVisual(
      final Context context,
      final VisualParent parent,
      final Map<String, Alignment> alignments,
      final int depth,
      final int depthScore) {
    if (visual != null) {
      visual.root(context, parent, alignments, depth, depthScore);
    } else {
      this.visual = new VisualAtom(context, parent, this, alignments, depth, depthScore);
    }
    return visual;
  }

  public void setParent(final Value.Parent parent) {
    this.parent = parent;
  }

  public Object syntaxLocateStep(String segment) {
    return fields.getOpt(segment);
  }

  public void write(Deque<Write.WriteState> stack) {
    TSMap<String, Object> childData = new TSMap<>();
    for (Map.Entry<String, Value> entry : fields.entries()) {
      if (entry.getValue() instanceof ValueAtom) {
        childData.put(entry.getKey(), ((ValueAtom) entry.getValue()).data);
      } else if (entry.getValue() instanceof ValueArray) {
        childData.put(entry.getKey(), ((ValueArray) entry.getValue()).data);
      } else if (entry.getValue() instanceof ValuePrimitive) {
        childData.put(entry.getKey(), ((ValuePrimitive) entry.getValue()).data);
      } else throw new Assertion();
    }
    stack.addLast(new Write.WriteStateBack(childData, type.back().iterator()));
  }

  public abstract static class Parent {
    public abstract Atom atom();

    public abstract boolean selectUp(final Context context);

    /**
     * Get path relative to syntax structure
     *
     * @return
     */
    public abstract Path getSyntaxPath();
  }
}
