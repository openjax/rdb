/* Copyright (c) 2014 OpenJAX
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

package org.openjax.rdb.jsql;

import java.sql.Connection;
import java.sql.SQLException;

import org.fastjax.sql.exception.SQLExceptions;
import org.fastjax.sql.exception.SQLInvalidSchemaNameException;
import org.fastjax.util.ConcurrentHashSet;
import org.openjax.rdb.vendor.DBVendor;

public abstract class Schema {
  private static final ConcurrentHashSet<Class<? extends Schema>> inited = new ConcurrentHashSet<>();

  protected static DBVendor getDBVendor(final Connection connection) throws SQLException {
    if (connection == null)
      return null;

    try {
      final String url = connection.getMetaData().getURL();
      if (url.contains("jdbc:sqlite"))
        return DBVendor.SQLITE;

      if (url.contains("jdbc:derby"))
        return DBVendor.DERBY;

      if (url.contains("jdbc:mariadb"))
        return DBVendor.MARIA_DB;

      if (url.contains("jdbc:mysql"))
        return DBVendor.MY_SQL;

      if (url.contains("jdbc:oracle"))
        return DBVendor.ORACLE;

      if (url.contains("jdbc:postgresql"))
        return DBVendor.POSTGRE_SQL;
    }
    catch (final SQLException e) {
      throw SQLExceptions.getStrongType(e);
    }

    return null;
  }

  private static final Class<? extends Schema> NULL = Schema.class;

  protected static Connection getConnection(final Class<? extends Schema> schema, final String dataSourceId) throws SQLException {
    final Connector dataSource = Registry.getDataSource(schema, dataSourceId);
    if (dataSource == null)
      throw new SQLInvalidSchemaNameException("A " + Connector.class.getSimpleName() + " has not been registered for " + (schema == null ? null : schema.getName()) + ", id: " + dataSourceId);

    try {
      final Connection connection = dataSource.getConnection();
      final Class<? extends Schema> key = schema != null ? schema : NULL;
      if (!inited.contains(key)) {
        synchronized (key) {
          if (!inited.contains(key)) {
            Compiler.getCompiler(getDBVendor(connection)).onRegister(connection);
            inited.add(key);
          }
        }
      }

      return connection;
    }
    catch (final SQLException e) {
      throw SQLExceptions.getStrongType(e);
    }
  }
}