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

package org.apache.hadoop.hbase.hbql.impl;

import org.apache.expreval.client.ResultMissingColumnException;
import org.apache.expreval.util.NullIterator;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.QueryListener;
import org.apache.hadoop.hbase.hbql.mapping.ResultAccessor;
import org.apache.hadoop.hbase.hbql.statement.select.RowRequest;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;


public class ExecutorResultSetImpl2<T> extends HResultSetImpl<T> {

    private final ExecutorImpl2<String> executor;
    private volatile boolean closed = false;
    private final BlockingQueue<ResultElement> resultQueue = new ArrayBlockingQueue<ResultElement>(100, true);
    private final AtomicInteger count = new AtomicInteger(0);

    ExecutorResultSetImpl2(final Query<T> query) throws HBqlException {
        super(query);
        // This may block waiting for a Executor to become available from the ExecutorPool
        this.executor = (ExecutorImpl2<String>)this.getQuery().getHConnectionImpl().getExecutorForConnection();
        // Submit work to executor
        this.submitWork();
    }

    public static class ResultElement {
        private final Result result;
        private final boolean scanComplete;

        private ResultElement(final Result result, boolean scanComplete) {
            this.result = result;
            this.scanComplete = scanComplete;
        }

        public static ResultElement newResult(final Result result) {
            return new ResultElement(result, false);
        }

        public static ResultElement newScanComplete() {
            return new ResultElement(null, true);
        }

        public boolean isScanComplete() {
            return this.scanComplete;
        }
    }

    private ExecutorImpl2<String> getExecutor() {
        return this.executor;
    }

    private BlockingQueue<ResultElement> getResultQueue() {
        return this.resultQueue;
    }

    @SuppressWarnings("unchecked")
    private void submitWork() throws HBqlException {
        final List<RowRequest> rowRequestList = this.getQuery().getRowRequestList();
        for (final RowRequest rowRequest : rowRequestList) {
            this.getExecutor().submit(new Callable<String>() {
                public String call() {
                    try {
                        setMaxVersions(rowRequest.getMaxVersions());
                        final ResultScanner scanner = rowRequest.getResultScanner(getSelectStmt().getMapping(),
                                                                                  getWithArgs(),
                                                                                  getHTableWrapper().getHTable());
                        for (final Result result : scanner) {

                            try {
                                if (getClientExpressionTree() != null
                                    && !getClientExpressionTree().evaluate(getHConnectionImpl(), result))
                                    continue;
                            }
                            catch (ResultMissingColumnException e) {
                                continue;
                            }

                            try {
                                getResultQueue().put(ResultElement.newResult(result));
                            }
                            catch (InterruptedException e) {
                                e.printStackTrace();
                                System.out.println("bailing 1");
                                return (new HBqlException(e)).getMessage();
                            }
                        }

                        scanner.close();
                    }
                    catch (HBqlException e) {
                        e.printStackTrace();
                        return e.getMessage();
                    }

                    finally {
                        try {
                            getResultQueue().put(ResultElement.newScanComplete());
                        }
                        catch (InterruptedException e) {
                            System.out.println("bailing 2");
                            e.printStackTrace();
                        }
                    }
                    return "OK";
                }
            });
        }
    }

    public void close() {
        if (!this.closed) {
            synchronized (this) {
                if (!this.closed) {
                    super.close();
                    this.getExecutor().release();
                    this.closed = true;
                }
            }
        }
    }

    public Iterator<T> iterator() {

        try {
            return new ResultSetIterator<T>(this) {

                protected Iterator<Result> getNextResultIterator() throws HBqlException {
                    return null;
                }

                protected void cleanUp(final boolean fromExceptionCatch) {
                    try {
                        if (!fromExceptionCatch && getListeners() != null) {
                            for (final QueryListener<T> listener : getListeners())
                                listener.onQueryComplete();
                        }

                        try {
                            if (getHTableWrapper() != null)
                                getHTableWrapper().getHTable().close();
                        }
                        catch (IOException e) {
                            // No op
                            e.printStackTrace();
                        }
                    }
                    finally {
                        // release to table pool
                        if (getHTableWrapper() != null)
                            getHTableWrapper().releaseHTable();
                        setTableWrapper(null);

                        close();
                    }
                }

                protected boolean moreResultsPending() {
                    return getExecutor().moreResultsPending(count.get());
                }

                @SuppressWarnings("unchecked")
                protected T fetchNextObject() throws HBqlException {

                    final ResultAccessor resultAccessor = getSelectStmt().getResultAccessor();

                    Result result;

                    // Read data until all jobs have sent DONE tokens
                    while (true) {
                        try {
                            ResultElement val = getResultQueue().take();
                            if (val.scanComplete) {
                                count.incrementAndGet();
                                //System.out.println("Read EOF for " + val.jobId);
                                if (!moreResultsPending())
                                    break;
                                else
                                    continue;
                            }
                            else {
                                result = val.result;
                            }
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                            continue;
                        }

                        incrementReturnedRecordCount();

                        if (getSelectStmt().isAnAggregateQuery()) {
                            getAggregateRecord().applyValues(result);
                        }
                        else {
                            final T val = (T)resultAccessor.newObject(getHConnectionImpl(),
                                                                      getSelectStmt(),
                                                                      getSelectStmt().getSelectElementList(),
                                                                      getMaxVersions(),
                                                                      result);

                            if (getListeners() != null)
                                for (final QueryListener<T> listener : getListeners())
                                    listener.onEachRow(val);

                            return val;
                        }
                    }

                    if (getSelectStmt().isAnAggregateQuery() && getAggregateRecord() != null) {
                        // Stash the value and then null it out for next time through
                        final AggregateRecord retval = getAggregateRecord();
                        setAggregateRecord(null);
                        return (T)retval;
                    }

                    return null;
                }
            };
        }
        catch (HBqlException e) {
            e.printStackTrace();
            return new NullIterator<T>();
        }
    }
}