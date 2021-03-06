package com.github.davidmoten.gitp;

import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.WithoutMojo;

import org.junit.Rule;
import static org.junit.Assert.*;
import org.junit.Test;

import com.google.common.io.Files;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class PropertiesTest {
    @Rule
    public MojoRule rule = new MojoRule() {
        @Override
        protected void before() throws Throwable {
        }

        @Override
        protected void after() {
        }
    };

    @Test
    public void testSomething() throws Exception {
        File pom = new File("target/test-classes/project-to-test/");
        assertNotNull(pom);
        assertTrue(pom.exists());
        System.out.println(pom.getAbsolutePath());

        PropertiesMojo myMojo = (PropertiesMojo) rule.lookupConfiguredMojo(pom, "properties");
        assertNotNull(myMojo);
        myMojo.execute();

        File outputDirectory = (File) rule.getVariableValueFromObject(myMojo, "outputDirectory");
        assertNotNull(outputDirectory);
        assertTrue(outputDirectory.exists());

        String filename = (String) rule.getVariableValueFromObject(myMojo, "filename");

        File file = new File(outputDirectory, filename);
        assertTrue(file.exists());
        
        System.out.println("git.properties:");
        System.out.println("------------------");
        Files.readLines(file, StandardCharsets.UTF_8).stream().forEach(System.out::println);
        System.out.println("------------------");
        
    }

    /** Do not need the MojoRule. */
    @WithoutMojo
    @Test
    public void testSomethingWhichDoesNotNeedTheMojoAndProbablyShouldBeExtractedIntoANewClassOfItsOwn() {
        assertTrue(true);
    }

}
