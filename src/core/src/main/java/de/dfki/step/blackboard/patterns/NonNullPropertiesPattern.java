package de.dfki.step.blackboard.patterns;

import java.util.ArrayList;
import java.util.List;

import de.dfki.step.kb.IKBObject;

/**
 * Pattern that matches objects that have non-null values for the given properties.
 */
public class NonNullPropertiesPattern extends Pattern {
	List<String> _props;

	public NonNullPropertiesPattern(String... properties) {
		this.setPriority(200);
		_props = List.of(properties);
	}

	@Override
	public boolean matches(IKBObject root) {
		for (String propName : _props) {
			if (!root.isSet(propName))
				return false;	
		}
		return true;
	}
	
	/**
	 * Returns a copy of the list of properties that should not be null. It does not
	 * return the list itself, because once the pattern is created the list of non 
	 * null properties should not change anymore to avoid inconsistencies.
	 * @return 
	 */
	public List<String> getProperties(){
		return new ArrayList<String>(_props);
	}
}
