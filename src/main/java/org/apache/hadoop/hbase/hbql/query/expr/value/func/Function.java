package org.apache.hadoop.hbase.hbql.query.expr.value.func;

import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.InternalErrorException;
import org.apache.hadoop.hbase.hbql.client.ResultMissingColumnException;
import org.apache.hadoop.hbase.hbql.client.TypeException;
import org.apache.hadoop.hbase.hbql.query.expr.node.BooleanValue;
import org.apache.hadoop.hbase.hbql.query.expr.node.DoubleValue;
import org.apache.hadoop.hbase.hbql.query.expr.node.FloatValue;
import org.apache.hadoop.hbase.hbql.query.expr.node.GenericValue;
import org.apache.hadoop.hbase.hbql.query.expr.node.IntegerValue;
import org.apache.hadoop.hbase.hbql.query.expr.node.LongValue;
import org.apache.hadoop.hbase.hbql.query.expr.node.ShortValue;
import org.apache.hadoop.hbase.hbql.query.expr.node.StringValue;
import org.apache.hadoop.hbase.hbql.query.expr.value.GenericExpr;
import org.apache.hadoop.hbase.hbql.query.expr.value.TypeSignature;
import org.apache.hadoop.hbase.hbql.query.util.HUtil;

public class Function extends GenericExpr {

    public static enum Type {
        // Return Strings
        TRIM(new TypeSignature(StringValue.class, StringValue.class)),
        LOWER(new TypeSignature(StringValue.class, StringValue.class)),
        UPPER(new TypeSignature(StringValue.class, StringValue.class)),
        CONCAT(new TypeSignature(StringValue.class, StringValue.class, StringValue.class)),
        REPLACE(new TypeSignature(StringValue.class, StringValue.class, StringValue.class, StringValue.class)),
        SUBSTRING(new TypeSignature(StringValue.class, StringValue.class, IntegerValue.class, IntegerValue.class)),

        // Return Numbers
        LENGTH(new TypeSignature(IntegerValue.class, StringValue.class)),
        INDEXOF(new TypeSignature(IntegerValue.class, StringValue.class, StringValue.class)),

        SHORT(new TypeSignature(ShortValue.class, StringValue.class)),
        INTEGER(new TypeSignature(IntegerValue.class, StringValue.class)),
        LONG(new TypeSignature(LongValue.class, StringValue.class)),
        FLOAT(new TypeSignature(FloatValue.class, StringValue.class)),
        DOUBLE(new TypeSignature(DoubleValue.class, StringValue.class)),

        VALID(new TypeSignature(BooleanValue.class, GenericValue.class));

        private final TypeSignature typeSignature;

        Type(final TypeSignature typeSignature) {
            this.typeSignature = typeSignature;
        }

        private TypeSignature getTypeSignature() {
            return typeSignature;
        }
    }

    private final Type functionType;

    public Function(final Type functionType, final GenericValue... exprs) {
        super(null, exprs);
        this.functionType = functionType;
    }

    protected Type getFunctionType() {
        return this.functionType;
    }

    protected TypeSignature getTypeSignature() {
        return this.getFunctionType().getTypeSignature();
    }

    private void checkForNull(final String... vals) throws HBqlException {
        for (final Object val : vals) {
            if (val == null)
                throw new HBqlException("Null value in " + this.asString());
        }
    }

    public Class<? extends GenericValue> validateTypes(final GenericValue parentExpr,
                                                       final boolean allowsCollections) throws TypeException {

        int i = 0;
        if (this.getArgList().size() != this.getTypeSignature().getArgCount())
            throw new TypeException("Incorrect number of arguments in function " + this.getFunctionType().name()
                                    + " in " + this.asString());

        for (final Class<? extends GenericValue> clazz : this.getTypeSignature().getArgs()) {
            final Class<? extends GenericValue> type = this.getArg(i).validateTypes(this, false);
            if (!HUtil.isParentClass(clazz, type))
                throw new TypeException("Invalid type " + type.getSimpleName() + " for arg " + i + " in function "
                                        + this.getFunctionType().name() + " in "
                                        + this.asString() + ".  Expecting type " + clazz.getSimpleName());
            i++;
        }

        return this.getTypeSignature().getReturnType();
    }

    public GenericValue getOptimizedValue() throws HBqlException {
        this.optimizeArgs();
        if (!this.isAConstant())
            return this;
        else
            try {
                return this.getFunctionType().getTypeSignature().newLiteral(this.getValue(null));
            }
            catch (ResultMissingColumnException e) {
                throw new InternalErrorException();
            }
    }

    public Object getValue(final Object object) throws HBqlException, ResultMissingColumnException {

        switch (this.getFunctionType()) {

            // Returns a string
            case TRIM: {
                final String val = (String)this.getArg(0).getValue(object);
                this.checkForNull(val);
                return val.trim();
            }

            case LOWER: {
                final String val = (String)this.getArg(0).getValue(object);
                this.checkForNull(val);
                return val.toLowerCase();
            }

            case UPPER: {
                final String val = (String)this.getArg(0).getValue(object);
                this.checkForNull(val);
                return val.toUpperCase();
            }

            case CONCAT: {
                final String v1 = (String)this.getArg(0).getValue(object);
                final String v2 = (String)this.getArg(1).getValue(object);
                this.checkForNull(v1, v2);
                return v1 + v2;
            }

            case REPLACE: {
                final String v1 = (String)this.getArg(0).getValue(object);
                final String v2 = (String)this.getArg(1).getValue(object);
                final String v3 = (String)this.getArg(2).getValue(object);
                this.checkForNull(v1, v2, v3);
                return v1.replace(v2, v3);
            }

            case SUBSTRING: {
                final String val = (String)this.getArg(0).getValue(object);
                final int begin = ((Number)this.getArg(1).getValue(object)).intValue();
                final int end = ((Number)this.getArg(2).getValue(object)).intValue();
                this.checkForNull(val);
                return val.substring(begin, end);
            }

            case LENGTH: {
                final String val = (String)this.getArg(0).getValue(object);
                this.checkForNull(val);
                return val.length();
            }

            case INDEXOF: {
                final String v1 = (String)this.getArg(0).getValue(object);
                final String v2 = (String)this.getArg(1).getValue(object);
                this.checkForNull(v1, v2);
                return v1.indexOf(v2);
            }

            case SHORT: {
                final String v1 = (String)this.getArg(0).getValue(object);
                return Short.valueOf(v1);
            }

            case INTEGER: {
                final String v1 = (String)this.getArg(0).getValue(object);
                return Integer.valueOf(v1);
            }

            case LONG: {
                final String v1 = (String)this.getArg(0).getValue(object);
                return Long.valueOf(v1);
            }

            case FLOAT: {
                final String v1 = (String)this.getArg(0).getValue(object);
                return Float.valueOf(v1);
            }

            case DOUBLE: {
                final String v1 = (String)this.getArg(0).getValue(object);
                return Double.valueOf(v1);
            }

            default:
                throw new HBqlException("Invalid function: " + this.getFunctionType());
        }
    }

    public String asString() {
        return this.getFunctionType().name() + "(" + super.asString() + ")";
    }
}