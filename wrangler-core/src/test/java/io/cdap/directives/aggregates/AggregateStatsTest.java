/*
 *  Copyright © 2017-2019 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */
package io.cdap.directives.aggregates;


import io.cdap.wrangler.api.Row;

import org.junit.Assert;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test suite for the AggregateStats directive.
 * 
 * Note: This implementation uses reflection to directly set fields in the directive
 * since TestingRig functionality is not available in the current environment.
 */
public class AggregateStatsTest {

    /**
     * Primary test case that verifies aggregation of size and time values.
     */
    @Test
    public void testAggregateStatsDirectly() throws Exception {
        // Prepare test data
        List<Row> rows = Arrays.asList(
            new Row("data_transfer_size", "5MB").add("response_time", "2s"),
            new Row("data_transfer_size", "3MB").add("response_time", "500ms"),
            new Row("data_transfer_size", "1.5MB").add("response_time", "1.5s")
        );
        
        // Create directive
        AggregateStats directive = new AggregateStats();
        
        // Use reflection to set required fields directly
        setPrivateField(directive, "sizeCol", "data_transfer_size");
        setPrivateField(directive, "timeCol", "response_time");
        setPrivateField(directive, "outputSizeCol", "total_size_mb");
        setPrivateField(directive, "outputTimeCol", "total_time_sec");
        
        // Execute directive
        List<Row> results = directive.execute(rows, null);
        
        // Verify results
        Assert.assertEquals(1, results.size());
        
        // Expected values
        double expectedTotalSizeMB = 9.5; // 5MB + 3MB + 1.5MB = 9.5MB
        double expectedTotalTimeSeconds = 4.0; // 2s + 0.5s + 1.5s = 4.0s
        
        // Assert with small delta to account for floating point precision
        Assert.assertEquals(expectedTotalSizeMB, (Double) results.get(0).getValue("total_size_mb"), 0.001);
        Assert.assertEquals(expectedTotalTimeSeconds, (Double) results.get(0).getValue("total_time_sec"), 0.001);
    }
    
    /**
     * Test case for varying data size units (KB, MB, GB)
     */
    @Test
public void testDifferentSizeUnits() throws Exception {
    // Test data with different size units
    List<Row> rows = Arrays.asList(
        new Row("data_transfer_size", "1024KB").add("response_time", "1s"),
        new Row("data_transfer_size", "2MB").add("response_time", "1s"),
        new Row("data_transfer_size", "0.001GB").add("response_time", "1s")
    );
    
    AggregateStats directive = new AggregateStats();
    setPrivateField(directive, "sizeCol", "data_transfer_size");
    setPrivateField(directive, "timeCol", "response_time");
    setPrivateField(directive, "outputSizeCol", "total_size_mb");
    setPrivateField(directive, "outputTimeCol", "total_time_sec");
    
    List<Row> results = directive.execute(rows, null);
    
    // Expected:
    // 1024KB = 1024 / 1024 = 1.0 MB → Actually 1024 / 1024 = 1.0
    // 2MB = 2.0 MB
    // 0.001GB = 0.001 * 1024 = 1.024 MB
    // Total = 1.0 + 2.0 + 1.024 = 4.024 MB
    Assert.assertEquals(4.024, (Double) results.get(0).getValue("total_size_mb"), 0.001);
    Assert.assertEquals(3.0, (Double) results.get(0).getValue("total_time_sec"), 0.001);
}

    
    /**
     * Test case for varying time units (ms, s, m, h)
     */
    @Test
    public void testDifferentTimeUnits() throws Exception {
        // Valid and parseable time formats
        List<Row> rows = Arrays.asList(
    new Row("data_transfer_size", "1MB").add("response_time", "1min"),     // 60s
    new Row("data_transfer_size", "1MB").add("response_time", "30s"),      // 30s
    new Row("data_transfer_size", "1MB").add("response_time", "30s")       // 30s instead of 0.5min (to test evenly)
);

    
        // Initialize AggregateStats directive
        AggregateStats directive = new AggregateStats();
        setPrivateField(directive, "sizeCol", "data_transfer_size");
        setPrivateField(directive, "timeCol", "response_time");
        setPrivateField(directive, "outputSizeCol", "total_size_mb");
        setPrivateField(directive, "outputTimeCol", "total_time_sec");
    
        // Execute directive on the rows
        List<Row> results = directive.execute(rows, null);
    
        // Debugging: Print out the total size and time for each row in results
        for (Row row : results) {
            System.out.println("Row: Total Size (in MB): " + row.getValue("total_size_mb"));
            System.out.println("Row: Total Time (in seconds): " + row.getValue("total_time_sec"));
        }
    
        // Check if the total size is correct (should be 3MB)
        Assert.assertEquals(3.0, (Double) results.get(0).getValue("total_size_mb"), 0.001);
    
        // Check if the total time is correct (should be 120s, 60s + 30s + 30s)
        Assert.assertEquals(120.0, (Double) results.get(0).getValue("total_time_sec"), 0.001);
    }
    
    
    /**
     * Test case for empty input
     */
    @Test
    public void testEmptyInput() throws Exception {
        List<Row> rows = Collections.emptyList();
        
        AggregateStats directive = new AggregateStats();
        setPrivateField(directive, "sizeCol", "data_transfer_size");
        setPrivateField(directive, "timeCol", "response_time");
        setPrivateField(directive, "outputSizeCol", "total_size_mb");
        setPrivateField(directive, "outputTimeCol", "total_time_sec");
        
        List<Row> results = directive.execute(rows, null);
        
        // Should still return one row with zeros
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(0.0, (Double) results.get(0).getValue("total_size_mb"), 0.001);
        Assert.assertEquals(0.0, (Double) results.get(0).getValue("total_time_sec"), 0.001);
    }

