metr
====

metric calculator
* sloc - lines of code
* dloc - dloc - check out ['What is DLOC'](https://github.com/agiledevteam/metr/wiki/What-is-DLOC%3F)
* cc - cyclomatic complexity
* ncalls - number of calls (including calls via overriding)

[![Build Status](https://travis-ci.org/agiledevteam/metr.png)](https://travis-ci.org/agiledevteam/metr)

run
===

`sbt "run -s src-roots -d dependent-jars -t report-targets"`

Output:

    method         sloc    dloc    ncalls
    my/A.foo(I;)V  11      8.5     2
    my/A.bar()I    8       7       1
    ...

Requirements:


Samples of Android app:

* `git submodule init/update` to retrieve sample projects' source
* samples/google-iosched requires 'android-18', 'Google Repository' and 'Build Tool v18.0.1'. To build `./gradlew asDe`
* samples/github-android requires 'android-16'. To build `mvn package`
* use *dex2jar* to extract jar from apk. 
* use `-d <jar from step above>:<android.jar>` as dependencies


Notes:

* `sbt update` after cloning (retrieve dependencies)
* `sbt update/eclipse` after modifying sbt dependencies
** requires sbt eclipse plugin
* `sbt assembly` to make a one jar executable
