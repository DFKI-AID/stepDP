package de.dfki.step.rr.constraints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Segment;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import com.fasterxml.jackson.databind.AnnotationIntrospector.ReferenceProperty.Type;

import de.dfki.step.kb.RRTypes.Axis;

// approximation of 2D projection of the a bounding box taking into account object rotation
public class BoundingBox2D {
		public enum AxisType {
			ABSCISSA, ORDINATE;
			
			public AxisType opposite() {
				if (ABSCISSA.equals(this))
					return ORDINATE;
				else
					return ABSCISSA;
			}
		}

		public enum BoundingBoxType {
			// TODO: replace this with general plane type and refactor binspatrel code
			ZY(Axis.Z, Axis.Y),
			XZ(Axis.X, Axis.Z);

			public Axis abscissa;
			public Axis ordinate;

			BoundingBoxType(Axis abscissa, Axis ordinate){
				this.abscissa = abscissa;
				this.ordinate = ordinate;
			}

			public static BoundingBoxType getType(Axis abscissa, Axis ordinate) {
				for (BoundingBoxType type : BoundingBoxType.values())
					if (type.abscissa.equals(abscissa) && type.ordinate.equals(ordinate))
						return type;
				return null;
			}
		}

		public enum BBBorderType {
			// FIXME make this prettier?
			ABS_MIN(AxisType.ABSCISSA, false, 180, Pair.of(false, false), Pair.of(false, true)), 
			ABS_MAX(AxisType.ABSCISSA, true, 0, Pair.of(true, true), Pair.of(true, false)),
			ORD_MIN(AxisType.ORDINATE, false, 270, Pair.of(true, false), Pair.of(false, false)),
			ORD_MAX(AxisType.ORDINATE, true, 90, Pair.of(false, true), Pair.of(true, true));
			
			// angle relative to abscissa (in degrees)
			private double borderAngle;
			public AxisType type;
			public boolean max;
			public Pair<Boolean, Boolean> leftCorner;
			public Pair<Boolean, Boolean> rightCorner;
			
			BBBorderType(AxisType type, boolean max, double angleMiddle, Pair<Boolean, Boolean> leftCorner, Pair<Boolean, Boolean> rightCorner){
				this.type = type;
				this.max = max;
				this.borderAngle = angleMiddle;
				this.leftCorner = leftCorner;
				this.rightCorner = rightCorner;
			}

			public static BBBorderType getBorderType(double angle) {
				for (BBBorderType type : BBBorderType.values()) {
					// add some room for rounding error
					double min =  type.getBorderAngle() - Math.toRadians(2);
					double max =  type.getBorderAngle() + Math.toRadians(2);
					if (angle >= min && angle <= max)
						return type;
				}
				return null;
			}

			public static BBBorderType getBorderType(AxisType type, boolean max) {
				if (type == null)
						return null;
				for (BBBorderType bbType : BBBorderType.values()) {
					if (bbType.equals(type) && max == bbType.max)
						return bbType;
				}
				return null;
			}

			public double getBorderAngle() {
				return Math.toRadians(this.borderAngle);
			}
		}

		private Vector2D center;
		// rotated according to object rotation
		private Vector2D extents;
		private BoundingBoxType type;
		private Map<BBBorderType, Border> borders = new HashMap<BBBorderType, Border>();
		public PhysicalObject parent;

		public BoundingBox2D(Vector3D center, Vector3D extents, BoundingBoxType type, PhysicalObject parent) throws Exception {
			if (center == null || extents == null || type == null)
				throw new Exception("Cannot instantiate bounding box with center, extents or type being null.");
			this.type = type;
			this.center = Axis.get2DVec(center, type.abscissa, type.ordinate);
			this.extents = Axis.get2DVec(extents, type.abscissa, type.ordinate);
			// ajust extents to object rotation
			Axis rotAxis = Axis.getRotationAxis(type.abscissa, type.ordinate);
			this.extents = rotateVector(this.extents, rotAxis.getValue(parent.getRotation()));
			this.parent = parent;
		}

		public Border getBorder(BBBorderType type) {
			if (borders.isEmpty())
				computeBorders();
			return borders.get(type);
		}

		public Border getExitBorder(Vector2D start, Line line) {
			if (borders.isEmpty())
				computeBorders();
			Map<Border, Vector2D> intersects = new HashMap<Border, Vector2D>();
			for (Border border : borders.values()) {
				Vector2D intersect = border.getLine().intersection(line);
				if (intersect != null && border.contains(intersect)) {
					intersects.put(border, intersect);
				}
			}
			Border last = null;
			double lastDist = 0;
			for (Entry<Border,Vector2D> intersect : intersects.entrySet()) {
				double dist = intersect.getValue().distance(start);
				if (dist > lastDist) {
					last = intersect.getKey();
					lastDist = dist;
				}
			}
			return last;
		}

		// angle relative to axis aligned bounding box
		// TODO: only allow the 4 valid angles here
		// angle in radians
		public Border getBorder(double angle) {
			BBBorderType type = BBBorderType.getBorderType(angle);
			if (borders.isEmpty())
				computeBorders();
			return borders.get(type);
		}

		private void computeBorders() {
			Map<Pair<Boolean,Boolean>,Vector2D> corners = new HashMap<Pair<Boolean, Boolean>, Vector2D>();
			for (Boolean absMax : List.of(Boolean.valueOf(true), Boolean.valueOf(false))) {
				for (Boolean ordMax : List.of(Boolean.valueOf(true), Boolean.valueOf(false))){
				double absSign = absMax ? 1 : -1;
				double ordSign = ordMax ? 1 : -1;
				Vector2D corner = new Vector2D(center.getX() + absSign * extents.getX(), center.getY() + ordSign * extents.getY());
				corners.put(Pair.of(absMax, ordMax), corner);
				}
			}
			for (BBBorderType curType : BBBorderType.values()) {
				Vector2D leftCorner = corners.get(curType.leftCorner);
				Vector2D rightCorner = corners.get(curType.rightCorner);
				try {
					Border border = new Border(curType, center, Pair.of(leftCorner, rightCorner));
					borders.put(curType, border);
				}
				catch (Exception e) {
					// do nothing
				}
			}
		}

		public Vector2D getCenter() {
			return this.center;
		}

		public Vector2D getExtents() {
			return this.extents;
		}

		private Vector2D rotateVector(Vector2D old, double angle) {
			double newX = old.getX() * Math.cos(angle) - old.getY() * Math.sin(angle);
		    double newY = old.getX() * Math.sin(angle) + old.getY() * Math.cos(angle);
		    return new Vector2D(newX, newY);
		}
		
}
