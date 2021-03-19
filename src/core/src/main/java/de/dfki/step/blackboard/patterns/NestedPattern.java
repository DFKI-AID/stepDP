package de.dfki.step.blackboard.patterns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.semantic.Type;

/**
 * Complex pattern that is composed of patterns defining constraints for the
 * top level object and also for inner objects that are referenced by the top
 * level object.
 * It is highly recommended to use the {@link de.dfki.step.blackboard.patterns.PatternBuilder} to define such
 * complex patterns.
 */
public class NestedPattern extends Pattern {
	private Type rootType;
	private List<Pattern> _rootPatterns = new ArrayList<Pattern>();
	private Map<String, Pattern> _refPropPatterns = new HashMap<String, Pattern>();

	/**
	 * It is strongly recommended to use {@link de.dfki.step.blackboard.patterns.PatternBuilder} instead of this constructor since
	 * it is more intuitive to use and it also performs some validity checks on the pattern.
	 */
	public NestedPattern(List<Pattern> rootPatterns, Map<String, Pattern> refPropPatterns) {
		if (rootPatterns != null)
			this._rootPatterns.addAll(rootPatterns);
		if (refPropPatterns != null)
			this._refPropPatterns.putAll(refPropPatterns);
		// sort by priority such that higher prio patterns are checked first for more efficiency
		Collections.sort(this._rootPatterns, (p1,p2)->Integer.compare(p1.getPriority(), p2.getPriority()));
		Optional<Pattern> typePattern = rootPatterns.stream().filter(p -> (p instanceof TypePattern)).findFirst();
		if (typePattern.isPresent())
			this.rootType = ((TypePattern) typePattern.get()).getType();
	}

	@Override
	public boolean matches(IKBObject root) {
		for (Pattern p : _rootPatterns)
			if (!p.matches(root))
				return false;
		for (String propKey : _refPropPatterns.keySet()) {
			IKBObject child = root.getResolvedReference(propKey);
			if (child == null)
				return false;
			Pattern p = _refPropPatterns.get(propKey);
			if ((p != null) && (!p.matches(child)))
				return false;
		}
		return true;
	}

	@Override
	public boolean hasType() {
		return rootType != null;
	}

	@Override
	public Type getType() {
		return rootType;
	}
}
