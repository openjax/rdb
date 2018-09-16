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

package org.openjax.rdb.ddlx.runner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.jar.JarFile;

import org.fastjax.sql.ConnectionProxy;
import org.openjax.rdb.vendor.DBVendor;
import org.fastjax.io.Files;
import org.fastjax.io.JarFiles;
import org.fastjax.net.URLs;

public class SQLite implements Vendor {
  private static final File db = new File("target/generated-test-resources/rdb/sqlite.db");

  @Override
  public DBVendor getDBVendor() {
    return DBVendor.SQLITE;
  }

  @Override
  public synchronized void init() throws IOException, SQLException {
    final File classes = new File("target/classes/sqlite.db");
    if (classes.exists() && !Files.deleteAll(classes.toPath()))
      throw new IOException("Unable to delete " + db.getPath());

    final File testClasses = new File("target/test-classes/sqlite.db");
    if (testClasses.exists() && !Files.deleteAll(testClasses.toPath()))
      throw new IOException("Unable to delete " + db.getPath());

    if (db.exists()) {
      if (db.length() != 0)
        return;

      if (!db.delete())
        throw new IOException("Unable to delete " + db.getPath());
    }

    final URL url = Thread.currentThread().getContextClassLoader().getResource("sqlite.db");
    if (url != null) {
      db.getParentFile().mkdirs();
      if (URLs.isJar(url)) {
        final JarFile jarFile = new JarFile(URLs.getParentJar(url).getPath());
        final String path = URLs.getPathInJar(url);
        JarFiles.extract(jarFile, path, db.getParentFile());
      }
      else {
        Files.copy(new File(url.getPath()), db);
      }
    }
  }

  @Override
  public Connection getConnection() throws IOException, SQLException {
    return new ConnectionProxy(DriverManager.getConnection("jdbc:sqlite:" + db.getPath()));
  }

  @Override
  public void destroy() throws SQLException {
  }
}