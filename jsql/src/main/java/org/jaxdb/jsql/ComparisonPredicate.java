/* Copyright (c) 2014 JAX-DB
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

package org.jaxdb.jsql;

import java.io.IOException;
import java.sql.SQLException;

import org.jaxdb.vendor.DBVendor;

final class ComparisonPredicate<V> extends data.BOOLEAN {
  final function.Logical<?> operator;
  final Subject a;
  final Subject b;

  ComparisonPredicate(final function.Logical<?> operator, final type.Column<?> a, final V b) {
    this.operator = operator;
    this.a = (Subject)a;
    this.b = org.jaxdb.jsql.data.Column.wrap(b);
  }

  ComparisonPredicate(final function.Logical<?> operator, final type.Column<?> a, final QuantifiedComparisonPredicate<?> b) {
    this.operator = operator;
    this.a = (Subject)a;
    this.b = b;
  }

  ComparisonPredicate(final function.Logical<?> operator, final V a, final type.Column<?> b) {
    this.operator = operator;
    this.a = org.jaxdb.jsql.data.Column.wrap(a);
    this.b = (Subject)b;
  }

  ComparisonPredicate(final function.Logical<?> operator, final type.Column<?> a, final type.Column<?> b) {
    this.operator = operator;
    this.a = (Subject)a;
    this.b = (Subject)b;
  }

  @Override
  final String compile(final DBVendor vendor) {
    return operator.toString();
  }

  @Override
  final void compile(final Compilation compilation, final boolean isExpression) throws IOException, SQLException {
    compilation.compiler.compilePredicate(this, compilation);
  }
}