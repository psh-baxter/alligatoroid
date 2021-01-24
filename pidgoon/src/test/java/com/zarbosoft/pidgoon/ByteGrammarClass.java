package com.zarbosoft.pidgoon;

import com.zarbosoft.pidgoon.bytes.BytesHelper;
import com.zarbosoft.pidgoon.bytes.ParseBuilder;
import com.zarbosoft.pidgoon.bytes.Position;
import com.zarbosoft.pidgoon.parse.Parse;
import com.zarbosoft.rendaw.common.Pair;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class ByteGrammarClass {
  private static final Grammar branchlessGrammar;

  static {
    branchlessGrammar = new Grammar();
    branchlessGrammar.add("root", BytesHelper.stringSequence("talking"));
  }

  @Test
  public void longestMatch0() {
    final Pair<Parse, Position> results =
        new ParseBuilder<>()
            .grammar(branchlessGrammar)
            .longestMatchFromStart(
                new ByteArrayInputStream("quiz".getBytes(StandardCharsets.UTF_8)));
    assertThat(results.second.absolute, equalTo(0L));
  }

  @Test
  public void longestMatchMid() {
    final Pair<Parse, Position> results =
        new ParseBuilder<>()
            .grammar(branchlessGrammar)
            .longestMatchFromStart(new ByteArrayInputStream("t".getBytes(StandardCharsets.UTF_8)));
    assertThat(results.second.absolute, equalTo(1L));
  }

  @Test
  public void longestMatchMid2() {
    final Pair<Parse, Position> results =
        new ParseBuilder<>()
            .grammar(branchlessGrammar)
            .longestMatchFromStart(
                new ByteArrayInputStream("talkin".getBytes(StandardCharsets.UTF_8)));
    assertThat(results.second.absolute, equalTo(6L));
  }

  @Test
  public void longestMatchFull() {
    final Pair<Parse, Position> results =
        new ParseBuilder<>()
            .grammar(branchlessGrammar)
            .longestMatchFromStart(
                new ByteArrayInputStream("talking".getBytes(StandardCharsets.UTF_8)));
    assertThat(results.second.absolute, equalTo(7L));
  }

  @Test
  public void longestMatchOver() {
    final Pair<Parse, Position> results =
        new ParseBuilder<>()
            .grammar(branchlessGrammar)
            .longestMatchFromStart(
                new ByteArrayInputStream("talkinger".getBytes(StandardCharsets.UTF_8)));
    assertThat(results.second.absolute, equalTo(7L));
  }
}
