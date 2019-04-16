package com.tamr.geo;

import java.io.IOException;
import java.text.ParseException;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.jts.JtsSpatialContextFactory;
import org.locationtech.spatial4j.distance.GeodesicSphereDistCalc;
import org.locationtech.spatial4j.exception.InvalidShapeException;
import org.locationtech.spatial4j.io.ShapeIO;
import org.locationtech.spatial4j.io.ShapeReader;
import org.locationtech.spatial4j.io.ShapeWriter;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.ShapeFactory;
import org.locationtech.spatial4j.shape.SpatialRelation;
import org.locationtech.spatial4j.shape.ShapeFactory.LineStringBuilder;
import org.locationtech.spatial4j.shape.jts.JtsShapeFactory;

import com.vividsolutions.jts.algorithm.match.HausdorffSimilarityMeasure;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.util.AffineTransformation;

public class TamrGeoUtils {

	private ShapeReader reader = null;
	private ShapeWriter writer = null;
	private SpatialContext ctx = null;
	JtsSpatialContextFactory scFactory = null;

	/**
	 * Create a TamrGeoUtils object.
	 */
	public TamrGeoUtils() {
		scFactory = new JtsSpatialContextFactory();
		scFactory.geo = true;
		scFactory.distCalc = new GeodesicSphereDistCalc.Haversine();
		ctx = scFactory.newSpatialContext();

		reader = ctx.getFormats().getReader(ShapeIO.GeoJSON);
		writer = ctx.getFormats().getWriter(ShapeIO.GeoJSON);

	}
	
	/**
	 * Get the underlying {@link SpatialContext}.  This should not be needed by most clients but if you 
	 * need to interact with the underlying libraries this is your access to it. 
	 * For example, create a shape manually:
	 * <pre>{@code
	 *   ShapeFactory sf = gu.getSpatialContext().getShapeFactory();
	 *   LineStringBuilder builder = sf.lineString();
	 *   builder.pointXY(someX, someY);
	 *   builder.pointXY(anotherX, anotherY);
	 *   Shape lineBetweenPoints = builder.build();
     * }</pre>
     *
	 * @return SpatialContext the underlying spatial context
	 */
	public SpatialContext getSpatialContext() {
		return ctx;
	}

	/**
	 * Create a {@link Shape} from a geoJson String.  This function does NOT support FeatureCollection, and none of the 
	 * other methods in this class support FeatureCollection.  Attempting to read a FeatureCollection will throw 
	 * ParseException. 
	 * 
	 * @param geoJsonString A valid geoJson String
	 * @return Shape The shape described by the geoJson.
	 * @throws InvalidShapeException
	 * @throws IOException
	 * @throws ParseException
	 */
	public Shape fromGeoJson(String geoJsonString) throws InvalidShapeException, IOException, ParseException {
		return reader.read(geoJsonString);
	}

	/**
	 * Output the given Shape as a geoJson String
	 * 
	 * @param shape The Shape to output geoJson for
	 * @return A String representing this Shape as a geoJson String
	 */
	public String toGeoJson(Shape shape) {
		return writer.toString(shape);
	}

	/**
	 * Return a Point representing the centroid of the given Shape.  If the Shape is a Point, the returned Point will be 
	 * identical to the Point passed in.  If the Shape passed in is a Line, the Point returned will be the midpoint of the 
	 * Line, and will be on the line.  If the Shape passed in is a Polygon the Point returned will be the centroid of the 
	 * Polygon, but <i>may not be contained inside the polygon</i> in the case of a Polygon with a 'donut hole' in the middle, 
	 * or a 'U' shaped Polygon, etc.
	 * 
	 * @param geometry The Shape to calculate the centroid of.
	 * @return a Point representing the centroid of this Shape.
	 */
	public Point getCentroid(Shape geometry) {
		return geometry.getCenter();
	}

	/**
	 * Calculate the distance between two Points, in Meters.
	 * 
	 * @param p1 Point 1
	 * @param p2 Point 2
	 * @return The distance between the two points in Meters
	 */
	public double calculateDistance(Point p1, Point p2) {
		double radiansDistance = ctx.getDistCalc().distance(p1, p2);
		double metersDistance = Math.toRadians(radiansDistance) * 6371000;
		return metersDistance;
	}

	/**
	 * Return true if the given Point is contained in the given Polygon.
	 * 
	 * @param polygon The Polygon
	 * @param p1 The Point to test 
	 * @return True if the Point is contained in the given Polygon, False otherwise
	 */
	public boolean polygonContainsPoint(Shape polygon, Point p1) {
		SpatialRelation relate = polygon.relate(p1);
		if (relate.intersects()) {
			return true;
		}
		return false;
	}
	

