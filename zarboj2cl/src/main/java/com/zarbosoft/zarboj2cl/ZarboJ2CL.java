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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mojo(name = "zarboj2cl", defaultPhase = LifecyclePhase.PACKAGE)
public class ZarboJ2CL extends AbstractMojo {
  @SuppressWarnings("unused")
  @Parameter(property = "project", readonly = true)
  private MavenProject project;

  @SuppressWarnings("unused")
  @Parameter(property = "sourcePath", required = true)
  private String sourcePath;

  private static String lswrap(String prefix, Collection<?> list, String suffix) {
    StringBuilder b = new StringBuilder();
    List list2 = new ArrayList(list);
    Collections.sort(list2);
    for (Object o : list2) {
      b.append(prefix);
      b.append(o.toString());
      b.append(suffix);
    }
    return b.toString();
  }

  public void execute() throws MojoExecutionException, MojoFailureException {
    Path sourcePath = Paths.get(this.sourcePath);
    Pattern importPattern =
        Pattern.compile("^import ((?:[a-z][a-zA-Z0-9]*\\.)+)[a-zA-Z0-9]+;", Pattern.MULTILINE);
    final Path[] entry = {null};
    try {
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
      Files.walkFileTree(
          sourcePath,
          new FileVisitor<Path>() {
            final Map<Path, DirectoryState> dependencies = new HashMap<>();

            @Override
            public FileVisitResult preVisitDirectory(
                Path path, BasicFileAttributes basicFileAttributes) throws IOException {
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path abs, BasicFileAttributes basicFileAttributes)
                throws IOException {
              Path rel = sourcePath.relativize(abs);
              Path base = rel.getParent();
              String filename = rel.getFileName().toString();
              if (!filename.endsWith(".java")) return FileVisitResult.CONTINUE;
              DirectoryState dir =
                  dependencies.computeIfAbsent(base, ignore -> new DirectoryState());
              if (filename.equals("Main.java")) {
                entry[0] = base;
                dir.main = true;
              }
              Matcher matcher =
                  importPattern.matcher(
                      new String(Files.readAllBytes(abs), StandardCharsets.UTF_8));
              while (matcher.find()) {
                String importPackage = matcher.group(1).replace(".", "/");
                if (importPackage.startsWith("elemental2/core"))
                  importPackage = "@com_google_elemental2//:elemental2-core-j2cl";
                else if (importPackage.startsWith("elemental2/dom"))
                  importPackage = "@com_google_elemental2//:elemental2-dom-j2cl";
                else if (importPackage.startsWith("elemental2/promise"))
                  importPackage = "@com_google_elemental2//:elemental2-promise-j2cl";
                else if (importPackage.startsWith("java")) continue;
                else if (importPackage.startsWith("jsinterop")) continue;
                else {
                  Path pathImport = Paths.get(importPackage);
                  if (pathImport.equals(base)) continue;
                  importPackage = "//" + pathImport.toString() + ":package0";
                }
                dir.dependencies.add(importPackage);
              }
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
              return FileVisitResult.TERMINATE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path abs, IOException e) throws IOException {
              Path rel = sourcePath.relativize(abs);
              DirectoryState dir = dependencies.get(rel);
              if (dir == null) return FileVisitResult.CONTINUE;
              try (OutputStream os = Files.newOutputStream(abs.resolve("BUILD"))) {
                if (dir.main) {
                  os.write(
                      "load(\"@com_google_j2cl//build_defs:rules.bzl\", \"j2cl_application\")\n"
                          .getBytes(StandardCharsets.UTF_8));
                }
                String uid = "package0";
                os.write(
                    ("\n"
                            + "load(\"@com_google_j2cl//build_defs:rules.bzl\", \"j2cl_library\")\n"
                            + "\n"
                            + "package(\n"
                            + "    default_visibility = [\"//visibility:public\"],\n"
                            + ")\n"
                            + "\n"
                            + "j2cl_library(\n"
                            + "    name = \""
                            + uid
                            + "\",\n"
                            + "    srcs = glob([\n"
                            + "        \"*.java\",\n"
                            + "    ]),\n"
                            + "    deps = [\n"
                            + lswrap("        \"", dir.dependencies, "\",\n")
                            + "    ],\n"
                            + ")\n")
                        .getBytes(StandardCharsets.UTF_8));
                if (dir.main)
                  os.write(
                      String.format(
                              "\n"
                                  + "j2cl_application(\n"
                                  + "    name = \"main\",\n"
                                  + "    entry_points = [\""
                                  + rel.toString().replace("/", ".")
                                  + "\"],\n"
                                  + "    deps = [\":"
                                  + uid
                                  + "\"],\n"
                                  + ")\n")
                          .getBytes(StandardCharsets.UTF_8));
              }
              return FileVisitResult.CONTINUE;
            }
          });
      // bazel=bazelisk
      String[] commandline = {"/usr/bin/bazel", "build", String.format("%s:main", entry[0])};
      Process proc =
          new ProcessBuilder()
              .command(commandline)
              .directory(sourcePath.toFile())
              .inheritIO()
              .start();
      proc.waitFor();
      if (proc.exitValue() != 0) {
        throw new RuntimeException(
            String.format("Bazel build failed with error code %d", proc.exitValue()));
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static class DirectoryState {
    public final Set<String> dependencies = new HashSet<>();
    public boolean main;
  }
}
