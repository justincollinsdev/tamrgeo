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
import org.locationtech.spatial4j.shape.jts.JtsGeometry;
import org.locationtech.spatial4j.shape.jts.JtsShapeFactory;

import com.vividsolutions.jts.geom.Geometry;

public class TamrGeoUtils {

	private ShapeReader reader = null;
	private ShapeWriter writer = null;
	private SpatialContext ctx = null;
	JtsSpatialContextFactory scFactory = null;

	public TamrGeoUtils() {
		scFactory = new JtsSpatialContextFactory();
		scFactory.geo = true;
		scFactory.distCalc = new GeodesicSphereDistCalc.Haversine();
		ctx = scFactory.newSpatialContext();

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
	

	public double calculateArea(Shape geometry) {
		double squareRadians = geometry.getArea(ctx);
		// this isn't correct, but it's a reasonable approximation and close enough for
		// now.  Nominal accuracy of .3%

		// To convert square degrees to meters, note that the radius of a sphere whose area is equal 
		// to that of the earth's ellipsoidal surface (an authalic sphere) is 6371 km, giving 
		// 111,194.9 meters per degree. 
		return Math.toRadians(squareRadians) * 6371000 * 111194.9;
	}
	
	public double getIntersectionArea(Shape s1, Shape s2) {
		JtsShapeFactory shapeFactory = (JtsShapeFactory)scFactory.makeShapeFactory(ctx);
		Geometry s1Geo = shapeFactory.getGeometryFrom(s1);
		Geometry s2Geo = shapeFactory.getGeometryFrom(s2);
		Geometry intersection = s1Geo.intersection(s2Geo);
		Shape resultShape = shapeFactory.makeShape(intersection);
		return calculateArea(resultShape);
	}
	
	
	

}
