package tamrgeocom.tamr.geo;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.Test;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.ShapeFactory;
import org.locationtech.spatial4j.shape.ShapeFactory.LineStringBuilder;

import com.google.common.io.Resources;
import com.tamr.geo.TamrGeoUtils;

class GeoUtilsTest {

	private static final String GEOJSON_DIR = "testGeoJson";
	
	//Set acceptable deviance from known values of length at .03%, area at .3% (should be exponentially worse thanks to area calculations) 
	private double ACCEPABLE_AREA_DEVIANCE = .003;
	private double ACCEPTABLE_LENGTH_DEVIANCE = .0003;

	private double ACTUAL_AREA_OF_PENTAGON = 97551.0;
	private double ACTUAL_AREA_OF_LARGE_WIDE_AREA = 2075647987.0;
	private double ACTUAL_AREA_OF_LARGE_TALL_AREA = 945195048.0;
	private double ACTUAL_AREA_OF_ONE_STORY_HOUSE = 73.42;
	private double ACTUAL_AREA_OF_HIGH_LAT_HIGH_SCHOOL = 3877.59;
	private double ACTUAL_DISTANCE_BTWN_HUMAN_ML_CENTROIDS = 3.815;
	private double ACTUAL_DISTANCE_BTWN_IDENTICAL_BUILDING_CENTROIDS = 25.61;
	private double ACTUAL_HAUSDORFF_DISTANCE_BTWN_IDENTICAL_BUILDINGS = 0.62216;
	private double ACTUAL_AREA_COLORADO_GEOTOOLS = 269660135805.06;
	private double USGS_AREA_COLORADO = 269837000000.0;
	private double ACTUAL_INTERSECTION_AREA = 700.31;



