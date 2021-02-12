package com.zarbosoft.zarboj2cl;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.zarbosoft.rendaw.common.Common.uncheck;

@Mojo(name = "zarboj2cl", defaultPhase = LifecyclePhase.PACKAGE)
public class ZarboJ2CL extends AbstractMojo {
  private static final Pattern importPattern =
      Pattern.compile(
          "^import ((?:[a-z][a-zA-Z0-9]*\\.)+)([a-zA-Z0-9]+)(?:\\.[a-zA-Z0-9])*;",
          Pattern.MULTILINE);
  Map<Path, BazelLibrary> lookup = new HashMap<>();

  @SuppressWarnings("unused")
  @Parameter(property = "project", readonly = true)
  private MavenProject project;

  @SuppressWarnings("unused")
  @Parameter(property = "sourcePath", required = true)
  private String sourcePath;

  public void execute() throws MojoExecutionException, MojoFailureException {
    Path sourcePath = Paths.get(this.sourcePath);
    try {
      /// Copy main source
      for (String root0 : project.getCompileSourceRoots()) {
        Path root = Paths.get(root0);
        System.out.format("Copying %s to %s\n", root, sourcePath);
        Files.walkFileTree(
            root,
            new FileVisitor<Path>() {
              @Override
              public FileVisitResult preVisitDirectory(
                  Path abs, BasicFileAttributes basicFileAttributes) throws IOException {
                Path rel = root.relativize(abs);
                Files.createDirectories(sourcePath.resolve(rel));
                return FileVisitResult.CONTINUE;
              }

              @Override
              public FileVisitResult visitFile(Path abs, BasicFileAttributes basicFileAttributes)
                  throws IOException {
                Path rel = root.relativize(abs);
                Files.copy(abs, sourcePath.resolve(rel), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
              }

              @Override
              public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
                return FileVisitResult.CONTINUE;
              }

              @Override
              public FileVisitResult postVisitDirectory(Path path, IOException e)
                  throws IOException {
                return FileVisitResult.CONTINUE;
              }
            });
      }

      /// Bazel root boilerplate
      try (OutputStream os = Files.newOutputStream(sourcePath.resolve(".bazelversion"))) {
        os.write("3.3.0\n".getBytes(StandardCharsets.UTF_8));
      }
      try (OutputStream os = Files.newOutputStream(sourcePath.resolve("WORKSPACE"))) {
        os.write(
            ("workspace(name = \"main\")\n"
                    + "\n"
                    + "load(\"@bazel_tools//tools/build_defs/repo:http.bzl\", \"http_archive\")\n"
                    + "\n"
                    + "http_archive(\n"
                    + "    name = \"com_google_j2cl\",\n"
                    + "    strip_prefix = \"j2cl-master\",\n"
                    + "    url = \"https://github.com/google/j2cl/archive/master.zip\",\n"
                    + ")\n"
                    + "\n"
                    + "load(\"@com_google_j2cl//build_defs:repository.bzl\", \"load_j2cl_repo_deps\")\n"
                    + "load_j2cl_repo_deps()\n"
                    + "\n"
                    + "load(\"@com_google_j2cl//build_defs:rules.bzl\", \"setup_j2cl_workspace\")\n"
                    + "setup_j2cl_workspace()\n"
                    + "\n"
                    + "_JSINTEROP_BASE_VERSION = \"1.0.0\"\n"
                    + "http_archive(\n"
                    + "    name = \"com_google_jsinterop_base\",\n"
                    + "    strip_prefix = \"jsinterop-base-%s\" % _JSINTEROP_BASE_VERSION,\n"
                    + "    url = \"https://github.com/google/jsinterop-base/archive/%s.zip\" % _JSINTEROP_BASE_VERSION,\n"
                    + ")\n"
                    + "\n"
                    + "_JSINTEROP_ANNOTATIONS_VERSION = \"2.0.0\"\n"
                    + "http_archive(\n"
                    + "    name = \"com_google_jsinterop_annotations\",\n"
                    + "    strip_prefix = \"jsinterop-annotations-%s\" % _JSINTEROP_ANNOTATIONS_VERSION,\n"
                    + "    url = \"https://github.com/google/jsinterop-annotations/archive/%s.zip\" % _JSINTEROP_ANNOTATIONS_VERSION,\n"
                    + ")\n"
                    + "\n"
                    + "http_archive(\n"
                    + "    name = \"com_google_elemental2\",\n"
                    + "    strip_prefix = \"elemental2-1.1.0\",\n"
                    + "    url = \"https://github.com/google/elemental2/archive/1.1.0.zip\",\n"
                    + ")\n"
                    + "\n"
                    + "load(\"@com_google_elemental2//build_defs:repository.bzl\", \"load_elemental2_repo_deps\")\n"
                    + "load_elemental2_repo_deps()\n"
                    + "\n"
                    + "load(\"@com_google_elemental2//build_defs:workspace.bzl\", \"setup_elemental2_workspace\")\n"
                    + "setup_elemental2_workspace()\n"
                    + "\n")
                .getBytes(StandardCharsets.UTF_8));
      }

      /// Build BUILD files and map dependencies
      Path editorRoot = Paths.get("com/zarbosoft/merman");
      BazelPackage editorPackage = new BazelPackage(editorRoot);
      BazelLibrary editorLibrary = editorPackage.library("editor");

      Path pidgoonRoot = Paths.get("com/zarbosoft/pidgoon");
      BazelPackage pidgoonPackage = new BazelPackage(pidgoonRoot);
      BazelLibrary pidgoonLibrary = pidgoonPackage.library("pidgoon");

      Map<Path, BazelPackage> bazelPackageMap = new HashMap<>();
      Map<Path, BazelLibrary> bazelLibraryMap = new HashMap<>();
      Files.walkFileTree(
          sourcePath,
          new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(
                Path abs, BasicFileAttributes basicFileAttributes) throws IOException {
              Path rel = sourcePath.relativize(abs);
              BazelPackage p = new BazelPackage(rel);
              bazelPackageMap.put(rel, p);
              bazelLibraryMap.put(rel, p.library(rel.getFileName().toString() + "_"));
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path abs, BasicFileAttributes basicFileAttributes)
                throws IOException {
              Path rel = sourcePath.relativize(abs);
              String filename = rel.getFileName().toString();
              if (!(filename.endsWith(".java") || filename.endsWith(".js")))
                return FileVisitResult.CONTINUE;
              if (rel.startsWith(editorRoot)) {
                editorLibrary.process(rel);
              } else if (rel.startsWith(pidgoonRoot)) {
                pidgoonLibrary.process(rel);
              } else {
                bazelLibraryMap.get(rel.getParent()).process(rel);
              }
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
              return FileVisitResult.TERMINATE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path abs, IOException e) throws IOException {
              return FileVisitResult.CONTINUE;
            }
          });
      pidgoonPackage.write();
      editorPackage.write();
      for (BazelPackage bazelPackage : bazelPackageMap.values()) {
        bazelPackage.write();
      }
      // if (entry[0] == null) throw new RuntimeException();
      // bazel=bazelisk
      /*
      String[] commandline = {
              "/usr/bin/bazel", "build", String.format("%s:entry", entry[0].getParent())
      };
       */
      String[] commandline = {
        "/usr/bin/bazel", "build", String.format("%s:editor", editorPackage.dir)
      };
      ProcessBuilder processBuilder =
          new ProcessBuilder().command(commandline).directory(sourcePath.toFile());
      processBuilder.environment().put("JAVA_HOME", "/usr/lib/jvm/java-11-openjdk");
      Process proc = processBuilder.inheritIO().start();
      proc.waitFor();
      if (proc.exitValue() != 0) {
        throw new RuntimeException(
            String.format("Bazel build failed with error code %d", proc.exitValue()));
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public class BazelPackage {
    public final Path dir;
    public final List<BazelLibrary> libraries = new ArrayList<>();

    public BazelPackage(Path dir) {
      this.dir = dir;
    }

    public BazelLibrary library(String name) {
      BazelLibrary l = new BazelLibrary(this, name);
      libraries.add(l);
      return l;
    }

    public void write() {
      uncheck(
          () -> {
            boolean empty = true;
            for (BazelLibrary library : libraries) {
              if (!library.empty()) empty = false;
            }
            if (empty) return;
            Path sourcePath = Paths.get(ZarboJ2CL.this.sourcePath);
            try (OutputStream os =
                Files.newOutputStream(sourcePath.resolve(dir).resolve("BUILD"))) {
              os.write(
                  ("\n"
                          + "load(\"@com_google_j2cl//build_defs:rules.bzl\", \"j2cl_library\")\n"
                          + "\n"
                          + "package(\n"
                          + "    default_visibility = [\"//visibility:public\"],\n"
                          + ")\n"
                          + "\n")
                      .getBytes(StandardCharsets.UTF_8));

              for (BazelLibrary library : libraries) {
                library.write(os);
              }
            }
          });
    }
  }

  public class BazelLibrary {
    private final String name;
    private final List<Path> sources = new ArrayList<>();
    private final BazelPackage bazelPackage;
    private Path app;

    public BazelLibrary(BazelPackage bazelPackage, String name) {
      this.name = name;
      this.bazelPackage = bazelPackage;
    }

    public boolean empty() {
      return sources.isEmpty();
    }

    public BazelLibrary process(Path source) {
      if (source.getFileName().toString().equals("main.js")) {
        app = source;
      } else {
        sources.add(source);
        lookup.put(source, this);
      }
      return this;
    }

    public void write(OutputStream os) {
      uncheck(
          () -> {
            os.write(
                ("\n" + "j2cl_library(\n" + "    name = \"" + name + "\",\n" + "    srcs = [\n")
                    .getBytes(StandardCharsets.UTF_8));

            Collections.sort(sources);
            for (Path source : sources) {
              os.write(
                  ("        \"" + bazelPackage.dir.relativize(source).toString() + "\",\n")
                      .getBytes(StandardCharsets.UTF_8));
            }
            if (app != null)
              os.write(
                  ("        \"" + bazelPackage.dir.relativize(app).toString() + "\",\n")
                      .getBytes(StandardCharsets.UTF_8));

            os.write(("    ],\n" + "    deps = [\n").getBytes(StandardCharsets.UTF_8));
            Set<String> seenDeps = new HashSet<>();
            Path sourcePath = Paths.get(ZarboJ2CL.this.sourcePath);
            for (Path source : sources) {
              if (source.getFileName().toString().endsWith(".js")) continue;
              Matcher matcher =
                  importPattern.matcher(
                      new String(
                          Files.readAllBytes(sourcePath.resolve(source)), StandardCharsets.UTF_8));
              while (matcher.find()) {
                String dep = matcher.group(1).replace(".", "/");
                String importName = matcher.group(2) + ".java";
                if (dep.startsWith("elemental2/core"))
                  dep = "@com_google_elemental2//:elemental2-core-j2cl";
                else if (dep.startsWith("elemental2/dom"))
                  dep = "@com_google_elemental2//:elemental2-dom-j2cl";
                else if (dep.startsWith("elemental2/promise"))
                  dep = "@com_google_elemental2//:elemental2-promise-j2cl";
                else if (dep.startsWith("java")) {
                  continue;
                  // dep = "@bazel_tools//tools/jdk:current_java_runtime";
                } else if (dep.startsWith("jsinterop/base")) {
                  dep = "@com_google_jsinterop_base//:jsinterop-base-j2cl";
                } else if (dep.startsWith("jsinterop/annotations")) {
                  dep = "@com_google_j2cl//:jsinterop-annotations-j2cl";
                } else {
                  Path localDep = Paths.get(dep).resolve(importName);
                  BazelLibrary dest = lookup.get(localDep);
                  if (dest == this) continue;
                  dep = "//" + dest.bazelPackage.dir.toString() + ":" + dest.name;
                }
                if (seenDeps.contains(dep)) continue;
                seenDeps.add(dep);
                os.write(("        \"" + dep + "\",\n").getBytes(StandardCharsets.UTF_8));
              }
            }
            os.write(("    ],\n" + ")\n").getBytes(StandardCharsets.UTF_8));

            if (app != null) {
              os.write(
                  ("load(\"@com_google_j2cl//build_defs:rules.bzl\", \"j2cl_application\")\n"
                          + "\n"
                          + "j2cl_application(\n"
                          + "    name = \"entry\",\n"
                          + "    entry_points = [\"com.zarbosoft.merman.webview.entry\"],\n"
                          + "    deps = [\":"
                          + name
                          + "\"],\n"
                          + ")\n")
                      .getBytes(StandardCharsets.UTF_8));
            }
          });
    }
  }
}
