package org.apache.hadoop.hbase.hbql.query.cmds;

public abstract class TableCmd {

    private final String tableName;

    protected TableCmd(final String tableName) {
        this.tableName = tableName;
    }

    protected String getTableName() {
        return tableName;
    }
}
