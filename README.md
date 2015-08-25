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

## Examples

If you want to build an application with on-demand module loading, or want to
use your code in more than one way, you can use Sceletus to load and configure
your modules based on the configuration file.

A pipeline like workflow is also easy to achieve with the help of the queues.
Extend the queues for your own use-case and process as lots of entities per
second with Sceletus. (Performance is on the plate.)

## Configuration

The whole framework is configured with a simple JSON file at the moment.
No include or overwrite is supported, but you can extend the configuration too.
