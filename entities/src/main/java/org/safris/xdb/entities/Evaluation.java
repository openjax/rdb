/* Copyright (c) 2015 Seva Safris
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

package org.safris.xdb.entities;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import org.safris.commons.lang.reflect.Classes;
import org.safris.xdb.entities.binding.Interval;
import org.safris.xdb.entities.binding.Interval.Unit;
import org.safris.xdb.schema.DBVendor;

public abstract class Evaluation<T> extends Variable<T> {
  private final Variable<T> a;
  private final Operator<Predicate<?>> operator;
  private final Object b;

  protected Evaluation(final Variable<T> a, final Operator<Predicate<?>> operator, final Object b) {
    super(null);
    this.a = a;
    this.operator = operator;
    this.b = b;
  }

  @Override
  protected void serialize(final Serializable caller, final Serialization serialization) {
    a.serialize(this, serialization);
    serialization.sql.append(" ");
    if (b instanceof Interval) {
      final Interval interval = (Interval)b;

      if (serialization.vendor == DBVendor.MY_SQL || serialization.vendor == DBVendor.POSTGRE_SQL) {
        final Set<Unit> units = interval.getUnits();
        final StringBuilder clause = new StringBuilder();
        for (final Unit unit : units)
          clause.append(" ").append(interval.getComponent(unit)).append(" " + unit.name());

        serialization.sql.append(" '").append(clause.substring(1)).append("'");
      }
      else {
        throw new UnsupportedOperationException();
      }
    }
    else {
      serialization.sql.append(operator).append(" ");
      Keyword.format(this, b, serialization);
    }
  }

  @Override
  protected Entity owner() {
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void get(final PreparedStatement statement, final int parameterIndex) throws SQLException {
    DataType.set(statement, parameterIndex, (Class<T>)Classes.getGenericSuperclasses(value.getClass())[0], this.value);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void set(final ResultSet resultSet, final int columnIndex) throws SQLException {
    this.value = DataType.get((Class<T>)Classes.getGenericSuperclasses(a.getClass())[0], resultSet, columnIndex);
  }
}