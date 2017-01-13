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

package org.safris.xdb.entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Logger;

import org.safris.commons.lang.Pair;
import org.safris.commons.util.Collections;
import org.safris.xdb.entities.DML.ALL;
import org.safris.xdb.entities.DML.DISTINCT;
import org.safris.xdb.entities.DML.NATURAL;
import org.safris.xdb.entities.DML.TYPE;
import org.safris.xdb.entities.exception.SQLExceptionCatalog;
import org.safris.xdb.entities.spec.select;
import org.safris.xdb.schema.DBVendor;

class Select {
  private static void serialize(final List<Pair<DataType<?>,Integer>> dataTypes, final Subject<?> subject) {
    if (subject instanceof Entity) {
      final Entity entity = (Entity)subject;
      for (int i = 0; i < entity.column().length; i++)
        dataTypes.add(new Pair<DataType<?>,Integer>(entity.column()[i], i));
    }
    else if (subject instanceof DataType<?>) {
      final DataType<?> dataType = (DataType<?>)subject;
      dataTypes.add(new Pair<DataType<?>,Integer>(dataType, -1));
    }
    else {
      throw new UnsupportedOperationException("Unknown entity type: " + subject.getClass().getName());
    }
  }

  private static <B extends Subject<?>>RowIterator<B> parseResultSet(final DBVendor vendor, final Connection connection, final ResultSet resultSet, final SELECT<?> select) throws SQLException {
    final List<Pair<DataType<?>,Integer>> dataTypes = new ArrayList<Pair<DataType<?>,Integer>>();
    for (final Subject<?> entity : select.entities)
      Select.serialize(dataTypes, entity);

    final int noColumns = resultSet.getMetaData().getColumnCount();
    return new RowIterator<B>() {
      private final Map<Class<? extends Entity>,Entity> prototypes = new HashMap<Class<? extends Entity>,Entity>();
      private final Map<Entity,Entity> cache = new HashMap<Entity,Entity>();
      private Entity currentTable = null;

      @Override
      @SuppressWarnings({"rawtypes", "unchecked"})
      public boolean nextRow() throws SQLException {
        if (rowIndex + 1 < rows.size()) {
          ++rowIndex;
          resetEntities();
          return true;
        }

        Subject<?>[] row;
        int index;
        Entity entity;
        try {
          if (!resultSet.next())
            return false;

          row = new Subject[select.entities.size()];
          index = 0;
          entity = null;
          for (int i = 0; i < noColumns; i++) {
            final Pair<DataType<?>,Integer> dataTypePrototype = dataTypes.get(i);
            final Variable variable;
            if (dataTypePrototype.b == -1) {
              variable = dataTypePrototype.a.clone();
              row[index++] = variable;
            }
            else {
              if (currentTable != null && currentTable != dataTypePrototype.a.entity) {
                final Entity cached = cache.get(entity);
                if (cached != null) {
                  row[index++] = cached;
                }
                else {
                  row[index++] = entity;
                  cache.put(entity, entity);
                  prototypes.put(entity.getClass(), entity.newInstance());
                }
              }

              currentTable = dataTypePrototype.a.entity;
              entity = prototypes.get(currentTable.getClass());
              if (entity == null)
                prototypes.put(currentTable.getClass(), entity = currentTable.newInstance());

              variable = entity.column()[dataTypePrototype.b];
            }

            variable.set(resultSet, i + 1);
          }
        }
        catch (final SQLException e) {
          throw SQLExceptionCatalog.lookup(e);
        }

        if (entity != null) {
          final Entity cached = cache.get(entity);
          row[index++] = cached != null ? cached : entity;
        }

        rows.add((B[])row);
        ++rowIndex;
        resetEntities();
        prototypes.clear();
        currentTable = null;
        return true;
      }

      @Override
      public void close() throws SQLException {
        try {
          resultSet.close();
          resultSet.getStatement().close();
          connection.close();
        }
        catch (final SQLException e) {
          throw SQLExceptionCatalog.lookup(e);
        }
        finally {
          prototypes.clear();
          cache.clear();
          currentTable = null;
          dataTypes.clear();
          rows.clear();
        }
      }
    };
  }

  protected static abstract class Execute<T extends Subject<?>> extends Keyword<T> implements select.SELECT<T> {
    @Override
    public T AS(final T as) {
      throw new UnsupportedOperationException();
    }

