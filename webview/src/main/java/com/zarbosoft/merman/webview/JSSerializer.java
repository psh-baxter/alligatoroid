package com.zarbosoft.merman.webview;

import com.zarbosoft.merman.core.AtomKey;
import com.zarbosoft.merman.core.BackPath;
import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.backevents.BackEvent;
import com.zarbosoft.merman.core.backevents.EArrayCloseEvent;
import com.zarbosoft.merman.core.backevents.EArrayOpenEvent;
import com.zarbosoft.merman.core.backevents.EKeyEvent;
import com.zarbosoft.merman.core.backevents.EObjectCloseEvent;
import com.zarbosoft.merman.core.backevents.EObjectOpenEvent;
import com.zarbosoft.merman.core.backevents.EPrimitiveEvent;
import com.zarbosoft.merman.core.backevents.JSpecialPrimitiveEvent;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.Document;
import com.zarbosoft.merman.core.serialization.EventConsumer;
import com.zarbosoft.merman.core.serialization.Serializer;
import com.zarbosoft.merman.core.serialization.WriteState;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.BackType;
import com.zarbosoft.merman.core.syntax.RootAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.webview.serialization.JSEventConsumer;
import com.zarbosoft.merman.webview.serialization.JsonEventConsumer;
import com.zarbosoft.pidgoon.events.ParseBuilder;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.pidgoon.nodes.UnitSequence;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;
import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.core.JsObject;
import jsinterop.base.JsPropertyMap;

import java.util.Map;

import static com.zarbosoft.merman.core.Context.UncopyContext.NONE;
import static com.zarbosoft.rendaw.common.Common.uncheck;

public class JSSerializer implements Serializer {
  public static final Reference.Key<ROList<AtomType.AtomParseResult>> ROOT_KEY =
      new Reference.Key<>();
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

  public String write(Context.CopyContext copyContext, ROList<Atom> atoms) {
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
        Integer i = keys.removeGet(key);
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
    return new Document(
        syntax,
        load(syntax, new Reference<>(new AtomKey(RootAtomType.ROOT_TYPE_ID)), data, NONE).get(0));
  }

  @Override
  public ROList<Atom> loadFromClipboard(
      Syntax syntax, Context.UncopyContext uncopyContext, String type, Object data) {
    return load(syntax, syntax.backRuleRef(type), (java.lang.String) data, uncopyContext);
  }

  public ROList<Atom> load(
      Syntax syntax,
      Node<AtomType.AtomParseResult> type,
      String data,
      Context.UncopyContext uncopyContext) {
    switch (backType) {
      case LUXEM:
        // TODO, dead atm
        throw new Assertion();
      case JSON:
        {
          final Grammar grammar = new Grammar(syntax.getGrammar());
          switch (uncopyContext) {
            case NONE:
              {
                grammar.add(
                    ROOT_KEY,
                    new Operator<AtomType.AtomParseResult, ROList<AtomType.AtomParseResult>>(type) {
                      @Override
                      protected ROList<AtomType.AtomParseResult> process(
                          AtomType.AtomParseResult value) {
                        return TSList.of(value);
                      }
                    });
                break;
              }
            case RECORD:
              {
                grammar.add(
                    ROOT_KEY,
                    new UnitSequence<ROList<AtomType.AtomParseResult>>()
                        .addIgnored(new MatchingEventTerminal<>(new EObjectOpenEvent()))
                        .add(new Repeat<AtomType.AtomParseResult>(type))
                        .addIgnored(new MatchingEventTerminal<>(new EObjectCloseEvent())));
                break;
              }
            case MAYBE_ARRAY:
              {
                grammar.add(
                    ROOT_KEY,
                    new Union<ROList<AtomType.AtomParseResult>>()
                        .add(
                            new UnitSequence<ROList<AtomType.AtomParseResult>>()
                                .addIgnored(new MatchingEventTerminal<>(new EArrayOpenEvent()))
                                .add(new Repeat<AtomType.AtomParseResult>(type))
                                .addIgnored(new MatchingEventTerminal<>(new EArrayCloseEvent())))
                        .add(
                            new Operator<
                                AtomType.AtomParseResult, ROList<AtomType.AtomParseResult>>(type) {
                              @Override
                              protected ROList<AtomType.AtomParseResult> process(
                                  AtomType.AtomParseResult value) {
                                return TSList.of(value);
                              }
                            }));
                break;
              }
          }
          TSList<ROPair<BackEvent, BackPath>> events = new TSList<>();
          walkJSJson(events, Global.JSON.parse(data), BackPath.root);
          ROList<AtomType.AtomParseResult> result =
              new ParseBuilder<ROList<AtomType.AtomParseResult>>(ROOT_KEY)
                  .grammar(grammar)
                  .parsePosition(events);
          TSList<Atom> finalOut = new TSList<>();
          for (AtomType.AtomParseResult e : result) {
            finalOut.add(e.finish());
          }
          return finalOut;
        }
      default:
        throw new DeadCode();
    }
  }
}
