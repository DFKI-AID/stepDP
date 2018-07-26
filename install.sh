#!/bin/bash
cd tecs/libtecs/java
mvn install -DskipTests
cd ../../..

cd device-platform
mvn install
cd ..

cd idl
mvn install
cd ..
