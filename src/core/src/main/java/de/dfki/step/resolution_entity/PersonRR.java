package de.dfki.step.resolution_entity;

import de.dfki.step.kb.Entity;
import de.dfki.step.kb.Ontology;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/* person with correct name get higher confidence */
public class PersonRR implements ReferenceResolver {

    private Collection<Entity> persons;
    private String personName = "";

    public PersonRR(Supplier<Collection<Entity>> personSupplier) {
        this.persons = personSupplier.get();
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }


    @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution distribution = new ReferenceDistribution();
        List<Entity> candidates = persons.stream().filter(p -> p.get(Ontology.name).get().equals(personName)).collect(Collectors.toList());

        for(Entity person: candidates) {
            distribution.getConfidences().put(person.get(Ontology.id).get(), 1.0/candidates.size());
        }

        return distribution;
    }

}
