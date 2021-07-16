package de.dfki.step.blackboard;

import java.util.HashMap;
import java.util.Map;

public class ValueReplacement {
	private ValueReplacement parent;
	private Map<String, Object> newValues;
	private Map<String, ValueReplacement> innerReplacements;

	public ValueReplacement() {
		this(null);
	}

	public ValueReplacement(Map<String, Object> newValues) {
		if (newValues == null || newValues.isEmpty())
			this.newValues = new HashMap<String, Object>();
		else
			this.newValues = Map.copyOf(newValues);
	}

	public ValueReplacement getInnerReplacement(String propertyName) {
		return innerReplacements.getOrDefault(propertyName, new ValueReplacement());
	}

	public void replaceValue(String propertyName, Object value) {
		newValues.put(propertyName, value);
	}
}
