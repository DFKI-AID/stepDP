package de.dfki.step.rr.constraints;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.semantic.Type;
import de.dfki.step.rr.RRConfigParameters;
import de.dfki.step.util.LogUtils;

public class TypeScorer extends ConstraintScorer {
    private static final Logger log = LoggerFactory.getLogger(TypeScorer.class);
	private static final int DEFAULT_PRIORITY = 3000;
	private Type type;
	private RRConfigParameters config;

	public TypeScorer(IKBObject constraint, KnowledgeBase kb, RRConfigParameters config) throws Exception {
		super(constraint, kb);
		this.config = config;
		this.setPriority(DEFAULT_PRIORITY);
		String refType= constraint.getString("refType");
		this.type = kb.getType(refType);
		if (this.type == null)
			log.error("Type does not exist in kb:" + refType);
	}

	@Override
	public List<ObjectScore> updateScores(List<ObjectScore> scores) {
		if (type == null)
			return scores;
		for (ObjectScore curScore : scores) {
			IKBObject obj = curScore.getObject();
			float accScore;
			if (obj.getType().isInheritanceFrom(type))
				accScore = 1;
			else
				accScore = 0;
			curScore.accumulateScore(accScore);
		}
		LogUtils.logScores("Totals after scoring Type " + this.type.getName(), scores);
		return scores;
	}

}
