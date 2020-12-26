/* Copyright (c) 2017 JAX-DB
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
import java.time.temporal.Temporal;
import java.util.Set;

import org.libj.lang.Numbers;
import org.libj.util.Temporals;

final class BetweenPredicates {
  abstract static class BetweenPredicate extends Predicate {
    final boolean positive;

    BetweenPredicate(final kind.DataType<?> dataType, final boolean positive) {
      super(dataType);
      this.positive = positive;
    }

    abstract Compilable a();
    abstract Compilable b();

    @Override
    final void compile(final Compilation compilation) throws IOException {
      compilation.compiler.compile(this, compilation);
    }
  }

  static class NumericBetweenPredicate extends BetweenPredicate {
    final Compilable a;
    final Compilable b;

    NumericBetweenPredicate(final kind.Numeric<?> dataType, final kind.Numeric<?> a, final kind.Numeric<?> b, final boolean positive) {
      super(dataType, positive);
      this.a = (Compilable)a;
      this.b = (Compilable)b;
    }

    @Override
    Compilable a() {
      return a;
    }

    @Override
    Compilable b() {
      return b;
    }

    @Override
    Boolean evaluate(final Set<Evaluable> visited) {
      if (dataType == null || a == null || b == null || !(dataType instanceof Evaluable) || !(a instanceof Evaluable) || !(b instanceof Evaluable))
        return null;

      final Number a = (Number)((Evaluable)this.a).evaluate(visited);
      final Number b = (Number)((Evaluable)this.b).evaluate(visited);
      final Number c = (Number)((Evaluable)this.dataType).evaluate(visited);
      return Numbers.compare(a, c) >= 0 && Numbers.compare(c, b) <= 0 == positive;
    }
  }

  static class TemporalBetweenPredicate extends BetweenPredicate {
    final Compilable a;
    final Compilable b;

    TemporalBetweenPredicate(final kind.Temporal<?> dataType, final kind.Temporal<?> a, final kind.Temporal<?> b, final boolean positive) {
      super(dataType, positive);
      this.a = (Compilable)a;
      this.b = (Compilable)b;
    }

    @Override
    Compilable a() {
      return a;
    }

    @Override
    Compilable b() {
      return b;
    }

    @Override
    Boolean evaluate(final Set<Evaluable> visited) {
      if (dataType == null || a == null || b == null || !(dataType instanceof Evaluable) || !(a instanceof Evaluable) || !(b instanceof Evaluable))
        return null;

      final Temporal a = (Temporal)((Evaluable)this.a).evaluate(visited);
      final Temporal b = (Temporal)((Evaluable)this.b).evaluate(visited);
      final Temporal c = (Temporal)((Evaluable)this.dataType).evaluate(visited);
      return Temporals.compare(a, c) <= 0 && Temporals.compare(c, b) >= 0 == positive;
    }
  }

  static class TimeBetweenPredicate extends BetweenPredicate {
    final Compilable a;
    final Compilable b;

    TimeBetweenPredicate(final kind.TIME dataType, final kind.TIME a, final kind.TIME b, final boolean positive) {
      super(dataType, positive);
      this.a = (Compilable)a;
      this.b = (Compilable)b;
    }

    @Override
    Compilable a() {
      return a;
    }

    @Override
    Compilable b() {
      return b;
    }

    @Override
    Boolean evaluate(final Set<Evaluable> visited) {
      if (dataType == null || a == null || b == null || !(dataType instanceof Evaluable) || !(a instanceof Evaluable) || !(b instanceof Evaluable))
        return null;

      final Temporal a = (Temporal)((Evaluable)this.a).evaluate(visited);
      final Temporal b = (Temporal)((Evaluable)this.b).evaluate(visited);
      final Temporal c = (Temporal)((Evaluable)this.dataType).evaluate(visited);
      return Temporals.compare(a, c) >= 0 && Temporals.compare(c, b) <= 0 == positive;
    }
  }

  static class TextualBetweenPredicate extends BetweenPredicate {
    final Compilable a;
    final Compilable b;

    TextualBetweenPredicate(final kind.Textual<?> dataType, final kind.Textual<?> a, final kind.Textual<?> b, final boolean positive) {
      super(dataType, positive);
      this.a = (Compilable)a;
      this.b = (Compilable)b;
    }

    @Override
    Compilable a() {
      return a;
    }

    @Override
    Compilable b() {
      return b;
    }

    @Override
    Boolean evaluate(final Set<Evaluable> visited) {
      if (dataType == null || a == null || b == null || !(dataType instanceof Evaluable) || !(a instanceof Evaluable) || !(b instanceof Evaluable))
        return null;

      final String a = (String)((Evaluable)this.a).evaluate(visited);
      final String b = (String)((Evaluable)this.b).evaluate(visited);
      final String c = (String)((Evaluable)this.dataType).evaluate(visited);
      return a.compareTo(c) >= 0 && c.compareTo(b) <= 0  == positive;
    }
  }

  private BetweenPredicates() {
  }
}