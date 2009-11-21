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

package org.apache.expreval.expr.compare;

import org.apache.expreval.client.ResultMissingColumnException;
import org.apache.expreval.expr.Operator;
import org.apache.expreval.expr.TypeSupport;
import org.apache.expreval.expr.node.BooleanValue;
import org.apache.expreval.expr.node.DateValue;
import org.apache.expreval.expr.node.GenericValue;
import org.apache.expreval.expr.node.NumberValue;
import org.apache.expreval.expr.node.StringValue;
import org.apache.hadoop.hbase.hbql.client.HBqlException;

public class DelegateCompare extends GenericCompare {

    private GenericCompare typedExpr = null;

    public DelegateCompare(final GenericValue arg0, final Operator operator, final GenericValue arg1) {
        super(arg0, operator, arg1);
    }

    private GenericCompare getTypedExpr() {
        return this.typedExpr;
    }

    public Class<? extends GenericValue> validateTypes(final GenericValue parentExpr,
                                                       final boolean allowCollections) throws HBqlException {

        final Class<? extends GenericValue> type0 = this.getArg(0).validateTypes(this, false);
        final Class<? extends GenericValue> type1 = this.getArg(1).validateTypes(this, false);

        if (TypeSupport.isParentClass(StringValue.class, type0, type1))
            typedExpr = new StringCompare(this.getArg(0), this.getOperator(), this.getArg(1));
        else if (TypeSupport.isParentClass(NumberValue.class, type0, type1))
            typedExpr = new NumberCompare(this.getArg(0), this.getOperator(), this.getArg(1));
        else if (TypeSupport.isParentClass(DateValue.class, type0, type1))
            typedExpr = new DateCompare(this.getArg(0), this.getOperator(), this.getArg(1));
        else if (TypeSupport.isParentClass(BooleanValue.class, type0, type1))
            typedExpr = new BooleanCompare(this.getArg(0), this.getOperator(), this.getArg(1));
        else
            this.throwInvalidTypeException(type0, type1);

        return this.getTypedExpr().validateTypes(parentExpr, false);
    }

    public GenericValue getOptimizedValue() throws HBqlException {
        this.optimizeArgs();
        return !this.isAConstant() ? this : this.getTypedExpr().getOptimizedValue();
    }

    public Boolean getValue(final Object object) throws HBqlException, ResultMissingColumnException {
        return this.getTypedExpr().getValue(object);
    }
}