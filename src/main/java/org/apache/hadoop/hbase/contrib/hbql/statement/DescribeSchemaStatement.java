package org.apache.hadoop.hbase.contrib.hbql.statement;

import org.apache.hadoop.hbase.contrib.hbql.client.HBqlException;
import org.apache.hadoop.hbase.contrib.hbql.client.Output;
import org.apache.hadoop.hbase.contrib.hbql.schema.ColumnAttrib;
import org.apache.hadoop.hbase.contrib.hbql.schema.HBaseSchema;

public class DescribeSchemaStatement extends SchemaStatement implements SchemaManagerStatement {

    public DescribeSchemaStatement(final String schemaName) {
        super(schemaName);
    }

    public Output execute() throws HBqlException {

        final HBaseSchema schema = this.getSchema();

        if (schema == null)
            return new Output("Unknown schema: " + this.getSchemaName());

        final Output retval = new Output();

        retval.out.println("Schema name: " + this.getSchemaName());
        retval.out.println("Table name: " + schema.getTableName());
        retval.out.println("Columns:");

        for (final String familyName : schema.getFamilySet()) {
            for (final ColumnAttrib column : schema.getColumnAttribListByFamilyName(familyName))
                retval.out.println("\t" + column.asString());
        }

        retval.out.flush();
        return retval;
    }
}