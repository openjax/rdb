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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.libx4j.rdb.jsql.model.kind;

final class expression {
  protected static abstract class Generic<T> extends Subject<T> {
  }

  protected static final class Count extends Generic<Long> {
    protected static final Count STAR  = new Count();

    protected final boolean distinct;
    protected final java.lang.String function = "COUNT";
    protected final kind.DataType<?> column;

    protected Count(final kind.DataType<?> column, final boolean distinct) {
      this.column = column;
      this.distinct = distinct;
    }

    private Count() {
      this.distinct = false;
      this.column = null;
    }

    @Override
    protected Object evaluate(final java.util.Set<Evaluable> visited) {
      throw new UnsupportedOperationException("COUNT(?) cannot be evaluated outside the DB");
    }

    @Override
    protected final void compile(final Compilation compilation) throws IOException {
      Compiler.getCompiler(compilation.vendor).compile(this, compilation);
    }
  }

  protected static final class Numeric extends Generic<Number> {
    protected final operator.Arithmetic operator;
    protected final kind.Numeric<?> a;
    protected final kind.Numeric<?> b;

    protected Numeric(final operator.Arithmetic operator, final kind.Numeric<?> a, final kind.Numeric<?> b) {
      this.operator = operator;
      this.a = a;
      this.b = b;
    }

    protected Numeric(final operator.Arithmetic operator, final Number a, final kind.Numeric<?> b) {
      this.operator = operator;
      this.a = (type.Numeric<?>)type.DataType.wrap(a);
      this.b = b;
    }

    protected Numeric(final operator.Arithmetic operator, final kind.Numeric<?> a, final Number b) {
      this.operator = operator;
      this.a = a;
      this.b = (type.Numeric<?>)type.DataType.wrap(b);
    }

    @Override
    protected void compile(final Compilation compilation) throws IOException {
      Compiler.getCompiler(compilation.vendor).compile(this, compilation);
    }

    @Override
    protected Number evaluate(final java.util.Set<Evaluable> visited) {
      if (!(this.a instanceof Evaluable) || !(this.b instanceof Evaluable))
        return null;

      final Number a = (Number)((Evaluable)this.a).evaluate(visited);
      if (a == null)
        return null;

      final Number b = (Number)((Evaluable)this.b).evaluate(visited);
      if (b == null)
        return null;

      return operator.evaluate(a, b);
    }
  }

  protected static class Temporal extends Generic<java.time.temporal.Temporal> {
    protected final operator.ArithmeticPlusMinus operator;
    protected final type.Temporal<?> a;
    protected final Interval b;

    protected Temporal(final operator.ArithmeticPlusMinus operator, final type.Temporal<?> a, final Interval b) {
      this.operator = operator;
      this.a = a;
      this.b = b;
    }

    @Override
    protected java.time.temporal.Temporal evaluate(final java.util.Set<Evaluable> visited) {
      if (a == null || b == null)
        return null;

      final java.time.temporal.Temporal temp = a.evaluate(visited);
      if (temp == null)
        return null;

      if (temp instanceof LocalDateTime)
        return operator.evaluate((LocalDateTime)temp, b);

      if (temp instanceof LocalDate)
        return operator.evaluate((LocalDate)temp, b);

      if (temp instanceof LocalTime)
        return operator.evaluate((LocalTime)temp, b);

      throw new UnsupportedOperationException(b.getClass().getName() + " is not a supported Temporal type");
    }

    @Override
    protected final void compile(final Compilation compilation) throws IOException {
      final Interval interval = a instanceof type.TIME ? b.toTimeInterval() : a instanceof type.DATE ? b.toDateInterval() : b;
      if (interval == null)
        Compiler.getCompiler(compilation.vendor).compile(a, compilation);
      else
        Compiler.getCompiler(compilation.vendor).compile(this, compilation);
    }
  }

  protected static final class String extends Generic<java.lang.String> {
    protected final operator.String operator;
    protected final kind.DataType<?>[] args;

