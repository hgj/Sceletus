# Sceletus

Sceletus is a Java framework or library that helps you build module based multi
threaded applications. Sceletus is an improved / standardised version of its
unreleased predecessor, developed and used for a lot of projects.

Feel free to use, share, fork or contribute. The framework is licenced under
the Creative Commons Attribution 4.0 International licence.

## Modules and Queues

There are two main components - modules and queues - that help you save time
while developing a module based, multi threaded application with Sceletus.

Modules are standalone components of the system, with their own lifecycle,
managed by the framework. They can be loaded dynamically, configured, started
and stopped by the framework.

You can utilise queues to help with a pipeline like processing of your data.

## Configuration

The whole framework is configured with a simple JSON file at the moment. No
include or overwrite is supported.

## Logging

Sceletus uses SLF4J for logging. Rules for logging are the following:

* **TRACE**: Everything that would pollute the log in a production environment.
* **DEBUG**: Extra information, normally not required in a production environment.
* **INFO**: Informational messages that are useful in a production environment.
* **WARNING**: Anything close to not normal behaviour that does not cause your
  application/workflow to fail.
* **ERROR**: A fatal error from what there is no recovery.

**NOTE**: It is strongly advised that you enable deduplication in your logger.