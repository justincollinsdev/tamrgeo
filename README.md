

#Tamrgeo

This is the tamrgeo project, a wrapper to encapsulate and simplify geospatial operations.

###Resources
GeoJson is a standard format to represent geospatial objects.
  
####Some geojson resources:

The spec:  
[http://geojson.org/](http://geojson.org/)

A useful tutorial:  
[https://macwright.org/2015/03/23/geojson-second-bite](https://macwright.org/2015/03/23/geojson-second-bite)

A visualizer:  
[http://geojson.io/](http://geojson.io/]  Just paste your geojson in the right hand panel and you can see it.

Useful sources of data that might be interesting:  
[http://www.gadm.org/country](http://www.gadm.org/country)  
[http://www.naturalearthdata.com/downloads/](http://www.naturalearthdata.com/downloads/)  
[http://census.ire.org/data/bulkdata.html](http://census.ire.org/data/bulkdata.html)  


Online resource to simplify and convert various formats, including to/from geojson.  If you have a very complex or large geojson file or shape file you can use this site to 'simplify' it to make it more appropriate for your specific use.  Very detailed geojson of large features like the United States, or Texas can be very very large (10s or 100s of MB) and for most purposes are not any better than vastly simplified versions.  For many uses, you can use mapshaper to simplify all the way down to single percentages and have just as usable results.  
[https://mapshaper.org/](https://mapshaper.org/)

Additional tools to sanity check results, including calculating distances between points.  
[https://www.movable-type.co.uk/scripts/latlong.html](https://www.movable-type.co.uk/scripts/latlong.html)


###Operations
Read and Write GeoJson  
```java
    String geoString = readFile("texas.json");
    Shape s = gu.fromGeoJson(geoString);
```
  
Object Centroid

  
Point in Polygon

    
Point to Point distance (can also be used to determine line length)

  
Object Repositioning

  
Area calculations

  
Intersection Area

  
Intersection Shape

    
Hausdorff Similarity

  

####Supporting Operations
  

 







an image


![some more text](https://github.com/justincollinsdev/tamrgeo/blob/master/img/ushapedintersection.png?raw=true)