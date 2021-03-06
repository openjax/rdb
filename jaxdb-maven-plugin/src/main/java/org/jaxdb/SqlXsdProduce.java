/* Copyright (c) 2019 JAX-DB
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

package org.jaxdb;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.jaxdb.sqlx.SQL;
import org.libj.jci.CompilationException;

abstract class SqlXsdProduce extends Produce<SqlXsdMojo.Configuration> {
  private static int index;
  static final SqlXsdProduce[] values = new SqlXsdProduce[2];

  private SqlXsdProduce(final String name) {
    super(name, values, index++);
  }

  static final SqlXsdProduce JAXB = new SqlXsdProduce("jaxb") {
    @Override
    void execute(final SqlXsdMojo.Configuration configuration, final SqlMojo<?,?> sqlMojo) throws CompilationException, IOException, JAXBException {
      SQL.xsd2jaxb(configuration.getDestDir(), configuration.getXsds());
    }
  };

  static final SqlXsdProduce JAXSB = new SqlXsdProduce("jaxsb") {
    @Override
    void execute(final SqlXsdMojo.Configuration configuration, final SqlMojo<?,?> sqlMojo) throws IOException {
      SQL.xsd2xsb(configuration.getDestDir(), configuration.getXsds());
    }
  };
}