/* Copyright (c) 2017 OpenJAX
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

package org.openjax.rdb.sqlx;

import org.fastjax.util.Hexadecimal;
import org.openjax.rdb.ddlx.dt;
import org.openjax.rdb.vendor.DBVendor;

final class OracleCompiler extends Compiler {
  @Override
  protected DBVendor getVendor() {
    return DBVendor.ORACLE;
  }

  @Override
  protected String compile(final dt.BINARY value) {
    return "HEXTORAW('" + new Hexadecimal(value.get()) + "')";
  }

  @Override
  protected String compile(final dt.BLOB value) {
    return "HEXTORAW('" + new Hexadecimal(value.get()) + "')";
  }

  @Override
  protected String compile(final dt.BOOLEAN value) {
    return value.get() ? "1" : "0";
  }

  @Override
  protected String compile(final dt.CHAR value) {
    final String string = value.get().replace("'", "''");
    return string.length() == 0 || string.charAt(0) == ' ' ? "' " + string + "'" : "'" + string + "'";
  }

  @Override
  protected String compile(final dt.DATE value) {
    return "TO_DATE('" + value + "','YYYY-MM-DD')";
  }

  @Override
  protected String compile(final dt.DATETIME value) {
    return "TO_TIMESTAMP('" + value + "', 'YYYY-MM-DD HH24:MI:SS.FF')";
  }

  @Override
  protected String compile(final dt.TIME value) {
    return "'0 " + value + "'";
  }
}