from subprocess import call
import os
import re
import sys

def ask_to_continue():
	# type: None -> bool
	print("Do you want to continue? [y/n]: ")
	if (sys.version_info > (3, 0)):
		rsp = input()
	else:
		rsp = raw_input()
 	return rsp is "y"

def init_submodules():
	call(["git", "submodule", "init"])
	call(["git", "submodule", "update"])

def version_to_string(version):
	return "{}.{}.{}".format(version[0], version[1], version[2])

def version_from_pom(pom_path):
	with open(pom_path, 'r') as pom_file:
		data = pom_file.read()

	version_match = re.search('<version>([0-9]+)\.([0-9]+)\.([0-9]+)</version>', data)
	major = version_match.group(1)
	minor = version_match.group(2)
	patch = version_match.group(3)
	return (major, minor, patch)

def update_pom_version(version, wd="."):
	call(["mvn", "versions:set", "-DnewVersion=" + version_to_string(version)], cwd=wd)

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
	    
def install_tocalog():
	install_mvn("tocalog")

def tocalog_version():
	return version_from_pom("tocalog/pom.xml")

def print_overview():
	print("="*30)
	print("Installing tocalog")
	print("="*30)
	print("tocalog version: {}".format(version_to_string(tocalog_version())))
	print("tecs version: {}".format(version_to_string(tecs_version())))
	print("clml version: {}".format(version_to_string(generator_version())))
	print("="*30)

def main():
	print_overview()
	if not ask_to_continue():
		return
	init_submodules()
	clean_tools()
	install_mvn("tecs/libtecs/java")
	copy_tecs_server()	
	install_mvn("clml/java")
	copy_generator()
	install_mvn("device-platform")

if __name__ == "__main__":
	main()



