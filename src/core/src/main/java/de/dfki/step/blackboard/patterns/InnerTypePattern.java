package de.dfki.step.blackboard.patterns;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.PropReference;
import de.dfki.step.kb.semantic.Type;

/**
 * Matches any object that itself has the given type or contains a property of the
 * given type. It recursively checks the object itself and all of its inner objects.
 * Mainly for performance reasons, a root type can be defined, such that only objects
 * of that root type are checked for inner objects of the given type. This helps to 
 * avoid performance issues that could occur when frequent inputs such as sensor data
 * are all scanned recursively by this pattern. 
 */
public class InnerTypePattern extends Pattern {
	Type _innerType;
	Type _rootType;

	public InnerTypePattern(Type innerType) {
		this(innerType, null);
	}
	
	public InnerTypePattern(Type innerType, Type rootType) {
		this.setPriority(1000);
		this._innerType = innerType;
		this._rootType = rootType;
	}
	
	@Override
	public boolean matches(IKBObject root) {
		if (!root.getType().isInheritanceFrom(this._rootType) && !root.getType().isInheritanceFrom(this._innerType))
			return false;
		return hasInnerType(root);
	}

	private boolean hasInnerType(IKBObject obj) {
		if (obj.getType().isInheritanceFrom(this._innerType))
			return true;

		for (IProperty prop : obj.getType().getProperties()) {
			if (!(prop instanceof PropReference))
				continue;
			IKBObject innerObject = obj.getResolvedReference(prop.getName());
			if (innerObject == null)
				continue;
			if (hasInnerType(innerObject))
				return true;
		}

		return false;
	}
	@Override
	public boolean hasType() {
		return false;
	}

	@Override
	public Type getType() {
		return null;
	}

}
