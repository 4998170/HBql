/*
 * Copyright (c) 2011.  The Apache Software Foundation
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

package org.apache.hadoop.hbase.hbql.statement;

import org.apache.expreval.expr.var.NamedParameter;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.util.AtomicReferences;
import org.apache.hadoop.hbase.hbql.util.Lists;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

public class NamedParameters implements Serializable {

    private static final long serialVersionUID = 1L;

    private final SortedSet<NamedParameter>             paramSet        = new TreeSet<NamedParameter>(NamedParameter.getComparator());
    private final AtomicReference<List<NamedParameter>> atomicParamList = AtomicReferences.newAtomicReference();

    private SortedSet<NamedParameter> getParamSet() {
        return this.paramSet;
    }

    public void addParameters(final Collection<NamedParameter> params) {
        if (params != null)
            this.getParamSet().addAll(params);
    }

    public AtomicReference<List<NamedParameter>> getAtomicParamList() {
        return atomicParamList;
    }

    public List<NamedParameter> getParameterList() {
        if (this.getAtomicParamList().get() == null) {
            synchronized (this) {
                if (this.getAtomicParamList().get() == null) {
                    // This takes the ordered set and converts to a list
                    // The order is determined by when the param was created.
                    final int size = this.getParamSet().size();
                    final List<NamedParameter> val = Lists.newArrayList(this.getParamSet()
                                                                            .toArray(new NamedParameter[size]));
                    this.getAtomicParamList().set(val);
                }
            }
        }
        return this.getAtomicParamList().get();
    }

    public NamedParameter getParameter(final int i) throws HBqlException {
        try {
            return this.getParameterList().get(i - 1);
        }
        catch (Exception e) {
            throw new HBqlException("Invalid index: " + (i - 1));
        }
    }

    public void clearParameters() {
        for (final NamedParameter param : this.getParameterList())
            param.reset();
    }
}
