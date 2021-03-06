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

package org.jaxdb.jsql.generator;

import org.jaxdb.ddlx.DDLxAudit;
import org.jaxdb.www.ddlx_0_5.xLygluGCXAA;
import org.jaxdb.www.jsql_0_5.xLygluGCXAA.$Bigint;
import org.jaxdb.www.jsql_0_5.xLygluGCXAA.$Binary;
import org.jaxdb.www.jsql_0_5.xLygluGCXAA.$Blob;
import org.jaxdb.www.jsql_0_5.xLygluGCXAA.$Boolean;
import org.jaxdb.www.jsql_0_5.xLygluGCXAA.$Char;
import org.jaxdb.www.jsql_0_5.xLygluGCXAA.$Clob;
import org.jaxdb.www.jsql_0_5.xLygluGCXAA.$Date;
import org.jaxdb.www.jsql_0_5.xLygluGCXAA.$Datetime;
import org.jaxdb.www.jsql_0_5.xLygluGCXAA.$Decimal;
import org.jaxdb.www.jsql_0_5.xLygluGCXAA.$Enum;
import org.jaxdb.www.jsql_0_5.xLygluGCXAA.$Float;
import org.jaxdb.www.jsql_0_5.xLygluGCXAA.$Int;
import org.jaxdb.www.jsql_0_5.xLygluGCXAA.$Smallint;
import org.jaxdb.www.jsql_0_5.xLygluGCXAA.$Time;
import org.jaxdb.www.jsql_0_5.xLygluGCXAA.$Tinyint;

public class JSqlAudit extends DDLxAudit {
  JSqlAudit(final DDLxAudit copy) {
    super(copy);
  }

  public static boolean isKeyForUpdate(final xLygluGCXAA.$Column column) {
    if (column instanceof $Char)
      return (($Char)column).getJsqlKeyForUpdate$().text();

    if (column instanceof $Clob)
      return (($Clob)column).getJsqlKeyForUpdate$().text();

    if (column instanceof $Binary)
      return (($Binary)column).getJsqlKeyForUpdate$().text();

    if (column instanceof $Blob)
      return (($Blob)column).getJsqlKeyForUpdate$().text();

    if (column instanceof $Tinyint)
      return (($Tinyint)column).getJsqlKeyForUpdate$().text();

    if (column instanceof $Smallint)
      return (($Smallint)column).getJsqlKeyForUpdate$().text();

    if (column instanceof $Int)
      return (($Int)column).getJsqlKeyForUpdate$().text();

    if (column instanceof $Bigint)
      return (($Bigint)column).getJsqlKeyForUpdate$().text();

    if (column instanceof $Float)
      return (($Float)column).getJsqlKeyForUpdate$().text();

    if (column instanceof $Decimal)
      return (($Decimal)column).getJsqlKeyForUpdate$().text();

    if (column instanceof $Date)
      return (($Date)column).getJsqlKeyForUpdate$().text();

    if (column instanceof $Time)
      return (($Time)column).getJsqlKeyForUpdate$().text();

    if (column instanceof $Datetime)
      return (($Datetime)column).getJsqlKeyForUpdate$().text();

    if (column instanceof $Boolean)
      return (($Boolean)column).getJsqlKeyForUpdate$().text();

    if (column instanceof $Enum)
      return (($Enum)column).getJsqlKeyForUpdate$().text();

    throw new UnsupportedOperationException("Unsupported column type: " + column.getClass().getName());
  }
}