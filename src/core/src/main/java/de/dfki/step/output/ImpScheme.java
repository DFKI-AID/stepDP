package de.dfki.step.output;

import de.dfki.step.kb.Entity;
import de.dfki.step.kb.Ontology;
import de.dfki.step.kb.TypeHierarchy;

/**
 */
public class ImpScheme {
    private TypeHierarchy hierarchy; //TODO set

    public boolean validateService(Entity service) {
        if (!hierarchy.isA(service, Ontology.Service)) {
            return false;
        }

        if (!service.get(Ontology.service).isPresent()) {
            return false;
        }

        if(!service.get(Ontology.uri).isPresent()) {
            return false;
        }

        return true;
    }
}
