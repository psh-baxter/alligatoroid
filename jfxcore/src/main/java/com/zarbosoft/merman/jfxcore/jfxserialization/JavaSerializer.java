package com.zarbosoft.merman.jfxcore.jfxserialization;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.zarbosoft.luxem.read.Parse;
import com.zarbosoft.luxem.read.Reader;
import com.zarbosoft.luxem.write.Writer;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.Document;
import com.zarbosoft.merman.core.editor.backevents.EArrayCloseEvent;
import com.zarbosoft.merman.core.editor.backevents.EArrayOpenEvent;
import com.zarbosoft.merman.core.editor.backevents.EKeyEvent;
import com.zarbosoft.merman.core.editor.backevents.EObjectCloseEvent;
import com.zarbosoft.merman.core.editor.backevents.EObjectOpenEvent;
import com.zarbosoft.merman.core.editor.backevents.EPrimitiveEvent;
import com.zarbosoft.merman.core.editor.backevents.ETypeEvent;
import com.zarbosoft.merman.core.editor.serialization.EventConsumer;
import com.zarbosoft.merman.core.editor.serialization.Serializer;
import com.zarbosoft.merman.core.editor.serialization.WriteState;
import com.zarbosoft.merman.core.syntax.BackType;
import com.zarbosoft.merman.core.syntax.RootAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.StackStore;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class JavaSerializer implements Serializer {
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
    return new Document(syntax, load(syntax, RootAtomType.ROOT_TYPE_ID, data, false).get(0));
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

  public Object write(ROList<Atom> atoms) {
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
                jsonGenerator.writeStartArray();
                writer = jsonEventConsumer(jsonGenerator);
                break;
              }
            default:
              throw new DeadCode();
          }
          for (final Atom atom : atoms) write(atom, writer);
          switch (backType) {
            case LUXEM:
              stream.write('\n');
              break;
            case JSON:
              jsonGenerator.writeEndArray();
              break;
            default:
              throw new DeadCode();
          }
          stream.flush();
        });
    return stream.toByteArray();
  }

  @Override
  public ROList<Atom> loadFromClipboard(Syntax syntax, String type, Object data) {
    return load(syntax, type, (byte[]) data, true);
  }

  public ROList<Atom> load(Syntax syntax, String type, byte[] data, boolean synthArrayContext) {
    switch (backType) {
      case LUXEM:
        {
          final Grammar grammar = new Grammar(syntax.getGrammar());
          grammar.add(
              data,
              new Sequence()
                  .add(StackStore.prepVarStack)
                  .add(
                      new Operator<StackStore>(
                          new Repeat(
                                  new Sequence()
                                      .add(new Reference(type))
                                      .add(StackStore.pushVarStackSingle))
                              .min(1)) {
                        @Override
                        protected StackStore process(StackStore store) {
                          final TSList<Atom> temp = TSList.of();
                          store = store.popVarSingleList(temp);
                          Collections.reverse(temp.inner_());
                          return store.pushStack(temp);
                        }
                      }));
          return new Parse<ROList<Atom>>()
              .grammar(grammar)
              .eventFactory(luxemEventFactory())
              .parse(new ByteArrayInputStream((byte[]) data));
        }
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
                          final TSList<Atom> temp = TSList.of();
                          store = store.popVarSingleList(temp);
                          Collections.reverse(temp.inner_());
                          return store.pushStack(temp);
                        }
                      }));
          return new JSONParse<ROList<Atom>>()
              .grammar(grammar)
              .parse(new ByteArrayInputStream((byte[]) data));
        }
      default:
        throw new DeadCode();
    }
  }
}
