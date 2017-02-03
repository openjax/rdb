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

import java.io.IOException;
import java.sql.SQLException;

import org.safris.xdb.entities.RowIterator;
import org.safris.xdb.entities.Subject;
import org.safris.xdb.entities.Transaction;

public interface ExecuteQuery<T extends Subject<?>> {
  public RowIterator<T> execute(final Transaction transaction) throws IOException, SQLException;
  public RowIterator<T> execute() throws IOException, SQLException;
}