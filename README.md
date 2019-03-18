# Iterator++

## Summary
This project augments and extends the standard Java *Iterator* and *Iterable* classes. In particular, it provides:  

* a collection of **Decorator-classes** for Java Iterator, Spliterator, and Iterable which allow filtering, mapping values or setting up custom hooks on deletion.
* the interface **Tree** and several its implementations which offer a flexible way of recursively traversing over all elements. In addition, every *Tree* is imbued with a powerful associative structure allowing the user to manipulate elements based on recursive searches by an element's full path within the Tree. 
* the class **Algorithms** which is a collection of routines that manipulate Java classes related to functional programming, such as *Function, Predicate, Operator, Consumer, Supplier*, etc.
* the class **Iterables** containing a variety of methods for searching in or modifying Iterables and Collections and the class **Streams** for performing similar operations on Java-Streams. Both make heavy use of the class *Algorithms*;

All *Tree*-implementations are heavily based on the above-mentioned wrappers for Iterable and Iterator and, hence, are compatible with the classes *Iterables* and *Algorithms*;

Some few `Iterator`-implementations duplicate the functionality of the *Apache Commons* library, in particular, of the package `org.apache.commons.collections.iterators`. However, the Apache Commons package works on the `Collection`-level, while our Project provides a more general view on Collections as `Iterable`. On top of that, to our knowledge, our `Tree` interface and its implementations are novel and are not contained in Apache Commons nor in any other publicly available library. 

## Installation
The project has the structure of an Eclipse-project without the .project-file. All you have to do is download the distribution, add log4j-1.2.15.jar to the classpath (it is included in the distribution in the `Iterator++/lib`-folder), and show to your IDE or compiler where the sources are (they are in the `Iterator++/src`-folder).

If you also want to run the included unit-tests, you have to have JUnit4 on your classpath. All tests are stored in the `Iterator++/test/src`-folder. There is a master TestSuite called `ms.ipp.TestSuiteIterable.java` which runs all unit tests at once.

## Getting started
To familiarize with how the proxy-Iterators and proxy-Iterables work, I recommed looking at the unit test `ms.ipp.iterable.TestIterable.java`. The unit test `ms.ipp.iterable.TestTree.java` shows some simple use cases for the interface `Tree` and its concrete implementations.

The documentation as well as simple tutorials are being currently created and will be added in the near future.
