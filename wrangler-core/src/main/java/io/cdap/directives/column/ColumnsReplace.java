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

package io.cdap.directives.column;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.SchemaResolutionContext;
import io.cdap.wrangler.api.annotations.Categories;
import io.cdap.wrangler.api.lineage.Lineage;
import io.cdap.wrangler.api.lineage.Many;
import io.cdap.wrangler.api.lineage.Mutation;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;
import org.unix4j.Unix4j;
import org.unix4j.builder.Unix4jCommandBuilder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Applies a sed expression on the column names.
 *
 * This directive helps clearing out the columns names to make it more readable.
 */
@Plugin(type = "directives")
@Name(ColumnsReplace.NAME)
@Categories(categories = { "column"})
@Description("Modifies column names in bulk using a sed-format expression.")
public class ColumnsReplace implements Directive, Lineage {
  public static final String NAME = "columns-replace";
  private Object sed;

  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder(NAME);
    builder.define("sed-expression", TokenType.TEXT);
    return builder.build();
  }

  @Override
  public void initialize(Arguments args) throws DirectiveParseException {
      // Ensure the argument is provided and it is of the correct type
      if (!args.contains("sed-expression")) {
          throw new DirectiveParseException("The argument 'sed-expression' is missing.");
      }
  
      Object sedArg = args.value("sed-expression");
  
      // Check if the object is a String (or String-like)
      if (sedArg instanceof String) {
          sed = sedArg;  // Assign directly if it's already a String
      } else if (sedArg instanceof Text) {
          sed = ((Text) sedArg).value();  // Extract value if it's a Text object
      } else {
          throw new DirectiveParseException("Expected 'sed-expression' to be a String or Text.");
      }
  }
  


  @Override
  public void destroy() {
    // no-op
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
    for (Row row : rows) {
      for (int i = 0; i < row.width(); ++i) {
        String name = row.getColumn(i);
        try {
          row.setColumn(i, getSedReplacedColumnName(name));
        } catch (IllegalArgumentException e) {
          throw new DirectiveExecutionException(NAME, e.getMessage(), e);
        }
      }
    }
    return rows;
  }

  @Override
  public Mutation lineage() {
    return Mutation.builder()
      .readable("Reformatted all columns using expression '%s'", sed)
      .all(Many.of())
      .build();
  }

  @Override
  public Schema getOutputSchema(SchemaResolutionContext context) {
    Schema inputSchema = context.getInputSchema();
    return Schema.recordOf(
      "outputSchema",
      inputSchema.getFields().stream()
        .map(
          field -> Schema.Field.of(getSedReplacedColumnName(field.getName()), field.getSchema())
        )
        .collect(Collectors.toList())
    );
  }

  private String getSedReplacedColumnName(String colName) {
    // Ensure that 'sed' is a String before passing it to the sed() method
    String sedExpression = (String) sed;  // Cast sed to String
    Unix4jCommandBuilder builder = Unix4j.echo(colName).sed(sedExpression);
    return builder.toStringResult();
}

}
