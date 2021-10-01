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
    /**
     * Works for properties that can hold one reference and for properties that can hold
     * an array of references.
     */
    default IKBObjectWriteable[] getResolvedRefOrRefArray(String propertyName) {
    	IProperty prop = getProperty(propertyName);
	    List<IKBObjectWriteable> innerObjs = new ArrayList<IKBObjectWriteable>();
	    // FIXME: find a better solution for this
	    try {
	        IKBObjectWriteable innerObj = getResolvedReference(propertyName);
	        if (innerObj != null)
	        	innerObjs.add(innerObj);
	        else
	        	throw new Exception();
	    } catch (Exception e1) {
	    	try {
	    		IKBObjectWriteable[] innerObjsArray = getResolvedReferenceArray(propertyName);
	            if (innerObjsArray != null)
		            innerObjs.addAll(Arrays.asList(innerObjsArray));
	            else
	            	throw new Exception();
	    	} catch (Exception e2) {
	    		return null;
	    	}
	    }
	    return innerObjs.toArray(new IKBObjectWriteable[innerObjs.size()]);

    }

}
