/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.cdap.wrangler.api.parser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TimeDurationTest {

    @Test
    public void testTimeDurationInitialization() {
        // Creating an instance of TimeDuration with the value "1h"
        TimeDuration timeDuration = new TimeDuration("1h");

        // Test to ensure the correct initialization of the TimeDuration object.
        assertEquals("1h", timeDuration.toString());  // Should match the input value
    }

    @Test
    public void testTokenType() {
        TimeDuration timeDuration = new TimeDuration("1h");

        // Ensure it returns the correct TokenType
        assertEquals(TokenType.TIME_DURATION, timeDuration.type());
    }


    @Test
public void testCanonicalValueConversion() {
    TimeDuration oneSecond = new TimeDuration("1s");
    assertEquals(1_000_000_000.0, ((Double) oneSecond.getValue()).doubleValue(), 0.001);

    TimeDuration halfSecond = new TimeDuration("0.5s");
    assertEquals(500_000_000.0, ((Double) halfSecond.getValue()).doubleValue(), 0.001);

    TimeDuration twoMillis = new TimeDuration("2ms");
    assertEquals(2_000_000.0, ((Double) twoMillis.getValue()).doubleValue(), 0.001);

    TimeDuration threeMinutes = new TimeDuration("3m");
    assertEquals(180_000_000_000.0, ((Double) threeMinutes.getValue()).doubleValue(), 0.001);

    TimeDuration oneHour = new TimeDuration("1h");
    assertEquals(3_600_000_000_000.0, ((Double) oneHour.getValue()).doubleValue(), 0.001);
}

    
}
