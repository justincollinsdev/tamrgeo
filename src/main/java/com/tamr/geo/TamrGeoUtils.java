package com.tamr.geo;

import java.io.IOException;
import java.text.ParseException;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.SpatialContextFactory;
import org.locationtech.spatial4j.context.jts.JtsSpatialContextFactory;
import org.locationtech.spatial4j.distance.GeodesicSphereDistCalc;
import org.locationtech.spatial4j.exception.InvalidShapeException;
import org.locationtech.spatial4j.io.ShapeIO;
import org.locationtech.spatial4j.io.ShapeReader;
import org.locationtech.spatial4j.io.ShapeWriter;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.SpatialRelation;

public class TamrGeoUtils {

	private ShapeReader reader = null;
	private ShapeWriter writer = null;
	private SpatialContext ctx = null;

	public TamrGeoUtils() {
		SpatialContextFactory factory = new JtsSpatialContextFactory();
		factory.geo = true;
		factory.distCalc = new GeodesicSphereDistCalc.Haversine();
		ctx = factory.newSpatialContext();

		reader = ctx.getFormats().getReader(ShapeIO.GeoJSON);
		writer = ctx.getFormats().getWriter(ShapeIO.GeoJSON);

	}
	
	public SpatialContext getSpatialContext() {
		return ctx;
	}

	public Shape fromGeoJson(String geoJsonString) throws InvalidShapeException, IOException, ParseException {
		return reader.read(geoJsonString);
	}

	public String toGeoJson(Shape shape) {
		return writer.toString(shape);
	}

	public Point getCentroid(Shape geometry) {
		return geometry.getCenter();
	}

	public double calculateDistance(Point p1, Point p2) {
		double radiansDistance = ctx.getDistCalc().distance(p1, p2);
		double metersDistance = Math.toRadians(radiansDistance) * 6371000;
		return metersDistance;
	}

	public boolean polygonContainsPoint(Shape polygon, Point p1) {
		SpatialRelation relate = polygon.relate(p1);
		if (relate.intersects()) {
			return true;
		}
		return false;
	}
	
//	public Shape overlay(Shape p1, Shape p2) {
//		p1.
//	}

	public double calculateArea(Shape geometry) {
		double squareRadians = geometry.getArea(ctx);
		// this isn't correct, but it's a reasonable approximation and close enough for
		// now.  The correct way to do it is to reproject using calcArea below.
		// That brings in a lot of dependencies though, it may not be worth the added complexity.

		//To convert these degrees to meters, note that the radius of a sphere whose area is equal 
		//to that of the earth's ellipsoidal surface (an authalic sphere) is 6371 km, giving 
		//111,194.9 meters per degree. 
		return Math.toRadians(squareRadians) * 6371000 * 111194.9;
	}

	
	/*
	 * This is the real way to do the calculate area (and there is an analogous
	 * version of distance). The trade off is you get to pull in all the geotools
	 * libraries with about 100 transitive dependencies. Including here for
	 * reference and discussion.
	 * 
	 * additional repos needed:
	 * 
	 * maven { 
	 *     url "http://maven.geotoolkit.org/" 
	 * } 
	 * maven { 
	 *     url "http://repo.boundlessgeo.com/main/" 
	 * }
	 *
	 * additional dependencies (there is a 22-SNAPSHOT available too): 
	 * testRuntime('org.geotools:gt-shapefile:21.0')
	 * testRuntime('org.geotools:gt-referencing:21.0')
	 * testRuntime('org.geotools:gt-coverage:21.0')
	 * 
	 */
//	private Measure<Double, Area> calcArea(SimpleFeature feature) {
//	    Polygon p = (Polygon) feature.getDefaultGeometry();
//	    Point centroid = p.getCentroid();
//	    try {
//	      String code = "AUTO:42001," + centroid.getX() + "," + centroid.getY();
//	      CoordinateReferenceSystem auto = CRS.decode(code);
//
//	      MathTransform transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, auto);
//
//	      Polygon projed = (Polygon) JTS.transform(p, transform);
//	      return Measure.valueOf(projed.getArea(), SI.SQUARE_METRE);
//	    } catch (MismatchedDimensionException | TransformException | FactoryException e) {
//	      // TODO Auto-generated catch block
//	      e.printStackTrace();
//	    }
//	    return Measure.valueOf(0.0, SI.SQUARE_METRE);
//	  }

}
