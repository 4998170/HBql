/*
 * Copyright (c) 2009.  The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.expreval.expr.function;

import org.apache.expreval.client.InternalErrorException;
import org.apache.expreval.client.ResultMissingColumnException;
import org.apache.expreval.expr.FunctionTypeSignature;
import org.apache.expreval.expr.GenericExpression;
import org.apache.expreval.expr.TypeSupport;
import org.apache.expreval.expr.node.BooleanValue;
import org.apache.expreval.expr.node.DateValue;
import org.apache.expreval.expr.node.DoubleValue;
import org.apache.expreval.expr.node.FloatValue;
import org.apache.expreval.expr.node.GenericValue;
import org.apache.expreval.expr.node.IntegerValue;
import org.apache.expreval.expr.node.LongValue;
import org.apache.expreval.expr.node.NumberValue;
import org.apache.expreval.expr.node.ShortValue;
import org.apache.expreval.expr.node.StringValue;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.TypeException;

import java.util.List;
import java.util.Random;

public abstract class Function extends GenericExpression {

    static Random randomVal = new Random();

    public static enum FunctionType {

        // Dealt with in DateFunction
        DATEINTERVAL(new FunctionTypeSignature(DateValue.class, LongValue.class)),
        DATECONSTANT(new FunctionTypeSignature(DateValue.class)),

        // Date functions
        DATE(new FunctionTypeSignature(DateValue.class, StringValue.class, StringValue.class)),
        LONGTODATE(new FunctionTypeSignature(DateValue.class, LongValue.class)),
        RANDOMDATE(new FunctionTypeSignature(DateValue.class)),

        // String functions
        TRIM(new FunctionTypeSignature(StringValue.class, StringValue.class)),
        LOWER(new FunctionTypeSignature(StringValue.class, StringValue.class)),
        UPPER(new FunctionTypeSignature(StringValue.class, StringValue.class)),
        CONCAT(new FunctionTypeSignature(StringValue.class, StringValue.class, StringValue.class)),
        REPLACE(new FunctionTypeSignature(StringValue.class, StringValue.class, StringValue.class, StringValue.class)),
        SUBSTRING(new FunctionTypeSignature(StringValue.class, StringValue.class, IntegerValue.class, IntegerValue.class)),
        ZEROPAD(new FunctionTypeSignature(StringValue.class, LongValue.class, IntegerValue.class)),
        REPEAT(new FunctionTypeSignature(StringValue.class, StringValue.class, IntegerValue.class)),

        // Number functions
        LENGTH(new FunctionTypeSignature(IntegerValue.class, StringValue.class)),
        INDEXOF(new FunctionTypeSignature(IntegerValue.class, StringValue.class, StringValue.class)),

        DATETOLONG(new FunctionTypeSignature(LongValue.class, DateValue.class)),

        SHORT(new FunctionTypeSignature(ShortValue.class, StringValue.class)),
        INTEGER(new FunctionTypeSignature(IntegerValue.class, StringValue.class)),
        LONG(new FunctionTypeSignature(LongValue.class, StringValue.class)),
        FLOAT(new FunctionTypeSignature(FloatValue.class, StringValue.class)),
        DOUBLE(new FunctionTypeSignature(DoubleValue.class, StringValue.class)),

        COUNT(new FunctionTypeSignature(LongValue.class), true),
        MIN(new FunctionTypeSignature(NumberValue.class, NumberValue.class), true),
        MAX(new FunctionTypeSignature(NumberValue.class, NumberValue.class), true),

        ABS(new FunctionTypeSignature(NumberValue.class, NumberValue.class)),
        LESSER(new FunctionTypeSignature(NumberValue.class, NumberValue.class, NumberValue.class)),
        GREATER(new FunctionTypeSignature(NumberValue.class, NumberValue.class, NumberValue.class)),

        RANDOMINTEGER(new FunctionTypeSignature(IntegerValue.class)),
        RANDOMLONG(new FunctionTypeSignature(LongValue.class)),
        RANDOMFLOAT(new FunctionTypeSignature(FloatValue.class)),
        RANDOMDOUBLE(new FunctionTypeSignature(DoubleValue.class)),

        // Boolean functions
        RANDOMBOOLEAN(new FunctionTypeSignature(BooleanValue.class)),
        DEFINEDINROW(new FunctionTypeSignature(BooleanValue.class, GenericValue.class)),
        EVAL(new FunctionTypeSignature(BooleanValue.class, StringValue.class));

        private final FunctionTypeSignature typeSignature;
        private final boolean anAggregateValue;

        FunctionType(final FunctionTypeSignature typeSignature) {
            this(typeSignature, false);
        }

        FunctionType(final FunctionTypeSignature typeSignature, final boolean anAggregateValue) {
            this.typeSignature = typeSignature;
            this.anAggregateValue = anAggregateValue;
        }

        private FunctionTypeSignature getTypeSignature() {
            return this.typeSignature;
        }

        public boolean isAnAggregateValue() {
            return this.anAggregateValue;
        }

        public static Function getFunction(final String functionName, final List<GenericValue> exprList) {

            final FunctionType type;

            try {
                type = FunctionType.valueOf(functionName.toUpperCase());
            }
            catch (IllegalArgumentException e) {
                return null;
            }

            final Class<? extends GenericValue> returnType = type.getTypeSignature().getReturnType();

            if (TypeSupport.isParentClass(BooleanValue.class, returnType))
                return new BooleanFunction(type, exprList);
            else if (TypeSupport.isParentClass(StringValue.class, returnType))
                return new StringFunction(type, exprList);
            else if (TypeSupport.isParentClass(NumberValue.class, returnType))
                return new NumberFunction(type, exprList);
            else if (TypeSupport.isParentClass(DateValue.class, returnType))
                return new DateFunction(type, exprList);

            return null;
        }
    }

    private final FunctionType functionType;

    public Function(final FunctionType functionType, final List<GenericValue> exprs) {
        super(null, exprs);
        this.functionType = functionType;
    }

    protected FunctionType getFunctionType() {
        return this.functionType;
    }

    protected FunctionTypeSignature getTypeSignature() {
        return this.getFunctionType().getTypeSignature();
    }

    protected boolean isIntervalDate() {
        return this.getFunctionType() == FunctionType.DATEINTERVAL;
    }

    public boolean isAnAggregateValue() {
        return this.getFunctionType().isAnAggregateValue();
    }

    protected boolean isConstantDate() {
        return this.getFunctionType() == FunctionType.DATECONSTANT;
    }

    protected void checkForNull(final String... vals) throws HBqlException {
        for (final Object val : vals) {
            if (val == null)
                throw new HBqlException("Null value in " + this.asString());
        }
    }

    public Class<? extends GenericValue> validateTypes(final GenericValue parentExpr,
                                                       final boolean allowCollections) throws HBqlException {

        int i = 0;
        if (this.getArgList().size() != this.getTypeSignature().getArgCount())
            throw new TypeException("Incorrect number of arguments in function " + this.getFunctionType().name()
                                    + " in " + this.asString());

        for (final Class<? extends GenericValue> clazz : this.getTypeSignature().getArgs()) {
            final Class<? extends GenericValue> type = this.getArg(i).validateTypes(this, false);
            try {
                this.validateParentClass(clazz, type);
            }
            catch (TypeException e) {
                // Catch the exception and improve message
                throw new TypeException("Invalid type " + type.getSimpleName() + " for arg " + i + " in function "
                                        + this.getFunctionName() + " in "
                                        + this.asString() + ".  Expecting type " + clazz.getSimpleName() + ".");
            }
            i++;
        }

        return this.getTypeSignature().getReturnType();
    }

    protected String getFunctionName() {
        return this.getFunctionType().name();
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

    public String asString() {
        return this.getFunctionName() + super.asString();
    }
}