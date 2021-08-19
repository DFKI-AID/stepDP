package de.dfki.step.kb;

import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.Type;

import java.util.List;
import java.util.UUID;

public interface IKBObject extends IUUID {

    String getName();

    boolean hasProperty(String propertyName);
    IProperty getProperty(String propertyName);
    boolean isSet(String propertyName);

    Type getType();
    String getString(String propertyName);
    Integer getInteger(String propertyName);
    Boolean getBoolean(String propertyName);
    Float getFloat(String propertyName);
    UUID getReference(String propertyName);
    IKBObjectWriteable getResolvedReference(String propertyName);
    default IKBObjectWriteable getResolvedReference(List<String> path) {
    	if (path.isEmpty())
    		return null;
		IKBObjectWriteable current = getResolvedReference(path.get(0));
		for (String property : path.subList(1, path.size())) {
			if (current == null)
				return null;
			current = current.getResolvedReference(property);
		}
		return current;
    }

    String[] getStringArray(String propertyName);
    Integer[] getIntegerArray(String propertyName);
    Boolean[] getBooleanArray(String propertyName);
    Float[] getFloatArray(String propertyName);
    UUID[] getReferenceArray(String propertyName);
    IKBObjectWriteable[] getResolvedReferenceArray(String propertyName);

}
