package de.dfki.step.resolution;

import de.dfki.step.core.ReferenceDistribution;
import de.dfki.step.core.ReferenceResolver;
import de.dfki.step.kb.Entity;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.KnowledgeMap;
import de.dfki.step.kb.Ontology;

import java.util.Collection;
import java.util.stream.Collectors;

public class GenderReferenceResolver implements ReferenceResolver {


    private String gender = "";
    private KnowledgeMap personMap;



    public GenderReferenceResolver(KnowledgeBase knowledgeBase) {
        personMap = knowledgeBase.getKnowledgeMap(Ontology.Person);
    }


    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution distribution = new ReferenceDistribution();

        //return equal distribution for all person when the gender is not set
        if(gender.equals("")) {
            for(Entity person: personMap.getAll()) {
                distribution.getConfidences().put(person.get(Ontology.id).get(),  1.0/personMap.getAll().size());
            }
            return distribution;
        }

        Collection<Entity> genderPersons = personMap.getAll().stream()
                .filter(p -> p.get(Ontology.gender).orElse("").equals(gender))
                .collect(Collectors.toList());

        Collection<Entity> otherGenderPersons = personMap.getAll().stream()
                .filter(p -> !p.get(Ontology.gender).orElse("").equals(gender))
                .collect(Collectors.toList());


        // better: take confidence that agent is in session
        for(Entity p: genderPersons) {
            distribution.getConfidences().put(p.get(Ontology.id).get(), 1.0/genderPersons.size());
        }

        for(Entity p: otherGenderPersons) {
            distribution.getConfidences().put(p.get(Ontology.id).get(), 0.0);
        }



        return distribution;

    }


}
