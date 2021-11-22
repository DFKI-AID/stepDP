package de.dfki.step.kb;

import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.PropReference;
import de.dfki.step.kb.semantic.PropReferenceArray;
import de.dfki.step.kb.semantic.Type;

import java.util.ArrayList;
import java.util.Arrays;
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
    IKBObject getResolvedReference(String propertyName);
    default IKBObject getResolvedReference(List<String> path) {
    	if (path.isEmpty())
    		return null;
		IKBObject current = getResolvedReference(path.get(0));
		for (String property : path.subList(1, path.size())) {
			if (current == null)
				return null;
			current = current.getResolvedReference(property);
		}
		return current;
    }

    // returns "path", i.e. list of property names to outer object (initialize with empty list)
    public default List<String> findInnerObjOfType(List<String> path, Type type) {
        if (this == null)
            return null;
        for (IProperty prop : this.getType().getProperties()) {
            if (prop instanceof PropReference || prop instanceof PropReferenceArray) {
                IKBObject[] innerObjs = this.getResolvedReferenceArray(prop.getName());
                for (IKBObject innerObj : innerObjs) {
                    if (innerObj.getType().isInheritanceFrom(type)) {
                           path.add(prop.getName());
                           return path;
                    }
                    else {
                        path.add(prop.getName());
                        List<String> innerPath = innerObj.findInnerObjOfType(path, type);
                        if (innerPath != null) {
                            path.addAll(innerPath);
                            return path;
                        }
                    }
                }
            }
        }
        return null;
    }

    String[] getStringArray(String propertyName);
    Integer[] getIntegerArray(String propertyName);
    Boolean[] getBooleanArray(String propertyName);
    Float[] getFloatArray(String propertyName);
    UUID[] getReferenceArray(String propertyName);
    IKBObject[] getResolvedReferenceArray(String propertyName);

}
