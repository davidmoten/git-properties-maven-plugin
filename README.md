# git-properties-maven-plugin
<a href="https://travis-ci.org/davidmoten/git-properties-maven-plugin"><img src="https://travis-ci.org/davidmoten/git-properties-maven-plugin.svg"/></a><br/>
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/git-properties-maven-plugin/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/git-properties-maven-plugin)<br/>
[![codecov](https://codecov.io/gh/davidmoten/git-properties-maven-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/davidmoten/git-properties-maven-plugin)<br/>

A maven plugin that 
* writes a file to the artifact classpath (by default) containing the current git commit hash (long and short) and the timestamp of that commit 
* relies on the existence of the git binary on the current path
* sets properties that can be used in the pom.xml (e.g. in <finalName>) but only if the plugin is bound to the **initialize** phase (which is the default)

**Status:** *deployed to Maven Central*
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
git.commit.hash=b49337feb3260a67bc4b5b188e4d0d3f17408150
git.commit.hash.short=b49337f
git.commit.timestamp=20200116220611
git.commit.timestamp.format=yyyyMMddHHmmss
git.commit.timestamp.format.timezone=UTC
git.commit.timestamp.iso8601=2020-01-16T22:06:11Z
git.commit.timestamp.iso8601.format=yyyy-MM-dd'T'HH:mm:ssX
git.commit.timestamp.epoch.ms=1579212371000

```

If you are generating a jar or war artifact then the file will a resource at the root of the classpath (`/git.properties`). 

The same properties are set for use in the pom.xml also so you can use `${git.commit.hash.short}` for instance anywhere in the pom.xml.

### Overriding defaults
You can override the default location, filename, and timestamp formats like this:

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
        <timestampFormat>yyyy-MM-dd HH:mm:ssZ</timestampFormat>
        <timestampFormatTimeZone>AEST</timestampFormatTimeZone>
    </configuration>
</plugin>
```
### Using properties set by git-properties-maven-plugin
Because the plugin is bound by default to the **initialize** phase the properties it sets (with the same key names and values as in the `git.properties` file) are available for use in the pom.xml. For example you can set the <finalName> to use the short git commit hash and the commit timestamp as well:

```xml
<finalName>${project-artifactId}-${project.version}-${git.commit.hash.short}-${git.commit.timestamp}</finalName>
```

