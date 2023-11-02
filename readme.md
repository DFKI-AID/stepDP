# stepDP: A Step Towards Expressive and Pervasive Dialog Platforms

## Getting Started

You can find the Main Documentation under [stepdp.dfki.de](http://stepdp.dfki.de).


## Components and Classes

### Token
An important class is the [Token](src/core/src/main/java/de/dfki/step/blackboard/BasicToken.java). It is persistent data structure to store arbitrary data. Think of it as map (in the form of String -> Object) or as a json object. Nevertheless, any java object can be stored. It is recommended, to only store immutable data to avoid concurrent modification and ensure a persistent dialog history. Input to the fusion component are stored in token. Intents or other messages are forwareded as tokens into the dialog core. Requests for presentation are encapsulated in tokens as well. This allows a very flexible approach with respect to extensiblity and reusablity of algorithms. However, data access may be more inconvenient. If your data is getting to complex, it might be suitable to create a own class for it and use a token to move it through the system. See [TokenTest](src/test/java/de/dfki/step/core/TokenTest.java) for such an example and general usage.

### Clock
stepDP does not use the system clock (e.g. System.currentTimeMillis()). The clock itself counts the number of update calls, whereby the frequency can be defined when the clock object is created. This approach makes 
- debugging easier: Time does not progress during break points
- it easier to define snapshot points
The clock class provides function for converting time into the number of iteration.


## Project Overview
The project is a multi-module maven project. This makes it easier to distribute parts of the code without many dependencies.

### core
The core-module contains the main code of the dialog platform. 

### spring
The spring-module uses spring boot to provide a web interface as well as the application entry points and configuration. 

### example
The example-module shows how the core and spring modules can be used to build an executable jar.


## Tools
### Web GUI / API
The web gui can be opened with a webbrowser on e.g. [http://localhost:50000](http://localhost:50000) (maps to /index.html) .

### State Chart Editor
The [Qt Editor](https://www.qt.io/download) comes with a scxml editor that can be used to create the state chart. You don't have to install the whole qt package, the editor is enough. See checkboxes during installation.

## Notes

<img align="left" height="150" src="bmbf.jpg">
The work on stepDP has been funded by the German Ministry of Education and Research (BMBF) in projects MADMACS (grant number 01IW14003), TRACTAT (grant number 01IW17004), and CAMELOT (grant number 01IW20008).

