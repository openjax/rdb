package org.safris.xdb.xde;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.safris.commons.lang.Pair;
import org.safris.xdb.xdl.DBVendor;

public class XDEException extends SQLException {
  private static final long serialVersionUID = 400839108529773414L;

  public static class ErrorSpec {
    @SafeVarargs
    public ErrorSpec(final Pair<DBVendor,String> ... specs) {
      for (final Pair<DBVendor,String> spec : specs) {
        Map<String,ErrorSpec> sqlCodeToException = XDEException.es.get(spec.a);
        if (sqlCodeToException == null)
          XDEException.es.put(spec.a, sqlCodeToException = new HashMap<String,ErrorSpec>());

        sqlCodeToException.put(spec.b, this);
      }
    }
  }

  public static final ErrorSpec UNIQUE_VIOLATION = new ErrorSpec(new Pair<DBVendor,String>(DBVendor.POSTGRE_SQL, "23505"));
  public static final ErrorSpec UNIMPLEMENTED = new ErrorSpec();

  private static final Map<DBVendor,Map<String,ErrorSpec>> es = new HashMap<DBVendor,Map<String,ErrorSpec>>();

  public static XDEException lookup(final SQLException e, final DBVendor vendor) {
    Map<String,ErrorSpec> sqlCodeToException = es.get(vendor);
    if (sqlCodeToException == null) {
      System.err.println("[WARNING] THE VENDOR " + vendor + " DOES NOT EXIST IN THE XDEException library!!!!!!!!!!!!!!! Add it!!!!!");
      return new XDEException(e, UNIMPLEMENTED);
    }

    final ErrorSpec errorSpec = sqlCodeToException.get(e.getSQLState());
    if (errorSpec == null) {
      System.err.println("[WARNING] THE SQLSTATE " + e.getSQLState() + " FOR VENDOR " + vendor + " DOES NOT EXIST IN THE XDEException library!!!!!!!!!!!!!!! Add it!!!!!");
      return new XDEException(e, UNIMPLEMENTED);
    }

    return new XDEException(e, errorSpec);
  }

  public static XDEException lookup(final SQLException e) {
    return lookup(e, null);
  }

  private final ErrorSpec ErrorSpec;

  private XDEException(final SQLException e, final ErrorSpec ErrorSpec) {
    super(e.getMessage(), e.getSQLState(), e.getErrorCode(), e.getCause());
    setStackTrace(e.getStackTrace());
    this.ErrorSpec = ErrorSpec;
  }

  public ErrorSpec getErrorSpec() {
    return ErrorSpec;
  }
}