    protected String(final operator.String operator, final kind.DataType<?> a, final kind.DataType<?> b) {
      this.operator = operator;
      this.args = new kind.DataType[] {a, b};
    }

    protected String(final operator.String operator, final kind.DataType<?> a, final kind.DataType<?> b, final CharSequence c) {
      this.operator = operator;
      this.args = new kind.DataType[] {a, b, type.DataType.wrap(c)};
    }

    protected String(final operator.String operator, final kind.DataType<?> a, final CharSequence b) {
      this.operator = operator;
      this.args = new kind.DataType[] {a, type.DataType.wrap(b)};
    }

    protected String(final operator.String operator, final kind.DataType<?> a, final CharSequence b, final kind.DataType<?> c) {
      this.operator = operator;
      this.args = new kind.DataType[] {a, type.DataType.wrap(b), c};
    }

    protected String(final operator.String operator, final kind.DataType<?> a, final CharSequence b, final kind.DataType<?> c, final CharSequence d) {
      this.operator = operator;
      this.args = new kind.DataType[] {a, type.DataType.wrap(b), c, type.DataType.wrap(d)};
    }

    protected String(final operator.String operator, final CharSequence a, final kind.DataType<?> b) {
      this.operator = operator;
      this.args = new kind.DataType[] {type.DataType.wrap(a), b};
    }

    protected String(final operator.String operator, final CharSequence a, final kind.DataType<?> b, final kind.DataType<?> c) {
      this.operator = operator;
      this.args = new kind.DataType[] {type.DataType.wrap(a), b, c};
    }

    protected String(final operator.String operator, final CharSequence a, final kind.DataType<?> b, final CharSequence c) {
      this.operator = operator;
      this.args = new kind.DataType[] {type.DataType.wrap(a), b, type.DataType.wrap(c)};
    }

    protected String(final operator.String operator, final CharSequence a, final kind.DataType<?> b, final kind.DataType<?> c, final CharSequence d) {
      this.operator = operator;
      this.args = new kind.DataType[] {type.DataType.wrap(a), b, c, type.DataType.wrap(d)};
    }

    protected String(final operator.String operator, final CharSequence a, final kind.DataType<?> b, final CharSequence c, final kind.DataType<?> d) {
      this.operator = operator;
      this.args = new kind.DataType[] {type.DataType.wrap(a), b, type.DataType.wrap(c), d};
    }

    protected String(final operator.String operator, final CharSequence a, final kind.DataType<?> b, final CharSequence c, final kind.DataType<?> d, final CharSequence e) {
      this.operator = operator;
      this.args = new kind.DataType[] {type.DataType.wrap(a), b, type.DataType.wrap(c), d, type.DataType.wrap(e)};
    }

    @Override
    protected final void compile(final Compilation compilation) throws IOException {
      Compiler.getCompiler(compilation.vendor).compile(this, compilation);
    }

    @Override
    protected java.lang.String evaluate(final java.util.Set<Evaluable> visited) {
      final StringBuilder builder = new StringBuilder();
      for (final kind.DataType<?> arg : args) {
        if (arg == null || !(arg instanceof Evaluable))
          return null;

        builder.append(((Evaluable)arg).evaluate(visited));
      }

      return builder.toString();
    }
  }

  protected static final class Set extends Generic<Object> {
    protected final java.lang.String function;
    protected final boolean distinct;
    protected final Subject<?> a;

    protected Set(final java.lang.String function, final Subject<?> subject, final boolean distinct) {
      this.function = function;
      this.a = subject;
      this.distinct = distinct;
    }

    @Override
    protected final void compile(final Compilation compilation) throws IOException {
      Compiler.getCompiler(compilation.vendor).compile(this, compilation);
    }

    @Override
    protected Object evaluate(final java.util.Set<Evaluable> visited) {
      throw new UnsupportedOperationException("SetFunction cannot be evaluated outside the DB");
    }
  }

  private expression() {
  }
}