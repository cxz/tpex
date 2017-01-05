package io.github.cxz.tpex;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import java.io.File;
import java.util.Map;

/**
 * Check submission file for invalid polygons.
 *
 */
public class App 
{
    public static WKTReader reader = new WKTReader();
    public static void main( String[] args ) throws java.io.IOException
    {
        if (args.length != 1) {
            System.err.println("Missing input filename.");
            System.exit(1);
        }

        File input = new File(args[0]);

        if (!input.exists()) {
            System.err.println("Couldn't locate input csv {}" + input.getAbsolutePath());
            System.exit(1);
        }

        WKTReader wktReader = new WKTReader();
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        MappingIterator<Map<String,String>> it = mapper.readerFor(Map.class).with(schema).readValues(input);
        int invalidCount = 0;
        while (it.hasNext()) {
            Map<String,String> rowAsMap = it.next();
            String rowInfo = new StringBuilder()
                    .append("ImageId: ").append(rowAsMap.get("ImageId"))
                    .append(", ")
                    .append("ClassType: ").append(rowAsMap.get("ClassType"))
                    .toString();
            String wkt = rowAsMap.get("MultipolygonWKT");
            try {
                Geometry geometry = reader.read(wkt);
                if (!geometry.isValid()) {
                    System.err.println("Invalid geometry: " + rowInfo);
                    invalidCount += 1;
                }
            } catch(Exception ex) {
                StringBuilder builder = new StringBuilder()
                        .append("Error on ")
                        .append(" :")
                        .append(ex.getMessage());
                System.err.println(builder.toString());
            }
        }
        if (invalidCount == 0) {
            System.out.println("Good to go!");
        } else {
            System.out.println("Summary: " + invalidCount + " invalid geometries");
        }
    }
}
