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

package org.apache.hadoop.hbase.hbql.statement;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.hbql.client.ExecutionResults;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.impl.HConnectionImpl;

import java.io.IOException;

public class DescribeTableStatement extends TableStatement {

    public DescribeTableStatement(final String tableName) {
        super(tableName);
    }

    public ExecutionResults execute(final HConnectionImpl connection) throws HBqlException {

        try {
            final byte[] bytes = this.getTableName().getBytes();
            final HTableDescriptor tableDesc = connection.newHBaseAdmin().getTableDescriptor(bytes);

            final ExecutionResults retval = new ExecutionResults();
            retval.out.println("Table name: " + tableDesc.getNameAsString());
            retval.out.println("Families:");
            for (final HColumnDescriptor columnDesc : tableDesc.getFamilies()) {
                retval.out.println("\t" + columnDesc.getNameAsString()
                                   + " Max versions: " + columnDesc.getMaxVersions()
                                   + " TTL: " + columnDesc.getTimeToLive()
                                   + " Block size: " + columnDesc.getBlocksize()
                                   + " Compression: " + columnDesc.getCompression().getName()
                                   + " Compression type: " + columnDesc.getCompressionType().getName()
                                   + " Block cache enabled: " + columnDesc.isBlockCacheEnabled()
                                   + " Bloom filter: " + columnDesc.isBloomfilter()
                                   + " In memory: " + columnDesc.isInMemory()
                );
            }

            retval.out.flush();
            return retval;
        }
        catch (IOException e) {
            throw new HBqlException(e);
        }
    }
}