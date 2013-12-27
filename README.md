metr
====

metric calculator for Java projects

for each method:

* sloc - source lines of code
* dloc - [dloc](https://github.com/agiledevteam/metr/wiki/What-is-DLOC%3F)
* cc - cyclomatic complexity 
* (not yet) ncalls - number of calls (including calls via overriding)

[![Build Status](https://travis-ci.org/agiledevteam/metr.png)](https://travis-ci.org/agiledevteam/metr)

run
===

`sbt "run -s src"`

output
======

Each line represents `sloc - dloc - cc - name` of a method.

    11      8.5     3   method1
    8       7       2   method2
    ...


samples
=======

This project points to two submodules as samples: GitHub's android app, Google's iosched app.

* `git submodule init/update` to retrieve sample projects' source
* `samples/google-iosched` requires 'android-18', 'Google Repository' and 'Build Tool v18.0.1'. To build `./gradlew asDe`
* `samples/github-android` requires 'android-16'. To build `mvn package`
* In `samples` directory, run `scala run_samples.scala` to run `metr` on all samples. 


notes
=====

* `sbt update` will retrieve managed dependencies.
* `sbt eclipse` will generate/update eclipse project setting.
* `sbt assembly` to make a one jar executable


TODOs
====

* cyclomatic complexity: method/class/package/project
* trends (git history)
