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

package org.safris.xdb.entities.spec;

import org.safris.xdb.entities.Condition;
import org.safris.xdb.entities.DML.ALL;
import org.safris.xdb.entities.DML.CROSS;
import org.safris.xdb.entities.DML.DISTINCT;
import org.safris.xdb.entities.DML.NATURAL;
import org.safris.xdb.entities.DML.TYPE;
import org.safris.xdb.entities.DataType;
import org.safris.xdb.entities.Entity;
import org.safris.xdb.entities.Subject;

public interface select {
  public interface OFFSET<T extends Subject<?>> extends SELECT<T> {
  }

  public interface _LIMIT<T extends Subject<?>> {
    public LIMIT<T> LIMIT(final int limit);
  }

  public interface LIMIT<T extends Subject<?>> extends SELECT<T> {
    public OFFSET<T> OFFSET(final int offset);
  }

  public interface _ORDER_BY<T extends Subject<?>> {
    public ORDER_BY<T> ORDER_BY(final DataType<?> ... column);
  }

  public interface ORDER_BY<T extends Subject<?>> extends SELECT<T>, _LIMIT<T> {
  }

  public interface _GROUP_BY<T extends Subject<?>> {
    public GROUP_BY<T> GROUP_BY(final Subject<?> ... subjects);
  }

  public interface GROUP_BY<T extends Subject<?>> extends SELECT<T>, _LIMIT<T>, _HAVING<T> {
  }

  public interface _HAVING<T extends Subject<?>> {
    public HAVING<T> HAVING(final Condition<?> condition);
  }

  public interface HAVING<T extends Subject<?>> extends SELECT<T>, _LIMIT<T>, _ORDER_BY<T> {
  }

  public interface _WHERE<T extends Subject<?>> {
    public WHERE<T> WHERE(final Condition<?> condition);
  }

  public interface WHERE<T extends Subject<?>> extends SELECT<T>, _GROUP_BY<T>, _LIMIT<T>, _ORDER_BY<T> {
  }

  public interface _ON<T extends Subject<?>> {
    public ON<T> ON(final Condition<?> condition);
  }

  public interface ON<T extends Subject<?>> extends SELECT<T>, _GROUP_BY<T>, _JOIN<T>, _LIMIT<T>, _ORDER_BY<T>, _WHERE<T> {
  }

  public interface _JOIN<T extends Subject<?>> {
    public JOIN<T> JOIN(final Entity table);
    public CROSS_JOIN<T> JOIN(final CROSS cross, Entity table);
    public JOIN<T> JOIN(final TYPE type, final Entity table);
    public CROSS_JOIN<T> JOIN(final NATURAL natural, final Entity table);
    public JOIN<T> JOIN(final NATURAL natural, final TYPE type, final Entity table);
  }

  public interface JOIN<T extends Subject<?>> extends _JOIN<T>, _ON<T> {
  }

  public interface CROSS_JOIN<T extends Subject<?>> extends SELECT<T>, _JOIN<T> {
  }

  public interface _FROM<T extends Subject<?>> {
    public FROM<T> FROM(final Entity ... tables);
  }

  public interface FROM<T extends Subject<?>> extends SELECT<T>, _GROUP_BY<T>, _HAVING<T>, _JOIN<T>, _LIMIT<T>, _ORDER_BY<T>, _WHERE<T> {
  }

  public interface _SELECT<T extends Subject<?>> extends SELECT<T>, _LIMIT<T>, _FROM<T> {
  }

  public interface SELECT<T extends Subject<?>> extends ExecuteQuery<T> {
    public T AS(final T as);
    public SELECT<T> UNION(final SELECT<T> union);
    public SELECT<T> UNION(final ALL all, final SELECT<T> union);
    public SELECT<T> UNION(final DISTINCT distinct, final SELECT<T> union);
  }
}