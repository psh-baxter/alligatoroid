package com.zarbosoft.merman.editor.serialization;

import com.zarbosoft.luxem.read.Parse;
import com.zarbosoft.luxem.read.Reader;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.Document;
import com.zarbosoft.merman.editor.backevents.EArrayCloseEvent;
import com.zarbosoft.merman.editor.backevents.EArrayOpenEvent;
import com.zarbosoft.merman.editor.backevents.EKeyEvent;
import com.zarbosoft.merman.editor.backevents.EObjectCloseEvent;
import com.zarbosoft.merman.editor.backevents.EObjectOpenEvent;
import com.zarbosoft.merman.editor.backevents.EPrimitiveEvent;
import com.zarbosoft.merman.editor.backevents.ETypeEvent;
import com.zarbosoft.merman.editor.serialization.json.JSONParse;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Grammar;
import com.zarbosoft.pidgoon.Store;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.DeadCode;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Load {
  public static Document load(final Syntax syntax, final Path path)
      throws FileNotFoundException, IOException {
    try (InputStream data = Files.newInputStream(path)) {
      return load(syntax, data);
    }
  }

  public static Document load(final Syntax syntax, final InputStream data) {
    switch (syntax.backType) {
      case LUXEM:
        return new Document(
            syntax,
            new Parse<Atom>()
                .eventFactory(luxemEventFactory())
                .grammar(syntax.getGrammar())
                .eventUncertainty(1000)
                .root(syntax.root.id())
                .parse(data));
      case JSON:
        return new Document(
            syntax,
            new JSONParse<Atom>()
                .grammar(syntax.getGrammar())
                .eventUncertainty(1000)
                .root(syntax.root.id())
                .parse(data));
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

  public static Document load(final Syntax syntax, final String string) {
    return load(syntax, new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
  }

  public static List<Atom> loadMultiple(
      final Syntax syntax, final String type, final InputStream data) {
    switch (syntax.backType) {
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
                          final List<Atom> temp = new ArrayList<>();
                          store = store.popVarSingleList(temp);
                          Collections.reverse(temp);
                          return store.pushStack(temp);
                        }
                      }));
          return new Parse<List<Atom>>()
              .grammar(grammar)
              .eventFactory(luxemEventFactory())
              .root(data)
              .eventUncertainty(1000)
              .parse(data);
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
                          final List<Atom> temp = new ArrayList<>();
                          store = store.popVarSingleList(temp);
                          Collections.reverse(temp);
                          return store.pushStack(temp);
                        }
                      })
                  .add(new MatchingEventTerminal(new EArrayCloseEvent())));
          return new JSONParse<List<Atom>>()
              .grammar(grammar)
              .root(data)
              .eventUncertainty(1000)
              .parse(data);
        }
      default:
        throw new DeadCode();
    }
  }
}
