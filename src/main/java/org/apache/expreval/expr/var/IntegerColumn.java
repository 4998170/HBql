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

package org.apache.expreval.expr.var;

import org.apache.expreval.client.ResultMissingColumnException;
import org.apache.expreval.expr.node.NumberValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.schema.ColumnAttrib;

public class IntegerColumn extends GenericColumn<NumberValue> implements NumberValue {

    public IntegerColumn(ColumnAttrib attrib) {
        super(attrib);
    }

    public Integer getValue(final Object object) throws HBqlException, ResultMissingColumnException {
        if (this.getExprContext().useResultData())
            return (Integer)this.getColumnAttrib().getValueFromBytes((Result)object);
        else
            return (Integer)this.getColumnAttrib().getCurrentValue(object);
    }
}