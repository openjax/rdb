/* Copyright (c) 2015 Seva Safris
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

package org.safris.dbb.ddlx;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.safris.dbb.ddlx.xe.$ddlx_column;
import org.safris.dbb.ddlx.xe.$ddlx_foreignKey;
import org.safris.dbb.ddlx.xe.$ddlx_integer;
import org.safris.dbb.ddlx.xe.$ddlx_named;
import org.safris.dbb.ddlx.xe.$ddlx_table;
import org.safris.dbb.vendor.DBVendor;
import org.safris.maven.common.Log;

public final class OracleSerializer extends Serializer {
  @Override
  protected DBVendor getVendor() {
    return DBVendor.ORACLE;
  }

  @Override
  protected void init(final Connection connection) throws SQLException {
  }

  @Override
  protected List<String> drops(final $ddlx_table table) {
    final List<String> statements = super.drops(table);
    if (table._column() != null) {
      for (final $ddlx_column column : table._column()) {
        if (column instanceof $ddlx_integer) {
          final $ddlx_integer type = ($ddlx_integer)column;
          if (!type._generateOnInsert$().isNull() && $ddlx_integer._generateOnInsert$.AUTO_5FINCREMENT.text().equals(type._generateOnInsert$().text())) {
            statements.add("BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE " + SQLDataTypes.getSequenceName(table, type) + "'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -2289 THEN RAISE; END IF; END;");
            statements.add("BEGIN EXECUTE IMMEDIATE 'DROP TRIGGER " + SQLDataTypes.getTriggerName(table, type) + "'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -4080 THEN RAISE; END IF; END;");
          }
        }
      }
    }

    return statements;
  }

  @Override
  protected String $null(final $ddlx_table table, final $ddlx_column column) {
    return !column._null$().isNull() ? !column._null$().text() ? "NOT NULL" : "NULL" : "";
  }

  @Override
  protected String $autoIncrement(final $ddlx_table table, final $ddlx_integer column) {
    return null;
  }

  @Override
  protected List<String> types(final $ddlx_table table) {
    final List<String> statements = new ArrayList<String>();
    if (table._column() != null) {
      for (final $ddlx_column column : table._column()) {
        if (column instanceof $ddlx_integer) {
          final $ddlx_integer type = ($ddlx_integer)column;
          if (!type._generateOnInsert$().isNull() && $ddlx_integer._generateOnInsert$.AUTO_5FINCREMENT.text().equals(type._generateOnInsert$().text())) {
            final String sequenceName = SQLDataTypes.getSequenceName(table, type);
            statements.add(0, "CREATE SEQUENCE " + sequenceName + " START WITH 1");
          }
        }
      }
    }

    statements.addAll(super.types(table));
    return statements;
  }

  @Override
  protected List<String> triggers(final $ddlx_table table) {
    final List<String> statements = new ArrayList<String>();
    if (table._column() != null) {
      for (final $ddlx_column column : table._column()) {
        if (column instanceof $ddlx_integer) {
          final $ddlx_integer type = ($ddlx_integer)column;
          if (!type._generateOnInsert$().isNull() && $ddlx_integer._generateOnInsert$.AUTO_5FINCREMENT.text().equals(type._generateOnInsert$().text())) {
            final String sequenceName = SQLDataTypes.getSequenceName(table, type);
            statements.add(0, "CREATE TRIGGER " + SQLDataTypes.getTriggerName(table, type) + " BEFORE INSERT ON " + table._name$().text() + " FOR EACH ROW when (new." + column._name$().text() + " IS NULL) BEGIN SELECT " + sequenceName + ".NEXTVAL INTO :new." + column._name$().text() + " FROM dual; END;");
          }
        }
      }
    }

    statements.addAll(super.types(table));
    return statements;
  }

  @Override
  protected String dropTableIfExists(final $ddlx_table table) {
    return "BEGIN EXECUTE IMMEDIATE 'DROP TABLE " + table._name$().text() + "'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;";
  }

  @Override
  protected String dropIndexOnClause(final $ddlx_table table) {
    return "";
  }

  @Override
  protected String createIndex(final boolean unique, final String indexName, final String type, final String tableName, final $ddlx_named ... columns) {
    return "CREATE " + (unique ? "UNIQUE " : "") + "INDEX " + indexName + " USING " + type + " ON " + tableName + " (" + SQLDataTypes.csvNames(columns) + ")";
  }

  private int foreignKeys = 0;
  private int primaryKeys = 0;
  private int checkConstraints = 0;

  @Override
  protected String check(final $ddlx_table table) {
    return "CONSTRAINT " + table._name$().text() + "_ck" + ++checkConstraints + " CHECK";
  }

  @Override
  protected String primaryKey(final $ddlx_table table) {
    return "CONSTRAINT " + table._name$().text() + "_pk" + ++primaryKeys + " PRIMARY KEY";
  }

  @Override
  protected String foreignKey(final $ddlx_table table) {
    return "CONSTRAINT " + table._name$().text() + "_fk" + ++foreignKeys + " FOREIGN KEY";
  }

  @Override
  protected String onUpdate(final $ddlx_foreignKey._onUpdate$ onUpdate) {
    Log.warn("ON UPDATE is not supported");
    return null;
  }
}