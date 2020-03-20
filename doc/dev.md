# Dev
See CI: https://travis-ci.org/github/dutoitc/copper
(update march 2020: travis-ci and sonarcloud configuration seems broken)

# Releasing to Maven central
(add this to ~/m2/settings)
<servers>
    <server>
        <id>GitHub</id>
        <username>[User]</username>
        <password>[Password]</password>
    </server>
</servers>

Then execute:
```
mvn release:prepare
mvn release:perform
```

Then to see errors:

https://oss.sonatype.org/

Login with Sonatype JIRA account