	/**
	 * Calculate the area of the give Shape in square meters.  By definition if the Shape passed in is a Point or Line this 
	 * method will return 0.0.  Only Polygons have Area.  This calculation assumes a spherical earth, in fact the earth is 
	 * ellipsoidal, which will cause this estimate to be accurate to approximately .3%.  More information 
	 * <a href="https://gis.stackexchange.com/questions/25494/how-accurate-is-approximating-the-earth-as-a-sphere#25580">here</a>
	 * 
	 * @param geometry Shape to calculate area of
	 * @return  The Area of the Shape in square meters
	 */
	public double calculateArea(Shape geometry) {
		double squareRadians = geometry.getArea(ctx);
		// To convert square degrees to meters, note that the radius of a sphere whose area is equal 
		// to that of the earth's ellipsoidal surface (an authalic sphere) is 6371 km, giving 
		// 111,194.9 meters per degree. 
		return Math.toRadians(squareRadians) * 6371000 * 111194.9;
	}
	
	/**
	 * Return a Shape representing the area of intersection between the two provided Shapes.
	 * By definition the intersection of a Point within a Polygon is the Point, or a Line within 
	 * a Polygon is the portion of the Line that is contained within the Polygon.
	 * 
	 * @param s1 Shape 1
	 * @param s2 Shape 2
	 * @return The Shape of overlap between the two provided Shapes, or an empty Shape if there is no overlap (does not return null in that case)
	 */
	public Shape getIntersection(Shape s1, Shape s2) {
		Geometry s1Geo = getGeometryFrom(s1);
		Geometry s2Geo = getGeometryFrom(s2);
		Geometry intersection = s1Geo.intersection(s2Geo);
		return ((JtsShapeFactory)scFactory.makeShapeFactory(ctx)).makeShape(intersection);
	}
	
	/**
	 * Get the area of the intersection of the two provided Shapes in square Meters.  By definition the intersection of a Point 
	 * with anything or a Line with anything will return 0.0, as Lines and Points have no area.
	 * 
	 * @param s1 Shape 1
	 * @param s2 Shape 2
	 * @return The area of overlap between the two shapes in square meters
	 */
	public double getIntersectionArea(Shape s1, Shape s2) {
		Shape resultShape = getIntersection(s1, s2);
		return calculateArea(resultShape);
	}
	
	/**
	 * Move the Shape src to the location of Shape dest.  More specifically, move the centroid of src to the locaiton of the 
	 * centroid of dest.  Although the primary use case of this is to compare Shapes similarity regardless of relative location 
	 * note that there is no guarantee that the relocated shape will have more overlap than pre-relocation.
	 *  
	 * @param src  The shape to relocate
	 * @param dest The shape that provides the location to move src to.
	 * @return a new Shape that is identical to src, but relocated so that its centroid is now located at the centroid of dest
	 */
	public Shape relocate(Shape src, Shape dest) {
		Geometry srcGeo = getGeometryFrom(src);
		Point srcCentroid = getCentroid(src);
		Point destCentroid = getCentroid(dest);
		double xOffset = destCentroid.getX() - srcCentroid.getX();
		double yOffset = destCentroid.getY() - srcCentroid.getY();
		AffineTransformation translationInstance = AffineTransformation.translationInstance(xOffset, yOffset);
		Geometry transformedGeo = translationInstance.transform(srcGeo);
		Shape resultShape = ((JtsShapeFactory)scFactory.makeShapeFactory(ctx)).makeShape(transformedGeo);
		return resultShape;
		
	}
	
	/**
	 * Measures the degree of similarity between two {@link Geometry}s
	 * using the Hausdorff distance metric.
	 * The measure is normalized to lie in the range [0, 1].
	 * Higher measures indicate a greater degree of similarity.
	 * <p>
	 * The measure is computed by computing the Hausdorff distance
	 * between the input geometries, and then normalizing
	 * this by dividing it by the diagonal distance across 
	 * the envelope of the combined geometries.
	 * 
	 * @param s1 Shape 1
	 * @param s2 Shape 2
	 * @return Hausdorff Similarity normalized to [0, 1]
	 */
	public double getHausdorffSimilarity(Shape s1, Shape s2) {
		Geometry s1Geo = getGeometryFrom(s1);
		Geometry s2Geo = getGeometryFrom(s2);
		HausdorffSimilarityMeasure hausdorffSimilarityMeasure = new HausdorffSimilarityMeasure();
		return hausdorffSimilarityMeasure.measure(s1Geo, s2Geo);
	}
	
	protected Geometry getGeometryFrom(Shape s) {
		return ((JtsShapeFactory)scFactory.makeShapeFactory(ctx)).getGeometryFrom(s);
	}
	

}
