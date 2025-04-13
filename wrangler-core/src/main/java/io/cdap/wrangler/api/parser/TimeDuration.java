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
public class TimeDuration implements Token {
  private final String rawValue;
  private final long nanos;

  public TimeDuration(String value) {
    this.rawValue = value;
    this.nanos = parseNanos(value);
  }

  private long parseNanos(String input) {
    input = input.trim().toLowerCase();

    if (input.endsWith("ms")) {
        return (long) (Double.parseDouble(input.replace("ms", "")) * 1_000_000);
    } else if (input.endsWith("s")) {
        return (long) (Double.parseDouble(input.replace("s", "")) * 1_000_000_000);
    } else if (input.endsWith("m") && !input.endsWith("min")) {  // To handle single "m" for minutes.
        return (long) (Double.parseDouble(input.replace("m", "")) * 60 * 1_000_000_000L);
    } else if (input.endsWith("min")) {  // Add this case for "min"
        return (long) (Double.parseDouble(input.replace("min", "")) * 60 * 1_000_000_000L);
    } else if (input.endsWith("h")) {
        return (long) (Double.parseDouble(input.replace("h", "")) * 60 * 60 * 1_000_000_000L);
    } else {
        throw new IllegalArgumentException("Invalid time duration format: " + input);
    }
}


  public long getNanos() {
    return nanos;
  }

  @Override
  public Object value() {
    return nanos;
  }

  @Override
  public TokenType type() {
    return TokenType.TIME_DURATION;
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
    long ms = nanos / 1_000_000;
    if (ms % 3600000 == 0) {
      return (ms / 3600000) + "h";
    }
    if (ms % 60000 == 0) {
      return (ms / 60000) + "m";
    }
    if (ms % 1000 == 0) {
      return (ms / 1000) + "s";
    }
    return ms + "ms";
  }

  public double getValue(String unit) {
    switch (unit.toLowerCase()) {
      case "ns": return nanos;
      case "ms": return nanos / 1_000_000.0;
      case "s":  return nanos / 1_000_000_000.0;
      case "m":  return nanos / (60.0 * 1_000_000_000);
      case "h":  return nanos / (60.0 * 60 * 1_000_000_000);
      default: throw new IllegalArgumentException("Unsupported unit: " + unit);
    }
  }

  public double getValue() {
    return getValue("ns");
  }

  public double getSeconds() {
    // TODO Auto-generated method stub
    return nanos / 1_000_000_000.0;
  }
}
