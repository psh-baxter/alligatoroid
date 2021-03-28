package com.zarbosoft.luxem;

import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TreeReaderTest {
  @Test
  public void testEmpty() {
    assertTrue(Luxem.parse("").isEmpty());
  }

  @Test
  public void testPrimitive() {
    assertThat(Luxem.parse("dog"), equalTo(Arrays.asList("dog")));
  }

  @Test
  public void testArray() {
    assertThat(Luxem.parse("[]"), equalTo(Arrays.asList(new ArrayList<>())));
  }

  @Test
  public void testArrayElement() {
    assertThat(Luxem.parse("[a]"), equalTo(Arrays.asList((Arrays.asList("a")))));
  }

  @Test
  public void testRecord() {
    assertThat(Luxem.parse("{}"), equalTo(Arrays.asList(new HashMap<>())));
  }

  @Test
  public void testRecordElement() {
    assertThat(Luxem.parse("{a:b}"), equalTo(Arrays.asList(new TSMap<String, String>().put("a", "b").inner)));
  }
}
