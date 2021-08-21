package com.zarbosoft.alligatoroid.compiler.jvmshared;

import com.zarbosoft.rendaw.common.Assertion;
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
import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BASTORE;
import static org.objectweb.asm.Opcodes.DSTORE;
import static org.objectweb.asm.Opcodes.FSTORE;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.LSTORE;

public abstract class JVMRWSharedCode extends JVMSharedCode {
  private static final Object scopePush = new Object();
  private static final Object scopePop = new Object();
  private final List<Object> children = new ArrayList<>();

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
  protected void render(JVMSharedCode.Scope scope, MethodVisitor out) {
    for (Object child : children) {
      if (child instanceof JVMSharedCode) {
        ((JVMSharedCode) child).render(scope, out);
      } else if (child instanceof InsnList) {
        // print((InsnList) child);
        ((InsnList) child).accept(out);
      } else if (child == scopePush) {
        scope = new JVMSharedCode.Scope(scope);
      } else if (child == scopePop) {
        scope = scope.parent;
      } else if (child instanceof StoreLoad) {
        int i = 0;
        Object childKey = ((StoreLoad) child).key;
        if (((StoreLoad) child).code == ISTORE
            || ((StoreLoad) child).code == LSTORE
            || ((StoreLoad) child).code == ASTORE
            || ((StoreLoad) child).code == BASTORE
            || ((StoreLoad) child).code == FSTORE
            || ((StoreLoad) child).code == DSTORE) {
          // Handle store
          int firstNull = -1;
          for (; i < scope.indexes.size(); ++i) {
            Object key = scope.indexes.get(i);
            if (key == null && firstNull == -1) {
              firstNull = i;
            } else if (key == childKey) {
              break;
            }
          }
          if (i == scope.indexes.size()) {
            if (firstNull == -1) {
              scope.indexes.add(childKey);
            } else {
              scope.indexes.set(firstNull, childKey);
            }
          }
        } else {
          // Handle load - must already exist
          i = findIndex(scope, childKey);
        }
        out.visitVarInsn(((StoreLoad) child).code, i);
      } else if (child instanceof Drop) {
        int i = findIndex(scope, ((Drop) child).key);
        scope.indexes.set(i, null);
      } else throw new Assertion();
    }
  }

  private int findIndex(JVMSharedCode.Scope scope, Object childKey) {
    int i = 0;
    for (; i < scope.indexes.size(); ++i) {
      if (scope.indexes.get(i) == childKey) break;
    }
    if (i == scope.indexes.size()) {
      // throw new Assertion();
      // FIXME! DEBUG
      return 99;
    }
    return i;
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

  public JVMRWSharedCode addScoped(JVMSharedCode child) {
    children.add(scopePush);
    children.add(child);
    children.add(scopePop);
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

  public JVMRWSharedCode addDrop(Object key) {
    children.add(new Drop(key));
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
      this.code = code;
      this.key = key;
    }
  }

  private static class Drop {
    final Object key;

    private Drop(Object key) {
      this.key = key;
    }
  }
}
