package de.dfki.step.blackboard.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.step.blackboard.BasicToken;
import de.dfki.step.blackboard.Board;
import de.dfki.step.blackboard.Condition;
import de.dfki.step.blackboard.IToken;
import de.dfki.step.blackboard.KBToken;
import de.dfki.step.blackboard.Rule;
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
import de.dfki.step.rr.SpatialRR;
import de.dfki.step.util.LogUtils;

public class SpatialReferencePreprocessingRule extends Rule {
    private static final Logger log = LoggerFactory.getLogger(SpatialReferencePreprocessingRule.class);
	// FIXME: what is the right prio for a preprocessing rule?
    private static final int DEFAULT_PRIO = 2000;
    private KnowledgeBase kb;
    private IKBObject speaker;
    private RRConfigParameters config;
	
	public SpatialReferencePreprocessingRule(KnowledgeBase kb, IKBObject defaultSpeaker, RRConfigParameters config) throws Exception {
		this.setPriority(DEFAULT_PRIO);
		this.kb = kb;
		this.config = config;
		this.speaker = defaultSpeaker;
		if (speaker == null)
			throw new Exception("Preprocessing rule needs to know the default speaker.");

		PatternBuilder builder = new PatternBuilder(RRTypes.USER_INTENT, kb);
		builder.hasRecursiveType(RRTypes.LM_SPAT_REF);
		Pattern p = builder.build();
		this.setCondition(new PatternCondition(p));
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
        		newToken = (IToken) convertLMRefsToKBRefs(newToken);
        		newToken.getOriginTokens().add(t);
        		t.addResultingTokens(List.of(newToken), this.getUUID());
        		Pattern p;
				p = new PatternBuilder("Object", kb)
								.hasRecursiveType(RRTypes.LM_SPAT_REF)
								.build();
	    		// FIXME: what if t had two references and one was resolved?
	    		if (!p.matches(newToken)) {
	    			LogUtils.printDebugInfo("TOKEN AFTER PREPROCESSING", newToken);
	    			this.kb.getBlackboard().addToken(newToken);
	    		}
				Pattern rrPattern = new PatternBuilder("Object", kb)
						.hasRecursiveType(RRTypes.SPAT_REF)
						.build();
				if (!rrPattern.matches(newToken)) {
					// rr rule will not trigger so print csv now
				    log.error("Preprocessing of spatial reference failed.");
				}
        	} catch (Exception e) {
        		log.error("Exception in spatial reference preprocessing rule.");
        		e.printStackTrace();
        	}

        }
	}

	private IKBObjectWriteable convertLMRefsToKBRefs(IKBObjectWriteable obj) {
		if (obj.getType() == null)
			return obj;
		if (obj.getType().equals(kb.getType(RRTypes.LM_SPAT_REF))) {
			Map<String, Object> kbRef = lMRefToKBRef(obj);
			BasicToken result = new BasicToken(kb);
			result.addAll(kbRef);
			return result;
		}
		for (IProperty prop : obj.getType().getProperties()) {
			IKBObjectWriteable[] innerObjects = obj.getResolvedRefOrRefArray(prop.getName());
			if (innerObjects == null)
				continue;
			for (IKBObjectWriteable innerObject : innerObjects) {
				if (innerObject == null)
					continue;
				if (innerObject.getType().equals(kb.getType(RRTypes.LM_SPAT_REF))) {
					Map<String, Object> innerKBRef = lMRefToKBRef(innerObject);
					obj.setReference(prop.getName(), innerKBRef);
				} else {
					IKBObjectWriteable converted = convertLMRefsToKBRefs(innerObject);
						obj.setReference(prop.getName(), converted);
				}
			}
		}
		return obj;
	}

	private Map<String, Object> lMRefToKBRef(IKBObject lmRef) {
		Map<String, Object> result = new HashMap<String, Object>();
		IKBObject io = lmRef.getResolvedReference("intendedObjectReference");
		IKBObject ro = lmRef.getResolvedReference("relatumObjectReference");
		String binRelation = lmRef.getString("binarySpatialRelation");
		if (io == null)
			return result;
		Map<String, Object> ioConverted = convertInnerRef(io, null);
		result.putAll(ioConverted);
		List<Map<String, Object>> constraints = (List<Map<String, Object>>) result.get("constraints");
		if (constraints == null)
			constraints = new ArrayList<Map<String, Object>>();
		Map<String, Object> additionalROConstraint = null;
		String region = lmRef.getString("region");
		if (region != null) {
			Map<String, Object> regionConstraint = new HashMap<String, Object>();
			regionConstraint.put("type", RRTypes.REGION_C);
			regionConstraint.put("region", region);
			if (config.MAP_AMBIGUOUS_CONSTRAINTS_TO_IO)
				constraints.add(regionConstraint);
			else
				additionalROConstraint = regionConstraint;
		}
		if (ro != null && binRelation != null) {
			Map<String, Object> binRelConstraint = new HashMap<String, Object>();
			binRelConstraint.put("type", RRTypes.BIN_SPAT_C);
			binRelConstraint.put("relation", binRelation);
			Map<String, Object> relatumRef = convertInnerRef(ro, additionalROConstraint);
			binRelConstraint.put("relatumReference", relatumRef);
			constraints.add(binRelConstraint);
		}
		result.put("constraints", constraints);

		return result;
 	}

	private Map<String, Object> convertInnerRef(IKBObject innerRef, Map<String, Object> additionalConstraint){
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("type", RRTypes.SPAT_REF);
		result.put("speaker", this.speaker.getUUID().toString());
		List<Map<String, Object>> constraints = new ArrayList<Map<String, Object>>();
		if (additionalConstraint != null)
			constraints.add(additionalConstraint);
		result = putNumber(result, innerRef, "cardinality", "nuance_CARDINAL_NUMBER");
		String type = innerRef.getString("refType");
		if (type != null) {
			Map<String, Object> typeConstraint = new HashMap<String, Object>();
			typeConstraint.put("type", RRTypes.TYPE_C);
			typeConstraint.put("refType", type);
			constraints.add(typeConstraint);
		}
		String region = innerRef.getString("region");
		if (region != null) {
			Map<String, Object> regionConstraint = new HashMap<String, Object>();
			regionConstraint.put("type", RRTypes.REGION_C);
			regionConstraint.put("region", region);
			constraints.add(regionConstraint);
		}
		if (innerRef.isSet("groupRelation") || innerRef.isSet("ordinality")) {
			Map<String, Object> groupRelConstraint = new HashMap<String, Object>();
			groupRelConstraint.put("type", RRTypes.GROUP_REL_C);
			// FIXME: make this adjustment in the LM
			String groupRelation = innerRef.getString("groupRelation");
			if (groupRelation != null) {
				groupRelation = groupRelation.replace("most", "");
				groupRelConstraint.put("relation", groupRelation);
			}
			groupRelConstraint = putNumber(groupRelConstraint, innerRef, "ordinality", "nuance_ORDINAL_NUMBER");
			constraints.add(groupRelConstraint);
		}
		result.put("constraints", constraints);
		return result;
	}

	private Map<String, Object> putNumber(Map<String, Object> map, IKBObject obj, String propName, String nuanceConcept){
		String numStr = null;
		IKBObject numObj = obj.getResolvedReference(propName);
		if (numObj != null)
			numStr = numObj.getString(nuanceConcept);
		if (numStr != null)
			try {
				int numInt = Integer.parseInt(numStr);
				map.put(propName, numInt);
			} catch (Exception e) {
				log.warn(numStr + " is not a valid " + propName + ".");
			}
		return map;
	}
}
