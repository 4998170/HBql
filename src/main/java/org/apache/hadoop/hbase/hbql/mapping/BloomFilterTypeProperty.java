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

package org.apache.hadoop.hbase.hbql.mapping;

import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.regionserver.StoreFile;

public class BloomFilterTypeProperty extends FamilyProperty {

    final String type;

    public BloomFilterTypeProperty(final String text, final String type) {
        super(text);
        this.type = type;
    }

    public StoreFile.BloomType getBloomValue() throws HBqlException {

        try {
            return StoreFile.BloomType.valueOf(this.type.toUpperCase());
        }
        catch (Exception e) {
            throw new HBqlException("Invalid bloom filter type: " + this.type);
        }
    }
}