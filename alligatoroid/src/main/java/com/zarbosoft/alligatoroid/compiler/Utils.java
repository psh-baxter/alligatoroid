package com.zarbosoft.alligatoroid.compiler;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.stream.Stream;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class Utils {
  public static void recursiveDelete(Path path) {
    uncheck(
        () -> {
          try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
          }
        });
  }

  public static class SHA256 {
    private final MessageDigest digest;

    public SHA256() {
      digest = uncheck(() -> MessageDigest.getInstance("SHA-256"));
    }

    public SHA256 add(String value) {
      digest.update(value.getBytes(StandardCharsets.UTF_8));
      return this;
    }

    public String buildHex() {
      byte[] hash = digest.digest();
      StringBuilder out = new StringBuilder(2 * hash.length);
      for (int i = 0; i < hash.length; ++i) {
        String hex = Integer.toHexString(Byte.toUnsignedInt(hash[i]));
        if (hex.length() == 1) {
          out.append('0');
        }
        out.append(hex);
      }
      return out.toString();
    }
  }
}
