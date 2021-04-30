package com.zarbosoft.merman.webview;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.Document;
import com.zarbosoft.merman.core.document.fields.Field;
import com.zarbosoft.merman.core.BackPath;
import com.zarbosoft.merman.core.backevents.BackEvent;
import com.zarbosoft.merman.core.backevents.EArrayCloseEvent;
import com.zarbosoft.merman.core.backevents.EArrayOpenEvent;
import com.zarbosoft.merman.core.backevents.EKeyEvent;
import com.zarbosoft.merman.core.backevents.EObjectCloseEvent;
import com.zarbosoft.merman.core.backevents.EObjectOpenEvent;
import com.zarbosoft.merman.core.backevents.EPrimitiveEvent;
import com.zarbosoft.merman.core.backevents.JSpecialPrimitiveEvent;
import com.zarbosoft.merman.core.serialization.EventConsumer;
import com.zarbosoft.merman.core.serialization.Serializer;
import com.zarbosoft.merman.core.serialization.WriteState;
import com.zarbosoft.merman.core.syntax.BackType;
import com.zarbosoft.merman.core.syntax.RootAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.webview.serialization.JSEventConsumer;
import com.zarbosoft.merman.webview.serialization.JsonEventConsumer;
import com.zarbosoft.pidgoon.events.ParseBuilder;
import com.zarbosoft.pidgoon.events.StackStore;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;
import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.core.JsObject;
import jsinterop.base.JsPropertyMap;

import java.util.Collections;
import java.util.Map;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class JSSerializer implements Serializer {
  private final BackType backType;
  private final ROList<String> prioritizeKeys;

  public JSSerializer(BackType backType, ROList<String> prioritizeKeys) {
    this.backType = backType;
    this.prioritizeKeys = prioritizeKeys;
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
  public String write(Atom atom) {
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

  public String write(ROList<Atom> atoms) {
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

  private void walkJSJson(TSList<ROPair<BackEvent, BackPath>> events, Object o, BackPath path) {
    if (o == null) {
      events.add(new ROPair<>(new JSpecialPrimitiveEvent("null"), path));
    } else if (o instanceof Double) {
      events.add(new ROPair<>(new JSpecialPrimitiveEvent(((Double) o).toString()), path));
    } else if (o instanceof java.lang.Boolean) {
      events.add(new ROPair<>(new JSpecialPrimitiveEvent(((Boolean) o).toString()), path));
    } else if (o instanceof java.lang.String) {
      events.add(new ROPair<>(new EPrimitiveEvent(((java.lang.String) o).toString()), path));
    } else if (o instanceof JsArray) {
      events.add(new ROPair<>(new EArrayOpenEvent(), path));
      for (int i = 0; i < ((JsArray<?>) o).length; ++i) {
        walkJSJson(events, ((JsArray<?>) o).getAt(i), path.add(i));
      }
      events.add(new ROPair<>(new EArrayCloseEvent(), path));
    } else if (o instanceof JsObject) {
      events.add(new ROPair<>(new EObjectOpenEvent(), path));
      JsArray<String> keys0 = JsObject.keys(o);
      TSMap<String, Integer> keys = new TSMap<>();
      for (int i = 0; i < keys0.length; ++i) {
        String key = keys0.getAt(i);
        if (!((JsObject) o).hasOwnProperty(key)) continue;
        keys.put(key, i);
      }
      for (String key : prioritizeKeys) {
        Integer i = keys.remove(key);
        if (i == null) continue;
        events.add(new ROPair<>(new EKeyEvent(key), path));
        walkJSJson(events, ((JsPropertyMap) o).get(key), path.add(i));
      }
      for (Map.Entry<String, Integer> pair : keys) {
        events.add(new ROPair<>(new EKeyEvent(pair.getKey()), path));
        walkJSJson(events, ((JsPropertyMap) o).get(pair.getKey()), path.add(pair.getValue()));
      }
      events.add(new ROPair<>(new EObjectCloseEvent(), path));
    } else throw new Assertion();
  }

  public Document loadDocument(Syntax syntax, java.lang.String data) {
    return new Document(syntax, load(syntax, RootAtomType.ROOT_TYPE_ID, data, false).get(0));
  }

  @Override
  public ROList<Atom> loadFromClipboard(
      Syntax syntax, java.lang.String type, java.lang.Object data) {
    return load(syntax, type, (java.lang.String) data, true);
  }

  public ROList<Atom> load(Syntax syntax, String type, String data, boolean synthArrayContext) {
    switch (backType) {
      case LUXEM:
        // TODO, dead atm
        throw new Assertion();
      case JSON:
        {
          final Grammar grammar = new Grammar(syntax.getGrammar());
          grammar.add(
              Grammar.DEFAULT_ROOT_KEY,
              new Sequence()
                  .add(
                      synthArrayContext
                          ? new Sequence()
                              .add(new MatchingEventTerminal(new EArrayOpenEvent()))
                              .add(StackStore.prepVarStack)
                              .add(
                                  new Repeat(
                                      new Sequence()
                                          .add(new Reference(type))
                                          .add(StackStore.pushVarStackSingle)))
                              .add(new MatchingEventTerminal(new EArrayCloseEvent()))
                          : new Sequence()
                              .add(new Reference(type))
                              .add(
                                  new Operator<StackStore>() {
                                    @Override
                                    protected StackStore process(StackStore store) {
                                      return store.pushStack(1);
                                    }
                                  }))
                  .add(
                      new Operator<StackStore>() {
                        @Override
                        protected StackStore process(StackStore store) {
                          final TSList<ROPair<Atom, ROList<ROPair<Field, Object>>>> temp =
                              TSList.of();
                          store = store.popVarSingleList(temp);
                          Collections.reverse(temp.inner_());
                          return store.pushStack(temp);
                        }
                      }));
          TSList<ROPair<BackEvent, BackPath>> events = new TSList<>();
          walkJSJson(events, Global.JSON.parse(data), BackPath.root);
          ROList<ROPair<Atom, ROMap<String, ROPair<Field, Object>>>> result =
              new ParseBuilder<ROList<ROPair<Atom, ROMap<String, ROPair<Field, Object>>>>>()
                  .grammar(grammar)
                  .parsePosition(events);
          TSList<Atom> finalOut = new TSList<>();
          for (ROPair<Atom, ROMap<String, ROPair<Field, Object>>> e : result) {
            finalOut.add(Serializer.initialSet(e));
          }
          return finalOut;
          /*
          {
            ParseEventSink<ROList<Atom>> eventStream =
                new ParseBuilder<ROList<Atom>>().grammar(grammar).parse();
            for (ROPair<? extends Event, ?> pair : events) {
              eventStream = eventStream.push(pair.first, pair.second);
              DomGlobal.console.log(Format.format("consumed %s %s", pair.first, pair.second));
              for (Parse.State leaf : eventStream.context().leaves) {
                DomGlobal.console.log(Format.format(" * %s %s", leaf, leaf.color()));
              }
            }
            return eventStream.result();
          }
           */
        }
      default:
        throw new DeadCode();
    }
  }
}
