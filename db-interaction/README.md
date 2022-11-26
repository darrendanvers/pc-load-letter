# Database Interaction

At their heart, many enterprise applications basically read something from a database, do
a bit of stuff with it, and write it back. While individual query performance is important, 
I find that most of the time when an application has database performance issues, this is rarely the
root cause. Instead, how the application interacts wit the database is the culprit.

This project contains a handful of examples of bad ways of interacting with a database and ways
to correct them.

This application relies on a database you can configure and run with the files in the
[db](../db) directory.

A brief description of what each package contains is below, but the details of how and why something
works (or doesn't) is in each class.

## Running the Applications

Most classes can be run as individual applications. A few classes are Spring Boot applications. 
These are annotated with `@SpringBootApplication` at the top of each class; `@SpringBootApplication` 
is commented out. To run each of these, uncomment that line and then run the `main` method in that 
class. Only one class at a time should have the annotation uncommented.

## The `resources` Package

This package contains classes that show the importance of closing resources you open.

## The `pooling` Package

This package demonstrates the performance gains and limits of database connection pooling.

## The `loading` Package

This package performs double-duty. It demonstrates using JDBC bulk inserts, and it loads data for the remaining
demonstrations.

## The `reading` Package

This package demonstrates various ways of processing ResultSets as streams so that you do not
exhaust resources reading in a full ResultSet into memory.

## The `locking` Package

This package demonstrates issues with reading and updating records in a multi-service environment
and how to lock records for exclusive access.
