package org.apache.hadoop.hbase.hbql.util;

import org.antlr.runtime.RecognitionException;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.SchemaManager;
import org.apache.hadoop.hbase.hbql.query.antlr.HBql;
import org.apache.hadoop.hbase.hbql.query.antlr.HBqlParser;
import org.apache.hadoop.hbase.hbql.query.expr.ExprTree;
import org.apache.hadoop.hbase.hbql.query.expr.value.var.GenericColumn;
import org.apache.hadoop.hbase.hbql.query.schema.ColumnAttrib;
import org.apache.hadoop.hbase.hbql.query.schema.Schema;
import org.apache.hadoop.hbase.hbql.query.stmt.args.QueryArgs;
import org.apache.hadoop.hbase.hbql.query.stmt.args.WhereArgs;
import org.apache.hadoop.hbase.hbql.query.stmt.select.ExprSelectElement;
import org.apache.hadoop.hbase.hbql.query.util.Lists;

import java.util.List;

public class TestSupport {

    public static void assertTrue(final boolean val) throws HBqlException {
        org.junit.Assert.assertTrue(val);
    }

    public static void assertFalse(final boolean val) throws HBqlException {
        org.junit.Assert.assertFalse(val);
    }

    public Number parseNumberValue(final String str) throws HBqlException {
        return (Number)HBql.parseExpression(str);
    }

    public void assertValidInput(final String expr, String... vals) throws HBqlException {
        assertTrue(evaluateExprColumnNames(expr, vals));
    }

    public void assertInvalidInput(final String expr, String... vals) throws HBqlException {
        assertFalse(evaluateExprColumnNames(expr, vals));
    }

    public static void assertEvalTrue(final String expr) throws HBqlException {
        assertEvalTrue(null, expr);
    }

    public static void assertEvalTrue(final Object recordObj, final String expr) throws HBqlException {
        assertTrue(evaluateExprTree(recordObj, expr));
    }

    public static ExprSelectElement parseSelectElement(final String str) throws HBqlException {
        return HBql.parseSelectElement(str);
    }

    public static void assertTypeAndValue(final ExprSelectElement expr, final Class clazz, final Object val) throws HBqlException {
        final Object obj = HBql.evaluateSelectElement(expr);
        System.out.println(expr.asString() + " returned value " + obj
                           + " expecting value " + val
                           + " returned type " + obj.getClass().getSimpleName()
                           + " expecting type " + clazz.getSimpleName());
        assertTrue(obj.getClass().equals(clazz) && obj.equals(val));
    }

    public static void assertTypeAndValue(final String str, final Class clazz, final Object val) throws HBqlException {
        ExprSelectElement expr = parseSelectElement(str);
        assertTypeAndValue(expr, clazz, val);
    }

    public static void assertEvalFalse(final String expr) throws HBqlException {
        assertExprEvalFalse(null, expr);
    }

    public static void assertExprEvalFalse(final Object recordObj, final String expr) throws HBqlException {
        assertFalse(evaluateExprTree(recordObj, expr));
    }

    public static void assertExprTreeEvalTrue(final ExprTree tree) throws HBqlException {
        assertExprTreeEvalTrue(null, tree);
    }

    public static void assertExprTreeEvalTrue(final Object recordObj, final ExprTree tree) throws HBqlException {
        assertTrue(evaluateExprTree(recordObj, tree));
    }

    public static void assertExprTreeEvalFalse(final ExprTree tree) throws HBqlException {
        assertEvalFalse(null, tree);
    }

    public static void assertEvalFalse(final Object recordObj, final ExprTree tree) throws HBqlException {
        assertFalse(evaluateExprTree(recordObj, tree));
    }

    public void assertHasException(final ExprTree tree, final Class clazz) {
        this.assertExprTreeHasException(null, tree, clazz);
    }

    public void assertExprTreeHasException(final Object recordObj, final ExprTree tree, final Class clazz) {
        Class eclazz = null;
        try {
            evaluateExprTree(recordObj, tree);
        }
        catch (HBqlException e) {
            e.printStackTrace();
            eclazz = e.getClass();
        }
        org.junit.Assert.assertTrue(eclazz != null && eclazz.equals(clazz));
    }

