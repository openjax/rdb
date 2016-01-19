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

package org.safris.xdb.xde.column;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.safris.xdb.xde.GenerateOn;
import org.safris.xdb.xde.Column;
import org.safris.xdb.xde.Table;

public final class Float extends Column<java.lang.Float> {
  protected static final int sqlType = Types.FLOAT;

  protected static void set(final PreparedStatement statement, final int parameterIndex, final java.lang.Float value) throws SQLException {
    statement.setFloat(parameterIndex, value);
  }

  public final int precision;
  public int decimal;
  public final boolean unsigned;
  public final java.lang.Float min;
  public final java.lang.Float max;

  public Float(final Table owner, final String csqlName, final String name, final java.lang.Float _default, final boolean unique, final boolean primary, final boolean nullable, final GenerateOn<java.lang.Float> generateOnInsert, final GenerateOn<java.lang.Float> generateOnUpdate, final int precision, final int decimal, final boolean unsigned, final java.lang.Float min, final java.lang.Float max) {
    super(sqlType, java.lang.Float.class, owner, csqlName, name, _default, unique, primary, nullable, generateOnInsert, generateOnUpdate);
    this.precision = precision;
    this.unsigned = unsigned;
    this.min = min;
    this.max = max;
  }

  protected Float(final Float column) {
    super(column);
    this.precision = column.precision;
    this.unsigned = column.unsigned;
    this.min = column.min;
    this.max = column.max;
  }

  protected void set(final PreparedStatement statement, final int parameterIndex) throws SQLException {
    set(statement, parameterIndex, get());
  }

  protected java.lang.Float get(final ResultSet resultSet, final int columnIndex) throws SQLException {
    final float value = resultSet.getFloat(columnIndex);
    return resultSet.wasNull() ? null : value;
  }
}