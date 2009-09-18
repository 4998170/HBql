package org.apache.hadoop.hbase.hbql.query.expr.value.func;

import org.apache.hadoop.hbase.hbql.client.HPersistException;
import org.apache.hadoop.hbase.hbql.query.expr.node.BooleanValue;
import org.apache.hadoop.hbase.hbql.query.expr.node.PredicateExpr;
import org.apache.hadoop.hbase.hbql.query.expr.value.literal.BooleanLiteral;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 29, 2009
 * Time: 2:35:57 PM
 */
public class BooleanTernary extends GenericTernary<BooleanValue> implements BooleanValue {

    public BooleanTernary(final PredicateExpr pred, final BooleanValue expr1, final BooleanValue expr2) {
        super(pred, expr1, expr2);
    }

    @Override
    public boolean optimizeForConstants(final Object object) throws HPersistException {

        boolean retval = true;

        if (this.getPred().optimizeForConstants(object))
            this.setPred(new BooleanLiteral(this.getPred().evaluate(object)));
        else
            retval = false;

        if (this.getExpr1().optimizeForConstants(object))
            this.setExpr1(new BooleanLiteral(this.getExpr1().getCurrentValue(object)));
        else
            retval = false;

        if (this.getExpr2().optimizeForConstants(object))
            this.setExpr2(new BooleanLiteral(this.getExpr2().getCurrentValue(object)));
        else
            retval = false;

        return retval;
    }

    @Override
    public Boolean getCurrentValue(final Object object) throws HPersistException {
        return (Boolean)super.getValue(object);
    }
}