package org.safris.xdb.xde;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.safris.xdb.xdl.DBVendor;

public class Serialization {
  protected void set(final PreparedStatement statement) {
    for (int i = 0; i < parameters.size(); i++) {
      final Object parameter = parameters.get(i);
      Column.set(statement, i + 1, parameter.getClass(), parameter);
    }
  }

  public final List<Object> parameters = new ArrayList<Object>();

  public final DBVendor vendor;
  public final Class<? extends Statement> prototype;
  public final StringBuilder sql = new StringBuilder();

  public Serialization(final DBVendor vendor, final Class<? extends Statement> prototype) {
    this.vendor = vendor;
    this.prototype = prototype;
  }

  public String toStringParameters() {
    if (prototype == PreparedStatement.class) {
      if (parameters.size() == 0)
        return "[]";

      final StringBuilder string = new StringBuilder();
      for (final Object parameter : parameters)
        string.append(", ").append(parameter);

      return "[" + string.substring(2) + "]";
    }

    if (prototype == Statement.class)
      return "";

    return super.toString();
  }
}