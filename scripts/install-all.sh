git submodule update --init --recursive
mvn clean install -f ../external/tecs/libtecs/java/pom.xml -DskipTests
mvn install -f ../src/pom.xml 
echo "Finished. Press enter to coninue."
read