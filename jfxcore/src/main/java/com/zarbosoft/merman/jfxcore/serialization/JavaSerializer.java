package com.zarbosoft.merman.jfxcore.serialization;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.zarbosoft.luxem.read.Parse;
import com.zarbosoft.luxem.read.Reader;
import com.zarbosoft.luxem.write.Writer;
import com.zarbosoft.merman.core.AtomKey;
import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.backevents.EArrayCloseEvent;
import com.zarbosoft.merman.core.backevents.EArrayOpenEvent;
import com.zarbosoft.merman.core.backevents.EKeyEvent;
import com.zarbosoft.merman.core.backevents.EObjectCloseEvent;
import com.zarbosoft.merman.core.backevents.EObjectOpenEvent;
import com.zarbosoft.merman.core.backevents.EPrimitiveEvent;
import com.zarbosoft.merman.core.backevents.ETypeEvent;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.Document;
import com.zarbosoft.merman.core.serialization.EventConsumer;
import com.zarbosoft.merman.core.serialization.Serializer;
import com.zarbosoft.merman.core.serialization.WriteState;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.BackType;
import com.zarbosoft.merman.core.syntax.RootAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.pidgoon.errors.InvalidStreamAt;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.Position;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.MismatchCause;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.pidgoon.nodes.UnitSequence;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Format;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class JavaSerializer implements Serializer {
  public static final Reference.Key<ROList<AtomType.AtomParseResult>> ROOT_KEY =
      new Reference.Key<>();
  private final BackType backType;

  public JavaSerializer(BackType backType) {
    this.backType = backType;
  }

  private static void write(final Atom atom, final EventConsumer writer) {
    final TSList<WriteState> stack = new TSList<>();
    atom.write(stack);
    uncheck(
        () -> {
          while (!stack.isEmpty()) stack.removeLast().run(stack, writer);
        });
  }

  private static EventConsumer luxemEventConsumer(final Writer writer) {
    return new EventConsumer() {
      @Override
      public void primitive(final String value) {
        writer.primitive(value);
      }

      @Override
      public void type(final String value) {
        writer.type(value);
      }

      @Override
      public void arrayBegin() {
        writer.arrayBegin();
      }

      @Override
      public void arrayEnd() {
        writer.arrayEnd();
      }

      @Override
      public void recordBegin() {
        writer.recordBegin();
      }

      @Override
      public void recordEnd() {
        writer.recordEnd();
      }

      @Override
      public void key(final String s) {
        writer.key(s);
      }

      @Override
      public void jsonSpecialPrimitive(final String value) {
        throw new AssertionError();
      }
    };
  }

  private static EventConsumer jsonEventConsumer(final JsonGenerator generator) {
    return new EventConsumer() {
      @Override
      public void primitive(final String value) {
        uncheck(() -> generator.writeString(value));
      }

      @Override
      public void type(final String value) {
        throw new AssertionError();
      }

      @Override
      public void arrayBegin() {
        uncheck(() -> generator.writeStartArray());
      }

      @Override
      public void arrayEnd() {
        uncheck(() -> generator.writeEndArray());
      }

      @Override
      public void recordBegin() {
        uncheck(() -> generator.writeStartObject());
      }

      @Override
      public void recordEnd() {
        uncheck(() -> generator.writeEndObject());
      }

      @Override
      public void key(final String s) {
        uncheck(() -> generator.writeFieldName(s));
      }

      @Override
      public void jsonSpecialPrimitive(final String value) {
        uncheck(() -> generator.writeRaw(value));
      }
    };
  }

  private static Reader.EventFactory luxemEventFactory() {
    return new Reader.EventFactory() {
      @Override
      public Event objectOpen() {
        return new EObjectOpenEvent();
      }

      @Override
      public Event objectClose() {
        return new EObjectCloseEvent();
      }

      @Override
      public Event arrayOpen() {
        return new EArrayOpenEvent();
      }

      @Override
      public Event arrayClose() {
        return new EArrayCloseEvent();
      }

      @Override
      public Event key(final String s) {
        return new EKeyEvent(s);
      }

      @Override
      public Event type(final String s) {
        return new ETypeEvent(s);
      }

      @Override
      public Event primitive(final String s) {
        return new EPrimitiveEvent(s);
      }
    };
  }

  public Document loadDocument(Syntax syntax, byte[] data) {
    return new Document(
        syntax,
        load(
                syntax,
                new Reference<>(new AtomKey(RootAtomType.ROOT_TYPE_ID)),
                data,
                Context.UncopyContext.NONE)
            .get(0));
  }

  @Override
  public Object write(Atom atom) {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    uncheck(
        () -> {
          JsonGenerator jsonGenerator = null;
          final EventConsumer writer;
          switch (backType) {
            case LUXEM:
              writer = luxemEventConsumer(new Writer(stream, (byte) ' ', 4));
              break;
            case JSON:
              {
                jsonGenerator = new JsonFactory().createGenerator(stream);
                jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
                writer = jsonEventConsumer(jsonGenerator);
                break;
              }
            default:
              throw new DeadCode();
          }
          write(atom, writer);
          if (backType == BackType.JSON) jsonGenerator.flush();
          stream.write('\n');
          stream.flush();
        });
    return stream.toByteArray();
  }

  public Object write(Context.CopyContext copyContext, ROList<Atom> atoms) {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    uncheck(
        () -> {
          JsonGenerator jsonGenerator = null;
          final EventConsumer writer;
          switch (backType) {
            case LUXEM:
              writer = luxemEventConsumer(new Writer(stream, (byte) ' ', 4));
              switch (copyContext) {
                case NONE:
                case ARRAY:
                  break;
                case RECORD:
                  writer.recordBegin();
                  break;
              }
              break;
            case JSON:
              {
                jsonGenerator = new JsonFactory().createGenerator(stream);
                jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
                switch (copyContext) {
                  case NONE:
                    break;
                  case RECORD:
                    jsonGenerator.writeStartObject();
                    break;
                  case ARRAY:
                    jsonGenerator.writeStartArray();
                    break;
                }
                writer = jsonEventConsumer(jsonGenerator);
                break;
              }
            default:
              throw new DeadCode();
          }
          for (final Atom atom : atoms) {
            write(atom, writer);
          }
          switch (backType) {
            case LUXEM:
              {
                switch (copyContext) {
                  case NONE:
                  case ARRAY:
                    break;
                  case RECORD:
                    writer.recordEnd();
                    break;
                }
                stream.write('\n');
                break;
              }
            case JSON:
              {
                switch (copyContext) {
                  case NONE:
                    break;
                  case RECORD:
                    jsonGenerator.writeEndObject();
                    break;
                  case ARRAY:
                    jsonGenerator.writeEndArray();
                    break;
                }
                jsonGenerator.flush();
                break;
              }
            default:
              throw new DeadCode();
          }
          stream.flush();
        });
    return stream.toByteArray();
  }

  @Override
  public ROList<Atom> loadFromClipboard(
      Syntax syntax, Context.UncopyContext uncopyContext, String type, Object data) {
    return load(syntax, syntax.backRuleRef(type), (byte[]) data, uncopyContext);
  }

  public ROList<Atom> load(
      Syntax syntax,
      Node<AtomType.AtomParseResult> type,
      byte[] data,
      Context.UncopyContext uncopyContext) {
    try {
      switch (backType) {
        case LUXEM:
          {
            final Grammar grammar = new Grammar(syntax.getGrammar());
            switch (uncopyContext) {
              case MAYBE_ARRAY:
              case NONE:
                grammar.add(ROOT_KEY, new Repeat<>(type));
                break;
              case RECORD:
                grammar.add(
                    ROOT_KEY,
                    new UnitSequence<ROList<AtomType.AtomParseResult>>()
                        .addIgnored(new MatchingEventTerminal(new EObjectOpenEvent()))
                        .add(new Repeat<>(type))
                        .addIgnored(new MatchingEventTerminal(new EObjectCloseEvent())));
                break;
            }
            ROList<AtomType.AtomParseResult> result =
                new Parse<ROList<AtomType.AtomParseResult>>(ROOT_KEY)
                    .grammar(grammar)
                    .eventFactory(luxemEventFactory())
                    .parse(new ByteArrayInputStream((byte[]) data));
            TSList<Atom> finalOut = new TSList<>();
            for (AtomType.AtomParseResult e : result) {
              finalOut.add(e.finish());
            }
            return finalOut;
          }
        case JSON:
          {
            final Grammar grammar = new Grammar(syntax.getGrammar());
            switch (uncopyContext) {
              case NONE:
                {
                  grammar.add(
                      ROOT_KEY,
                      new Operator<AtomType.AtomParseResult, ROList<AtomType.AtomParseResult>>(
                          type) {
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
                                  AtomType.AtomParseResult, ROList<AtomType.AtomParseResult>>(
                                  type) {
                                @Override
                                protected ROList<AtomType.AtomParseResult> process(
                                    AtomType.AtomParseResult value) {
                                  return TSList.of(value);
                                }
                              }));
                  break;
                }
            }
            ROList<AtomType.AtomParseResult> result =
                new JSONParse<ROList<AtomType.AtomParseResult>>(ROOT_KEY)
                    .grammar(grammar)
                    .parse(new ByteArrayInputStream((byte[]) data));
            TSList<Atom> finalOut = new TSList<>();
            for (AtomType.AtomParseResult e : result) {
              finalOut.add(e.finish());
            }
            return finalOut;
          }
        default:
          throw new DeadCode();
      }
    } catch (InvalidStreamAt e) {
      StringBuilder message = new StringBuilder();
      for (MismatchCause error : (ROList<MismatchCause>) e.step.errors) {
        message.append(Format.format(" * %s\n", error));
      }
      throw new RuntimeException(
          Format.format(
              "Clipboard contents don't conform to syntax tree\nat %s %s\nmismatches at final stream element:\n%s",
              ((Position) e.at).at, ((Position) e.at).event, message.toString()));
    }
  }
}
