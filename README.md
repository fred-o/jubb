## Overview

Jubb is a simple distributed job queue with a minimal footprint and
setup cost. It features:

 * Persistent FIFO queuing of jobs.
 * A simple REST API for adding and removing jobs, and retrieving queue status information.
 * A job is an arbitrary chunk of text. The default serialization option is to use JSON, but 
   there's nothing stopping you from going with XML, regular Java serialization, YAML or whatever.
 * Support for attaching metadata to jobs using HTTP headers. 

## Components

### jubb-core

Contains code common to all server-side implementations. In order to
simplify integration the `JubbFacade` class is provided. This class
contains all the functionality for handling HTTP requests against the
jubb queues, and the idea is that the actual server implementations
can simply delegate to this class. For example, the `QueueServlet`
class is a very thin wrapper around `JubbFacade` that delegates all
calls to `doGet()` and `doPost()`. 

### jubb-servlet

This is a simple Servlet-based jubb server. It is packaged as WAR file
and can be deployed to any standard J2EE servlet container.

To start the server in development mode (default port: 8081), run the
following:

> mvn jetty:run 

### jubb-client

Contains classes for a jubb client implementation. The class
`JsonJubbClient` provide methods for direct access to the jubb queue,
while `JubbProducer` and `JubbConsumer` provide a higher-level,
asynchronous interface.

## Installation

Deploy jubb-servlet.war as you normally would with a regular web application.

## API
TBA

