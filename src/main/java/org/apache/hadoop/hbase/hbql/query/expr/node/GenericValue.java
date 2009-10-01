package org.apache.hadoop.hbase.hbql.query.expr.node;

import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.TypeException;
import org.apache.hadoop.hbase.hbql.query.expr.ExprContext;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Sep 3, 2009
 * Time: 8:13:01 PM
 */
public interface GenericValue extends Serializable {

    void setExprContext(final ExprContext context) throws HBqlException;

    Object getValue(final Object object) throws HBqlException;

    GenericValue getOptimizedValue() throws HBqlException;

    Class<? extends GenericValue> validateTypes(final GenericValue parentExpr,
                                                final boolean allowsCollections) throws TypeException;

    boolean isAConstant();

    String asString();

}
