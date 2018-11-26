package de.dfki.tocalog.core.resolution;

import de.dfki.tocalog.core.ReferenceDistribution;
import de.dfki.tocalog.core.ReferenceResolver;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.kb.KnowledgeMap;
import de.dfki.tocalog.kb.Ontology;

public class DiscourseReferenceResolver implements ReferenceResolver {

  //  private KnowledgeMap<Discourse> discourseMap;

    public DiscourseReferenceResolver(KnowledgeBase kb) {
   //     this.discourseMap = kb.getKnowledgeMap(Ontology.Discourse);
    }
    @Override
    public ReferenceDistribution getReferences() {
        return null;
    }
}
