package de.dfki.step.resolution;

import de.dfki.step.deprecated.kb.DataEntry;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/* person with correct name get higher confidence */
public class PersonRR implements ReferenceResolver {

    private Collection<DataEntry> persons;
    private String personName = "";

    public PersonRR(Supplier<Collection<DataEntry>> personSupplier) {
        this.persons = personSupplier.get();
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }


    @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution distribution = new ReferenceDistribution();
        List<DataEntry> candidates = persons.stream().filter(p -> p.get("name").get().equals(personName)).collect(Collectors.toList());

        for(DataEntry person: candidates) {
            distribution.getConfidences().put(person.getId(), 1.0/candidates.size());
        }

        return distribution;
    }

}
