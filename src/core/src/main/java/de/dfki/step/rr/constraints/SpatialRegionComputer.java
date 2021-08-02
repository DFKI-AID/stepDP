package de.dfki.step.rr.constraints;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import org.apache.commons.math3.geometry.euclidean.twod.Euclidean2D;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.RRTypes;
import de.dfki.step.kb.RRTypes.Axis;
import de.dfki.step.rr.ObjectGroup;

public class SpatialRegionComputer {
	
	public static Double computePrototype(List<IKBObject> objects, RRTypes.SpatialRegion region) {
		List<Pair<IKBObject, Double>> ordered = orderDescBy(objects, region);
		if (ordered == null || ordered.isEmpty())
			return null;
		else
			return ordered.get(0).getValue();
	}

	public static IKBObject computeObjectForGroupRelation(ObjectGroup group, RRTypes.SpatialRegion relation, Integer ordinality) {
		if (ordinality == null)
			ordinality = 1;
		List<IKBObject> members = group.getObjects();
		List<Pair<IKBObject, Double>> ordered = orderDescBy(members, relation);
		if (ordered.size() < ordinality)
			return null;
		else
			return ordered.get(ordinality - 1).getKey();
	}

	private static List<Pair<IKBObject, Double>> orderDescBy(List<IKBObject> objects, RRTypes.SpatialRegion region) {
		Axis axis = region.getAxis();
		Comparator<Pair<IKBObject,Double>> comp = Comparator.comparing(Pair::getValue);
		if (region.positive())
			comp = comp.reversed();
	    List<Pair<IKBObject, Double>> ordered = objects
	    	      					.stream()
	    	      					// FIXME: actually it should be the outermost point not the center point
	    	      					.map(o -> Pair.of(o, new PhysicalObject(o).getPositionOn(axis)))
	    	      					.sorted(comp)
	    							.collect(Collectors.toList());
	    return ordered;
	}

}
