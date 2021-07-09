package de.dfki.step.blackboard.patterns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.PropReference;
import de.dfki.step.kb.semantic.Type;

/**
 * Helper class for defining a {@link Pattern} declaratively.
 * It allows to define constraints for a root object and its properties
 * and builds a {@link Pattern} that matches objects satisfying all constraints.
 * The builder enforces that the pattern has at least a root type and checks that the added
 * constraints are consistent w.r.t. the semantic tree.
 * For an example, refer to the StepDP documentation or the example maven module.
 * Requires a reference to the knowledge base to access the semantic tree.
 */
public class PatternBuilder {
	private PatternBuilder _parent;
	private KnowledgeBase _kb;
	
	private TypePattern _rootTypePattern;
	private Type _recursiveType;
	private NonNullPropertiesPattern _nonNullPropsPattern;
	private Map<String, PatternBuilder> _refPropBuilders = new HashMap<String, PatternBuilder>();

	
	/**
	 * Creates a pattern builder.
	 * @param rootTypeName the type of the object that the pattern should match
	 * @param kb a reference to the knowledge base that contains the semantic tree
	 * @throws Exception if type name or kb are null or if the type does not exist in the semantic tree
	 */
	public PatternBuilder(String rootTypeName, KnowledgeBase kb) throws Exception {
		this(rootTypeName, kb, null);
	}
	
	private PatternBuilder(String rootTypeName, KnowledgeBase kb, PatternBuilder parent) throws Exception {
		if (kb == null)
			throw new Exception("Knowledge Base cannot be null.");
		this._kb = kb;
		Type type = getTypeFromSemanticTree(rootTypeName);
		this._rootTypePattern = new TypePattern(type);
		this._parent = parent;
	}
	
	/**
	 * Adds a type constraint to the pattern, i.e. only objects with the given type or
	 * a subtype match.
	 * @param typeName the name of the type that the pattern should match
	 * @return reference to this builder to allow method chaining
	 * @throws Exception if type name is null, if the type does not exist in the semantic tree,
	 * or if the new type constraint is in conflict with an existing type constraint in the pattern
	 */
	public PatternBuilder hasType(String typeName) throws Exception {
		Type newType = getTypeFromSemanticTree(typeName);
	    Type oldType = _rootTypePattern.getType();
		if (!newType.isInheritanceFrom(oldType))
			throw new Exception("Type has to be a subtype of " + oldType.getName());
		_rootTypePattern = new TypePattern(newType);
		return this;
	}

	/**
	 * Adds a recursive type constraint to the pattern, i.e. only objects that either are
	 * of the given type t1 or have inner object of type t1 match.
	 * Important Note: This constraint cannot be combined with any other constraints
	 * except a type constraint. Any other constraints of this builder are ignored.
	 * In combination with a type constraint of type t2, the pattern matches objects (I) of type
	 * t1 or (II) of type t2 with at least one inner object of type t1.
	 * @param typeName the name of the type that the pattern should match
	 * @return reference to this builder to allow method chaining
	 * @throws Exception if type name is null or if the type does not exist in the semantic tree
	 */
	public PatternBuilder hasRecursiveType(String typeName) throws Exception {
		this._recursiveType = getTypeFromSemanticTree(typeName);
		return this;
	}
	
	/**
	 * Adds the constraint to the pattern that it only matches objects where the given properties
	 * are set, i.e. they don't have a null value. <br>
	 * Note: For reference properties, this does not guarantee that the reference is actually 
	 * leading to a valid object. This should be checked in the execution part of the rule.
	 * @param propertyNames the names of the properties that should not be null
	 * @return reference to this builder to allow method chaining
	 * @throws Exception if a property name is null or if the type does not have a property with the
	 * given name
	 */
	public PatternBuilder hasNonNullProperties(String... propertyNames) throws Exception {
		if (propertyNames.length == 0)
			return this;
		for (String propName : propertyNames) {
			assertHasProperty(propName);
		}
		List<String> nonNullProps = new ArrayList<String>(List.of(propertyNames));
		if (_nonNullPropsPattern != null) {
			List<String> oldProps = _nonNullPropsPattern.getProperties();
			nonNullProps.addAll(oldProps);
		}
		_nonNullPropsPattern = new NonNullPropertiesPattern(nonNullProps.toArray(new String[0]));
		return this;
	}
	
