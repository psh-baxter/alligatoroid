package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.luxem.read.BufferedReader;
import com.zarbosoft.luxem.read.path.LuxemArrayPath;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.TSList;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Deserializer {
  public static void deserialize(TSList<Error> errors, Path path, TSList<State> stack) {
    // TODO luxem path
    BufferedReader reader =
        new BufferedReader() {
          LuxemPath luxemPath = new LuxemArrayPath(null);

          @Override
          protected void eatRecordBegin() {
            luxemPath = luxemPath.pushRecordOpen();
            stack.last().eatRecordBegin(errors, stack, luxemPath);
          }

          @Override
          protected void eatArrayBegin() {
            luxemPath = luxemPath.pushArrayOpen();
            stack.last().eatArrayBegin(errors, stack, luxemPath);
          }

          @Override
          protected void eatArrayEnd() {
            luxemPath = luxemPath.pop();
            stack.last().eatArrayEnd(errors, stack, luxemPath);
          }

          @Override
          protected void eatRecordEnd() {
            luxemPath = luxemPath.pop();
            stack.last().eatRecordEnd(errors, stack, luxemPath);
          }

          @Override
          protected void eatType(String value) {
            luxemPath = luxemPath.type();
            stack.last().eatType(errors, stack, luxemPath, value);
          }

          @Override
          protected void eatKey(String value) {
            luxemPath = luxemPath.key(value);
            stack.last().eatKey(errors, stack, luxemPath, value);
          }

          @Override
          protected void eatPrimitive(String value) {
            luxemPath = luxemPath.value();
            stack.last().eatPrimitive(errors, stack, luxemPath, value);
          }
        };
    if (!Files.exists(path)) {
      return;
    }
    try (InputStream stream = Files.newInputStream(path)) {
      reader.feed(stream);
    } catch (Exception e) {
      errors.add(Error.unexpected(e));
    }
  }
}
