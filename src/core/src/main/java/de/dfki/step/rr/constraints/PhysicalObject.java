package de.dfki.step.rr.constraints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.RRTypes;
import de.dfki.step.kb.RRTypes.Axis;
import de.dfki.step.rr.constraints.BoundingBox2D.BoundingBoxType;

public class PhysicalObject {
    private static final Logger log = LoggerFactory.getLogger(PhysicalObject.class);
	private IKBObject physObj;
	private Vector3D position;
	private Vector3D rotation;
	private BoundingBox2D bb;
	private Map<BoundingBoxType, BoundingBox2D> bbs = new HashMap<BoundingBoxType, BoundingBox2D>();

	PhysicalObject(IKBObject physObj) {
		this.physObj = physObj;
		IKBObject position = physObj.getResolvedReference(List.of("transform", "position"));
		this.position = createVector3D(position);
		IKBObject rotation = physObj.getResolvedReference(List.of("transform", "rotation"));
		this.rotation = createVector3D(rotation);
		IKBObject center = physObj.getResolvedReference(List.of("boundingBox", "center"));
		IKBObject extents = physObj.getResolvedReference(List.of("boundingBox", "extents"));
		try {
			for (BoundingBoxType bbType : BoundingBoxType.values()) {
				BoundingBox2D bb = new BoundingBox2D(createVector3D(center), createVector3D(extents), bbType, this);
				this.bbs.put(bbType, bb);
			}
		} catch (Exception e) {
			String name = physObj.getName();
			name = (name != null) ? name : "<no name>"; 
			log.warn("Instantiating PhysicalObject {} without valid bounding box.", name);
		}
	}

	public String getName() {
		if (this.physObj == null)
			return null;
		return this.physObj.getName();
	}

	public Vector3D createVector3D(IKBObject obj) {
		if (obj == null)
			return null;
		// FIXME: make configurable if y or z is height
		// in unity, y is the height
		Float x = obj.getFloat("x");
		Float y = obj.getFloat("y");
		Float z = obj.getFloat("z");
		if (x == null | y == null | z == null)
			return null;
		return new Vector3D(x, y, z);
	}

	public BoundingBox2D getBoundingBox(BoundingBoxType type) {
		return this.bbs.get(type);
	}

	public Vector3D getPosition3D() {
		return this.position;
	}

	public Vector3D getRotation() {
		return this.rotation;
	}

	public Vector2D getPosition2D(RRTypes.Axis abscissa, RRTypes.Axis ordinate) {
		return Axis.get2DVec(this.position, abscissa, ordinate);
	}

	public double getPositionOn(Axis axis) {
		return axis.getValue(this.position);
	}

	public double getX() {
		return this.position.getX();
	}

	public double getY() {
		return this.position.getY();
	}

	public double getZ() {
		return this.position.getZ();
	}
}
