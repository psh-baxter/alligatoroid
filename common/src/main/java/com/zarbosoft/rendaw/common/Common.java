package com.zarbosoft.rendaw.common;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.time.ZoneOffset.UTC;

public class Common {
  private static Object notReallyNull = new Object();

  public static <T> Iterable<T> iterable(final Stream<T> stream) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return stream.iterator();
      }
    };
  }

  public static <T> Iterable<T> iterable(final Iterator<T> iterator) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return iterator;
      }
    };
  }

  public static <T> boolean isOrdered(final Comparator<T> comparator, final T a, final T b) {
    return comparator.compare(a, b) <= 0;
  }

  public static <T extends Comparable<T>> boolean isOrdered(final T a, final T b) {
    return a.compareTo(b) <= 0;
  }
  public static <T extends Comparable<T>> boolean isOrderedExclusive(final T a, final T b) {
    return a.compareTo(b) < 0;
  }

  public static RuntimeException uncheckAll(final Throwable e) {
    if (e instanceof Exception) {
      return Common.uncheck((Exception) e);
    }
    return new UncheckedException(e);
  }

  public static RuntimeException uncheck(final Exception e) {
    if (e instanceof RuntimeException) return (RuntimeException) e;
    if (e instanceof NoSuchFileException) return new UncheckedNoSuchFileException(e);
    if (e instanceof FileNotFoundException) return new UncheckedFileNotFoundException(e);
    if (e instanceof IOException) return new UncheckedIOException((IOException) e);
    return new UncheckedException(e);
  }

  public static RuntimeException uncheck(final Error e) {
    return new UncheckedException(e);
  }

  public static <T> T uncheck(final Thrower1<T> code) {
    try {
      return code.get();
    } catch (final Exception e) {
      throw Common.uncheck(e);
    }
  }

  public static void uncheck(final Thrower2 code) {
    try {
      code.get();
    } catch (final Exception e) {
      throw Common.uncheck(e);
    }
  }

  public static <T> Optional<T> opt(T v) {
    if (v == null) return (Optional<T>) Optional.of(notReallyNull);
    else return Optional.of(v);
  }

  public static <T> T unopt(Optional<T> v) {
    Object out = v.get();
    if (out == notReallyNull) return null;
    else return (T) out;
  }

  public static double clamp(double low, double high, double value) {
    return Math.max(low, Math.min(high, value));
  }

  public static <T> T revAt(final List<T> values, int offset) {
    if (offset < 0 || offset >= values.size()) throw new Assertion();
    return values.get(values.size() - 1 - offset);
  }

  public static <T> Optional<T> lastOpt(final List<T> values) {
    if (values.isEmpty()) return Optional.empty();
    return Optional.of(Common.last(values));
  }

  public static <T> T last(final List<T> values) {
    return values.get(values.size() - 1);
  }

  public static <T> T last(final T[] values) {
    return values[values.length - 1];
  }

  public static <T> Optional<T> firstOpt(final List<T> values) {
    if (values.isEmpty()) return Optional.empty();
    return Optional.of(Common.first(values));
  }

  public static <T> T first(final List<T> values) {
    return values.get(0);
  }

  public static <T> T first(final T[] values) {
    return values[0];
  }

  public static <T> T removeLast(List<T> values) {
    return values.remove(values.size() - 1);
  }

  public static <T> Optional<T> removeLastOpt(List<T> values) {
    if (values.isEmpty()) return Optional.empty();
    return Optional.of(values.remove(values.size() - 1));
  }

  public static <T> List<T> sublist(final List<T> list, int start, int end) {
    if (start < 0) start = Math.max(0, list.size() + start);
    if (start >= list.size()) return Collections.emptyList();
    if (end < 0) end = Math.max(0, list.size() + end);
    end = Math.min(end, list.size());
    if (start >= end) return Collections.emptyList();
    return list.subList(start, end);
  }

  public static <T> List<T> sublist(final List<T> list, int start) {
    if (start < 0) start = Math.max(0, list.size() + start);
    if (start >= list.size()) return Collections.emptyList();
    return list.subList(start, list.size());
  }

  public static String substr(final String string, int start, int end) {
    if (start < 0) start = Math.max(0, string.length() + start);
    if (start >= string.length()) return "";
    if (end < 0) end = Math.max(0, string.length() + end);
    end = Math.min(end, string.length());
    if (start >= end) return "";
    return string.substring(start, end);
  }

  public static String substr(final String string, int start) {
    if (start < 0) start = Math.max(0, string.length() + start);
    if (start >= string.length()) return "";
    return string.substring(start);
  }

  /**
   * Shortest of two streams
   *
   * @param a
   * @param b
   * @param <A>
   * @param <B>
   * @return
   */
  public static <A, B> Stream<Pair<A, B>> zip(
      final Stream<? extends A> a, final Stream<? extends B> b) {
    final Spliterator<? extends A> aSpliterator = Objects.requireNonNull(a).spliterator();
    final Spliterator<? extends B> bSpliterator = Objects.requireNonNull(b).spliterator();

    // Zipping looses DISTINCT and SORTED characteristics
    final int characteristics =
        aSpliterator.characteristics()
            & bSpliterator.characteristics()
            & ~(Spliterator.DISTINCT | Spliterator.SORTED);

    final long zipSize =
        ((characteristics & Spliterator.SIZED) != 0)
            ? Math.min(aSpliterator.getExactSizeIfKnown(), bSpliterator.getExactSizeIfKnown())
            : -1;

    final Iterator<A> aIterator = Spliterators.iterator(aSpliterator);
    final Iterator<B> bIterator = Spliterators.iterator(bSpliterator);
    final Iterator<Pair<A, B>> cIterator =
        new Iterator<Pair<A, B>>() {
          @Override
          public boolean hasNext() {
            return aIterator.hasNext() && bIterator.hasNext();
          }

          @Override
          public Pair<A, B> next() {
            return new Pair<>(aIterator.next(), bIterator.next());
          }
        };

    final Spliterator<Pair<A, B>> split =
        Spliterators.spliterator(cIterator, zipSize, characteristics);
    return (a.isParallel() || b.isParallel())
        ? StreamSupport.stream(split, true)
        : StreamSupport.stream(split, false);
  }

  public static <T> Stream<T> flatStream(final Stream<T>... values) {
    return Stream.of(values).flatMap(v -> v);
  }

  public static <T> Stream<T> stream(final Iterator<T> iterator) {
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
  }

  public static <T> Stream<Pair<Integer, T>> enumerate(final Stream<T> stream) {
    return Common.enumerate(stream, 0);
  }

  public static <T> Stream<Pair<Integer, T>> enumerate(final Stream<T> stream, final int start) {
    final Mutable<Integer> count = new Mutable<>(start);
    return stream.map(e -> new Pair<>(count.value++, e));
  }

  public static <T> Stream<Pair<Boolean, T>> streamFinality(final Iterator<T> data) {
    return Common.stream(
        new Iterator<Pair<Boolean, T>>() {
          @Override
          public boolean hasNext() {
            return data.hasNext();
          }

          @Override
          public Pair<Boolean, T> next() {
            final T temp = data.next();
            return new Pair<>(!data.hasNext(), temp);
          }
        });
  }

  public static <T> Stream<T> concatNull(final Stream<T> stream) {
    return Stream.concat(stream, Stream.of((T[]) new Object[] {null}));
  }

  public static class InputStreamIterator implements Iterator<byte[]> {
    private final InputStream source;
    private byte[] bytes = null;
    private int length = -1;

    public InputStreamIterator(final InputStream source) {
      this.source = source;
    }

    @Override
    public boolean hasNext() {
      if (this.bytes == null) this.read();
      return this.length != -1;
    }

    @Override
    public byte[] next() {
      if (this.bytes == null) this.read();
      if (this.length == -1) throw new NoSuchElementException();
      final byte[] out = Arrays.copyOfRange(this.bytes, 0, this.length);
      this.read();
      return out;
    }

    private void read() {
      if (this.bytes == null) this.bytes = new byte[1024];
      this.length = Common.uncheck(() -> this.source.read(this.bytes));
    }
  }

  public static Stream<byte[]> stream(final InputStream source) {
    return Common.stream(new InputStreamIterator(source));
  }

  public static <T> Stream<T> drain(final Deque<T> queue) {
    class DrainIterator implements Iterator<T> {

      @Override
      public boolean hasNext() {
        return !queue.isEmpty();
      }

      @Override
      public T next() {
        return queue.pollFirst();
      }
    }
    return Common.stream(new DrainIterator());
  }

  @FunctionalInterface
  public interface Thrower1<T> {
    T get() throws Exception, Error;
  }

  @FunctionalInterface
  public interface Thrower2 {
    void get() throws Exception, Error;
  }

  public static class UncheckedException extends RuntimeException {
    public UncheckedException(final Throwable e) {
      super(e);
    }
  }

  public static class UncheckedFileNotFoundException extends RuntimeException {
    public UncheckedFileNotFoundException(final Throwable e) {
      super(e);
    }
  }

  public static class UncheckedNoSuchFileException extends RuntimeException {
    public UncheckedNoSuchFileException(final Throwable e) {
      super(e);
    }
  }

  public static class Mutable<T> {
    public T value;

    public Mutable(final T value) {
      this.value = value;
    }

    public Mutable() {}
  }

  public static class UserData {
    private Object value;

    public UserData() {
      this.value = null;
    }

    public UserData(final Object value) {
      this.value = value;
    }

    public <T> T get() {
      return (T) this.value;
    }

    public <T> T get(final Supplier<T> supplier) {
      if (this.value == null) this.value = supplier.get();
      return this.get();
    }
  }

  public static String shash1(final Object... source) {
    final MessageDigest hash = Common.uncheck(() -> MessageDigest.getInstance("SHA-1"));
    for (final Object o : source) hash.update(o.toString().getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(hash.digest());
  }

  public static String shash256(final Object... source) {
    final MessageDigest hash = Common.uncheck(() -> MessageDigest.getInstance("SHA-256"));
    for (final Object o : source) hash.update(o.toString().getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(hash.digest());
  }

  public byte[] md5(final byte[] data) {
    final MessageDigest hash = Common.uncheck(() -> MessageDigest.getInstance("MD5"));
    hash.update(data);
    return hash.digest();
  }

  public byte[] sha256(final byte[] data) {
    final MessageDigest hash = Common.uncheck(() -> MessageDigest.getInstance("SHA-256"));
    hash.update(data);
    return hash.digest();
  }

  public static Stream<String> splitLines(final String source) {
    return Arrays.stream(source.split("\r?\n"));
  }

  public static Optional<Matcher> regex(final String source, final String pattern) {
    final Pattern compiled = Pattern.compile(pattern);
    final Matcher matcher = compiled.matcher(source);
    return matcher.find() ? Optional.of(matcher) : Optional.empty();
  }

  public static void deleteTree(final Path root) {
    Common.uncheck(
        () ->
            Files.walkFileTree(
                root,
                new SimpleFileVisitor<Path>() {
                  @Override
                  public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                      throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                  }

                  @Override
                  public FileVisitResult postVisitDirectory(final Path dir, final IOException exc)
                      throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                  }
                }));
  }

  public static <T> Iterable<T> reversed(List<T> values) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return new Iterator<T>() {
          int after = values.size();

          @Override
          public boolean hasNext() {
            return after > 0;
          }

          @Override
          public T next() {
            return values.get(--after);
          }
        };
      }
    };
  }

  public static void atomicWrite(Path path, Consumer<OutputStream> cb) {
    uncheck(
        () -> {
          Path tempPath =
              path.getParent().resolve(String.format(".%s.atomic", path.getFileName().toString()));
          try (OutputStream out = Files.newOutputStream(tempPath)) {
            cb.accept(out);
          }
          try {
            Files.move(tempPath, path, REPLACE_EXISTING, ATOMIC_MOVE);
          } catch (AtomicMoveNotSupportedException e) {
            Files.move(tempPath, path, REPLACE_EXISTING);
            for (int i = 0; i < 1000; ++i) {
              if (Files.exists(path)) break;
              Thread.sleep(10);
            }
          }
        });
  }

  public static boolean aeq(double a, double b, double e) {
    return (a - b) * (a - b) < e;
  }

  public static boolean aeq(double a, double b) {
    return aeq(a, b, 0.01);
  }

  public static int ceilDiv(int x, int y) {
    return -Math.floorDiv(-x, y);
  }

  public static long ceilDiv(long x, long y) {
    return -Math.floorDiv(-x, y);
  }
}
