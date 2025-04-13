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
import io.cdap.wrangler.api.LazyNumber;
import org.junit.Test;
import static org.junit.Assert.assertEquals;





public class ByteSizeTest {

    @Test
    public void testValue() {
        ByteSize byteSize = new ByteSize("1KB");
        assertEquals("1KB", byteSize.toString());
    }

    @Test
    public void testType() {
        ByteSize byteSize = new ByteSize("1KB");
        assertEquals(TokenType.BYTE_SIZE, byteSize.type());
    }

    @Test
    public void testCanonicalValueParsing() {
        assertEquals(1024.0, new ByteSize("1KB").getValue("B"), 0.001);
        assertEquals(1.5 * 1024 * 1024, new ByteSize("1.5MB").getValue("B"), 0.001);
        assertEquals(5.0, new ByteSize("5B").getValue("B"), 0.001);
        assertEquals(2.0 * 1024 * 1024 * 1024, new ByteSize("2GB").getValue("B"), 0.001);
    }
    

    @Test
    public void testCanonicalValueIEC() {
        assertEquals(1024.0, new ByteSize("1KiB").getValue("B"), 0.001);
        assertEquals(1.5 * 1024 * 1024, new ByteSize("1.5MiB").getValue("B"), 0.001);
        assertEquals(2.0 * 1024 * 1024 * 1024, new ByteSize("2GiB").getValue("B"), 0.001);
    }
    


}
