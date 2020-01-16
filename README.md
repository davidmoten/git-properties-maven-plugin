# git-properties-maven-plugin
<a href="https://travis-ci.org/davidmoten/git-properties-maven-plugin"><img src="https://travis-ci.org/davidmoten/git-properties-maven-plugin.svg"/></a><br/>
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/git-properties-maven-plugin/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/git-properties-maven-plugin)<br/>
[![codecov](https://codecov.io/gh/davidmoten/git-properties-maven-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/davidmoten/git-properties-maven-plugin)<br/>

A maven plugin that 
* writes a file to the artifact classpath (by default) containing the current git commit hash (long and short) and the timestamp of that commit 
* relies on the existence of the git binary on the current path
* sets properties that can be used in the pom.xml (e.g. in <finalName>) but only if the plugin is bound to the `initialize` phase (which is the default)

## Usage

Add this fragment to the build/plugins section in your pom.xml:

```xml
<plugin>
    <groupId>com.github.davidmoten</groupId>
    <artifactId>git-properties-maven-plugin</artifactId>
    <version>VERSION_HERE</version>
    <executions>
        <execution>
            <id>properties</id>
            <goals>
                <goal>properties</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
The above fragment will add a `git.properties` file to the generated artifact classpath (`${project.build.outputDirectory}`) when you run `mvn clean install`. The file looks like this:

```
git.commit.hash=02a8e7e8f1102715e90f8f6b4c037641d04ee3c8
git.commit.hash.short=02a8e7e
git.commit.timestamp=2020-01-15 17:08:22 +1100
```
### Overriding defaults
You can override the default location and filename like this:

```xml
<plugin>
    <groupId>com.github.davidmoten</groupId>
    <artifactId>git-properties-maven-plugin</artifactId>
    <version>VERSION_HERE</version>
    <executions>
        <execution>
            <id>write-properties</id>
            <goals>
                <goal>properties</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <outputDirectory>${project.build.outputDirectory}/git</outputDirectory>
        <filename>commit.properties</filename>
    </configuration>
</plugin>
```
### Using properties set by git-properties-maven-plugin
Because the plugin is bound by default to the **initialize** phase the properties it sets (with the same key names and values as in the `git.properties` file) are available for use in the pom.xml. For example you can set the finalName to use the short git commit has as well:

```xml
<finalName>${project-artifactId}-${project.version}-${git.commit.hash.short}</finalName>
```

