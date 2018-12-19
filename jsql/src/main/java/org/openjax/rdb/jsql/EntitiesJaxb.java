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

package org.openjax.rdb.jsql;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

import org.fastjax.util.Identifiers;
import org.openjax.rdb.ddlx.dt;
import org.openjax.rdb.ddlx.annotation.Column;
import org.openjax.rdb.ddlx.annotation.Schema;
import org.openjax.rdb.ddlx.annotation.Table;
import org.openjax.rdb.sqlx_0_9_9.Database;
import org.openjax.rdb.sqlx_0_9_9.Insert;
import org.openjax.rdb.sqlx_0_9_9.Row;

final class EntitiesJaxb {
  @SuppressWarnings({"rawtypes", "unchecked"})
  private static type.Entity toEntity(final Database database, final Row row) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException {
    final Schema schema = database.getClass().getAnnotation(Schema.class);
    final Table table = row.getClass().getAnnotation(Table.class);
    // FIXME: This is brittle... Need to modularize it and make it clearer:
    final Class<?> binding = Class.forName(Entities.class.getPackage().getName() + "." + Identifiers.toInstanceCase(schema.name()) + "$" + Identifiers.toClassCase(table.name()));
    final type.Entity entity = (type.Entity)binding.getDeclaredConstructor().newInstance();
    for (final Method method : row.getClass().getMethods()) {
      if (method.getName().startsWith("get") && dt.DataType.class.isAssignableFrom(method.getReturnType())) {
        final dt.DataType<?> column = (dt.DataType<?>)method.invoke(row);
        if (column == null)
          continue;

        final Field field = binding.getField(Identifiers.toCamelCase(method.getAnnotation(Column.class).name()));
        final type.DataType dataType = (type.DataType<?>)field.get(entity);

        final Object value = column.get();
        if (value == null)
          dataType.set(null);
        else if (column instanceof dt.BLOB)
          dataType.set(new ByteArrayInputStream(((String)value).getBytes()));
        else if (column instanceof dt.BINARY)
          dataType.set(((String)value).getBytes());
        else if (column instanceof dt.CLOB)
          dataType.set(new StringReader((String)value));
        else if (column instanceof dt.DATE)
          dataType.set(LocalDate.parse((String)value));
        else if (column instanceof dt.DATETIME)
          dataType.set(LocalDateTime.parse((String)value));
        else if (column instanceof dt.TIME)
          dataType.set(LocalTime.parse((String)value));
        else if (column instanceof dt.ENUM) {
          for (final Object constant : ((type.ENUM)dataType).type().getEnumConstants()) {
            if (constant.toString().equals(value)) {
              dataType.set(constant);
              break;
            }
          }

          if (!dataType.wasSet())
            throw new IllegalArgumentException("'" + value + "' is not a valid value for " + dataType.name);
        }
        else
          dataType.set(value);
      }
    }

    return entity;
  }

  @SuppressWarnings("unchecked")
  public static <T extends type.Entity>T[] toEntities(final Database database) {
    try {
      final List<type.Entity> entities = new ArrayList<>();
      final Insert insert = (Insert)database.getClass().getMethod("getInsert").invoke(database);
      final XmlType xmlType = insert.getClass().getAnnotation(XmlType.class);
      for (final String tableName : xmlType.propOrder())
        for (final Row row : (List<Row>)insert.getClass().getMethod("get" + Identifiers.toClassCase(tableName)).invoke(insert))
          entities.add(toEntity(database, row));

      return (T[])entities.toArray(new type.Entity[entities.size()]);
    }
    catch (final ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchFieldException | NoSuchMethodException e) {
      throw new UnsupportedOperationException(e);
    }
  }

  private EntitiesJaxb() {
  }
}