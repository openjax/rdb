/* Copyright (c) 2015 OpenJAX
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

package org.openjax.rdb.ddlx;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fastjax.lang.PackageLoader;
import org.fastjax.lang.PackageNotFoundException;
import org.fastjax.util.Numbers;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$Bigint;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$Binary;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$Blob;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$Boolean;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$ChangeRule;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$Char;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$Check;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$Clob;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$Column;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$Columns;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$Constraints;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$Date;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$Datetime;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$Decimal;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$Double;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$Enum;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$Float;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$ForeignKey;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$Index;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$Int;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$Integer;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$Named;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$Smallint;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$Table;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$Time;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.$Tinyint;
import org.openjax.rdb.ddlx_0_9_9.xL0gluGCXYYJc.Schema;
import org.openjax.rdb.vendor.DBVendor;

abstract class Compiler {
  private static final Compiler[] compilers = new Compiler[DBVendor.values().length];

  static {
    try {
      final Set<Class<?>> classes = PackageLoader.getContextPackageLoader().loadPackage(Compiler.class.getPackage());
      for (final Class<?> cls : classes) {
        if (Compiler.class.isAssignableFrom(cls) && !Modifier.isAbstract(cls.getModifiers())) {
          final Compiler compiler = (Compiler)cls.getDeclaredConstructor().newInstance();
          compilers[compiler.getVendor().ordinal()] = compiler;
        }
      }
    }
    catch (final IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException | PackageNotFoundException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  protected static Compiler getCompiler(final DBVendor vendor) {
    final Compiler compiler = compilers[vendor.ordinal()];
    if (compiler == null)
      throw new UnsupportedOperationException("Vendor " + vendor + " is not supported");

    return compiler;
  }

  protected static boolean isAutoIncrement(final $Integer column) {
    return column.getGenerateOnInsert$() != null && $Integer.GenerateOnInsert$.AUTO_5FINCREMENT.text().equals(column.getGenerateOnInsert$().text());
  }

  protected abstract DBVendor getVendor();

  protected abstract CreateStatement createIndex(final boolean unique, final String indexName, final $Index.Type$ type, final String tableName, final $Named ... columns);

  protected abstract void init(final Connection connection) throws SQLException;

  protected final String q(final String name) {
    return getVendor().getDialect().quoteIdentifier(name);
  }

  protected CreateStatement createSchemaIfNotExists(final Schema schema) {
    return null;
  }

  protected CreateStatement createTableIfNotExists(final $Table table, final Map<String,$Column> columnNameToColumn) throws GeneratorExecutionException {
    final StringBuilder builder = new StringBuilder();
    final String tableName = table.getName$().text();
    builder.append("CREATE TABLE ").append(q(tableName)).append(" (\n");
    if (table.getColumn() != null)
      builder.append(createColumns(table));

    final CreateStatement constraints = createConstraints(columnNameToColumn, table);
    if (constraints != null)
      builder.append(constraints);

    builder.append("\n)");
    return new CreateStatement(builder.toString());
  }

  private String createColumns(final $Table table) {
    final StringBuilder ddl = new StringBuilder();
    for (final $Column column : table.getColumn())
      ddl.append(",\n  ").append(createColumn(table, column));

    return ddl.substring(2);
  }

  private CreateStatement createColumn(final $Table table, final $Column column) {
    final StringBuilder ddl = new StringBuilder();
    ddl.append(q(column.getName$().text())).append(' ');
    if (column instanceof $Char) {
      final $Char type = ($Char)column;
      ddl.append(getVendor().getDialect().compileChar(type.getVarying$().text(), type.getLength$() == null ? null : type.getLength$().text()));
    }
    else if (column instanceof $Binary) {
      final $Binary type = ($Binary)column;
      ddl.append(getVendor().getDialect().compileBinary(type.getVarying$().text(), type.getLength$() == null ? null : type.getLength$().text()));
    }
    else if (column instanceof $Blob) {
      final $Blob type = ($Blob)column;
      ddl.append(getVendor().getDialect().compileBlob(type.getLength$() == null ? null : type.getLength$().text()));
    }
    else if (column instanceof $Clob) {
      final $Clob type = ($Clob)column;
      ddl.append(getVendor().getDialect().compileClob(type.getLength$() == null ? null : type.getLength$().text()));
    }
    else if (column instanceof $Integer) {
      ddl.append(createIntegerColumn(($Integer)column));
    }
    else if (column instanceof $Float) {
      final $Float type = ($Float)column;
      ddl.append(getVendor().getDialect().declareFloat(type.getUnsigned$().text()));
    }
    else if (column instanceof $Double) {
      final $Double type = ($Double)column;
      ddl.append(getVendor().getDialect().declareDouble(type.getUnsigned$().text()));
    }
    else if (column instanceof $Decimal) {
      final $Decimal type = ($Decimal)column;
      ddl.append(getVendor().getDialect().declareDecimal(type.getPrecision$() == null ? null : type.getPrecision$().text(), type.getScale$() == null ? null : type.getScale$().text(), type.getUnsigned$().text()));
    }
    else if (column instanceof $Date) {
      ddl.append(getVendor().getDialect().declareDate());
    }
    else if (column instanceof $Time) {
      final $Time type = ($Time)column;
      ddl.append(getVendor().getDialect().declareTime(type.getPrecision$() == null ? null : type.getPrecision$().text()));
    }
    else if (column instanceof $Datetime) {
      final $Datetime type = ($Datetime)column;
      ddl.append(getVendor().getDialect().declareDateTime(type.getPrecision$() == null ? null : type.getPrecision$().text()));
    }
    else if (column instanceof $Boolean) {
      ddl.append(getVendor().getDialect().declareBoolean());
    }
    else if (column instanceof $Enum) {
      ddl.append(getVendor().getDialect().declareEnum(($Enum)column));
    }

    final String defaultFragement = $default(table, column);
    if (defaultFragement != null && defaultFragement.length() > 0)
      ddl.append(" DEFAULT ").append(defaultFragement);

    final String nullFragment = $null(table, column);
    if (nullFragment != null && nullFragment.length() > 0)
      ddl.append(' ').append(nullFragment);

    if (column instanceof $Integer) {
      final String autoIncrementFragment = $autoIncrement(table, ($Integer)column);
      if (autoIncrementFragment != null && autoIncrementFragment.length() > 0)
        ddl.append(' ').append(autoIncrementFragment);
    }

    return new CreateStatement(ddl.toString());
  }

  protected String createIntegerColumn(final $Integer column) {
    if (column instanceof $Tinyint) {
      final $Tinyint type = ($Tinyint)column;
      return getVendor().getDialect().compileInt8(type.getPrecision$() == null ? null : type.getPrecision$().text(), type.getUnsigned$().text());
    }

    if (column instanceof $Smallint) {
      final $Smallint type = ($Smallint)column;
      return getVendor().getDialect().compileInt16(type.getPrecision$() == null ? null : type.getPrecision$().text(), type.getUnsigned$().text());
    }

    if (column instanceof $Int) {
      final $Int type = ($Int)column;
      return getVendor().getDialect().compileInt32(type.getPrecision$() == null ? null : type.getPrecision$().text(), type.getUnsigned$().text());
    }

    if (column instanceof $Bigint) {
      final $Bigint type = ($Bigint)column;
      return getVendor().getDialect().compileInt64(type.getPrecision$() == null ? null : type.getPrecision$().text(), type.getUnsigned$().text());
    }

    throw new UnsupportedOperationException("Unsupported type: " + column.getClass().getName());
  }

  private CreateStatement createConstraints(final Map<String,$Column> columnNameToColumn, final $Table table) throws GeneratorExecutionException {
    final StringBuffer contraintsBuffer = new StringBuffer();
    if (table.getConstraints() != null) {
      final $Constraints constraints = table.getConstraints();

      // unique constraint
      final List<$Columns> uniques = constraints.getUnique();
      if (uniques != null) {
        StringBuilder uniqueString = new StringBuilder();
        int uniqueIndex = 1;
        for (final $Columns unique : uniques) {
          final List<$Named> columns = unique.getColumn();
          StringBuilder columnsString = new StringBuilder();
          for (final $Named column : columns)
            columnsString.append(", ").append(q(column.getName$().text()));

          uniqueString.append(",\n  CONSTRAINT ").append(q(table.getName$().text() + "_unique_" + uniqueIndex++)).append(" UNIQUE (").append(columnsString.substring(2)).append(')');
        }

        contraintsBuffer.append(uniqueString);
      }

      // check constraint
      final List<$Check> checks = constraints.getCheck();
      if (checks != null) {
        String checkString = "";
        for (final $Check check : checks) {
          final String checkClause = recurseCheckRule(check);
          checkString += ",\n  CHECK " + (checkClause.startsWith("(") ? checkClause : "(" + checkClause + ")");
        }

        contraintsBuffer.append(checkString);
      }

      // primary key constraint
      final String primaryKeyConstraint = blockPrimaryKey(table, constraints, columnNameToColumn);
      if (primaryKeyConstraint != null)
        contraintsBuffer.append(primaryKeyConstraint);
    }

    if (table.getColumn() != null) {
      for (final $Column column : table.getColumn()) {
        if (column.getForeignKey() != null) {
          final $ForeignKey foreignKey = column.getForeignKey();
          contraintsBuffer.append(",\n  ").append(foreignKey(table)).append(" (").append(q(column.getName$().text()));
          contraintsBuffer.append(") REFERENCES ").append(q(foreignKey.getReferences$().text()));
          contraintsBuffer.append(" (").append(q(foreignKey.getColumn$().text())).append(')');
          if (foreignKey.getOnDelete$() != null) {
            final String onDelete = onDelete(foreignKey.getOnDelete$());
            if (onDelete != null)
              contraintsBuffer.append(' ').append(onDelete);
          }

          if (foreignKey.getOnUpdate$() != null) {
            final String onUpdate = onUpdate(foreignKey.getOnUpdate$());
            if (onUpdate != null)
              contraintsBuffer.append(' ').append(onUpdate);
          }
        }
      }

      // Parse the min & max constraints of numeric types
      for (final $Column column : table.getColumn()) {
        String minCheck = null;
        String maxCheck = null;
        if (column instanceof $Integer) {
          if (column instanceof $Tinyint) {
            final $Tinyint type = ($Tinyint)column;
            minCheck = type.getMin$() != null ? String.valueOf(type.getMin$().text()) : null;
            maxCheck = type.getMax$() != null ? String.valueOf(type.getMax$().text()) : null;
          }
          else if (column instanceof $Smallint) {
            final $Smallint type = ($Smallint)column;
            minCheck = type.getMin$() != null ? String.valueOf(type.getMin$().text()) : null;
            maxCheck = type.getMax$() != null ? String.valueOf(type.getMax$().text()) : null;
          }
          else if (column instanceof $Int) {
            final $Int type = ($Int)column;
            minCheck = type.getMin$() != null ? String.valueOf(type.getMin$().text()) : null;
            maxCheck = type.getMax$() != null ? String.valueOf(type.getMax$().text()) : null;
          }
          else if (column instanceof $Bigint) {
            final $Bigint type = ($Bigint)column;
            minCheck = type.getMin$() != null ? String.valueOf(type.getMin$().text()) : null;
            maxCheck = type.getMax$() != null ? String.valueOf(type.getMax$().text()) : null;
          }
          else {
            throw new UnsupportedOperationException("Unsupported type: " + column.getClass().getName());
          }
        }
        else if (column instanceof $Float) {
          final $Float type = ($Float)column;
          minCheck = type.getMin$() != null ? String.valueOf(type.getMin$().text()) : null;
          maxCheck = type.getMax$() != null ? String.valueOf(type.getMax$().text()) : null;
        }
        else if (column instanceof $Double) {
          final $Double type = ($Double)column;
          minCheck = type.getMin$() != null ? String.valueOf(type.getMin$().text()) : null;
          maxCheck = type.getMax$() != null ? String.valueOf(type.getMax$().text()) : null;
        }
        else if (column instanceof $Decimal) {
          final $Decimal type = ($Decimal)column;
          minCheck = type.getMin$() != null ? String.valueOf(type.getMin$().text()) : null;
          maxCheck = type.getMax$() != null ? String.valueOf(type.getMax$().text()) : null;
        }

        if (minCheck != null)
          minCheck = q(column.getName$().text()) + " >= " + minCheck;

        if (maxCheck != null)
          maxCheck = q(column.getName$().text()) + " <= " + maxCheck;

        if (minCheck != null) {
          if (maxCheck != null)
            contraintsBuffer.append(",\n  ").append(check(table)).append(" (" + minCheck + " AND " + maxCheck + ")");
          else
            contraintsBuffer.append(",\n  ").append(check(table)).append(" (" + minCheck + ")");
        }
        else if (maxCheck != null) {
          contraintsBuffer.append(",\n  ").append(check(table)).append(" (" + maxCheck + ")");
        }
      }

      // parse the <check/> element per type
      for (final $Column column : table.getColumn()) {
        String operator = null;
        String condition = null;
        if (column instanceof $Char) {
          final $Char type = ($Char)column;
          if (type.getCheck() != null) {
            operator = $Char.Check.Operator$.eq.text().equals(type.getCheck().getOperator$().text()) ? "=" : $Char.Check.Operator$.ne.text().equals(type.getCheck().getOperator$().text()) ? "!=" : null;
            condition = "'" + type.getCheck().getCondition$().text() + "'";
          }
        }
        else if (column instanceof $Tinyint) {
          final $Tinyint type = ($Tinyint)column;
          if (type.getCheck() != null) {
            operator = $Tinyint.Check.Operator$.eq.text().equals(type.getCheck().getOperator$().text()) ? "=" : $Tinyint.Check.Operator$.ne.text().equals(type.getCheck().getOperator$().text()) ? "!=" : $Tinyint.Check.Operator$.gt.text().equals(type.getCheck().getOperator$().text()) ? ">" : $Tinyint.Check.Operator$.gte.text().equals(type.getCheck().getOperator$().text()) ? ">=" : $Tinyint.Check.Operator$.lt.text().equals(type.getCheck().getOperator$().text()) ? "<" : $Tinyint.Check.Operator$.lte.text().equals(type.getCheck().getOperator$().text()) ? "<=" : null;
            condition = String.valueOf(type.getCheck().getCondition$().text());
          }
        }
        else if (column instanceof $Smallint) {
          final $Smallint type = ($Smallint)column;
          if (type.getCheck() != null) {
            operator = $Smallint.Check.Operator$.eq.text().equals(type.getCheck().getOperator$().text()) ? "=" : $Smallint.Check.Operator$.ne.text().equals(type.getCheck().getOperator$().text()) ? "!=" : $Smallint.Check.Operator$.gt.text().equals(type.getCheck().getOperator$().text()) ? ">" : $Smallint.Check.Operator$.gte.text().equals(type.getCheck().getOperator$().text()) ? ">=" : $Smallint.Check.Operator$.lt.text().equals(type.getCheck().getOperator$().text()) ? "<" : $Smallint.Check.Operator$.lte.text().equals(type.getCheck().getOperator$().text()) ? "<=" : null;
            condition = String.valueOf(type.getCheck().getCondition$().text());
          }
        }
        else if (column instanceof $Int) {
          final $Int type = ($Int)column;
          if (type.getCheck() != null) {
            operator = $Int.Check.Operator$.eq.text().equals(type.getCheck().getOperator$().text()) ? "=" : $Int.Check.Operator$.ne.text().equals(type.getCheck().getOperator$().text()) ? "!=" : $Int.Check.Operator$.gt.text().equals(type.getCheck().getOperator$().text()) ? ">" : $Int.Check.Operator$.gte.text().equals(type.getCheck().getOperator$().text()) ? ">=" : $Int.Check.Operator$.lt.text().equals(type.getCheck().getOperator$().text()) ? "<" : $Int.Check.Operator$.lte.text().equals(type.getCheck().getOperator$().text()) ? "<=" : null;
            condition = String.valueOf(type.getCheck().getCondition$().text());
          }
        }
        else if (column instanceof $Bigint) {
          final $Bigint type = ($Bigint)column;
          if (type.getCheck() != null) {
            operator = $Bigint.Check.Operator$.eq.text().equals(type.getCheck().getOperator$().text()) ? "=" : $Bigint.Check.Operator$.ne.text().equals(type.getCheck().getOperator$().text()) ? "!=" : $Bigint.Check.Operator$.gt.text().equals(type.getCheck().getOperator$().text()) ? ">" : $Bigint.Check.Operator$.gte.text().equals(type.getCheck().getOperator$().text()) ? ">=" : $Bigint.Check.Operator$.lt.text().equals(type.getCheck().getOperator$().text()) ? "<" : $Bigint.Check.Operator$.lte.text().equals(type.getCheck().getOperator$().text()) ? "<=" : null;
            condition = String.valueOf(type.getCheck().getCondition$().text());
          }
        }
        else if (column instanceof $Float) {
          final $Float type = ($Float)column;
          if (type.getCheck() != null) {
            operator = $Float.Check.Operator$.eq.text().equals(type.getCheck().getOperator$().text()) ? "=" : $Float.Check.Operator$.ne.text().equals(type.getCheck().getOperator$().text()) ? "!=" : $Float.Check.Operator$.gt.text().equals(type.getCheck().getOperator$().text()) ? ">" : $Float.Check.Operator$.gte.text().equals(type.getCheck().getOperator$().text()) ? ">=" : $Float.Check.Operator$.lt.text().equals(type.getCheck().getOperator$().text()) ? "<" : $Float.Check.Operator$.lte.text().equals(type.getCheck().getOperator$().text()) ? "<=" : null;
            condition = String.valueOf(type.getCheck().getCondition$().text());
          }
        }
        else if (column instanceof $Double) {
          final $Double type = ($Double)column;
          if (type.getCheck() != null) {
            operator = $Double.Check.Operator$.eq.text().equals(type.getCheck().getOperator$().text()) ? "=" : $Double.Check.Operator$.ne.text().equals(type.getCheck().getOperator$().text()) ? "!=" : $Double.Check.Operator$.gt.text().equals(type.getCheck().getOperator$().text()) ? ">" : $Double.Check.Operator$.gte.text().equals(type.getCheck().getOperator$().text()) ? ">=" : $Double.Check.Operator$.lt.text().equals(type.getCheck().getOperator$().text()) ? "<" : $Double.Check.Operator$.lte.text().equals(type.getCheck().getOperator$().text()) ? "<=" : null;
            condition = String.valueOf(type.getCheck().getCondition$().text());
          }
        }
        else if (column instanceof $Decimal) {
          final $Decimal type = ($Decimal)column;
          if (type.getCheck() != null) {
            operator = $Decimal.Check.Operator$.eq.text().equals(type.getCheck().getOperator$().text()) ? "=" : $Decimal.Check.Operator$.ne.text().equals(type.getCheck().getOperator$().text()) ? "!=" : $Decimal.Check.Operator$.gt.text().equals(type.getCheck().getOperator$().text()) ? ">" : $Decimal.Check.Operator$.gte.text().equals(type.getCheck().getOperator$().text()) ? ">=" : $Decimal.Check.Operator$.lt.text().equals(type.getCheck().getOperator$().text()) ? "<" : $Decimal.Check.Operator$.lte.text().equals(type.getCheck().getOperator$().text()) ? "<=" : null;
            condition = String.valueOf(type.getCheck().getCondition$().text());
          }
        }

        if (operator != null) {
          if (condition != null)
            contraintsBuffer.append(",\n  ").append(check(table)).append(" (" + q(column.getName$().text()) + " " + operator + " " + condition + ")");
          else
            throw new UnsupportedOperationException("Unsupported 'null' condition encountered on column '" + column.getName$().text());
        }
        else if (condition != null)
          throw new UnsupportedOperationException("Unsupported 'null' operator encountered on column '" + column.getName$().text());
      }
    }

    return new CreateStatement(contraintsBuffer.toString());
  }

  protected String check(final $Table table) {
    return "CHECK";
  }

  protected String blockPrimaryKey(final $Table table, final $Constraints constraints, final Map<String,$Column> columnNameToColumn) throws GeneratorExecutionException {
    if (constraints.getPrimaryKey() == null)
      return "";

    final StringBuffer primaryKeyBuffer = new StringBuffer();
    for (final $Named primaryColumn : constraints.getPrimaryKey().getColumn()) {
      final String primaryKeyColumn = primaryColumn.getName$().text();
      final $Column column = columnNameToColumn.get(primaryKeyColumn);
      if (column.getNull$().text())
        throw new GeneratorExecutionException("Column " + column.getName$() + " must be NOT NULL to be a PRIMARY KEY.");

      primaryKeyBuffer.append(", ").append(q(primaryKeyColumn));
    }

    return ",\n  " + primaryKey(table) + " (" + primaryKeyBuffer.substring(2) + ")";
  }

  protected String foreignKey(final $Table table) {
    return "FOREIGN KEY";
  }

  protected String primaryKey(final $Table table) {
    return "PRIMARY KEY";
  }

  protected String onDelete(final $ForeignKey.OnDelete$ onDelete) {
    return "ON DELETE " + changeRule(onDelete);
  }

  protected String onUpdate(final $ForeignKey.OnUpdate$ onUpdate) {
    return "ON UPDATE " + changeRule(onUpdate);
  }

  protected String changeRule(final $ChangeRule changeRule) {
    return changeRule.text();
  }

  private static String recurseCheckRule(final $Check check) {
    final String condition;
    if (check.getColumn().size() == 2)
      condition = check.getColumn(0).text();
    else if (check.getValue() != null)
      condition = Numbers.isNumber(check.getValue().text()) ? Numbers.roundInsignificant(check.getValue().text()) : "'" + check.getValue().text() + "'";
    else
      throw new UnsupportedOperationException("Unsupported condition on column '" + check.getColumn(0).text() + "'");

    final String clause = check.getColumn(0).text() + " " + check.getOperator().text() + " " + condition;
    if (check.getAnd() != null)
      return "(" + clause + " AND " + recurseCheckRule(check.getAnd()) + ")";

    if (check.getOr() != null)
      return "(" + clause + " OR " + recurseCheckRule(check.getOr()) + ")";

    return clause;
  }

  protected List<CreateStatement> triggers(final $Table table) {
    return new ArrayList<>();
  }

  protected List<CreateStatement> indexes(final $Table table) {
    final List<CreateStatement> statements = new ArrayList<>();
    if (table.getIndexes() != null) {
      for (final $Table.Indexes.Index index : table.getIndexes().getIndex()) {
        final CreateStatement createIndex = createIndex(index.getUnique$() != null && index.getUnique$().text(), SQLDataTypes.getIndexName(table, index), index.getType$(), table.getName$().text(), index.getColumn().toArray(new $Named[index.getColumn().size()]));
        if (createIndex != null)
          statements.add(createIndex);
      }
    }

    if (table.getColumn() != null) {
      for (final $Column column : table.getColumn()) {
        if (column.getIndex() != null) {
          final CreateStatement createIndex = createIndex(column.getIndex().getUnique$() != null && column.getIndex().getUnique$().text(), SQLDataTypes.getIndexName(table, column.getIndex(), column), column.getIndex().getType$(), table.getName$().text(), column);
          if (createIndex != null)
            statements.add(createIndex);
        }
      }
    }

    return statements;
  }

  protected List<CreateStatement> types(final $Table table) {
    return new ArrayList<>();
  }

  protected abstract String dropIndexOnClause(final $Table table);

  protected final LinkedHashSet<DropStatement> dropTable(final $Table table) {
    final LinkedHashSet<DropStatement> statements = new LinkedHashSet<>();
    // FIXME: Explicitly dropping indexes on tables that may not exist will throw errors!
//    if (table.getIndexes() != null)
//      for (final $Table.getIndexes.getIndex index : table.getIndexes(0).getIndex())
//        statements.add(dropIndexIfExists(SQLDataTypes.getIndexName(table, index) + dropIndexOnClause(table)));

//    if (table.getColumn() != null)
//      for (final $Column column : table.getColumn())
//        if (column.getIndex() != null)
//          statements.add(dropIndexIfExists(SQLDataTypes.getIndexName(table, column.getIndex(0), column) + dropIndexOnClause(table)));

    if (table.getTriggers() != null)
      for (final $Table.Triggers.Trigger trigger : table.getTriggers().getTrigger())
        for (final String action : trigger.getActions$().text())
          statements.add(new DropStatement("DROP TRIGGER IF EXISTS " + q(SQLDataTypes.getTriggerName(table.getName$().text(), trigger, action))));

    final DropStatement dropTable = dropTableIfExists(table);
    if (dropTable != null)
      statements.add(dropTable);

    return statements;
  }

  protected LinkedHashSet<DropStatement> dropTypes(final $Table table) {
    return new LinkedHashSet<>();
  }

  protected DropStatement dropTableIfExists(final $Table table) {
    return new DropStatement("DROP TABLE IF EXISTS " + q(table.getName$().text()));
  }

  protected DropStatement dropIndexIfExists(final String indexName) {
    return new DropStatement("DROP INDEX IF EXISTS " + q(indexName));
  }

  private static void checkNumericDefault(final DBVendor vendor, final $Column type, final Number defaultValue, final boolean positive, final Short precision, final boolean unsigned) {
    if (!positive && unsigned)
      throw new IllegalArgumentException(type.name().getPrefix() + ":" + type.name().getLocalPart() + " column '" + type.getName$().text() + "' DEFAULT " + defaultValue + " is negative, but type is declared UNSIGNED");

    if (type instanceof $Bigint) {
      final BigInteger maxValue = vendor.getDialect().allowsUnsignedNumeric() ? BigInteger.valueOf(2).pow(8 * 8) : BigInteger.valueOf(2).pow(8 * 8).divide(BigInteger.valueOf(2));
      if (((BigInteger)defaultValue).compareTo(maxValue) >= 0)
        throw new IllegalArgumentException(type.name().getPrefix() + ":" + type.name().getLocalPart() + " column '" + type.getName$().text() + "' DEFAULT " + defaultValue + " is larger than the maximum value of " + maxValue.subtract(BigInteger.ONE) + " allowed by " + vendor);
    }
    else if (type instanceof $Decimal) {
      final BigDecimal defaultDecimal = (BigDecimal)defaultValue;
      if (defaultDecimal.precision() > precision)
        throw new IllegalArgumentException(type.name().getPrefix() + ":" + type.name().getLocalPart() + " column '" + type.getName$().text() + "' DEFAULT " + defaultValue + " is longer than declared PRECISION " + precision);
    }
  }

  protected String $default(final $Table table, final $Column column) {
    if (column instanceof $Char) {
      final $Char type = ($Char)column;
      if (type.getDefault$() == null)
        return null;

      if (type.getDefault$().text().length() > type.getLength$().text())
        throw new IllegalArgumentException(type.name().getPrefix() + ":" + type.name().getLocalPart() + " column '" + column.getName$().text() + "' DEFAULT '" + type.getDefault$().text() + "' is longer than declared LENGTH(" + type.getLength$().text() + ")");

      return "'" + type.getDefault$().text() + "'";
    }

    if (column instanceof $Binary) {
      final $Binary type = ($Binary)column;
      if (type.getDefault$() == null)
        return null;

      if (type.getDefault$().text().getBytes().length > type.getLength$().text())
        throw new IllegalArgumentException(type.name().getPrefix() + ":" + type.name().getLocalPart() + " column '" + column.getName$().text() + "' DEFAULT '" + type.getDefault$().text() + "' is longer than declared LENGTH " + type.getLength$().text());

      return compileBinary(type.getDefault$().text().toString());
    }

    if (column instanceof $Integer) {
      final BigInteger _default;
      final Byte precision;
      final boolean unsigned;
      if (column instanceof $Tinyint) {
        final $Tinyint type = ($Tinyint)column;
        _default = type.getDefault$() == null ? null : type.getDefault$().text();
        precision = type.getPrecision$() == null ? null : type.getPrecision$().text();
        unsigned = type.getUnsigned$() != null && type.getUnsigned$().text();
      }
      else if (column instanceof $Smallint) {
        final $Smallint type = ($Smallint)column;
        _default = type.getDefault$() == null ? null : type.getDefault$().text();
        precision = type.getPrecision$() == null ? null : type.getPrecision$().text();
        unsigned = type.getUnsigned$() != null && type.getUnsigned$().text();
      }
      else if (column instanceof $Int) {
        final $Int type = ($Int)column;
        _default = type.getDefault$() == null ? null : type.getDefault$().text();
        precision = type.getPrecision$() == null ? null : type.getPrecision$().text();
        unsigned = type.getUnsigned$() != null && type.getUnsigned$().text();
      }
      else if (column instanceof $Bigint) {
        final $Bigint type = ($Bigint)column;
        _default = type.getDefault$() == null ? null : type.getDefault$().text();
        precision = type.getPrecision$() == null ? null : type.getPrecision$().text();
        unsigned = type.getUnsigned$() != null && type.getUnsigned$().text();
      }
      else {
        throw new UnsupportedOperationException("Unsupported type: " + column.getClass().getName());
      }

      if (_default == null)
        return null;

      if (precision != null)
        checkNumericDefault(getVendor(), column, _default, _default.compareTo(BigInteger.ZERO) >= 0, precision.shortValue(), unsigned);

      return String.valueOf(_default);
    }

    if (column instanceof $Float) {
      final $Float type = ($Float)column;
      if (type.getDefault$() == null)
        return null;

      checkNumericDefault(getVendor(), type, type.getDefault$().text(), type.getDefault$().text() > 0, null, type.getUnsigned$().text());
      return type.getDefault$().text().toString();
    }

    if (column instanceof $Double) {
      final $Double type = ($Double)column;
      if (type.getDefault$() == null)
        return null;

      checkNumericDefault(getVendor(), type, type.getDefault$().text(), type.getDefault$().text() > 0, null, type.getUnsigned$().text());
      return type.getDefault$().text().toString();
    }

    if (column instanceof $Decimal) {
      final $Decimal type = ($Decimal)column;
      if (type.getDefault$() == null)
        return null;

      checkNumericDefault(getVendor(), type, type.getDefault$().text(), type.getDefault$().text().doubleValue() > 0, type.getPrecision$() == null ? null : type.getPrecision$().text(), type.getUnsigned$().text());
      return type.getDefault$().text().toString();
    }

    if (column instanceof $Date) {
      final $Date type = ($Date)column;
      if (type.getDefault$() == null)
        return null;

      return compileDate(type.getDefault$().text());
    }

    if (column instanceof $Time) {
      final $Time type = ($Time)column;
      if (type.getDefault$() == null)
        return null;

      return compileTime(type.getDefault$().text());
    }

    if (column instanceof $Datetime) {
      final $Datetime type = ($Datetime)column;
      if (type.getDefault$() == null)
        return null;

      return compileDateTime(type.getDefault$().text());
    }

    if (column instanceof $Boolean) {
      final $Boolean type = ($Boolean)column;
      if (type.getDefault$() == null)
        return null;

      return type.getDefault$().text().toString();
    }

    if (column instanceof $Enum) {
      final $Enum type = ($Enum)column;
      if (type.getDefault$() == null)
        return null;

      return "'" + type.getDefault$().text() + "'";
    }

    if (column instanceof $Clob || column instanceof $Blob)
      return null;

    throw new UnsupportedOperationException("Unknown type: " + column.getClass().getName());
  }

  protected String truncate(final String tableName) {
    return "DELETE FROM " + q(tableName);
  }

  protected abstract String $null(final $Table table, final $Column column);
  protected abstract String $autoIncrement(final $Table table, final $Integer column);

  protected String compileBinary(final String value) {
    return "X'" + value + "'";
  }

  protected String compileDate(final String value) {
    return "'" + value + "'";
  }

  protected String compileDateTime(final String value) {
    return "'" + value + "'";
  }

  protected String compileTime(final String value) {
    return "'" + value + "'";
  }
}