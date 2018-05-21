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

package org.libx4j.rdb.sqlx;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.lib4j.jci.CompilationException;
import org.lib4j.test.MixedTest;
import org.lib4j.xml.ValidationException;
import org.libx4j.rdb.ddlx.DDLxTest;
import org.libx4j.rdb.ddlx.GeneratorExecutionException;
import org.libx4j.rdb.ddlx.runner.Derby;
import org.libx4j.rdb.ddlx.runner.MySQL;
import org.libx4j.rdb.ddlx.runner.Oracle;
import org.libx4j.rdb.ddlx.runner.PostgreSQL;
import org.libx4j.rdb.ddlx.runner.SQLite;
import org.libx4j.rdb.ddlx.runner.VendorRunner;
import org.xml.sax.SAXException;

@RunWith(VendorRunner.class)
@VendorRunner.Test({Derby.class, SQLite.class})
@VendorRunner.Integration({MySQL.class, PostgreSQL.class, Oracle.class})
@Category(MixedTest.class)
public class ClassicModelsTest extends SQLxTest {
  private static final String name = "classicmodels";

  static {
    try {
      createXSDs(name);
    }
    catch (final CompilationException | IOException | JAXBException | TransformerException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  @Test
  public void testLoadData(final Connection connection) throws GeneratorExecutionException, IOException, SQLException, ValidationException {
    DDLxTest.recreateSchema(connection, name);
    Assert.assertEquals(3864, loadData(connection, name).length);
  }

  @Test
  public void testSql(final Connection connection) throws IOException, SAXException, SQLException {
    createSql(connection, name);
  }
}