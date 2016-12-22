/* Copyright (c) 2014 Seva Safris
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

package org.safris.xdb.entities.datatype;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.temporal.Temporal;

import org.safris.xdb.entities.DataType;
import org.safris.xdb.entities.Entity;
import org.safris.xdb.entities.GenerateOn;
import org.safris.xdb.schema.DBVendor;

public final class Date extends DataType<LocalDate> {
  protected static final int sqlType = Types.DATE;

  @SuppressWarnings("deprecation")
  protected static LocalDate get(final ResultSet resultSet, final int columnIndex) throws SQLException {
    final java.sql.Date value = resultSet.getDate(columnIndex);
    return value == null ? null : LocalDate.of(value.getYear(), value.getMonth(), value.getDate());
  }

  @SuppressWarnings("deprecation")
  protected static void set(final PreparedStatement statement, final int parameterIndex, final LocalDate value) throws SQLException {
    if (value != null)
      statement.setDate(parameterIndex, new java.sql.Date(value.getYear() - 1900, value.getMonthValue() - 1, value.getDayOfMonth()));
    else
      statement.setNull(parameterIndex, sqlType);
  }

  public Date(final Entity owner, final String specName, final String name, final LocalDate _default, final boolean unique, final boolean primary, final boolean nullable, final GenerateOn<Temporal> generateOnInsert, final GenerateOn<Temporal> generateOnUpdate) {
    super(sqlType, LocalDate.class, owner, specName, name, _default, unique, primary, nullable, generateOnInsert, generateOnUpdate);
  }

  protected Date(final Date copy) {
    super(copy);
  }

  @Override
  protected String getPreparedStatementMark(final DBVendor vendor) {
    return "?";
  }

  @Override
  protected void get(final PreparedStatement statement, final int parameterIndex) throws SQLException {
    set(statement, parameterIndex, get());
  }

  @Override
  protected void set(final ResultSet resultSet, final int columnIndex) throws SQLException {
    this.value = get(resultSet, columnIndex);
  }
}