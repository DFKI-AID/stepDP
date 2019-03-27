package de.dfki.step.resolution;

import de.dfki.step.kb.Entity;
import de.dfki.step.kb.Ontology;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/* persons of the requested gender receive confidence */
public class GenderRR implements ReferenceResolver {


    private String gender = "";
    private Collection<Entity> persons;



    public GenderRR(Supplier<Collection<Entity>> personSupplier) {
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
            for(Entity person: persons) {
                distribution.getConfidences().put(person.get(Ontology.id).get(),  1.0/persons.size());
            }
            return distribution;
        }

        Collection<Entity> genderPersons = persons.stream()
                .filter(p -> p.get(Ontology.gender).orElse("").equals(gender))
                .collect(Collectors.toList());

        Collection<Entity> otherPersons = persons.stream()
                .filter(p -> !p.get(Ontology.gender).orElse("").equals(gender))
                .collect(Collectors.toList());


        // better: take confidence that agent is in session
        for(Entity p: genderPersons) {
            distribution.getConfidences().put(p.get(Ontology.id).get(), 1.0/genderPersons.size());
        }

        for(Entity other: otherPersons) {
            distribution.getConfidences().put(other.get(Ontology.id).get(), 0.0);
        }

        return distribution;

    }


}
