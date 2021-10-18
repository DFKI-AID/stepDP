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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.commons.math3.geometry.euclidean.twod.Euclidean2D;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.RRTypes.Axis;
import de.dfki.step.kb.SpatialRegion;
import de.dfki.step.rr.ObjectGroup;
import de.dfki.step.rr.RRConfigParameters;

public class SpatialRegionComputer {
    private static final Logger log = LoggerFactory.getLogger(SpatialRegionComputer.class);
	private static final ObjectMapper mapper = new ObjectMapper();

	public static Double computePrototype(List<IKBObject> objects, SpatialRegion region, RRConfigParameters config) {
		if (!region.isMiddle()) {
			return protoForNotMiddle(objects, region);
		} else {
			SpatialRegion regionPos = SpatialRegion.getRegion(region.getAxis(), true);
			SpatialRegion regionNeg = SpatialRegion.getRegion(region.getAxis(), false);
			Double protoPos = protoForNotMiddle(objects, regionPos);
			Double protoNeg = protoForNotMiddle(objects, regionNeg);
			if (protoNeg == null || protoPos == null)
				return null;
			return (protoNeg + protoPos) / 2;
		}
	}

	private static Double protoForNotMiddle(List<IKBObject> objects, SpatialRegion region) {
		List<Pair<IKBObject, Double>> ordered = orderDescBy(objects, region);
		if (ordered == null || ordered.isEmpty())
			return null;
		else
			return ordered.get(0).getValue();
	}

	public static List<IKBObject> computeObjectsForGroupRelation(ObjectGroup group, SpatialRegion relation, Integer ordinality, Integer cardinality, RRConfigParameters config) {
		if (ordinality == null)
			ordinality = 1;
		List<IKBObject> members = group.getObjects();
		// FIXME: what about "middle"?
		List<Pair<IKBObject, Double>> ordered = orderDescBy(members, relation);
	    try {
	    	List<Pair<String, Double>> debug = ordered.stream().map(p -> Pair.of(p.getLeft().getName(), p.getRight())).toList();
			log.debug("ORDERED GROUP: " + mapper.writeValueAsString(debug));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<Pair<IKBObject, Double>> result;
		if (cardinality == null || cardinality == 1) {
			if (ordered.size() < ordinality)
				return null;
			else
				result = ordered.subList(ordinality-1, ordinality);
		} else {
			if (ordered.size() < cardinality)
				// FIXME: does this make sense?
				result = ordered;
			else
				result = ordered.subList(0, cardinality);
		}
		return result.stream().map(p -> p.getKey()).toList();
	}

	private static List<Pair<IKBObject, Double>> orderDescBy(List<IKBObject> objects, SpatialRegion region) {
		Axis axis = region.getAxis();
		Comparator<Pair<IKBObject,Double>> comp = Comparator.comparing(Pair::getValue);
		if (region.positive() != null && region.positive())
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