	/**
	 * Allows to define a pattern for the object referenced by the given property name. 
	 * Constraints for this pattern can be defined with the pattern builder that is returned
	 * by this method. In order to navigate back to the pattern builder for the parent object, 
	 * use the method {@link PatternBuilder#endPropertyPattern}.<br>
	 * Important: Don't forget to navigate back to the root pattern builder before calling 
	 * {@link PatternBuilder#build}, because otherwise it will return the pattern for the 
	 * inner object and not the root object.
	 * @param propertyName the name of a property holding an object reference
	 * @return the pattern builder for the referenced object
	 * @throws Exception if the property name is null or if the type does not have a reference 
	 * property with the given name
	 */
	public PatternBuilder addPatternForProperty(String propertyName) throws Exception {
		PatternBuilder existingBuilder = _refPropBuilders.get(propertyName);
		if (existingBuilder != null)
			return existingBuilder;
		assertHasProperty(propertyName);
		IProperty prop = _rootTypePattern.getType().getProperty(propertyName);
		if (!(prop instanceof PropReference))
			throw new Exception("Property with name " + propertyName + " is not a reference property.");
		Type refType = ((PropReference) prop).getType();
		PatternBuilder propBuilder = new PatternBuilder(refType.getName(), _kb, this);
		_refPropBuilders.put(propertyName, propBuilder);
		return propBuilder;
	}
	
	/**
	 * Signifies the end of the pattern for a referenced object and returns the pattern builder
	 * for the parent object. <br>
	 * Important: Don't forget to navigate back to the root pattern builder before calling 
	 * {@link PatternBuilder#build}, because otherwise it will return the pattern for the 
	 * inner object and not the root object.
	 * @return the pattern builder for the parent object
	 * @throws Exception if this builder does not have a parent
	 */
	public PatternBuilder endPropertyPattern() throws Exception {
		if (_parent == null)
			throw new Exception("This pattern builder does not have a parent.");
		return _parent;
	}
	
	/**
	 * Returns a pattern that contains all constraints which were defined on this
	 * builder and its inner builders (the builders for the patterns of referenced
	 * objects).
	 * @return the resulting pattern
	 */
	public Pattern build() {
		if (_recursiveType != null) {
			// other patterns are ignored
			return new RecursiveTypePattern(_recursiveType, _rootTypePattern.getType());
		}
		List<Pattern> rootPatterns = new ArrayList<Pattern>();
		if (_rootTypePattern != null)
			rootPatterns.add(_rootTypePattern);
		if (_nonNullPropsPattern != null)
			rootPatterns.add(_nonNullPropsPattern);
		Map<String, Pattern> refPropPatterns = new HashMap<String, Pattern>();
		for (Entry<String, PatternBuilder> entry : _refPropBuilders.entrySet()) {
			Pattern p = entry.getValue().build();
			refPropPatterns.put(entry.getKey(), p);
		}
		return new NestedPattern(rootPatterns, refPropPatterns);
	}
	
	private void assertHasProperty(String propName) throws Exception {
		if (propName == null)
			throw new Exception("Property name cannot be null.");
		Type rootType = _rootTypePattern.getType();
		if (!rootType.hasProperty(propName))
			throw new Exception("The type " + rootType.getName() + " has no property with name " + propName);
	}
	
	private Type getTypeFromSemanticTree(String typeName) throws Exception {
		if (typeName == null) {
			throw new Exception("Type name cannot be null!");
		}
		Type type = this._kb.getType(typeName);
		if (type == null)
			throw new Exception("Type " + typeName + " does not exist in the semantic tree.");
		return type;
	}
}
