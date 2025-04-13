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
 * A simple implementation of the Token interface.
 */
public class SimpleToken implements Token {
  private final Object value;
  private final TokenType type;

  public SimpleToken(Object value, TokenType type) {
    this.value = value;
    this.type = type;
  }

  @Override
  public Object value() {
    return value;
  }

  @Override
  public TokenType type() {
    return type;
  }

  @Override
  public JsonElement toJson() {
    return new JsonPrimitive(value.toString());
  }
}