    @Override
    public select.SELECT<T> UNION(final select.SELECT<T> as) {
      throw new UnsupportedOperationException();
    }

    @Override
    public select.SELECT<T> UNION(final ALL all, final select.SELECT<T> as) {
      throw new UnsupportedOperationException();
    }

    @Override
    public final RowIterator<T> execute() throws SQLException {
      return execute(null);
    }

    @Override
    public RowIterator<T> execute(final Transaction transaction) throws SQLException {
      final SELECT<?> select = (SELECT<?>)getParentRoot(this);
      final Class<? extends Schema> schema = select.from().tables[0].schema();
      DBVendor vendor = null;
      try {
        final Connection connection = transaction != null ? transaction.getConnection() : Schema.getConnection(schema);
        vendor = Schema.getDBVendor(connection);
        final Serialization serialization = new Serialization(Select.class, vendor, EntityRegistry.getStatementType(schema));

        serialize(serialization);
        Subject.clearAliases();

        final ResultSet resultSet = serialization.executeQuery(connection);
        return parseResultSet(serialization.getVendor(), connection, resultSet, select);
      }
      catch (final SQLException e) {
        throw SQLExceptionCatalog.lookup(e);
      }
    }
  }

  protected static abstract class FROM_JOIN_ON<T extends Subject<?>> extends Execute<T> implements select.FROM<T> {
    private final Keyword<T> parent;

    protected FROM_JOIN_ON(final Keyword<T> parent) {
      this.parent = parent;
    }

    @Override
    public final WHERE<T> WHERE(final Condition<?> condition) {
      return new WHERE<T>(this, condition);
    }

    @Override
    public HAVING<T> HAVING(final Condition<?> condition) {
      return new HAVING<T>(this, condition);
    }

    @Override
    public final JOIN<T> JOIN(final Entity entity) {
      return new JOIN<T>(this, null, null, entity);
    }

    @Override
    public final JOIN<T> JOIN(final TYPE type, final Entity entity) {
      return new JOIN<T>(this, null, type, entity);
    }

    @Override
    public final JOIN<T> JOIN(final NATURAL natural, final Entity entity) {
      return new JOIN<T>(this, natural, null, entity);
    }

    @Override
    public final JOIN<T> JOIN(final NATURAL natural, final TYPE type, final Entity entity) {
      return new JOIN<T>(this, natural, type, entity);
    }

    @Override
    public final LIMIT<T> LIMIT(final int limit) {
      return new LIMIT<T>(this, limit);
    }

    @Override
    protected final Keyword<T> parent() {
      return parent;
    }
  }

  protected final static class FROM<T extends Subject<?>> extends FROM_JOIN_ON<T> implements select.FROM<T> {
    protected final Entity[] tables;

    protected FROM(final Keyword<T> parent, final Entity ... tables) {
      super(parent);
      this.tables = tables;
    }

    @Override
    public GROUP_BY<T> GROUP_BY(final Subject<?> ... subjects) {
      return new GROUP_BY<T>(this, subjects);
    }

    @Override
    public ORDER_BY<T> ORDER_BY(final Variable<?> ... variables) {
      return new ORDER_BY<T>(this, variables);
    }
  }

  protected final static class GROUP_BY<T extends Subject<?>> extends Execute<T> implements select.GROUP_BY<T> {
    private final Keyword<T> parent;
    protected final LinkedHashSet<? extends Subject<?>> subjects;

    protected GROUP_BY(final Keyword<T> parent, final LinkedHashSet<? extends Subject<?>> subjects) {
      this.parent = parent;
      this.subjects = subjects;
    }

    protected GROUP_BY(final Keyword<T> parent, final Subject<?> ... subjects) {
      this(parent, (LinkedHashSet<? extends Subject<?>>)Collections.asCollection(LinkedHashSet.class, subjects));
    }

    public ORDER_BY<T> ORDER_BY(final Variable<?> ... columns) {
      return new ORDER_BY<T>(this, columns);
    }

    @Override
    public HAVING<T> HAVING(final Condition<?> condition) {
      return new HAVING<T>(this, condition);
    }

    @Override
    public LIMIT<T> LIMIT(final int limit) {
      return new LIMIT<T>(this, limit);
    }

    @Override
    protected Keyword<T> parent() {
      return parent;
    }
  }

