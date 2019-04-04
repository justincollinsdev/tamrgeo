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

import com.google.common.io.Resources;
import com.tamr.geo.TamrGeoUtils;

class GeoTestTest {

	private static final String GEOJSON_DIR = "testGeoJson";

	@Test
	void testAreaOneStoryHouseLot() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String geoString = readFile("oneStoryHouseLot.json");
		Shape s = gu.fromGeoJson(geoString);
		double area = gu.calculateArea(s);
		System.out.println("area of one story house lot " + area);
	}
	
	@Test
	void testAreaOneStoryHouse() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String geoString = readFile("oneStoryHouse.json");
		Shape s = gu.fromGeoJson(geoString);
		double area = gu.calculateArea(s);
		System.out.println("area of one story house only " + area);
	}

	@Test
	void testCentroid() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String geoString = readFile("oneStoryHouse.json");
		Shape s = gu.fromGeoJson(geoString);
		Point polygonCentroid = gu.getPolygonCentroid(s);
		assertTrue(gu.polygonContainsPoint(s, polygonCentroid));
	}
	
	@Test
	void testDistanceBetweenHumanAndMLCentroids() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String bldg1JsonString = readFile("uShapedMLGeneratedBuilding.json");
		Shape bldg1CentroidShape = gu.fromGeoJson(bldg1JsonString);
		Point bldg1Centroid = gu.getPolygonCentroid(bldg1CentroidShape);
		String bldg2JsonString = readFile("uShapedHumanGeneratedBuilding.json");
		Shape bldg2CentroidShape = gu.fromGeoJson(bldg2JsonString);
		Point bldg2Centroid = gu.getPolygonCentroid(bldg2CentroidShape);

		double centroidDifference = gu.calculateDistance(bldg1Centroid, bldg2Centroid);
		System.out.println("ML vs Human centroidDifference is "+centroidDifference);
		assertTrue(gu.calculateDistance(bldg1Centroid, bldg2Centroid) < 4);
		assertTrue(gu.calculateDistance(bldg1Centroid, bldg2Centroid) > 3);
	}
	
	
	@Test
	void testDistanceBetweenIdenticalBuildingCentroids() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String bldg1JsonString = readFile("identicalBuildingCentroid1.json");
		Shape bldg1CentroidShape = gu.fromGeoJson(bldg1JsonString);
		Point bldg1Centroid = gu.getPolygonCentroid(bldg1CentroidShape);
		String bldg2JsonString = readFile("identicalBuildingCentroid2.json");
		Shape bldg2CentroidShape = gu.fromGeoJson(bldg2JsonString);
		Point bldg2Centroid = gu.getPolygonCentroid(bldg2CentroidShape);

		double centroidDifference = gu.calculateDistance(bldg1Centroid, bldg2Centroid);
		System.out.println("identical building centroidDifference is "+centroidDifference);
		assertTrue(gu.calculateDistance(bldg1Centroid, bldg2Centroid) < 26);
		assertTrue(gu.calculateDistance(bldg1Centroid, bldg2Centroid) > 25);
	}

	@Test
	void testPolyContainsPoint() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String geoString = readFile("oneStoryHouse.json");
		Shape s = gu.fromGeoJson(geoString);
		Point polygonCentroid = gu.getPolygonCentroid(s);
		assertTrue(gu.polygonContainsPoint(s, polygonCentroid));
	}

	@Test
	void testPolyDoesNotContainPoint() throws Exception {
		TamrGeoUtils gu = new TamrGeoUtils();
		String geoString = readFile("exaggeratedUShapedBuilding.json");
		Shape s = gu.fromGeoJson(geoString);
		Point polygonCentroid = gu.getPolygonCentroid(s);
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

	static String readFile(final String fileName) throws IOException {
		final String path = String.format("%s/%s", GEOJSON_DIR, fileName);
		final URL responsesUrl = GeoTestTest.class.getClassLoader().getResource(path);
		return Resources.toString(responsesUrl, UTF_8);
	}

}
