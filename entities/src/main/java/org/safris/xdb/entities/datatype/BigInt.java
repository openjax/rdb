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

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.safris.xdb.entities.DataType;
import org.safris.xdb.entities.Entity;
import org.safris.xdb.entities.GenerateOn;
import org.safris.xdb.schema.DBVendor;

public final class BigInt extends DataType<BigInteger> {
  protected static final int sqlType = Types.BIGINT;

  protected static BigInteger get(final ResultSet resultSet, final int columnIndex) throws SQLException {
    final Object value = resultSet.getObject(columnIndex);
    if (value == null)
      return null;

    if (value instanceof BigInteger)
      return (BigInteger)value;

    if (value instanceof java.lang.Long)
      return BigInteger.valueOf((java.lang.Long)value);

    throw new UnsupportedOperationException("Unsupported class for BigInt data type: " + value.getClass().getName());
  }

  protected static void set(final PreparedStatement statement, final int parameterIndex, final BigInteger value) throws SQLException {
    if (value != null)
      statement.setObject(parameterIndex, value, sqlType);
    else
      statement.setNull(parameterIndex, sqlType);
  }

  public final int precision;
  public final boolean unsigned;
  public final BigInteger min;
  public final BigInteger max;

  // FIXME: This is not properly supported by Derby, as in derby, only signed numbers are allowed. But in MySQL, unsigned values of up to 18446744073709551615 are allowed.
  public BigInt(final Entity owner, final String specName, final String name, final BigInteger _default, final boolean unique, final boolean primary, final boolean nullable, final GenerateOn<BigInteger> generateOnInsert, final GenerateOn<BigInteger> generateOnUpdate, final int precision, final boolean unsigned, final BigInteger min, final BigInteger max) {
    super(sqlType, BigInteger.class, owner, specName, name, _default, unique, primary, nullable, generateOnInsert, generateOnUpdate);
    this.precision = precision;
    this.unsigned = unsigned;
    this.min = min;
    this.max = max;
  }

  protected BigInt(final BigInt copy) {
    super(copy);
    this.precision = copy.precision;
    this.unsigned = copy.unsigned;
    this.min = copy.min;
    this.max = copy.max;
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