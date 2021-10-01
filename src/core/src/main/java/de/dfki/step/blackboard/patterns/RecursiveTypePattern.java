package de.dfki.step.blackboard.patterns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.PropReference;
import de.dfki.step.kb.semantic.PropReferenceArray;
import de.dfki.step.kb.semantic.Type;

/**
 * Pattern that matches objects that have a given type t1 or contain a property with type t1. 
 * It recursively checks the object itself and all of its inner objects.
 * Mainly for performance reasons, a root type t2 can be defined. In this case, only objects
 * of type t2 are checked for inner objects of type t1. This helps to avoid performance 
 * issues that could occur when high-frequency inputs, such as sensor data, are all scanned
 * recursively by this pattern. 
 */
public class RecursiveTypePattern extends Pattern {
	Type _recursiveType;
	Type _rootType;

	public RecursiveTypePattern(Type recursiveType) {
		this(recursiveType, null);
	}
	
	public RecursiveTypePattern(Type recursiveType, Type rootType) {
		this.setPriority(1000);
		this._recursiveType = recursiveType;
		this._rootType = rootType;
	}
	
	@Override
	public boolean matches(IKBObject root) {
		if (root == null)
			return false;
		if (!root.getType().isInheritanceFrom(this._rootType) && !root.getType().isInheritanceFrom(this._recursiveType))
			return false;
		return hasRecursiveType(root);
	}

	private boolean hasRecursiveType(IKBObject obj) {
		if (obj == null)
			return false;

		if (obj.getType().isInheritanceFrom(this._recursiveType))
			return true;

		for (IProperty prop : obj.getType().getProperties()) {
			if (innerMatch(obj, prop))
				return true;
		}

		return false;
	}

	public boolean innerMatch(IKBObject obj, IProperty prop) {
		IKBObject[] innerObjects = obj.getResolvedRefOrRefArray(prop.getName());
		if (innerObjects == null)
			return false;
		for (IKBObject innerObj : innerObjects) {
			if (hasRecursiveType(innerObj))
				return true;
		}
		return false;
	}
	
	@Override
	public boolean hasType() {
		// this pattern matches objects of different types, 
		// i.e. _rootType or _recursiveType
		return false;
	}

	@Override
	public Type getType() {
		return null;
	}

}
