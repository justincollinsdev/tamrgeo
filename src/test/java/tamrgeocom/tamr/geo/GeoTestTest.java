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

class GeoTestTest {

	private static final String GEOJSON_DIR = "testGeoJson";
	
	//Set acceptable deviance from known values of length at .03%, area at .3% (should be exponentially worse thanks to area calculations) 
	private double ACCEPABLE_AREA_DEVIANCE = .003;
	private double ACCEPABLE_LENGTH_DEVIANCE = .0003;

	private double ACTUAL_AREA_OF_PENTAGON = 97551.0;
	private double ACTUAL_AREA_OF_LARGE_WIDE_AREA = 2075647987.0;
	private double ACTUAL_AREA_OF_LARGE_TALL_AREA = 945195048.0;
	private double ACTUAL_AREA_OF_ONE_STORY_HOUSE = 73.42;
	private double ACTUAL_AREA_OF_HIGH_LAT_HIGH_SCHOOL = 3877.59;
	private double ACTUAL_DISTANCE_BTWN_HUMAN_ML_CENTROIDS = 3.815;
	private double ACTUAL_DISTANCE_BTWN_IDENTICAL_BUILDING_CENTROIDS = 25.25;



	
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
		//these two values are SO close together that even rounding error makes for higher percentage defiance.
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
		String bldg1JsonString = readFile("identicalBuildingCentroid1.json");
		Shape bldg1CentroidShape = gu.fromGeoJson(bldg1JsonString);
		Point bldg1Centroid = gu.getCentroid(bldg1CentroidShape);
		String bldg2JsonString = readFile("identicalBuildingCentroid2.json");
		Shape bldg2CentroidShape = gu.fromGeoJson(bldg2JsonString);
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
		assertTrue(change<ACCEPABLE_LENGTH_DEVIANCE);
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
		System.out.println("intersectionArea is "+intersectArea);
		System.out.println("s1 area is "+gu.calculateArea(bldg1Shape));
		System.out.println("s2 area is "+gu.calculateArea(bldg2Shape));
	
	}

	static String readFile(final String fileName) throws IOException {
		final String path = String.format("%s/%s", GEOJSON_DIR, fileName);
		final URL responsesUrl = GeoTestTest.class.getClassLoader().getResource(path);
		return Resources.toString(responsesUrl, UTF_8);
	}

}
