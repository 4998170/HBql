package org.apache.hadoop.hbase.hbql.query.schema;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.filter.HBqlFilter;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.HRecord;
import org.apache.hadoop.hbase.hbql.query.antlr.HBql;
import org.apache.hadoop.hbase.hbql.query.expr.ExprTree;
import org.apache.hadoop.hbase.hbql.query.stmt.select.SelectElement;
import org.apache.hadoop.hbase.hbql.query.util.Lists;
import org.apache.hadoop.hbase.hbql.query.util.Maps;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DefinedSchema extends HBaseSchema {

    private final static Map<String, DefinedSchema> definedSchemaMap = Maps.newHashMap();

    final String tableName;
    final String tableAliasName;

    public DefinedSchema(final List<ColumnDescription> varList) throws HBqlException {
        this.tableName = "embedded";
        this.tableAliasName = "embedded";
        for (final ColumnDescription var : varList)
            this.processColumn(var, false);
    }

    public DefinedSchema(final String tableName,
                         final String tableAliasName,
                         final List<ColumnDescription> columnDescriptionList) throws HBqlException {
        this.tableName = tableName;
        this.tableAliasName = tableAliasName;
        for (final ColumnDescription columnDescription : columnDescriptionList)
            processColumn(columnDescription, true);
    }

    public synchronized static DefinedSchema newDefinedSchema(final String tableName,
                                                              final String aliasName,
                                                              final List<ColumnDescription> varList) throws HBqlException {

        if (doesDefinedSchemaExist(tableName))
            throw new HBqlException("Table " + tableName + " already defined");

        if (aliasName != null && doesDefinedSchemaExist(aliasName))
            throw new HBqlException("Alias " + aliasName + " already defined");

        final DefinedSchema schema = new DefinedSchema(tableName, aliasName, varList);

        getDefinedSchemaMap().put(tableName, schema);

        // Add in the same schema if there is an alias
        if (aliasName != null && !tableName.equals(aliasName))
            getDefinedSchemaMap().put(aliasName, schema);

        return schema;
    }

    private static boolean doesDefinedSchemaExist(final String tableName) {
        return null != getDefinedSchemaMap().get(tableName);
    }

    private void processColumn(final ColumnDescription columnDescription,
                               final boolean requireFamilyName) throws HBqlException {

        final DefinedAttrib attrib = new DefinedAttrib(columnDescription);

        this.addAttribToVariableNameMap(attrib, attrib.getNamesForColumn());
        this.addAttribToFamilyQualifiedNameMap(attrib);
        this.addVersionAttribToFamilyQualifiedNameMap(attrib);
        this.addColumnAttribListToFamilyNameMap(attrib);

        if (attrib.isKeyAttrib()) {
            if (this.getKeyAttrib() != null)
                throw new HBqlException("Table " + this + " has multiple instance variables marked as keys");
            this.setKeyAttrib(attrib);
        }
        else {
            final String family = attrib.getFamilyName();
            if (requireFamilyName && family.length() == 0)
                throw new HBqlException(attrib.getColumnName() + " is missing family name");
        }
    }

    private static Map<String, DefinedSchema> getDefinedSchemaMap() {
        return definedSchemaMap;
    }

    public static DefinedSchema getDefinedSchema(final String tableName) {
        return getDefinedSchemaMap().get(tableName);
    }

    public String toString() {
        return this.getTableName();
    }

    public String getTableAliasName() {
        return this.tableAliasName;
    }

    public String getTableName() {
        return this.tableName;
    }

    public List<HColumnDescriptor> getColumnDescriptors() {
        final List<HColumnDescriptor> descList = Lists.newArrayList();
        for (final String familyName : this.getFamilySet())
            descList.add(new HColumnDescriptor(familyName));

        return descList;
    }

    public String getSchemaName() {
        return this.getTableName();
    }

    public HRecord newObject(final Collection<ColumnAttrib> attribList,
                             final List<SelectElement> selectElementList,
                             final int maxVersions,
                             final Result result) throws HBqlException {

        // Create object and assign values
        final HRecord newrec = new HRecord(this);

        // Set key value
        this.getKeyAttrib().setCurrentValue(newrec, 0, result.getRow());

        // Assign values
        this.assignSelectValues(newrec, attribList, selectElementList, maxVersions, result);

        return newrec;
    }

    public HBqlFilter newHBqlFilter(final String query) throws HBqlException {
        final ExprTree exprTree = HBql.parseWhereExpression(query, this);
        return new HBqlFilter(exprTree, -1);
    }

    protected DefinedSchema getDefinedSchemaEquivalent() {
        return this;
    }
}
