package com.zarbosoft.merman.webview;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.Document;
import com.zarbosoft.merman.editor.backevents.BackEvent;
import com.zarbosoft.merman.editor.backevents.EArrayCloseEvent;
import com.zarbosoft.merman.editor.backevents.EArrayOpenEvent;
import com.zarbosoft.merman.editor.backevents.EObjectCloseEvent;
import com.zarbosoft.merman.editor.backevents.EObjectOpenEvent;
import com.zarbosoft.merman.editor.backevents.JSpecialPrimitiveEvent;
import com.zarbosoft.merman.editor.serialization.EventConsumer;
import com.zarbosoft.merman.editor.serialization.WriteState;
import com.zarbosoft.merman.syntax.BackType;
import com.zarbosoft.merman.syntax.RootAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.webview.serialization.JSEventConsumer;
import com.zarbosoft.merman.webview.serialization.JsonEventConsumer;
import com.zarbosoft.pidgoon.Grammar;
import com.zarbosoft.pidgoon.events.ParseBuilder;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;
import def.js.Array;
import def.js.JSON;
import def.js.Number;
import def.js.Object;
import def.js.String;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class JSSerializer implements com.zarbosoft.merman.editor.serialization.Serializer {
  private final BackType backType;

  public JSSerializer(BackType backType) {
    this.backType = backType;
    if (backType == BackType.LUXEM) throw new Assertion();
  }

  private static void write(final Atom atom, final EventConsumer writer) {
    final TSList<WriteState> stack = new TSList<>();
    atom.write(stack);
    uncheck(
        () -> {
          while (!stack.isEmpty()) stack.removeLast().run(stack, writer);
        });
  }

  @Override
  public byte[] write(Atom atom) {
    return uncheck(
        () -> {
          final JSEventConsumer writer;
          switch (backType) {
            case LUXEM:
              // TODO, dead atm
              throw new Assertion();
            case JSON:
              {
                writer = new JsonEventConsumer();
                break;
              }
            default:
              throw new DeadCode();
          }
          write(atom, writer);
          return writer.resultOne();
        });
  }

  public byte[] write(ROList<Atom> atoms) {
    return uncheck(
        () -> {
          final JSEventConsumer writer;
          switch (backType) {
            case LUXEM:
              // TODO, dead atm
              throw new Assertion();
            case JSON:
              {
                writer = new JsonEventConsumer();
                break;
              }
            default:
              throw new DeadCode();
          }
          for (final Atom atom : atoms) write(atom, writer);
          return writer.resultMany();
        });
  }

  private void walkJSJson(TSList<BackEvent> events, java.lang.Object o) {
    if (o instanceof Double) {
      events.add(new JSpecialPrimitiveEvent(((Double) o).toString()));
    } else if (o instanceof java.lang.Boolean) {
      events.add(new JSpecialPrimitiveEvent(((Boolean) o).toString()));
    } else if (o instanceof java.lang.String) {
      events.add(new JSpecialPrimitiveEvent(((java.lang.String) o).toString()));
    } else if (o instanceof Array) {
      events.add(new EArrayOpenEvent());
      for (int i = 0; i < ((Array<?>) o).length; ++i) {
        walkJSJson(events, ((Array<?>) o).$get(i));
      }
      events.add(new EArrayCloseEvent());
    } else if (o instanceof Object) {
      events.add(new EObjectOpenEvent());
      for (String key : Object.keys(o)) {
        if (!((Object) o).hasOwnProperty(key)) continue;
        walkJSJson(events, ((Object) o).$get(key));
      }
      events.add(new EObjectCloseEvent());
    } else throw new Assertion();
  }

  public Document loadDocument(Syntax syntax, java.lang.String data) {
    return new Document(syntax, load(syntax, RootAtomType.ROOT_TYPE_ID, data).get(0));
  }

  @Override
  public ROList<Atom> loadFromClipboard(
      Syntax syntax, java.lang.String type, java.lang.Object data) {
    return load(syntax, type, (java.lang.String) data);
  }

  public ROList<Atom> load(Syntax syntax, java.lang.String type, java.lang.String data) {
    switch (backType) {
      case LUXEM:
        // TODO, dead atm
        throw new Assertion();
      case JSON:
        {
          TSList<BackEvent> events = new TSList<>();
          walkJSJson(events, JSON.parse(data));
          final Grammar grammar = new Grammar(syntax.getGrammar());
          grammar.add(
              data,
              new Sequence()
                  .add(StackStore.prepVarStack)
                  .add(new MatchingEventTerminal(new EArrayOpenEvent()))
                  .add(StackStore.prepVarStack)
                  .add(
                      new Operator<StackStore>(
                          new Repeat(
                                  new Sequence()
                                      .add(new Reference(type))
                                      .add(StackStore.pushVarStackSingle))
                              .max(7)) {
                        @Override
                        protected StackStore process(StackStore store) {
                          final TSList<Atom> temp = TSList.of();
                          store = store.popVarSingleList(temp);
                          Collections.reverse(temp.inner_());
                          return store.pushStack(temp);
                        }
                      })
                  .add(new MatchingEventTerminal(new EArrayCloseEvent())));
          return new ParseBuilder<ROList<Atom>>()
              .grammar(grammar)
              .root(data)
              .parse(events.inner_());
        }
      default:
        throw new DeadCode();
    }
  }
}
