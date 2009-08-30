package com.imap4j.hbase.hbql.expr.value;

import com.imap4j.hbase.hbql.HPersistException;
import com.imap4j.hbase.hbql.expr.BooleanValue;
import com.imap4j.hbase.hbql.expr.EvalContext;
import com.imap4j.hbase.hbql.expr.PredicateExpr;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 29, 2009
 * Time: 2:35:57 PM
 */
public class BooleanTernary implements BooleanValue {

    private PredicateExpr pred;
    private PredicateExpr expr1 = null, expr2 = null;

    public BooleanTernary(final PredicateExpr pred, final PredicateExpr expr1, final PredicateExpr expr2) {
        this.pred = pred;
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    private PredicateExpr getPred() {
        return this.pred;
    }

    private PredicateExpr getExpr1() {
        return this.expr1;
    }

    private PredicateExpr getExpr2() {
        return this.expr2;
    }

    @Override
    public boolean optimizeForConstants(final EvalContext context) throws HPersistException {

        boolean retval = true;

        if (this.getPred().optimizeForConstants(context))
            this.pred = new BooleanLiteral(this.getPred().evaluate(context));
        else
            retval = false;

        if (this.getExpr1().optimizeForConstants(context))
            this.expr1 = new BooleanLiteral(this.getExpr1().evaluate(context));
        else
            retval = false;

        if (this.getExpr2().optimizeForConstants(context))
            this.expr2 = new BooleanLiteral(this.getExpr2().evaluate(context));
        else
            retval = false;

        return retval;
    }

    @Override
    public Boolean getValue(final EvalContext context) throws HPersistException {

        if (this.getPred().evaluate(context))
            return this.getExpr1().evaluate(context);
        else
            return this.getExpr2().evaluate(context);
    }
}