    public static void assertExprColumnsMatchTrue(final String expr, String... vals) throws HBqlException {
        assertTrue(evaluateExprColumnNames(expr, vals));
    }

    public static void assertExprColumnsMatchFalse(final String expr, String... vals) throws HBqlException {
        assertFalse(evaluateExprColumnNames(expr, vals));
    }

    public ExprTree parseExpr(final String expr) throws HBqlException {
        return this.parseExpr(null, expr);
    }

    public ExprTree parseExpr(final Object recordObj, final String expr) throws HBqlException {
        final Schema schema = SchemaManager.getObjectSchema(recordObj);
        return parseDescWhereExpr(expr, schema);
    }

    private static boolean evaluateExprTree(final Object recordObj, final String expr) throws HBqlException {
        final Schema schema = SchemaManager.getObjectSchema(recordObj);
        final ExprTree tree = parseDescWhereExpr(expr, schema);
        return evaluateExprTree(recordObj, tree);
    }

    private static boolean evaluateExprTree(final Object recordObj, final ExprTree tree) throws HBqlException {
        System.out.println("Evaluating: " + tree.asString());
        return tree.evaluate(recordObj);
    }

    private static boolean evaluateSelectNames(final String expr, String vals) {

        try {
            final QueryArgs args = HBql.parseSelectStmt(null, expr);
            final List<String> valList = Lists.newArrayList(vals.replace(" ", "").split(","));

            final Schema schema = args.getSchema();

            final List<ColumnAttrib> attribList = args.getSelectAttribList();

            final List<ColumnAttrib> specifiedAttribList = Lists.newArrayList();

            boolean retval = true;

            for (final String val : valList) {

                final ColumnAttrib attrib = schema.getAttribByVariableName(val);

                if (attrib == null) {
                    System.out.println("Invalid column name: " + val);
                    retval = false;
                    continue;
                }

                if (!attribList.contains(attrib)) {
                    System.out.println("Missing column name in attrib list: " + val);
                    retval = false;
                    continue;
                }

                specifiedAttribList.add(attrib);
            }

            for (final ColumnAttrib attrib : attribList) {
                if (!specifiedAttribList.contains(attrib)) {
                    System.out.println("Missing column name in specified list: " + attrib.getFamilyQualifiedName());
                    retval = false;
                }
            }
            return retval;
        }
        catch (HBqlException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean evaluateExprColumnNames(final String expr, String... vals) {

        try {
            final ExprTree tree = parseDescWhereExpr(expr, null);
            final List<String> valList = Lists.newArrayList(vals);

            final List<String> attribList = Lists.newArrayList();
            for (final GenericColumn column : tree.getColumnsUsedInExpr())
                attribList.add(column.getVariableName());

            boolean retval = true;

            for (final String val : valList) {
                if (!attribList.contains(val)) {
                    System.out.println("Missing column name in attrib list : " + val);
                    retval = false;
                }
            }

            for (final String var : attribList) {
                if (!valList.contains(var)) {
                    System.out.println("Missing column name in specified list : " + var);
                    retval = false;
                }
            }
            return retval;
        }
        catch (HBqlException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ExprTree parseDescWhereExpr(final String str, final Schema schema) throws HBqlException {
        try {
            final HBqlParser parser = HBql.newParser(str);
            final ExprTree exprTree = parser.descWhereExpr();
            exprTree.setSchema(schema);
            return exprTree;
        }
        catch (RecognitionException e) {
            e.printStackTrace();
            throw new HBqlException("Error parsing: " + str);
        }
    }

    public void assertValidInput(final String expr) throws HBqlException {
        assertTrue(evaluateWhereValue(expr));
    }

    public void assertInvalidInput(final String expr) throws HBqlException {
        assertFalse(evaluateWhereValue(expr));
    }

    private static boolean evaluateWhereValue(final String expr) {
        try {
            final WhereArgs args = HBql.parseWithClause(expr);
            System.out.println("Evaluating: " + args.asString());
            return true;
        }
        catch (HBqlException e) {
            return false;
        }
    }
}