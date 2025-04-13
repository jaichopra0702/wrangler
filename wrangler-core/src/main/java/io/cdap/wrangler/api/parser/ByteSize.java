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

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
/**
 * This class implements the directive for aggregating byte size and time duration values.
 */
public class ByteSize implements Token {
  private final String rawValue;
  private final long bytes;

  public ByteSize(String value) {
    this.rawValue = value;
    this.bytes = parseBytes(value);
  }

  private long parseBytes(String input) {
    input = input.trim().toUpperCase();
  
    if (input.endsWith("KIB")) {
      return (long) (Double.parseDouble(input.replace("KIB", "")) * 1024);
    } else if (input.endsWith("MIB")) {
      return (long) (Double.parseDouble(input.replace("MIB", "")) * 1024 * 1024);
    } else if (input.endsWith("GIB")) {
      return (long) (Double.parseDouble(input.replace("GIB", "")) * 1024 * 1024 * 1024);
    } else if (input.endsWith("TIB")) {
      return (long) (Double.parseDouble(input.replace("TIB", "")) * 1024L * 1024 * 1024 * 1024);
    } else if (input.endsWith("KB")) {
      return (long) (Double.parseDouble(input.replace("KB", "")) * 1024);
    } else if (input.endsWith("MB")) {
      return (long) (Double.parseDouble(input.replace("MB", "")) * 1024 * 1024);
    } else if (input.endsWith("GB")) {
      return (long) (Double.parseDouble(input.replace("GB", "")) * 1024 * 1024 * 1024);
    } else if (input.endsWith("TB")) {
      return (long) (Double.parseDouble(input.replace("TB", "")) * 1024L * 1024 * 1024 * 1024);
    } else if (input.endsWith("B")) {
      return (long) Double.parseDouble(input.replace("B", ""));
    } else {
      throw new IllegalArgumentException("Invalid byte size format: " + input);
    }
  }
  

  public long getBytes() {
    return bytes;
  }

  @Override
  public Object value() {
    return bytes;
  }

  @Override
  public TokenType type() {
    return TokenType.BYTE_SIZE;
  }

  @Override
  public JsonElement toJson() {
    return new JsonPrimitive(rawValue);
  }
  @Override
  public String toString() {
    return toHumanReadable();
  }

  public String toHumanReadable() {
    if (bytes < 1024) {
      return bytes + "B";
    }
    int exp = (int) (Math.log(bytes) / Math.log(1024));
    String unit = "KMGTPE".charAt(exp - 1) + "B";
    return String.format("%.0f%s", bytes / Math.pow(1024, exp), unit);
  }

  public double getValue(String unit) {
    switch (unit.toUpperCase()) {
      case "B": return bytes;
      case "KB": return bytes / 1024.0;
      case "MB": return bytes / (1024.0 * 1024);
      case "GB": return bytes / (1024.0 * 1024 * 1024);
      case "TB": return bytes / (1024.0 * 1024 * 1024 * 1024);
      default: throw new IllegalArgumentException("Unsupported unit: " + unit);
    }
  }

  public double getValue() {
    return getValue("B");
  }

  

}