	@Test
	void testAreaColoradoCourseUSGS() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String geoString = readFile("coloradoCourse.json");
		Shape s = gu.fromGeoJson(geoString);
		double area = gu.calculateArea(s);
		double change = Math.abs((area - USGS_AREA_COLORADO))/USGS_AREA_COLORADO;
		assertTrue(change<ACCEPABLE_AREA_DEVIANCE);
	}
	
	@Test
	void testAreaColoradoCourseGeoTools() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String geoString = readFile("coloradoCourse.json");
		Shape s = gu.fromGeoJson(geoString);
		double area = gu.calculateArea(s);
		double change = Math.abs((area - ACTUAL_AREA_COLORADO_GEOTOOLS))/ACTUAL_AREA_COLORADO_GEOTOOLS;
		assertTrue(change<ACCEPABLE_AREA_DEVIANCE);
	}
	
	@Test
	void testAreaLargeWideAreaCrossingEquator() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String geoString = readFile("largeWideAreaCrossingEquator.json");
		Shape s = gu.fromGeoJson(geoString);
		double area = gu.calculateArea(s);
		double change = Math.abs((area - ACTUAL_AREA_OF_LARGE_WIDE_AREA))/ACTUAL_AREA_OF_LARGE_WIDE_AREA;
		assertTrue(change<ACCEPABLE_AREA_DEVIANCE);
	}
	
	@Test
	void testAreaLargeTallAreaCrossingEquator() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String geoString = readFile("largeTallAreaCrossingEquator.json");
		Shape s = gu.fromGeoJson(geoString);
		double area = gu.calculateArea(s);
		double change = Math.abs((area - ACTUAL_AREA_OF_LARGE_TALL_AREA))/ACTUAL_AREA_OF_LARGE_TALL_AREA;
		assertTrue(change<ACCEPABLE_AREA_DEVIANCE);
	}
	
	@Test
	void testAreaOneStoryHouse() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String geoString = readFile("oneStoryHouse.json");
		Shape s = gu.fromGeoJson(geoString);
		double area = gu.calculateArea(s);
		double change = Math.abs((area - ACTUAL_AREA_OF_ONE_STORY_HOUSE))/ACTUAL_AREA_OF_ONE_STORY_HOUSE;

		assertTrue(change<ACCEPABLE_AREA_DEVIANCE);
	}
	
	@Test
	void testAreaPentagon() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String geoString = readFile("pentagon.json");
		Shape s = gu.fromGeoJson(geoString);
		double area = gu.calculateArea(s);
		double change = Math.abs((area - ACTUAL_AREA_OF_PENTAGON))/ACTUAL_AREA_OF_PENTAGON;

		assertTrue(change<ACCEPABLE_AREA_DEVIANCE);
	}
	
	@Test
	void testAreaVeryHighLatHighSchool() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String geoString = readFile("highSchoolVeryHighLat.json");
		Shape s = gu.fromGeoJson(geoString);
		double area = gu.calculateArea(s);
		double change = Math.abs((area - ACTUAL_AREA_OF_HIGH_LAT_HIGH_SCHOOL))/ACTUAL_AREA_OF_HIGH_LAT_HIGH_SCHOOL;
		assertTrue(change<ACCEPABLE_AREA_DEVIANCE);
	}

	@Test
	void testCentroid() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String geoString = readFile("oneStoryHouse.json");
		Shape s = gu.fromGeoJson(geoString);
		Point polygonCentroid = gu.getCentroid(s);
		assertTrue(gu.polygonContainsPoint(s, polygonCentroid));
	}
	
	@Test
	void testDistanceBetweenHumanAndMLCentroids() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String bldg1JsonString = readFile("uShapedMLGeneratedBuilding.json");
		Shape bldg1CentroidShape = gu.fromGeoJson(bldg1JsonString);
		Point bldg1Centroid = gu.getCentroid(bldg1CentroidShape);
		String bldg2JsonString = readFile("uShapedHumanGeneratedBuilding.json");
		Shape bldg2CentroidShape = gu.fromGeoJson(bldg2JsonString);
		Point bldg2Centroid = gu.getCentroid(bldg2CentroidShape);

		double centroidDifference = gu.calculateDistance(bldg1Centroid, bldg2Centroid);
		double change = Math.abs((centroidDifference - ACTUAL_DISTANCE_BTWN_HUMAN_ML_CENTROIDS))/ACTUAL_DISTANCE_BTWN_HUMAN_ML_CENTROIDS;
		//these two values are SO close together that even rounding error makes for higher percentage deviance.
		//there is only 1.2 cm between these two values, but that's .32%, slightly above the nominal .3%
		assertTrue(change < .0035);
	}
	
	@Test
	void testPointCentroid() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String bldg1JsonString = readFile("identicalBuildingCentroid1.json");
		Shape bldg1CentroidShape = gu.fromGeoJson(bldg1JsonString);
		Point bldg1Centroid = gu.getCentroid(bldg1CentroidShape);

		assertTrue(bldg1Centroid.equals(bldg1CentroidShape));
	}
	
	@Test
	void testLineCentroid() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String bldg1CentoidJsonString = readFile("identicalBuildingCentroid1.json");
		Shape bldg1CentroidShape = gu.fromGeoJson(bldg1CentoidJsonString);
		Point bldg1Centroid = gu.getCentroid(bldg1CentroidShape);
		String bldg2CentoidJsonString = readFile("identicalBuildingCentroid2.json");
		Shape bldg2CentroidShape = gu.fromGeoJson(bldg2CentoidJsonString);
		Point bldg2Centroid = gu.getCentroid(bldg2CentroidShape);
		
		ShapeFactory sf = gu.getSpatialContext().getShapeFactory();
		
		LineStringBuilder builder = sf.lineString();
		builder.pointXY(bldg1Centroid.getX(), bldg1Centroid.getY());
		builder.pointXY(bldg2Centroid.getX(), bldg2Centroid.getY());
		Shape lineBetweenCentroids = builder.build();

		Point lineCentroid = gu.getCentroid(lineBetweenCentroids);
		double dist1 = gu.calculateDistance(bldg1Centroid, lineCentroid);
		double dist2 = gu.calculateDistance(lineCentroid, bldg2Centroid);
		
		//There is always going to be a very small amount of 'slop' here, this test validates it to .1 mm
		//which is way beyond the actual capabilities of any real world data collection device.  Actual 
		//difference in this case is several orders of magnitude smaller, ~4.1 Micro Meter
		assertTrue(Math.abs(dist1 - dist2) < .0001);
	}
	
	@Test
	void testPolyCentroid() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String bldg1JsonString = readFile("identicalBuilding1.json");
		Shape bldg1Shape = gu.fromGeoJson(bldg1JsonString);
		Point bldg1Centroid = gu.getCentroid(bldg1Shape);

		String bldg1CentoidJsonString = readFile("identicalBuildingCentroid1.json");
		Shape bldg1CentroidShape = gu.fromGeoJson(bldg1CentoidJsonString);
		assertEquals(gu.getCentroid(bldg1Shape), bldg1CentroidShape);
	}
	
	
	@Test
	void testDistanceBetweenIdenticalBuildingCentroids() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String bldg1JsonString = readFile("identicalBuildingCentroid1.json");
		Shape bldg1CentroidShape = gu.fromGeoJson(bldg1JsonString);
		Point bldg1Centroid = gu.getCentroid(bldg1CentroidShape);
		String bldg2JsonString = readFile("identicalBuildingCentroid2.json");
		Shape bldg2CentroidShape = gu.fromGeoJson(bldg2JsonString);
		Point bldg2Centroid = gu.getCentroid(bldg2CentroidShape);

		double centroidDifference = gu.calculateDistance(bldg1Centroid, bldg2Centroid);
		double change = Math.abs((centroidDifference - ACTUAL_DISTANCE_BTWN_IDENTICAL_BUILDING_CENTROIDS))/ACTUAL_DISTANCE_BTWN_IDENTICAL_BUILDING_CENTROIDS;
		assertTrue(change<ACCEPTABLE_LENGTH_DEVIANCE);
	}

	@Test
	void testPolyContainsPoint() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String geoString = readFile("oneStoryHouse.json");
		Shape s = gu.fromGeoJson(geoString);
		Point polygonCentroid = gu.getCentroid(s);
		assertTrue(gu.polygonContainsPoint(s, polygonCentroid));
	}

	@Test
	void testPolyDoesNotContainPoint() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String geoString = readFile("exaggeratedUShapedBuilding.json");
		Shape s = gu.fromGeoJson(geoString);
		Point polygonCentroid = gu.getCentroid(s);
		assertFalse(gu.polygonContainsPoint(s, polygonCentroid));
	}

	@Test
	void testReadValidJson() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String geoString = readFile("uShapedMLGeneratedBuilding.json");
		Shape shapeFromGeoJson = gu.fromGeoJson(geoString);
		assertNotNull(shapeFromGeoJson);
	}

	@Test
	void testReadInvalidJson() {
		TamrGeoUtils gu = new TamrGeoUtils();
		assertThatThrownBy(() -> gu.fromGeoJson(readFile("invalidUShapedMLGeneratedBuilding.json")))
				.hasMessageContaining("Unexpected String Value for key: null");

	}
	
	@Test
	void testReadInvalidFeatureCollectionJson() {
		TamrGeoUtils gu = new TamrGeoUtils();
		assertThatThrownBy(() -> gu.fromGeoJson(readFile("InvalidFeatureCollection.json")))
				.hasMessageContaining("Unknown type: FeatureCollection");

	}
	
	@Test
	void testWriteValidJson() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String geoString = readFile("uShapedMLGeneratedBuilding.json");
		Shape shapeFromGeoJson = gu.fromGeoJson(geoString);
		assertNotNull(shapeFromGeoJson);
		String resultGeoJson = gu.toGeoJson(shapeFromGeoJson);
		Shape resultFromGeoJsonWrite = gu.fromGeoJson(resultGeoJson);
		assertEquals(gu.calculateArea(shapeFromGeoJson), gu.calculateArea(resultFromGeoJsonWrite));
	}
	
	@Test
	void testIntersectionArea() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String bldg1JsonString = readFile("uShapedHumanGeneratedBuilding.json");
		Shape bldg1Shape = gu.fromGeoJson(bldg1JsonString);
		String bldg2JsonString = readFile("uShapedMLGeneratedBuilding.json");
		Shape bldg2Shape = gu.fromGeoJson(bldg2JsonString);
		
		double intersectArea = gu.getIntersectionArea(bldg1Shape, bldg2Shape);
		double change = Math.abs((intersectArea - ACTUAL_INTERSECTION_AREA))/ACTUAL_INTERSECTION_AREA;

		//this will be higher than ACCEPABLE_AREA_DEVIANCE, as both the calculation of the overlap shape 
		//and the calculation of area itself are very slightly different from geotools
		assertTrue(change<(ACCEPABLE_AREA_DEVIANCE*2));
		
	
		//Shape s = gu.getIntersection(bldg1Shape, bldg2Shape);
		//System.out.println("intersection geojson is "+gu.toGeoJson(s));
	}
	
	@Test
	void testIntersectionAreaWithPoint() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String bldg1JsonString = readFile("identicalBuilding1.json");
		Shape bldg1Shape = gu.fromGeoJson(bldg1JsonString);
		String bldg1CentroidJsonString = readFile("identicalBuildingCentroid1.json");
		Shape bldg1CentroidShape = gu.fromGeoJson(bldg1CentroidJsonString);
		
		Shape intersectionShape = gu.getIntersection(bldg1Shape, bldg1CentroidShape);
		assertTrue(gu.calculateDistance(gu.getCentroid(intersectionShape), gu.getCentroid(bldg1CentroidShape)) == 0.0);
		
		double intersectArea = gu.getIntersectionArea(bldg1Shape, bldg1CentroidShape);
		//Points have no area, so the intersection area is 0.0
		assertTrue(intersectArea == 0.0);
	}
	
	@Test
	void testEmptyIntersectionArea() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String bldg1JsonString = readFile("identicalBuilding1.json");
		Shape bldg1Shape = gu.fromGeoJson(bldg1JsonString);
		String bldg2JsonString = readFile("identicalBuilding2.json");
		Shape bldg2Shape = gu.fromGeoJson(bldg2JsonString);
		
		//there is no overlap here
		Shape intersectionShape = gu.getIntersection(bldg1Shape, bldg2Shape);

		assertTrue(gu.calculateArea(intersectionShape) == 0.0);
		
		double intersectArea = gu.getIntersectionArea(bldg1Shape, bldg2Shape);
		//Points have no area, so the intersection area is 0.0
		assertTrue(intersectArea == 0.0);
	}
	
	@Test
	void testIntersectionAreaWithLine() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String bldg1CentoidJsonString = readFile("identicalBuildingCentroid1.json");
		Shape bldg1CentroidShape = gu.fromGeoJson(bldg1CentoidJsonString);
		Point bldg1Centroid = gu.getCentroid(bldg1CentroidShape);
		String bldg2CentoidJsonString = readFile("identicalBuildingCentroid2.json");
		Shape bldg2CentroidShape = gu.fromGeoJson(bldg2CentoidJsonString);
		Point bldg2Centroid = gu.getCentroid(bldg2CentroidShape);
		
		ShapeFactory sf = gu.getSpatialContext().getShapeFactory();
		
		LineStringBuilder builder = sf.lineString();
		builder.pointXY(bldg1Centroid.getX(), bldg1Centroid.getY());
		builder.pointXY(bldg2Centroid.getX(), bldg2Centroid.getY());
		Shape lineBetweenCentroids = builder.build();
		
		String bldg1JsonString = readFile("identicalBuilding1.json");
		Shape bldg1Shape = gu.fromGeoJson(bldg1JsonString);
		
		
		//Shape intersectionShape = gu.getIntersection(bldg1Shape, lineBetweenCentroids);
		double intersectArea = gu.getIntersectionArea(bldg1Shape, lineBetweenCentroids);
		//Lines have no area, so the intersection area is 0.0
		assertTrue(intersectArea == 0.0);
	}
	
	/*
	 * Check the hausdorff similarity between two versions of the same building, one created by ML, one by a human. 
	 * Should be very high, compare to known value established by geotools.
	 */
	@Test
	void testHausdorffSimilarityMLvsHumanBuilding() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String bldg1JsonString = readFile("uShapedHumanGeneratedBuilding.json");
		Shape bldg1Shape = gu.fromGeoJson(bldg1JsonString);
		String bldg2JsonString = readFile("uShapedMLGeneratedBuilding.json");
		Shape bldg2Shape = gu.fromGeoJson(bldg2JsonString);
		double hausdorffSimilarity= gu.getHausdorffSimilarity(bldg1Shape, bldg2Shape);
		assertTrue(hausdorffSimilarity < .94 && hausdorffSimilarity >.92);
	}
	
	/*
	 * Check the hausdorff similarity between two buildings that are nearby and almost identical shapes. 
	 * Should be much lower than ML vs Human polys above, compare to known value established by geotools.
	 */
	@Test
	void testHausdorffSimilarityIdenticalButDifferentBuildings() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String bldg1JsonString = readFile("identicalBuilding1.json");
		Shape bldg1Shape = gu.fromGeoJson(bldg1JsonString);
		String bldg2JsonString = readFile("identicalBuilding2.json");
		Shape bldg2Shape = gu.fromGeoJson(bldg2JsonString);
		double hausdorffSimilarity= gu.getHausdorffSimilarity(bldg1Shape, bldg2Shape);
		double change = Math.abs((hausdorffSimilarity - ACTUAL_HAUSDORFF_DISTANCE_BTWN_IDENTICAL_BUILDINGS))/ACTUAL_HAUSDORFF_DISTANCE_BTWN_IDENTICAL_BUILDINGS;
		assertTrue(change < ACCEPTABLE_LENGTH_DEVIANCE);
	}
	
	/*
	 * Relocation of point to point.  No calculations done here, just moving the origin to the dest
	 */
	@Test
	void testRelocateIdenticalButDifferentBuildings() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String bldg1JsonString = readFile("identicalBuildingCentroid1.json");
		Shape bldg1Shape = gu.fromGeoJson(bldg1JsonString);
		String bldg2JsonString = readFile("identicalBuildingCentroid2.json");
		Shape bldg2Shape = gu.fromGeoJson(bldg2JsonString);
		Shape relocatedShape = gu.relocate(bldg1Shape, bldg2Shape);

		//the only case where these will be TRUELY identical is for points, no rounding error there
		assertTrue(gu.getCentroid(relocatedShape).equals(gu.getCentroid(bldg2Shape)));
	}
	
	/*
	 * Relocation of poly to poly.  Verify that the resulting centroid distance is within acceptable deviance
	 */
	@Test
	void testRelocateMLVsHumanUshapedBldg() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String bldg1JsonString = readFile("uShapedHumanGeneratedBuilding.json");
		Shape bldg1Shape = gu.fromGeoJson(bldg1JsonString);
		String bldg2JsonString = readFile("uShapedMLGeneratedBuilding.json");
		Shape bldg2Shape = gu.fromGeoJson(bldg2JsonString);
		Shape relocatedShape = gu.relocate(bldg1Shape, bldg2Shape);

		assertTrue(gu.calculateDistance(gu.getCentroid(relocatedShape), gu.getCentroid(bldg2Shape)) < ACCEPTABLE_LENGTH_DEVIANCE);
	}

	static String readFile(final String fileName) throws IOException {
		final String path = String.format("%s/%s", GEOJSON_DIR, fileName);
		final URL responsesUrl = GeoUtilsTest.class.getClassLoader().getResource(path);
		return Resources.toString(responsesUrl, UTF_8);
	}

}
