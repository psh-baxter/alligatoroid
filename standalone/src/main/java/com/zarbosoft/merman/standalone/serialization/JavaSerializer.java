package com.zarbosoft.merman.standalone.serialization;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.zarbosoft.luxem.read.Parse;
import com.zarbosoft.luxem.read.Reader;
import com.zarbosoft.luxem.write.Writer;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.backevents.EArrayCloseEvent;
import com.zarbosoft.merman.editor.backevents.EArrayOpenEvent;
import com.zarbosoft.merman.editor.backevents.EKeyEvent;
import com.zarbosoft.merman.editor.backevents.EObjectCloseEvent;
import com.zarbosoft.merman.editor.backevents.EObjectOpenEvent;
import com.zarbosoft.merman.editor.backevents.EPrimitiveEvent;
import com.zarbosoft.merman.editor.backevents.ETypeEvent;
import com.zarbosoft.merman.editor.serialization.EventConsumer;
import com.zarbosoft.merman.editor.serialization.WriteState;
import com.zarbosoft.merman.syntax.BackType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.events.StackStore;
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

public class JavaSerializer implements com.zarbosoft.merman.editor.serialization.Serializer {
  private final BackType backType;

  public JavaSerializer(BackType backType) {
    this.backType = backType;
  }

  @Override
  public byte[] write(Atom atom) {
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

  public byte[] write(ROList<Atom> atoms) {
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

  @Override
  public ROList<Atom> load(Syntax syntax, String type, byte[] data) {
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
                          store = store.popVarSingleList(temp.inner_());
                          Collections.reverse(temp.inner_());
                          return store.pushStack(temp);
                        }
                      }));
          return new Parse<ROList<Atom>>()
              .grammar(grammar)
              .eventFactory(luxemEventFactory())
              .root(data)
              .eventUncertainty(1000)
              .parse(new ByteArrayInputStream(data));
        }
      case JSON:
        {
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
                          store = store.popVarSingleList(temp.inner_());
                          Collections.reverse(temp.inner_());
                          return store.pushStack(temp);
                        }
                      })
                  .add(new MatchingEventTerminal(new EArrayCloseEvent())));
          return new JSONParse<ROList<Atom>>()
              .grammar(grammar)
              .root(data)
              .eventUncertainty(1000)
              .parse(new ByteArrayInputStream(data));
        }
      default:
        throw new DeadCode();
    }
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
}
