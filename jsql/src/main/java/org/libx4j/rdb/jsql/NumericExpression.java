/* Copyright (c) 2015 lib4j
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

package org.libx4j.rdb.jsql;

import java.io.IOException;

import org.libx4j.rdb.jsql.type.Numeric;

final class NumericExpression extends Expression<Number> {
  protected final Operator<NumericExpression> operator;
  protected final Serializable a;
  protected final Serializable b;

  protected NumericExpression(final Operator<NumericExpression> operator, final Numeric<?> a, final Numeric<?> b) {
    this.operator = operator;
    this.a = a;
    this.b = b;
  }

  protected NumericExpression(final Operator<NumericExpression> operator, final Number a, final Numeric<?> b) {
    this.operator = operator;
    this.a = type.DataType.wrap(a);
    this.b = b;
  }

  protected NumericExpression(final Operator<NumericExpression> operator, final Numeric<?> a, final Number b) {
    this.operator = operator;
    this.a = a;
    this.b = type.DataType.wrap(b);
  }

  @Override
  protected void serialize(final Serialization serialization) throws IOException {
    Serializer.getSerializer(serialization.vendor).serialize(this, serialization);
  }
}