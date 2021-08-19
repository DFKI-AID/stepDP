package de.dfki.step.rr.constraints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import com.fasterxml.jackson.databind.AnnotationIntrospector.ReferenceProperty.Type;

import de.dfki.step.kb.RRTypes.Axis;

public class BoundingBox {
		public enum BBElementType {
			// FIXME X_MIN and X_MAX have two possible ordinates: Y or Z
			X_MIN(Axis.X, false, 180, Axis.X, Axis.Y, Axis.Y), 
			X_MAX(Axis.X, true, 0, Axis.X, Axis.Y, Axis.Y),
			Y_MIN(Axis.Y, false, 270, Axis.X, Axis.Y, Axis.X),
			Y_MAX(Axis.Y, true, 90, Axis.X, Axis.Y, Axis.X),
			Z_MIN(Axis.Z, false, 270, Axis.X, Axis.Z, Axis.X), 
			Z_MAX(Axis.Z, true, 90, Axis.X, Axis.Z, Axis.X);
			
			private double borderAngle;
			public Axis axis;
			public boolean max;
			public Axis abscissa;
			public Axis ordinate;
			public Axis borderVariable;
			
			BBElementType(Axis axis, boolean max, double angleMiddle, Axis abscissa, Axis ordinate, Axis borderVariable){
				this.axis = axis;
				this.max = max;
				this.borderAngle = angleMiddle;
				this.abscissa = abscissa;
				this.ordinate = ordinate;
				this.borderAngle = borderAngle;
			}

			public static BBElementType getBorderType(double angle) {
				for (BBElementType type : BBElementType.values()) {
					if (type.borderAngle == angle)
						return type;
				}
				return null;
			}

			public static BBElementType getBorderType(Axis axis, boolean max) {
				if (axis == null)
						return null;
				for (BBElementType type : BBElementType.values()) {
					if (axis.equals(type.axis) && max == type.max)
						return type;
				}
				return null;
			}
	
			public static List<BBElementType> getBorderTypes(Axis abscissa, Axis ordinate) {
				List<BBElementType> result = new ArrayList<BBElementType>();
				for (BBElementType type : BBElementType.values()) {
					if (type.abscissa.equals(abscissa) && type.ordinate.equals(ordinate))
						result.add(type);
				}
				return result;
			}

			public double getBorderAngle() {
				return Math.toRadians(this.borderAngle);
			}
		}

		private Vector3D center;
		private Vector3D extents;
		private Map<BBElementType, Border> borders = new HashMap<BBElementType, Border>();

		public BoundingBox(Vector3D center, Vector3D extents) throws Exception {
			if (center == null || extents == null)
				throw new Exception("Cannot instantiate bounding box with center or extents being null.");
			this.center = center;
			this.extents = extents;
		}

		public Border getBorder(BBElementType type) {
			Border border = borders.get(type);
			if (border != null)
				return border;
			else {
				border = computeBorder(type);
				borders.put(type, border);
				return border;
			}
		}

		public Border getBorder(double angle, Axis abscissa, Axis ordinate) {
			List<BBElementType> borderTypes = BBElementType.getBorderTypes(abscissa, ordinate);
			for (BBElementType type : borderTypes) {
				Border border = this.borders.get(type);
				if (border == null)
					border = computeBorder(type);
				if (border.contains(angle))
					return border;
			}
			return null;
		}

		public Vector2D getCenter2D(Axis abscissa, Axis ordinate) {
			return Axis.get2DVec(this.center, abscissa, ordinate);
		}

		public Vector2D getExtents2D(Axis abscissa, Axis ordinate) {
			return Axis.get2DVec(this.extents, abscissa, ordinate);
		}

		public Vector3D getMax() {
			return this.center.add(this.extents);
		}

		public Vector3D getMin() {
			return this.center.subtract(this.extents);
		}

		public Vector2D getCorner(BBElementType abscissa, BBElementType ordinate) {
			double absSign = abscissa.max ? 1 : -1;
			double absValue = abscissa.axis.getValue(this.center) + absSign * abscissa.axis.getValue(this.extents);
			double ordSign = ordinate.max ? 1 : -1;
			double ordValue = ordinate.axis.getValue(this.center) + ordSign * ordinate.axis.getValue(this.extents);
			return new Vector2D(absValue, ordValue);
		}

		private Border computeBorder(BBElementType type) {
			if (type == null)
				return null;
			Axis axis = type.axis;
			boolean max = type.max;
			Axis varAxis = type.borderVariable;
			Vector2D cornerLeft;
			Vector2D cornerRight;
			// TODO: refactor / find smarter way to do this (possibly by rotating extents vec?
			if (type.abscissa.equals(axis)) {
				BBElementType ordMin = BBElementType.getBorderType(type.ordinate, false);
				BBElementType ordMax = BBElementType.getBorderType(type.ordinate, true);
				if (type.borderAngle >= 90 && type.borderAngle < 270) {
					cornerLeft = getCorner(type, ordMin);
					cornerRight = getCorner(type, ordMax);
				} else {
					cornerRight = getCorner(type, ordMin);
					cornerLeft = getCorner(type, ordMax);
				}
			} else {
				BBElementType absMin = BBElementType.getBorderType(type.abscissa, false);
				BBElementType absMax = BBElementType.getBorderType(type.abscissa, true);
				if (type.borderAngle >= 90 && type.borderAngle < 270) {
					cornerLeft = getCorner(absMin, type);
					cornerRight = getCorner(absMax, type);
				} else {
					cornerRight = getCorner(absMin, type);
					cornerLeft = getCorner(absMax, type);
				}
			}
			try {
				Border border = new Border(type, getCenter2D(type.abscissa, type.ordinate), Pair.of(cornerLeft, cornerRight));
				this.borders.put(type, border);
				return border;
			} catch (Exception e) {
				return null;
			}
		}
		
}