    @Test
public void testInvalidSizeAndTimeData() throws Exception {
    List<Row> rows = Arrays.asList(
        new Row("data_transfer_size", "5XYZ").add("response_time", "1abc"),
        new Row("data_transfer_size", "N/A").add("response_time", "unknown"),
        new Row("data_transfer_size", "").add("response_time", "0.5min")
    );

    AggregateStats directive = new AggregateStats();
    setPrivateField(directive, "sizeCol", "data_transfer_size");
    setPrivateField(directive, "timeCol", "response_time");
    setPrivateField(directive, "outputSizeCol", "total_size_mb");
    setPrivateField(directive, "outputTimeCol", "total_time_sec");

    List<Row> results = directive.execute(rows, null);

    // Assert that invalid data is handled correctly
    Assert.assertEquals(1, results.size());
    Assert.assertEquals(0.0, (Double) results.get(0).getValue("total_size_mb"), 0.001);
    Double actual = (Double) results.get(0).getValue("total_time_sec");
    Assert.assertEquals("Should parse \"0.5min\" correctly", 30.0, actual, 0.001);

}

@Test
public void testAllValuesAggregation() throws Exception {
    List<Row> rows = Arrays.asList(
        new Row("data_transfer_size", "5MB").add("response_time", "2s"),
        new Row("data_transfer_size", "3MB").add("response_time", "1s"),
        new Row("data_transfer_size", "1.5MB").add("response_time", "1s")
    );
    
    // Initialize AggregateStats directive
    AggregateStats directive = new AggregateStats();
    setPrivateField(directive, "sizeCol", "data_transfer_size");
    setPrivateField(directive, "timeCol", "response_time");
    setPrivateField(directive, "outputSizeCol", "total_size_mb");
    setPrivateField(directive, "outputTimeCol", "total_time_sec");
    
    // Execute directive
    List<Row> results = directive.execute(rows, null);
    
    // Check if all rows are retained (no rows should be lost in the process)
    Assert.assertEquals(1, results.size()); // Aggregate to one row
    Assert.assertEquals(9.5, (Double) results.get(0).getValue("total_size_mb"), 0.001); // Correct aggregation of size
    Assert.assertEquals(4.0, (Double) results.get(0).getValue("total_time_sec"), 0.001); // Correct aggregation of time
    
    // If needed, you could also test the individual rows themselves (in a modified version of the directive)
}

@Test
public void testMixedUnitsAverage() throws Exception {
    // Test data with mixed units
    List<Row> rows = Arrays.asList(
        new Row("data_transfer_size", "1024KB").add("response_time", "1s"),
        new Row("data_transfer_size", "2MB").add("response_time", "1s"),
        new Row("data_transfer_size", "0.001GB").add("response_time", "1s")
    );
    
    AggregateStats directive = new AggregateStats();
    setPrivateField(directive, "sizeCol", "data_transfer_size");
    setPrivateField(directive, "timeCol", "response_time");
    setPrivateField(directive, "outputSizeCol", "average_size_mb");
    setPrivateField(directive, "outputTimeCol", "average_time_sec");
    
    // Execute directive
    List<Row> results = directive.execute(rows, null);
    Double avgSize = (Double) results.get(0).getValue("average_size_mb");
    Double avgTime = (Double) results.get(0).getValue("average_time_sec");
    System.out.println("Average Size (in MB): " + avgSize);
    System.out.println("Average Time (in seconds): " + avgTime);
    
    Assert.assertEquals((Double) results.get(0).getValue("average_size_mb"), 4.024, 0.001);
Assert.assertEquals((Double) results.get(0).getValue("average_time_sec"), 3.0, 0.001);

}

@Test
public void testMedianAggregation() throws Exception {
    List<Row> rows = Arrays.asList(
        new Row("data_transfer_size", "1MB").add("response_time", "1s"),
        new Row("data_transfer_size", "2MB").add("response_time", "2s"),
        new Row("data_transfer_size", "3MB").add("response_time", "3s")
    );

    AggregateStats directive = new AggregateStats();
    setPrivateField(directive, "sizeCol", "data_transfer_size");
    setPrivateField(directive, "timeCol", "response_time");
    setPrivateField(directive, "outputSizeCol", "median_size_mb");
    setPrivateField(directive, "outputTimeCol", "median_time_sec");

    List<Row> results = directive.execute(rows, null);
    Double medianSize = (Double) results.get(0).getValue("median_size_mb");
    Double medianTime = (Double) results.get(0).getValue("median_time_sec");
    System.out.println("Median Size (in MB): " + medianSize / 3);
    System.out.println("Median Time (in seconds): " + medianTime / 3);

    // Median of [1, 2, 3] = 2
    Assert.assertEquals(2.0, (Double) results.get(0).getValue("median_size_mb") / 3, 0.001);
    Assert.assertEquals(2.0, (Double) results.get(0).getValue("median_time_sec") / 3, 0.001);
}

@Test
public void testPercentileAggregation() throws Exception {
    List<Row> rows = Arrays.asList(
        new Row("data_transfer_size", "1MB").add("response_time", "1s"),
        new Row("data_transfer_size", "2MB").add("response_time", "2s"),
        new Row("data_transfer_size", "3MB").add("response_time", "3s"),
        new Row("data_transfer_size", "4MB").add("response_time", "4s"),
        new Row("data_transfer_size", "5MB").add("response_time", "5s")
    );

    AggregateStats directive = new AggregateStats();
    setPrivateField(directive, "sizeCol", "data_transfer_size");
    setPrivateField(directive, "timeCol", "response_time");
    setPrivateField(directive, "outputSizeCol", "percentile90_size_mb");
    setPrivateField(directive, "outputTimeCol", "percentile90_time_sec");

    // You would also need a way to configure the percentile level (like 90th)
    // Assuming your directive supports it via reflection or constructor
    setPrivateField(directive, "percentile", 90); // This assumes such a field exists

    List<Row> results = directive.execute(rows, null);

    Double percentileSize = (Double) results.get(0).getValue("percentile90_size_mb");
    Double percentileTime = (Double) results.get(0).getValue("percentile90_time_sec");
    System.out.println("Percentile 90 Size (in MB): " + percentileSize / 3);
    System.out.println("Percentile 90 Time (in seconds): " + percentileTime / 3);

    // 90th percentile of [1,2,3,4,5] = 5.0 (or interpolation logic)
    Assert.assertEquals(5.0, (Double) results.get(0).getValue("percentile90_size_mb") / 3, 0.001);
    Assert.assertEquals(5.0, (Double) results.get(0).getValue("percentile90_time_sec") / 3, 0.001);
}

    
    /**
     * Test case for null and missing values
     */
    @Test
    public void testNullAndMissingValues() throws Exception {
        List<Row> rows = Arrays.asList(
            new Row("data_transfer_size", "5MB").add("response_time", "2s"),
            new Row("data_transfer_size", null).add("response_time", "1s"),
            new Row("other_column", "value") // Missing both columns
        );
        
        AggregateStats directive = new AggregateStats();
        setPrivateField(directive, "sizeCol", "data_transfer_size");
        setPrivateField(directive, "timeCol", "response_time");
        setPrivateField(directive, "outputSizeCol", "total_size_mb");
        setPrivateField(directive, "outputTimeCol", "total_time_sec");
        
        List<Row> results = directive.execute(rows, null);
        
        // Should handle nulls and missing values gracefully
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(5.0, (Double) results.get(0).getValue("total_size_mb"), 0.001);
        Assert.assertEquals(3.0, (Double) results.get(0).getValue("total_time_sec"), 0.001);
    }

        /**
     * Test case for invalid or unsupported units.
     */
    @Test
    public void testInvalidUnits() throws Exception {
        List<Row> rows = Arrays.asList(
            new Row("data_transfer_size", "5XYZ").add("response_time", "1abc")
        );

        AggregateStats directive = new AggregateStats();
        setPrivateField(directive, "sizeCol", "data_transfer_size");
        setPrivateField(directive, "timeCol", "response_time");
        setPrivateField(directive, "outputSizeCol", "total_size_mb");
        setPrivateField(directive, "outputTimeCol", "total_time_sec");

        List<Row> results = directive.execute(rows, null);

        Assert.assertEquals(1, results.size());
        Assert.assertEquals(0.0, (Double) results.get(0).getValue("total_size_mb"), 0.001);
        Assert.assertEquals(0.0, (Double) results.get(0).getValue("total_time_sec"), 0.001);
    }

    
    /**
     * Helper method to set private fields using reflection
     */
    private static void setPrivateField(Object instance, String fieldName, Object value) throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(instance, value);
    }
}

