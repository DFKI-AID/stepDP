from subprocess import call
import os
import re

def init_submodules():
	call(["git", "submodule", "init"])
	call(["git", "submodule", "update"])

def version_from_pom(pom_path):
	with open(pom_path, 'r') as pom_file:
		data = pom_file.read()

	version_match = re.search('<version>([0-9])\.([0-9])\.([0-9])</version>', data)
	major = version_match.group(1)
	minor = version_match.group(2)
	patch = version_match.group(3)
	return (major, minor, patch)

def tecs_version():
	return version_from_pom('tecs/libtecs/java/pom.xml')

def copy_tecs_server(version=None):
	# type: (int, int, int) -> None
	if not version:
		version = tecs_version()
	version_str = version[0] + "." + version[1] + "." + version[2]
	call(["cp", "tecs/libtecs/java/server/target/tecs-server-" + version_str + "-shaded.jar", "tools/tecs-server.jar"], cwd=".")

def copy_generator():
	version = generator_version()
	version_str = version[0] + "." + version[1] + "." + version[2]
	call(["cp", "clml/java/generator/target/idl-generator-" + version_str + ".jar", "tools/clml-generator.jar"], cwd=".")

def generator_version():
	return version_from_pom('clml/java/pom.xml')

def install_mvn(path):
	call(["mvn", "install", "-DskipTests"], cwd=path)

def clean_tools():
	import glob
	# call(["rm", "tools/*.jar"], cwd=".")
	files = glob.glob("tools/*.jar")
	for file in files:
		print("removing " + str(file))
		os.remove(file)
	    


if __name__ == "__main__":
	init_submodules()
	clean_tools()
	install_mvn("tecs/libtecs/java")
	copy_tecs_server()	
	install_mvn("clml/java")
	copy_generator()
	install_mvn("device-platform")



