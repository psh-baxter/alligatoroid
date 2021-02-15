package com.zarbosoft.zarboj2cl;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.nio.file.Path;
import java.nio.file.Paths;

@Mojo(name = "build", defaultPhase = LifecyclePhase.PACKAGE)
public class BuildGoal extends AbstractMojo {
  @SuppressWarnings("unused")
  @Parameter(property = "project", readonly = true)
  private MavenProject project;

  @SuppressWarnings("unused")
  @Parameter(property = "sourcePath", required = true)
  private String sourcePath;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      Path sourcePath = Paths.get(this.sourcePath);

      // bazel=bazelisk
      String[] commandline = {"/usr/bin/bazel", "build", String.format(":entry")};
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
}
