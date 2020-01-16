package com.github.davidmoten.gitp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Writes a git properties file to the given output directory containing the
 * commit hash and the commit timestamp.
 */
@Mojo(name = "properties", defaultPhase = LifecyclePhase.INITIALIZE)
public class PropertiesMojo extends AbstractMojo {
    
    private static final String ISO_8601 = "yyyy-MM-dd'T'HH:mm:ssX";
    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", property = "outputDir", required = true)
    private File outputDirectory;

    @Parameter(defaultValue = "git.properties", property = "filename", required = true)
    private String filename;

    @Parameter(defaultValue = "yyyyMMddHHmmss", property = "timestampFormat", required = true)
    private String timestampFormat;

    @Parameter(defaultValue = "UTC", property = "timestampFormatTimeZone", required = true)
    private String timestampFormatTimeZone;
    
    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {
        Log log = getLog();
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        SimpleDateFormat sdf = new SimpleDateFormat(timestampFormat);
        sdf.setTimeZone(TimeZone.getTimeZone(timestampFormatTimeZone));

        SimpleDateFormat sdf2 = new SimpleDateFormat(ISO_8601);
        sdf2.setTimeZone(TimeZone.getTimeZone(timestampFormatTimeZone));
        
        File file = new File(outputDirectory, filename);
        try {
            String commitHash = run("git", "rev-parse", "HEAD").trim();
            String commitHashShort = run("git", "rev-parse", "--short", "HEAD").trim();
            long commitTime = Long.parseLong(run("git", "show", "-s", "--format=%ct", "HEAD").trim()) * 1000;
            log.info(new Date(commitTime).toString());
            Map<String, String> map = new LinkedHashMap<>();
            map.put("git.commit.hash", commitHash);
            map.put("git.commit.hash.short", commitHashShort);
            map.put("git.commit.timestamp", sdf.format(commitTime));
            map.put("git.commit.timestamp.format", timestampFormat);
            map.put("git.commit.timestamp.format.timezone", timestampFormatTimeZone);
            map.put("git.commit.timestamp.iso8601", sdf2.format(commitTime));
            map.put("git.commit.timestamp.iso8601.format", ISO_8601);
            map.put("git.commit.timestamp.epoch.ms", String.valueOf(commitTime));
            try (FileWriter w = new FileWriter(file)) {
                for (Entry<String, String> entry : map.entrySet()) {
                    String line = entry.getKey() + "=" + entry.getValue();
                    w.write(line + "\n");
                    log.info(line);
                    project.getProperties().put(entry.getKey(), entry.getValue());
                }
            }
            log.info("git properties written to " + file);
            log.info("maven project properties also set with above key-values");
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
            getLog().error("An error occurred calling\n" + cmd + ". The output of the command is:\n" + message);
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
