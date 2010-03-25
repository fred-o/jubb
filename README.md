## Overview

Jubb is a simple distributed job queue with a minimal footprint and
setup cost. It features:

 * Persistent FIFO queuing of jobs.
 * A simple REST API for adding and removing jobs, and retrieving queue status information.
 * A job is an arbitrary chunk of text. The default serialization option is to use JSON, but 
   there's nothing stopping you from going with XML, regular Java serialization, YAML or whatever.
 * Support for attaching metadata to jobs using HTTP headers. 

## Installation

Deploy jubb-servlet.war as you normally would with a regular web application.

## API
TBA

