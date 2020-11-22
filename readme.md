# Markdown Indexer

[Spring Initializr link](https://start.spring.io/#!type=gradle-project&language=java&platformVersion=2.4.0.RELEASE&packaging=jar&jvmVersion=15&groupId=org.rmb&artifactId=Markdown-Indexer&name=Markdown%20Indexer&description=Indexer%20for%20collections%20of%20markdown%20files&packageName=org.rmb.md.indexer&dependencies=lombok)

Run this over a directory of markdown files to generate a set of URL shortcut links for them that can be indexed by a program such as [KeyPirinha](https://keypirinha.com/).

Will read each file and create an anchor based shortcuts for each heading too.

## Build 

Get source and build project.

```bash
cd '/path/to/projects'
git clone git@github.com:robertmarkbram/Markdown-Indexer.git
cd Markdown-Indexer
export JAVA_HOME="C:\Program Files\Java\jdk-15"
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew clean build
```

Create an environment specific properties file 

```bash
cp src/main/resources/application.properties build/libs/application-${hostname}.properties 
```

Modify paths in that properties file to suit your environment.

Run it.

```bash
# Assume you have JDK 15 on the path already. 
cd '/path/to/projects/Markdown-Indexer/build/libs'
java -jar -Dspring.profiles.active=${hostname} Markdown-Indexer-0.0.1-SNAPSHOT.jar 
```

## Replacement Sequence files

Default replacement sequence files:

1. `markdown-file-replacement-sequence.txt`
    1. Used to process the path to a markdown file and will output the URL file name.
2. `markdown-heading-replacement-sequence.txt`
    1. Used to process headings within markdown files and will output the URL file name.

Notes about these files.

1. The listed files above are defaults only. You can override them by providing your own path to them in the properties file.
2. The only lines that matter are those that start with one of these

    ```
    find=
    replace=
    ```

    All other lines are ignore. It looks like I am using comments here (lines starting with #). That's just a way to visually delineate these lines from all the others.

3. Don't trim trailing spaces. They are important.


4. End a line with these three characters if you do want to trim trailing space - these characters will be removed:

    ```
    ;;;
    ```

5. Find and replace sections are regular expressions as per the [Pattern.java class](https://docs.oracle.com/en/java/javase/15/docs/api/java.base/java/lang/String.html#replaceAll(java.lang.String,java.lang.String)).
6. Backslash `\` does need to be escaped if you need a literal backslash, i.e. `\\` but other escape sequences for Java regex isn't needed here because you are not writing Java code, but text that Java will read literally.

Notes about Java Regex:

1. [Java Regex Pattern](https://docs.oracle.com/en/java/javase/15/docs/api/java.base/java/util/regex/Pattern.html).
2. [Java Regex Tester](https://www.freeformatter.com/java-regex-tester.html).
