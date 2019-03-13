# Iterator++

## Summary
This project augments and extends the standard Java *Iterator* and *Iterable* classes. In particular, it provides:  

* a collection of proxy-Iterators, proxy-Spliterators, and proxy-Iterables which allow filtering, mapping values or setting up custom hooks on deletion;
* the interface *Tree* and several its implementations which offer a flexible way of recursively traversing over all elements. In addition, these tree-like structures are imbued with a powerful associative structure allowing the user to perform recursive searches for an elements by its full path within the structure. All *Tree*-implementations are heavily based on the proxy-Iterables and proxy-Iterators mentioned above;
* the class *Iterables* containing a variety of methods for searching in or modifying Iterables and Collections and the class *Streams* for performing similar operations on Java-Streams;
* the class *Algorithms* which is a collection of routines that manipulate Java classes related to functional programming, such as *Function, Predicate, Operator, Consumer, Supplier*, etc.

## Installation
The project has the structure of an Eclipse-project without the .project-file. All you have to do is download the distribution, add log4j-1.2.15.jar,  to the classpath (a copy is included in the distribution) and show to your IDE or compiler where the sources are (they are in the `Iterator++/src`-folder).

If you also want to run the included unit-tests, you have to have JUnit4 on your classpath. All tests are stored in the `Iterator++/test/src`-folder. There is a master TestSuite called `ms.ipp.TestSuiteIterable.java` which runs all unit tests at once.

## Getting started
To familiarize with how the proxy-Iterators and proxy-Iterables work, I recommed looking at the unit test `ms.ipp.iterable.TestIterable.java`. The unit test `ms.ipp.iterable.TestTree.java` shows some simple use cases for different types of the interfance `Tree` and its concrete implementations.
