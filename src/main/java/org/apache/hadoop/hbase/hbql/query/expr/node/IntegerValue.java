package org.apache.hadoop.hbase.hbql.query.expr.node;

import org.apache.hadoop.hbase.hbql.client.HBqlException;

public interface IntegerValue extends NumberValue {

    Integer getValue(final Object object) throws HBqlException;
}