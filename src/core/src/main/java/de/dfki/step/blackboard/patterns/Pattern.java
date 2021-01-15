package de.dfki.step.blackboard.patterns;

import de.dfki.step.kb.IKBObject;

/**
 * A pattern implements the method {@link Pattern#matches(IKBObject)} that matches objects satisfying 
 * certain constraints. It can, for example, match objects that have a certain type or whose properties 
 * have a certain type. For an example, refer to the StepDP documentation or the example maven module.
 * It is recommended to use the {@link PatternBuilder} to define patterns. 
 */
public abstract class Pattern {
	private int _priority = 10000;
	
	public abstract boolean matches(IKBObject root);
	
	/**
	 * Returns the priority of this pattern. When checking if an object matches a list of patterns,
	 * the patterns with higher priority (i.e. lower number) should be checked first. This should
	 * ensure efficient matching, e.g. by checking the type of the object, which is usually the
	 * most distinctive field, before checking anything else. 
	 */
	public int getPriority() {
		return _priority;
	}
	
	public void setPriority(int priority) {
		this._priority = priority;
	}
}
