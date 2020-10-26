package de.dfki.step.resolution;

import de.dfki.step.kb.DataEntry;
import de.dfki.step.kb.Entity;
import de.dfki.step.kb.Ontology;

import java.sql.DatabaseMetaData;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/* persons of the requested gender receive confidence */
public class GenderRR implements ReferenceResolver {


    private String gender = "";
    private Collection<DataEntry> persons;



    public GenderRR(Supplier<Collection<DataEntry>> personSupplier) {
        persons = personSupplier.get();
    }


    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution distribution = new ReferenceDistribution();

        //return equal distribution for all person when the gender is not set
        if(gender.equals("")) {
            for(DataEntry person: persons) {
                distribution.getConfidences().put(person.getId(),  1.0/persons.size());
            }
            return distribution;
        }

        Collection<DataEntry> genderPersons = persons.stream()
                .filter(p -> p.get("gender").orElse("").equals(gender))
                .collect(Collectors.toList());

        Collection<DataEntry> otherPersons = persons.stream()
                .filter(p -> !p.get("gender").orElse("").equals(gender))
                .collect(Collectors.toList());


        // better: take confidence that agent is in session
        for(DataEntry p: genderPersons) {
            distribution.getConfidences().put(p.getId(), 1.0/genderPersons.size());
        }

        for(DataEntry other: otherPersons) {
            distribution.getConfidences().put(other.getId(), 0.0);
        }

        return distribution;

    }


}
