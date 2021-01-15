package de.dfki.step.blackboard.patterns;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.semantic.Type;

/**
 * Pattern that matches objects of the given type.
 */
public class TypePattern extends Pattern {
	Type _type;

	public TypePattern(Type type) {
		this.setPriority(100);
		this._type = type;
	}
	
	@Override
	public boolean matches(IKBObject root) {
		if (root == null)
			return false;
		if (_type != null) {
			if (root.getType() == null)
				return false;
			if(!root.getType().isInheritanceFrom(this._type))
				return false;
		}
		return true;
	}

	public Type getType() {
		return this._type;
	}
}
