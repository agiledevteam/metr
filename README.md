metr
====

metric calculator
* sloc - lines of code (after pretty formatting)
* dloc - dloc - check out ['What is DLOC'](https://github.com/agiledevteam/metr/wiki/What-is-DLOC%3F)
* cc - cyclomatic complexity (a little bit modified)
* ncalls - number of calls (including calls via overriding)

[![Build Status](https://travis-ci.org/agiledevteam/metr.png)](https://travis-ci.org/agiledevteam/metr)

run
===

`sbt "run -i data -p <Processor> -v --compliance 6"`

Processors:

* com.lge.metr.LocCalculator - prints sloc/dloc for each method
* com.lge.metr.MethodCallCounter (work in progress)

Requirements:

* After cloning, git submodule init/update
* samples/google-iosched requires 'Google Repository' and 'Build Tool v18.0.1'
* samples/github-android requires 'android-16'
 

