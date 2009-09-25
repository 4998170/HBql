package org.apache.hadoop.hbase.hbql.util;

import org.apache.hadoop.hbase.hbql.client.HPersistException;
import org.apache.hadoop.hbase.hbql.client.SchemaManager;
import org.apache.hadoop.hbase.hbql.query.antlr.HBql;
import org.apache.hadoop.hbase.hbql.query.expr.ExprTree;
import org.apache.hadoop.hbase.hbql.query.expr.ExprVariable;
import org.apache.hadoop.hbase.hbql.query.schema.FieldType;
import org.apache.hadoop.hbase.hbql.query.schema.Schema;
import org.apache.hadoop.hbase.hbql.query.util.Lists;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 23, 2009
 * Time: 4:49:02 PM
 */
public class WhereExprTests {

    public void assertValidInput(final String expr, String... vals) throws HPersistException {
        org.junit.Assert.assertTrue(evalColumnNames(expr, vals));
    }

    public void assertInvalidInput(final String expr, String... vals) throws HPersistException {
        org.junit.Assert.assertFalse(evalColumnNames(expr, vals));
    }

    public static void assertTrue(final boolean val) throws HPersistException {
        org.junit.Assert.assertTrue(val);
    }

    public static void assertFalse(final boolean val) throws HPersistException {
        org.junit.Assert.assertFalse(val);
    }

    public static void assertEvalTrue(final String expr) throws HPersistException {
        assertEvalTrue(null, expr);
    }

    public static void assertEvalTrue(final Object recordObj, final String expr) throws HPersistException {
        org.junit.Assert.assertTrue(evalExpr(recordObj, expr));
    }

    public static void assertEvalFalse(final String expr) throws HPersistException {
        assertEvalFalse(null, expr);
    }

    public static void assertEvalFalse(final Object recordObj, final String expr) throws HPersistException {
        org.junit.Assert.assertFalse(evalExpr(recordObj, expr));
    }

    public static void assertEvalTrue(final ExprTree tree) throws HPersistException {
        assertEvalTrue(null, tree);
    }

    public static void assertEvalTrue(final Object recordObj, final ExprTree tree) throws HPersistException {
        org.junit.Assert.assertTrue(evalExpr(recordObj, tree));
    }

    public static void assertEvalFalse(final ExprTree tree) throws HPersistException {
        assertEvalFalse(null, tree);
    }

    public static void assertEvalFalse(final Object recordObj, final ExprTree tree) throws HPersistException {
        org.junit.Assert.assertFalse(evalExpr(recordObj, tree));
    }

    public void assertHasException(final ExprTree tree, final Class clazz) {
        this.assertHasException(null, tree, clazz);
    }


    public void assertHasException(final Object recordObj, final ExprTree tree, final Class clazz) {
        Class eclazz = null;
        try {
            evalExpr(recordObj, tree);
        }
        catch (HPersistException e) {
            eclazz = e.getClass();
        }
        org.junit.Assert.assertTrue(eclazz != null && eclazz.equals(clazz));
    }


    public static void assertColumnsMatchTrue(final String expr, String... vals) throws HPersistException {
        org.junit.Assert.assertTrue(evalColumnNames(expr, vals));
    }

    public static void assertColumnsMatchFalse(final String expr, String... vals) throws HPersistException {
        org.junit.Assert.assertFalse(evalColumnNames(expr, vals));
    }

    public ExprTree parseExpr(final String expr) throws HPersistException {
        return this.parseExpr(null, expr);
    }

    public ExprTree parseExpr(final Object recordObj, final String expr) throws HPersistException {
        final Schema schema = SchemaManager.getObjectSchema(recordObj);
        return HBql.parseDescWhereExpr(expr, schema);

    }

    private static boolean evalExpr(final Object recordObj, final String expr) throws HPersistException {

        final Schema schema = SchemaManager.getObjectSchema(recordObj);
        final ExprTree tree = HBql.parseDescWhereExpr(expr, schema);

        return evalExpr(recordObj, tree);
    }

    private static boolean evalExpr(final Object recordObj, final ExprTree tree) throws HPersistException {
        return tree.evaluate(recordObj);
    }


    private static boolean evalColumnNames(final String expr, String... vals) {

        try {
            final ExprTree tree = HBql.parseDescWhereExpr(expr, null);
            final List<ExprVariable> attribs = tree.getExprVariablesList();
            final List<String> valList = Lists.newArrayList(vals);
            boolean retval = true;

            for (final String val : valList) {
                if (!attribs.contains(new ExprVariable(val, FieldType.StringType))) {
                    System.out.println("Missing column name: " + val);
                    retval = false;
                }
            }

            for (final ExprVariable var : attribs) {
                if (!valList.contains(var.getName())) {
                    System.out.println("Missing column name: " + var.getName());
                    retval = false;
                }
            }

            return retval;
        }
        catch (HPersistException e) {
            return false;
        }
    }
}