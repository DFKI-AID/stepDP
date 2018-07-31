#!/bin/bash
cd tecs/libtecs/java
mvn install -DskipTests
cp server/target/tecs-server-3.0.0-shaded.jar ../../../tools/tecs-server-3.0.0.jar
cd ../../..

cd device-platform
mvn install
cd ..

cd clml/java
mvn install
cd ..
