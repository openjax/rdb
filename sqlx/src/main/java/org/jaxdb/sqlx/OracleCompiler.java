/* Copyright (c) 2017 JAX-DB
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * You should have received a copy of The MIT License (MIT) along with this
 * program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package org.jaxdb.sqlx;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import org.jaxdb.ddlx.dt;
import org.jaxdb.vendor.DBVendor;
import org.libj.lang.Hexadecimal;

final class OracleCompiler extends Compiler {
  OracleCompiler() {
    super(DBVendor.ORACLE);
  }

  @Override
  String compile(final dt.BINARY value) {
    return "HEXTORAW('" + new Hexadecimal(value.get()) + "')";
  }

  @Override
  String compile(final dt.BLOB value) {
    return "HEXTORAW('" + new Hexadecimal(value.get()) + "')";
  }

  @Override
  String compile(final dt.BOOLEAN value) {
    return value.get() ? "1" : "0";
  }

  @Override
  String compile(final dt.CHAR value) {
    final String string = value.get().replace("'", "''");
    return string.length() == 0 || string.charAt(0) == ' ' ? "' " + string + "'" : "'" + string + "'";
  }

  @Override
  String compile(final dt.DATE value) {
    return "TO_DATE('" + value + "','YYYY-MM-DD')";
  }

  @Override
  String compile(final dt.DATETIME value) {
    return "TO_TIMESTAMP('" + value + "', 'YYYY-MM-DD HH24:MI:SS.FF')";
  }

  @Override
  String compile(final dt.TIME value) {
    return "'0 " + value + "'";
  }

  @Override
  boolean sequenceReset(final Connection connection, final Appendable builder, final String tableName, final String columnName, final long restartWith) throws IOException, SQLException {
    final String sequenceName = getSequenceName(tableName, columnName);
    if (connection != null) {
      try (final CallableStatement statement = connection.prepareCall("{ ? = call reset_sequence(?, ?) }")) {
        statement.registerOutParameter(1, Types.NUMERIC);
        statement.setString(2, sequenceName);
        statement.setLong(3, restartWith);
        statement.executeUpdate();
        return statement.getInt(1) == 1;
      }
    }

    builder.append("\nEXEC DBMS_OUTPUT.PUT_LINE(reset_sequence('" + sequenceName + "', " + restartWith + "));");
    return true;
  }
}