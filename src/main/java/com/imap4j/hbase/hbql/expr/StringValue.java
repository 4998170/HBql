package com.imap4j.hbase.hbql.expr;

import com.imap4j.hbase.hbql.HPersistException;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 29, 2009
 * Time: 7:16:27 PM
 */
public interface StringValue extends Serializable {

    boolean optimizeForConstants(final EvalContext context) throws HPersistException;

    String getValue(final EvalContext context) throws HPersistException;
}