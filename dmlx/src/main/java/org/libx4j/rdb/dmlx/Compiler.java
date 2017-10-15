/* Copyright (c) 2017 lib4j
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

package org.libx4j.rdb.dmlx;

import java.lang.reflect.Modifier;
import java.util.Set;

import org.lib4j.lang.PackageLoader;
import org.libx4j.rdb.vendor.DBVendor;

abstract class Compiler {
  private static final Compiler[] compilers = new Compiler[DBVendor.values().length];

  static {
    try {
      final Set<Class<?>> classes = PackageLoader.getSystemContextPackageLoader().loadPackage(Compiler.class.getPackage());
      for (final Class<?> cls : classes) {
        if (Compiler.class.isAssignableFrom(cls) && !Modifier.isAbstract(cls.getModifiers())) {
          final Compiler compiler = (Compiler)cls.newInstance();
          compilers[compiler.getVendor().ordinal()] = compiler;
        }
      }
    }
    catch (final ReflectiveOperationException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  protected static Compiler getCompiler(final DBVendor vendor) {
    final Compiler compiler = compilers[vendor.ordinal()];
    if (compiler == null)
      throw new UnsupportedOperationException("Vendor " + vendor + " is not supported");

    return compiler;
  }

  protected abstract DBVendor getVendor();

  protected String compile(final sqlx.BIGINT value) {
    return value.toString();
  }

  protected String compile(final sqlx.BINARY value) {
    return "X'" + value + "'";
  }

  protected String compile(final sqlx.BLOB value) {
    return "X'" + value + "'";
  }

  protected String compile(final sqlx.BOOLEAN value) {
    return value.toString();
  }

  protected String compile(final sqlx.CHAR value) {
    return "'" + value.get().replace("'", "''") + "'";
  }

  protected String compile(final sqlx.CLOB value) {
    return "'" + value.get().replace("'", "''") + "'";
  }

  protected String compile(final sqlx.DATE value) {
    return "'" + value + "'";
  }

  protected String compile(final sqlx.DATETIME value) {
    return "'" + value + "'";
  }

  protected String compile(final sqlx.DECIMAL value) {
    return value.toString();
  }

  protected String compile(final sqlx.DOUBLE value) {
    return value.toString();
  }

  protected String compile(final sqlx.ENUM value) {
    return "'" + value + "'";
  }

  protected String compile(final sqlx.FLOAT value) {
    return value.toString();
  }

  protected String compile(final sqlx.INT value) {
    return value.toString();
  }

  protected String compile(final sqlx.SMALLINT value) {
    return value.toString();
  }

  protected String compile(final sqlx.TIME value) {
    return "'" + value + "'";
  }

  protected String compile(final sqlx.TINYINT value) {
    return value.toString();
  }
}