  protected final static class HAVING<T extends Subject<?>> extends Execute<T> implements select.HAVING<T> {
    private final Keyword<T> parent;
    protected final Condition<?> condition;

    protected HAVING(final Keyword<T> parent, final Condition<?> condition) {
      this.parent = parent;
      this.condition = condition;
    }

    @Override
    public ORDER_BY<T> ORDER_BY(final Variable<?> ... column) {
      return new ORDER_BY<T>(this, column);
    }

    @Override
    public LIMIT<T> LIMIT(final int limit) {
      return new LIMIT<T>(this, limit);
    }

    @Override
    protected Keyword<T> parent() {
      return parent;
    }
  }

  protected final static class JOIN<T extends Subject<?>> extends FROM_JOIN_ON<T> implements select.JOIN<T> {
    protected final NATURAL natural;
    protected final TYPE type;
    protected final Entity entity;

    protected JOIN(final Keyword<T> parent, final NATURAL natural, final TYPE type, final Entity entity) {
      super(parent);
      this.natural = natural;
      this.type = type;
      this.entity = entity;
    }

    @Override
    public ON<T> ON(final Condition<?> condition) {
      return new ON<T>(this, condition);
    }

    @Override
    public GROUP_BY<T> GROUP_BY(final Subject<?> ... subjects) {
      return new GROUP_BY<T>(this, subjects);
    }

    @Override
    public ORDER_BY<T> ORDER_BY(final Variable<?> ... variables) {
      return new ORDER_BY<T>(this, variables);
    }
  }

  protected final static class ON<T extends Subject<?>> extends FROM_JOIN_ON<T> implements select.ON<T> {
    protected final Condition<?> condition;

    protected ON(final Keyword<T> parent, final Condition<?> condition) {
      super(parent);
      this.condition = condition;
    }

    @Override
    public GROUP_BY<T> GROUP_BY(final Subject<?> ... subjects) {
      return new GROUP_BY<T>(this, subjects);
    }

    @Override
    public ORDER_BY<T> ORDER_BY(final Variable<?> ... variables) {
      return new ORDER_BY<T>(this, variables);
    }
  }

  protected final static class ORDER_BY<T extends Subject<?>> extends Execute<T> implements select.ORDER_BY<T> {
    private final Keyword<T> parent;
    protected final Variable<?>[] columns;

    protected ORDER_BY(final Keyword<T> parent, final Variable<?> ... columns) {
      this.parent = parent;
      this.columns = columns;
    }

    @Override
    public LIMIT<T> LIMIT(final int limit) {
      return new LIMIT<T>(this, limit);
    }

    @Override
    protected Keyword<T> parent() {
      return parent;
    }
  }

  protected final static class LIMIT<T extends Subject<?>> extends Execute<T> implements select.LIMIT<T> {
    private final Keyword<T> parent;
    protected final int limit;

    protected LIMIT(final Keyword<T> parent, final int limit) {
      this.parent = parent;
      this.limit = limit;
    }

    @Override
    protected Keyword<T> parent() {
      return parent;
    }
  }

  protected final static class SELECT<T extends Subject<?>> extends Keyword<T> implements select._SELECT<T> {
    private static final Logger logger = Logger.getLogger(SELECT.class.getName());

    protected final ALL all;
    protected final DISTINCT distinct;
    protected final LinkedHashSet<T> entities;
    private FROM<T> from;

    @SafeVarargs
    public SELECT(final ALL all, final DISTINCT distinct, final T ... entities) {
      this.all = all;
      this.distinct = distinct;
      this.entities = Collections.asCollection(LinkedHashSet.class, entities);
    }

    @Override
    public FROM<T> FROM(final Entity ... table) {
      if (from != null)
        throw new IllegalStateException("FROM() has already been called for this SELECT object.");

      return from = new FROM<T>(this, table);
    }

    @Override
    public LIMIT<T> LIMIT(final int limit) {
      return new LIMIT<T>(this, limit);
    }

    @Override
    public T AS(final T as) {
      throw new UnsupportedOperationException();
    }

    @Override
    public select.SELECT<T> UNION(final select.SELECT<T> as) {
      throw new UnsupportedOperationException();
    }

    @Override
    public select.SELECT<T> UNION(final ALL all, final select.SELECT<T> as) {
      throw new UnsupportedOperationException();
    }

