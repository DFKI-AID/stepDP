package de.dfki.step.blackboard.rules;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.step.blackboard.Board;
import de.dfki.step.blackboard.Condition;
import de.dfki.step.blackboard.IToken;
import de.dfki.step.blackboard.KBToken;
import de.dfki.step.blackboard.Rule;
import de.dfki.step.blackboard.ValueReplacement;
import de.dfki.step.blackboard.conditions.PatternCondition;
import de.dfki.step.blackboard.patterns.Pattern;
import de.dfki.step.blackboard.patterns.PatternBuilder;
import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.IKBObjectWriteable;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.RRTypes;
import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.PropReference;
import de.dfki.step.rr.RRConfigParameters;
import de.dfki.step.rr.ResolutionResult;
import de.dfki.step.rr.SpatialRR;
import de.dfki.step.rr.constraints.BinarySpatialRelationScorer;
import de.dfki.step.rr.constraints.ObjectScore;
import de.dfki.step.util.LogUtils;

public class SpatialReferenceResolutionRule extends Rule {
    private static final Logger log = LoggerFactory.getLogger(SpatialReferenceResolutionRule.class);
    private static final int DEFAULT_PRIO = 3000;
    private RRConfigParameters config;
	KnowledgeBase kb;
	SpatialRR rr;

	/**
	 * 
	 * @param kb
	 * @param minTokenAge in millisecs
	 * @throws Exception
	 */
	public SpatialReferenceResolutionRule(KnowledgeBase kb, long minTokenAge, RRConfigParameters config) throws Exception {
		this.config = config;
		this.setPriority(DEFAULT_PRIO);
		this.kb = kb;
		this.rr = new SpatialRR(kb);

		PatternBuilder builder = new PatternBuilder(RRTypes.USER_INTENT, kb);
		builder.hasRecursiveType(RRTypes.SPAT_REF);
		Pattern p = builder.build();
		Condition c = new PatternCondition(p);
		c.setMinTokenAge(minTokenAge);
		this.setCondition(c);
	}
	
	@Override
	public void onMatch(List<IToken[]> tokens, Board board) {
        if(tokens == null || tokens.size() == 0)
            return;

        for (IToken[] tArray : tokens) {
        	try {
            	IToken t = tArray[0];
            	t.usedBy(this.getUUID());
            	IToken newToken = t.createTokenWithSameContent();
        		IToken resolved = (IToken) findAndResolveReferences(newToken);
        		resolved.getOriginTokens().add(t);
        		t.addResultingTokens(List.of(resolved), this.getUUID());
        		Pattern p;
				p = new PatternBuilder("Object", kb)
								.hasRecursiveType(RRTypes.SPAT_REF)
								.build();
	    		// FIXME: what if t had two references and one was resolved?
	    		if (!p.matches(resolved)) {
	    			this.kb.getBlackboard().addToken(resolved);
	    			LogUtils.printDebugInfo("RESOLVED TOKEN", resolved);
	    		}
        	} catch (Exception e) {
        		log.error("Exception in spatial reference resolution rule.");
        		e.printStackTrace();
        	}

        }
	}

	// replaces spatial references with the most likely referent object
	private IKBObjectWriteable findAndResolveReferences(IKBObjectWriteable obj) {
		if (obj.getType() == null)
			return obj;
		// FIXME: how to deal with top-level references?
//		if (RRTypes.isSpatialReference(obj, kb)) {
//			IKBObject referent = resolveReference(obj);
//			return new KBToken(kb, referent);
//		}
		for (IProperty prop : obj.getType().getProperties()) {
			IKBObjectWriteable[] innerObjs = obj.getResolvedRefOrRefArray(prop.getName());
			if (innerObjs == null)
				continue;
			for (IKBObjectWriteable innerObject : innerObjs) {
				if (innerObject == null)
					continue;
				if (RRTypes.isSpatialReference(innerObject, kb)) {
					List<UUID> innerReferents = resolveReference(innerObject);
					if (innerReferents != null && !innerReferents.isEmpty())
						// FIXME: what if there's more than one reference in the array (others will be overriden)
						obj.setReferenceArray(prop.getName(), innerReferents.toArray(new UUID[] {}));
				} else {
					IKBObjectWriteable resolved = findAndResolveReferences(innerObject);
						obj.setReference(prop.getName(), resolved);
					
				}
			}
		}
		return obj;
	}
	
	private List<UUID> resolveReference(IKBObjectWriteable ref) {
		ResolutionResult result = this.rr.resolveReference(ref, this.config);
		List<ObjectScore> referents = result.getMostLikelyReferents(ref.getInteger("cardinality"));
		List<String> referentNames = referents.stream().map(r -> r.getObject().getName()).collect(Collectors.toList());
		if (referents.isEmpty()) {
		    log.debug("NO INTENDED OBJECT FOUND");
			return null;
		}
	    log.debug("INTENDED OBJECTS: " + referentNames);
	    return referents.stream().map(r -> r.getObject().getUUID()).collect(Collectors.toList());
	}
}
