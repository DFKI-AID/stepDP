namespace java de.dfki.assemblyrobot
namespace csharp DFKI.AssemblyRobot 


struct UpdateGrammar {
	1: string grammar;
}

/**
* Thrift for interacting with the hololens
**/
service AssemblyRobotView {
	void updateGrammar(1: required UpdateGrammar updateGrammar);
}
