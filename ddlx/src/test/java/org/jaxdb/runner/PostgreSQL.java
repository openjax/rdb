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

package org.jaxdb.runner;

import java.sql.SQLException;

import org.jaxdb.vendor.DBVendor;

//  CREATE USER jaxdb WITH PASSWORD 'jaxdb';
//  CREATE DATABASE jaxdb;
//  GRANT ALL PRIVILEGES ON DATABASE jaxdb TO jaxdb;
public class PostgreSQL extends Vendor {
  public PostgreSQL() {
    this("jdbc:postgresql://localhost:15432/jaxdb?user=jaxdb&password=jaxdb");
  }

  public PostgreSQL(final String url) {
    super(url);
  }

  @Override
  public void destroy() throws SQLException {
  }

  @Override
  public DBVendor getDBVendor() {
    return DBVendor.POSTGRE_SQL;
  }
}