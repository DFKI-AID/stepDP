package de.dfki.tocalog.output;

import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.Ontology;
import de.dfki.tocalog.kb.TypeHierarchy;

import java.util.Optional;

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
