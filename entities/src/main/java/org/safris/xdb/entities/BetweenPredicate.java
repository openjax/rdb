/* Copyright (c) 2017 Seva Safris
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

import java.io.IOException;

final class BetweenPredicate<T> extends Predicate<T> {
  protected final boolean positive;
  protected final DataType<?> dataType;
  protected final DataType<?> a;
  protected final DataType<?> b;

  protected BetweenPredicate(final boolean positive, final DataType<?> dataType, final DataType<?> a, final DataType<?> b) {
    this.positive = positive;
    this.dataType = dataType;
    this.a = a;
    this.b = b;
  }

  @Override
  protected final void serialize(final Serialization serialization) throws IOException {
    Serializer.getSerializer(serialization.vendor).serialize(this, serialization);
  }
}