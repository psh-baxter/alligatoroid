package com.zarbosoft.alligatoroid.compiler.jvmshared;

import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Iterator;

import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BASTORE;
import static org.objectweb.asm.Opcodes.DSTORE;
import static org.objectweb.asm.Opcodes.FSTORE;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.LSTORE;

public abstract class JVMRWSharedCode extends JVMSharedCode {
  private final TSList<Object> children = new TSList<>();

  public static void print(MethodNode m) {
    // FIXME! DEBUG
    System.out.format("--\n");
    Textifier printer = new Textifier();
    m.accept(new TraceMethodVisitor(printer));
    PrintWriter printWriter = new PrintWriter(System.out);
    printer.print(printWriter);
    printWriter.flush();
    // FIXME! DEBUG
  }

  public JVMRWSharedCode line(Integer line) {
    if (line != null) {
      LabelNode label = new LabelNode();
      m().add(label);
      m().add(new LineNumberNode(line, label));
    }
    return this;
  }

  @Override
  public void render(MethodVisitor out, TSList<Object> initialIndexes) {
    TSList<Object> children = new TSList<>();

    // Flatten for ease of use, find last uses
    TSMap<Object, Integer> lastUses = new TSMap<>();
    ArrayDeque<Iterator<Object>> stack = new ArrayDeque<>();
    {
      Iterator<Object> iter = this.children.iterator();
      if (iter.hasNext()) stack.addLast(iter);
    }
    while (!stack.isEmpty()) {
      Object next;
      {
        Iterator<Object> iter = stack.peekLast();
        next = iter.next();
        if (!iter.hasNext()) stack.removeLast();
      }

      // Recurse other code chunks
      if (next instanceof JVMRWSharedCode) {
        Iterator<Object> iter = ((JVMRWSharedCode) next).children.iterator();
        if (iter.hasNext()) stack.addLast(iter);
        continue;
      }

      // Process element
      if (next instanceof StoreLoad) {
        lastUses.putReplace(((StoreLoad) next).key, children.size());
      }
      children.add(next);
    }

    // Render, considering
    TSList<Object> indexes = initialIndexes.mut();
    for (int i = 0; i < children.size(); i++) {
      Object child = children.get(i);
      if (child instanceof InsnList) {
        ((InsnList) child).accept(out);
      } else if (child instanceof StoreLoad) {
        Object childKey = ((StoreLoad) child).key;
        int index = -1;
        if (((StoreLoad) child).code == ISTORE
            || ((StoreLoad) child).code == LSTORE
            || ((StoreLoad) child).code == ASTORE
            || ((StoreLoad) child).code == BASTORE
            || ((StoreLoad) child).code == FSTORE
            || ((StoreLoad) child).code == DSTORE) {
          // Handle store
          for (int j = 0; j < indexes.size(); j++) {
            {
              Object lastKey;
              Integer lastIndex;
              if ((lastKey = indexes.get(j)) != null
                  && (lastIndex = lastUses.get(lastKey)) != null
                  && (lastIndex > i)) {
                continue;
              }
            }
            indexes.set(j, childKey);
            index = j;
          }
          if (index == -1) {
            index = indexes.size();
            indexes.add(childKey);
          }
        } else {
          // Handle load - must already exist
          for (int j = 0; j < indexes.size(); j++) {
            if (indexes.get(j) == childKey) {
              index = j;
              break;
            }
          }
          if (index == -1) throw new Assertion();
        }
        out.visitVarInsn(((StoreLoad) child).code, index);
      } else throw new Assertion();
    }
  }

  public JVMRWSharedCode add(AbstractInsnNode node) {
    m().add(node);
    return this;
  }

  public JVMRWSharedCode add(int opcode) {
    m().add(new InsnNode(opcode));
    return this;
  }

  public JVMRWSharedCode add(JVMSharedCode child) {
    if (child == null) throw new Assertion(); // FIXME debug
    children.add(child);
    return this;
  }

  public InsnList m() {
    Object last;
    if (children.isEmpty()) {
      children.add(last = new InsnList());
    } else {
      last = children.get(children.size() - 1);
      if (!(last instanceof InsnList)) {
        last = new InsnList();
        children.add(last);
      }
    }
    return (InsnList) last;
  }

  public JVMRWSharedCode addVarInsn(int opcode, Object key) {
    children.add(new StoreLoad(opcode, key));
    return this;
  }

  public JVMRWSharedCode addString(String value) {
    m().add(new LdcInsnNode(value));
    return this;
  }

  private static class StoreLoad {
    final int code;
    final Object key;

    public StoreLoad(int code, Object key) {
      if (key == null) {
        throw new Assertion();
      }
      this.code = code;
      this.key = key;
    }
  }
}
