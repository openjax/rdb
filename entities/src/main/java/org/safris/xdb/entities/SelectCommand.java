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

import java.util.ArrayList;
import java.util.List;

import org.safris.xdb.entities.Select.FROM;
import org.safris.xdb.entities.Select.GROUP_BY;
import org.safris.xdb.entities.Select.HAVING;
import org.safris.xdb.entities.Select.JOIN;
import org.safris.xdb.entities.Select.LIMIT;
import org.safris.xdb.entities.Select.OFFSET;
import org.safris.xdb.entities.Select.ON;
import org.safris.xdb.entities.Select.ORDER_BY;
import org.safris.xdb.entities.Select.SELECT;
import org.safris.xdb.entities.Select.WHERE;

public final class SelectCommand extends Command {
  private final SELECT<?> select;
  private FROM<?> from;
  private WHERE<?> where;
  private List<JOIN<?>> join;
  private List<ON<?>> on;
  private GROUP_BY<?> groupBy;
  private HAVING<?> having;
  private ORDER_BY<?> orderBy;
  private LIMIT<?> limit;
  private OFFSET<?> offset;

  public SelectCommand(final SELECT<?> select) {
    this.select = select;
  }

  protected SELECT<?> select() {
    return select;
  }

  protected void add(final FROM<?> from) {
    this.from = from;
  }

  protected FROM<?> from() {
    return from;
  }

  protected void add(final WHERE<?> where) {
    this.where = where;
  }

  protected WHERE<?> where() {
    return where;
  }

  protected void add(final JOIN<?> join) {
    if (this.join == null)
      this.join = new ArrayList<JOIN<?>>();

    this.join.add(join);
  }

  protected List<JOIN<?>> join() {
    return join;
  }

  protected void add(final ON<?> on) {
    if (this.on == null)
      this.on = new ArrayList<ON<?>>();

    // Since ON is optional, for each JOIN without ON, add a null to this.on
    for (int i = 0; i < this.join.size() - this.on.size(); i++)
      this.on.add(null);

    this.on.add(on);
  }

  protected List<ON<?>> on() {
    return on;
  }

  protected void add(final GROUP_BY<?> groupBy) {
    this.groupBy = groupBy;
  }

  protected GROUP_BY<?> groupBy() {
    return groupBy;
  }

  protected void add(final HAVING<?> having) {
    this.having = having;
  }

  protected HAVING<?> having() {
    return having;
  }

  protected void add(final ORDER_BY<?> orderBy) {
    this.orderBy = orderBy;
  }

  protected ORDER_BY<?> orderBy() {
    return orderBy;
  }

  protected void add(final LIMIT<?> limit) {
    this.limit = limit;
  }

  protected LIMIT<?> limit() {
    return limit;
  }

  protected void add(final OFFSET<?> offset) {
    this.offset = offset;
  }

  protected OFFSET<?> offset() {
    return offset;
  }
}