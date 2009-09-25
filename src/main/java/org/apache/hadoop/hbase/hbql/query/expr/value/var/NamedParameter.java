package org.apache.hadoop.hbase.hbql.query.expr.value.var;

import org.apache.hadoop.hbase.hbql.client.HPersistException;
import org.apache.hadoop.hbase.hbql.query.expr.ExprTree;
import org.apache.hadoop.hbase.hbql.query.expr.node.ValueExpr;
import org.apache.hadoop.hbase.hbql.query.expr.value.literal.BooleanLiteral;
import org.apache.hadoop.hbase.hbql.query.expr.value.literal.DateLiteral;
import org.apache.hadoop.hbase.hbql.query.expr.value.literal.IntegerLiteral;
import org.apache.hadoop.hbase.hbql.query.expr.value.literal.StringLiteral;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 25, 2009
 * Time: 6:58:31 PM
 */
public class NamedParameter implements ValueExpr {

    private ExprTree context = null;
    private final String paramName;

    private ValueExpr typedExpr = null;

    public NamedParameter(final String paramName) {
        this.paramName = paramName;
    }

    @Override
    public Class<? extends ValueExpr> validateType() throws HPersistException {

        if (this.typedExpr == null)
            throw new HPersistException("Parameter " + this.getParamName() + " not assigned a value");

        return this.typedExpr.getClass();
    }

    @Override
    public Object getValue(final Object object) throws HPersistException {
        return this.typedExpr.getValue(object);
    }

    @Override
    public void setContext(final ExprTree context) {
        this.context = context;
        this.context.addNamedParameter(this);
    }

    @Override
    public ValueExpr getOptimizedValue() throws HPersistException {
        return this;
    }

    public boolean isAConstant() {
        return false;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParam(final Object val) throws HPersistException {

        if (val instanceof Boolean) {
            this.typedExpr = new BooleanLiteral((Boolean)val);
            return;
        }

        if (val instanceof String) {
            this.typedExpr = new StringLiteral((String)val);
            return;
        }

        if (val instanceof Integer) {
            this.typedExpr = new IntegerLiteral((Integer)val);
            return;
        }

        if (val instanceof Date) {
            this.typedExpr = new DateLiteral((Date)val);
            return;
        }

        throw new HPersistException("Parameter " + this.getParamName() + " assigned an unsupported type "
                                    + val.getClass().getName());
    }
}