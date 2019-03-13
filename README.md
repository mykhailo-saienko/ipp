# Iterator++

This project augments and extends the standard Java *Iterator* and *Iterable* classes. In particular, it provides:  

* a collection of proxy-Iterators, proxy-Spliterators, and proxy-Iterables which allow filtering, mapping values or setting up custom hooks on deletion;
* the interface *Tree* and several its implementations which offer a flexible way of recursively traversing over all elements. In addition, these tree-like structures are imbued with a powerful associative structure allowing the user to perform recursive searches for an elements by its full path within the structure. All *Tree*-implementations are heavily based on the proxy-Iterables and proxy-Iterators mentioned above;
* the class *Iterables* containing a variety of methods for searching in or modifying Iterables and Collections and the class *Streams* for performing similar operations on Java-Streams;
* the class *Algorithms* which is a collection of routines that manipulate Java classes related to functional programming, such as *Function, Predicate, Operator, Consumer, Supplier*, etc.