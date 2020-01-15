package com.github.davidmoten.gitp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Writes a git properties file to the given output directory containing the
 * commit hash and the commit timestamp.
 */
@Mojo(name = "properties", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class PropertiesMojo extends AbstractMojo {
    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", property = "outputDir", required = true)
    private File outputDirectory;

    @Parameter(defaultValue = "git.properties", property = "filename", required = true)
    private String filename;

    public void execute() throws MojoExecutionException {
        Log log = getLog();
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        File file = new File(outputDirectory, filename);
        try {
            String commitHash = run("git", "rev-parse", "HEAD");
            String commitTime = run("git", "show", "-s", "--format=%ci", "HEAD");
            try (FileWriter w = new FileWriter(file)) {
                w.write("git.commit.hash=" + commitHash.trim() + "\n");
                w.write("git.commit.timestamp=" + commitTime.trim());
            }
            log.info("git.commit.hash=" + commitHash);
            log.info("git.commit.timestamp=" + commitTime);
            log.info("git properties written to " + file);
        } catch (IOException | InterruptedException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private String run(String... command) throws IOException, InterruptedException {
        Process p = new ProcessBuilder() //
                .command(Arrays.asList(command)) //
                .redirectErrorStream(true) //
                .start();
        p.waitFor(10, TimeUnit.SECONDS);
        final String message;
        try (InputStream in = p.getInputStream()) {
            byte[] bytes = read(in);
            message = new String(bytes, StandardCharsets.UTF_8);
        }
        if (p.exitValue() != 0) {
            String cmd = Arrays.stream(command).collect(Collectors.joining(" "));
            getLog().error("An error occurred calling\n" + cmd + ". The output of the command is:\n"
                    + message);
            throw new RuntimeException("An error occurred calling '" + cmd + "'. See log for details.");
        } else {
            return message;
        }
    }

    public static byte[] read(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
            os.write(buffer, 0, len);
        }
        return os.toByteArray();
    }
}