    @Override
    protected Keyword<T> parent() {
      return null;
    }

    protected FROM<T> from() {
      return from;
    }

    private static final Predicate<Subject<?>> entitiesWithOwnerPredicate = new Predicate<Subject<?>>() {
      @Override
      public boolean test(final Subject<?> t) {
        return (t instanceof DataType) && ((DataType<?>)t).owner() == null;
      }
    };

    @SuppressWarnings("unchecked")
    protected LinkedHashSet<T> getEntitiesWithOwners() {
      final LinkedHashSet<T> clone = (LinkedHashSet<T>)entities.clone();
      clone.removeIf(entitiesWithOwnerPredicate);
      return clone;
    }

    @Override
    public RowIterator<T> execute() throws SQLException {
      return execute(null);
    }

    @Override
    public RowIterator<T> execute(final Transaction transaction) throws SQLException {
      if (entities.size() == 1) {
        final Subject<?> subject = entities.iterator().next();
        if (subject instanceof Entity) {
          final Entity entity = (Entity)subject;
          final Entity out = entity.newInstance();
          final DataType<?>[] dataTypes = entity.column();
          String sql = "SELECT ";
          String select = "";
          String where = "";
          for (final DataType<?> dataType : dataTypes) {
            if (dataType.primary)
              where += " AND " + dataType.name + " = ?";
            else
              select += ", " + dataType.name;
          }

          sql += select.substring(2) + " FROM " + entity.name() + " WHERE " + where.substring(5);
          try {
            final Connection connection = transaction != null ? transaction.getConnection() : Schema.getConnection(entity.schema());
            final PreparedStatement statement = connection.prepareStatement(sql);
            int index = 0;
            for (final DataType<?> dataType : dataTypes)
              if (dataType.primary)
                dataType.get(statement, ++index);

            logger.info(statement.toString());
            try (final ResultSet resultSet = statement.executeQuery()) {
              return new RowIterator<T>() {
                @Override
                @SuppressWarnings({"rawtypes", "unchecked"})
                public boolean nextRow() throws SQLException {
                  if (rowIndex + 1 < rows.size()) {
                    ++rowIndex;
                    resetEntities();
                    return true;
                  }

                  try {
                    if (!resultSet.next())
                      return false;

                    int index = 0;
                    for (final Variable variable : out.column())
                      variable.set(resultSet, ++index);
                  }
                  catch (final SQLException e) {
                    throw SQLExceptionCatalog.lookup(e);
                  }

                  rows.add((T[])new Entity[] {out});
                  ++rowIndex;
                  resetEntities();
                  return true;
                }

                @Override
                public void close() throws SQLException {
                  try {
                    resultSet.close();
                    statement.close();
                    connection.close();
                  }
                  catch (final SQLException e) {
                    throw SQLExceptionCatalog.lookup(e);
                  }
                  finally {
                    rows.clear();
                  }
                }
              };
            }
          }
          catch (final SQLException e) {
            throw SQLExceptionCatalog.lookup(e);
          }
        }
      }

      Subject.clearAliases();
      return null;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj)
        return true;

      if (!(obj instanceof SELECT<?>))
        return false;

      final SELECT<?> that = (SELECT<?>)obj;
      return all == that.all && distinct == that.distinct && Collections.equals(entities, that.entities);
    }

    @Override
    public int hashCode() {
      int mask = 0;
      if (all != null)
        mask |= 1;
      if (distinct != null)
        mask |= 2;

      ++mask;
      return Collections.hashCode(entities) ^ mask;
    }
  }

  protected final static class WHERE<T extends Subject<?>> extends Execute<T> implements select.WHERE<T> {
    private final Keyword<T> parent;
    protected final Condition<?> condition;

    protected WHERE(final Keyword<T> parent, final Condition<?> condition) {
      this.parent = parent;
      this.condition = condition;
    }

    @Override
    public GROUP_BY<T> GROUP_BY(final Subject<?> ... subjects) {
      return new GROUP_BY<T>(this, subjects);
    }

    @Override
    public ORDER_BY<T> ORDER_BY(final Variable<?> ... variables) {
      return new ORDER_BY<T>(this, variables);
    }

    @Override
    public LIMIT<T> LIMIT(final int limit) {
      return new LIMIT<T>(this, limit);
    }

    @Override
    protected Keyword<T> parent() {
      return parent;
    }
  }
}