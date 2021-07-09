package de.dfki.step.rr;

import de.dfki.step.kb.IKBObject;

public interface ReferenceResolver {
	
	public ResolutionResult resolveReference(IKBObject reference) throws Exception;

}
