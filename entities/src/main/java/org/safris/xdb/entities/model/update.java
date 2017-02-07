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

package org.safris.xdb.entities.model;

import org.safris.xdb.entities.Condition;
import org.safris.xdb.entities.Subject;
import org.safris.xdb.entities.type;

public interface update {
  public interface SET extends UPDATE_SET {
    public UPDATE WHERE(final Condition<?> condition);
  }

  public interface UPDATE_SET extends UPDATE {
    public <T>SET SET(final type.DataType<? extends T> set, final type.DataType<? extends T> to);
    public <T>SET SET(final type.DataType<T> set, final T to);
    public <T extends Subject<?>>SET SET(final T set, final select.SELECT<T> to);
  }

  public interface UPDATE extends ExecuteUpdate {
  }
}