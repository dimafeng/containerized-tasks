# Containerized-tasks

Gradle plugin that allows to launch tasks inside docker containers.

## Why do I need it?

I love java for its tools and reliability. But when you step on wind land of modern development
tools like `node.js` all what you love stops working.

For instance, you have a java project that has a part written in javascript, that means you
have to test, build, minimize, concatenate it. Of course, you can install node locally and it'll work
fine until you need to share your project with co-workers or even build your project on CI servers -
that's where the hell begins.

![JS Hell](http://www.toonbarn.com/wordpress/wp-content/uploads/2012/04/Finn-is-losing-it-giving-up.jpg)

**This gradle plugin preserves consistency and immutability of the build environment.**

Now you can build your project on **different operating systems** with variety of configurations, but
get **the same result!**

## Setup

Build script snippet for use in all Gradle versions:

```
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.com.dimafeng:containerized-tasks:0.5.1"
  }
}

apply plugin: "com.dimafeng.containerizedTask"
```

Build script snippet for new, incubating, plugin mechanism introduced in Gradle 2.1:

```
plugins {
  id "com.dimafeng.containerizedTask" version "0.5.1"
}
```

## Requirements

* JDK >= 1.8

## Quick Start

Execution of node.js related environment:

```groovy
npmContainerizedTask {
    sourcesDir = 'src/clientSide'
    //imageName = 'monostream/nodejs-gulp-bower';
    //scriptBody = 'npm install\ngulp'
    //outputLevel = 'INFO' // 'ALL', 'DEBUG'
}
```
* sourcesDir - location of the folder with `package.json`
* imageName - docker image which the task will be executed in
* scriptBody - script that should be launched
* outputLevel - the level of logging which all container output will be going to. E.g. if it's set
to 'INFO' the output will be available with `-info` gradle option. 'ALL' is default.

## Release notes

* **0.5.2**
    * TestContainers 

* **0.5.1**
    * Fix of #2

* **0.4.0**
    * Removed extra empty lines from container console output

* **0.2.0**
    * `outputLevel` option
    * Newer version of *test-containers*

## License

The MIT License (MIT)

Copyright (c) 2016 Dmitry Fedosov

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
