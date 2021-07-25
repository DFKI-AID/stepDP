package de.dfki.step.blackboard.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.dfki.step.blackboard.BasicToken;
import de.dfki.step.blackboard.Board;
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
import de.dfki.step.rr.SpatialRR;
import de.dfki.step.util.LogUtils;

public class SpatialReferencePreprocessingRule extends Rule {
	// FIXME: what is the right prio for a preprocessing rule?
    private static final int DEFAULT_PRIO = 2000;
    private KnowledgeBase kb;
	
	public SpatialReferencePreprocessingRule(KnowledgeBase kb) throws Exception {
		this.setPriority(DEFAULT_PRIO);
		this.kb = kb;

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
        	IToken t = tArray[0];
        	t.usedBy(this.getUUID());
        	IToken newToken = t.createTokenWithSameContent();
    		newToken = (IToken) convertLMRefsToKBRefs(newToken);
    		newToken.getOriginTokens().add(t);
    		t.addResultingTokens(List.of(newToken), this.getUUID());
    		Pattern p;
			try {
				p = new PatternBuilder("Object", kb)
								.hasRecursiveType(RRTypes.LM_SPAT_REF)
								.build();
	    		// FIXME: what if t had two references and one was resolved?
	    		if (!p.matches(newToken)) {
	    			LogUtils.printDebugInfo("TOKEN AFTER PREPROCESSING", newToken);
	    			this.kb.getBlackboard().addToken(newToken);
	    		}

			} catch (Exception e) {
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
			if (!(prop instanceof PropReference))
				continue;
			IKBObjectWriteable innerObject = obj.getResolvedReference(prop.getName());
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
		return obj;
	}

	private Map<String, Object> lMRefToKBRef(IKBObject lmRef) {
		Map<String, Object> result = new HashMap<String, Object>();
		IKBObject io = lmRef.getResolvedReference("intendedObjectReference");
		IKBObject ro = lmRef.getResolvedReference("relatumObjectReference");
		String relation = lmRef.getString("binarySpatialRelation");
		if (io == null)
			return result;
		Map<String, Object> ioConverted = convertInnerRef(io);
		result.putAll(ioConverted);
		List<Map<String, Object>> constraints = (List<Map<String, Object>>) result.get("constraints");
		if (constraints == null)
			constraints = new ArrayList<Map<String, Object>>();
		if (ro != null && relation != null) {
			Map<String, Object> relConstraint = new HashMap<String, Object>();
			relConstraint.put("type", RRTypes.BIN_SPAT_C);
			relConstraint.put("relation", relation);
			Map<String, Object> relatumRef = convertInnerRef(ro);
			relConstraint.put("relatumReference", relatumRef);
			constraints.add(relConstraint);
		}
		result.put("constraints", constraints);

		return result;
 	}

	private Map<String, Object> convertInnerRef(IKBObject innerRef){
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("type", RRTypes.SPAT_REF);
		List<Map<String, Object>> constraints = new ArrayList<Map<String, Object>>();
		String type = innerRef.getString("refType");
		if (type != null) {
			Map<String, Object> typeConstraint = new HashMap<String, Object>();
			typeConstraint.put("type", RRTypes.TYPE_C);
			typeConstraint.put("refType", type);
			constraints.add(typeConstraint);
		}
		result.put("constraints", constraints);
		return result;
	}

}
