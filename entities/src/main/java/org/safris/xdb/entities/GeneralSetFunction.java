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

import org.safris.xdb.entities.DML.SetQualifier;

final class GeneralSetFunction<T> extends Expression<T> {
  protected final String function;
  protected final DML.SetQualifier qualifier;
  protected final Subject<?> a;
  protected final DataType<?> b;

  protected GeneralSetFunction(final String function, final SetQualifier qualifier, final Subject<?> subject) {
    this.function = function;
    this.qualifier = qualifier;
    this.a = subject;
    this.b = null;
  }

  protected GeneralSetFunction(final String function, final Subject<?> subject) {
    this(function, (DML.SetQualifier)null, subject);
  }

  @Override
  protected final void serialize(final Serialization serialization) throws IOException {
    Serializer.getSerializer(serialization.vendor).serialize(this, serialization);
  }
}