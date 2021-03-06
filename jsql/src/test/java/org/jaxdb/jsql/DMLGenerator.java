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

package org.jaxdb.jsql;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.libj.lang.Classes;

@SuppressWarnings("unused")
public class DMLGenerator {
  private DMLGenerator() {
  }

  public static class Args {
    public final Class<?> a;
    public final Class<?> b;

    Args(final Class<?> a, final Class<?> b) {
      this.a = a;
      this.b = b;
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj == this)
        return true;

      if (!(obj instanceof Args))
        return false;

      final Args that = (Args)obj;
      return a == that.a && b == that.b;
    }

    @Override
    public int hashCode() {
      return a.hashCode() ^ b.hashCode();
    }

    @Override
    public String toString() {
      return "(" + DMLGenerator.getName(a) + ", " + DMLGenerator.getName(b) + ")";
    }
  }

  private static final Class<?>[] types = new Class<?>[] {
    data.BIGINT.class,
    data.DECIMAL.class,
    data.DOUBLE.class,
    data.FLOAT.class,
    data.INT.class,
    data.SMALLINT.class,
    data.TINYINT.class
  };

  private static final Map<Args,Class<?>> scaledMap = new HashMap<>();
  private static final Map<Args,Class<?>> directMap = new HashMap<>();
  private static final Map<Class<?>,Class<?>> singleMap = new LinkedHashMap<>();

  private static void put(final Map<? super Args,? super Class<?>> map, final Class<?> r, final Class<?> a, final Class<?> b) {
    final Args args = new Args(a, b);
//    final Class<?> exists = map.get(args);
//    if (exists != null && exists != r)
//      System.err.println("WARNING: " + args + ": " + getName(exists) + " with " + getName(r));

    map.put(args, r);
    if (data.Numeric.class.isAssignableFrom(b))
      map.put(new Args(a, getGenericType(b)), r);

    if (data.Numeric.class.isAssignableFrom(a))
      map.put(new Args(b, getGenericType(a)), r);
  }

  private static void putApprox(final Class<?> r, final Class<?> a, final Class<?> b, final boolean includeScaled) {
    if (includeScaled)
      put(scaledMap, r, a, b);

    put(directMap, r, a, b);
  }

  private static void putApproxs(final Class<?> a, final Class<?> b, final Class<?> r) {
    put(a, b, r, true);
  }

  private static void putDirect(final Class<?> a, final Class<?> b, final Class<?> r) {
    put(a, b, r, false);
  }

  private static void put(final Class<?> a, final Class<?> b, final Class<?> r, final boolean includeScaled) {
    putApprox(r, a, b, includeScaled);

    final Class<?> ua = getUnsignedClass(a);
    final Class<?> ur = getUnsignedClass(r);
      putApprox(r, ua, b, includeScaled);

    final Class<?> ub = getUnsignedClass(b);
    putApprox(r, a, ub, includeScaled);
    putApprox(ur, ua, ub, includeScaled);
  }

  private static Class<?> getGenericType(final Class<?> cls) {
    final Type[] genericTypes = Classes.getSuperclassGenericTypes(cls);
    return genericTypes != null ? (Class<?>)genericTypes[0] : getGenericType(cls.getSuperclass());
  }

  private static Class<?> getUnsignedClass(final Class<?> cls) {
    final Class<?> unsignedClass = cls.getClasses()[0];
    assert("UNSIGNED".equals(unsignedClass.getSimpleName()));
    return unsignedClass;
  }

  static {
    for (final Class<?> type : types) {
      if (data.ApproxNumeric.class.isAssignableFrom(type)) {
        singleMap.put(type, type);
        final Class<?> unsignedType = getUnsignedClass(type);
        singleMap.put(unsignedType, unsignedType);
      }
    }

    singleMap.put(data.TINYINT.class, data.FLOAT.class);
    singleMap.put(data.SMALLINT.class, data.FLOAT.class);
    singleMap.put(data.INT.class, data.FLOAT.class);
    singleMap.put(data.BIGINT.class, data.DOUBLE.class);
    singleMap.put(data.DECIMAL.class, data.DECIMAL.class);
    assert(singleMap.size() == 14);

    putApproxs(data.FLOAT.class, data.FLOAT.class, data.FLOAT.class);
    putApproxs(data.FLOAT.class, data.DOUBLE.class, data.DOUBLE.class);
    putApproxs(data.FLOAT.class, data.TINYINT.class, data.FLOAT.class);
    putApproxs(data.FLOAT.class, data.SMALLINT.class, data.FLOAT.class);
    putApproxs(data.FLOAT.class, data.INT.class, data.FLOAT.class);
    putApproxs(data.FLOAT.class, data.BIGINT.class, data.DOUBLE.class);
    putApproxs(data.FLOAT.class, data.DECIMAL.class, data.DECIMAL.class);

    putApproxs(data.DOUBLE.class, data.DOUBLE.class, data.DOUBLE.class);
    putApproxs(data.DOUBLE.class, data.TINYINT.class, data.DOUBLE.class);
    putApproxs(data.DOUBLE.class, data.SMALLINT.class, data.DOUBLE.class);
    putApproxs(data.DOUBLE.class, data.INT.class, data.DOUBLE.class);
    putApproxs(data.DOUBLE.class, data.BIGINT.class, data.DOUBLE.class);
    putApproxs(data.DOUBLE.class, data.DECIMAL.class, data.DECIMAL.class);

    putApproxs(data.TINYINT.class, data.TINYINT.class, data.FLOAT.class);
    putApproxs(data.TINYINT.class, data.SMALLINT.class, data.FLOAT.class);
    putApproxs(data.TINYINT.class, data.INT.class, data.FLOAT.class);
    putApproxs(data.TINYINT.class, data.BIGINT.class, data.DOUBLE.class);
    putApproxs(data.TINYINT.class, data.DECIMAL.class, data.DECIMAL.class);

    putApproxs(data.SMALLINT.class, data.SMALLINT.class, data.FLOAT.class);
    putApproxs(data.SMALLINT.class, data.INT.class, data.FLOAT.class);
    putApproxs(data.SMALLINT.class, data.BIGINT.class, data.DOUBLE.class);
    putApproxs(data.SMALLINT.class, data.DECIMAL.class, data.DECIMAL.class);

    putApproxs(data.INT.class, data.INT.class, data.FLOAT.class);
    putApproxs(data.INT.class, data.BIGINT.class, data.DOUBLE.class);
    putApproxs(data.INT.class, data.DECIMAL.class, data.DECIMAL.class);

    putApproxs(data.BIGINT.class, data.BIGINT.class, data.DOUBLE.class);
    putApproxs(data.BIGINT.class, data.DECIMAL.class, data.DECIMAL.class);

    putApproxs(data.DECIMAL.class, data.DECIMAL.class, data.DECIMAL.class);

    putDirect(data.TINYINT.class, data.TINYINT.class, data.TINYINT.class);
    putDirect(data.TINYINT.class, data.SMALLINT.class, data.SMALLINT.class);
    putDirect(data.TINYINT.class, data.INT.class, data.INT.class);
    putDirect(data.TINYINT.class, data.BIGINT.class, data.BIGINT.class);

    putDirect(data.SMALLINT.class, data.SMALLINT.class, data.SMALLINT.class);
    putDirect(data.SMALLINT.class, data.INT.class, data.INT.class);
    putDirect(data.SMALLINT.class, data.BIGINT.class, data.BIGINT.class);

    putDirect(data.INT.class, data.INT.class, data.INT.class);
    putDirect(data.INT.class, data.BIGINT.class, data.BIGINT.class);

    putDirect(data.BIGINT.class, data.BIGINT.class, data.BIGINT.class);
  }

  private static final String[] singleParamFunctions = {
    "$1 ROUND(final $2 a) {\n  return ($1)$n1.wrapper(new function.Round(a, 0));\n}",
    "$1 ROUND(final $2 a, final int scale) {\n  return ($1)$n1.wrapper(new function.Round(a, scale));\n}",
    "$1 ABS(final $2 a) {\n  return ($1)$n1.wrapper(new function.Abs(a));\n}",
    "$1 FLOOR(final $2 a) {\n  return ($1)$n1.wrapper(new function.Floor(a));\n}",
    "$1 CEIL(final $2 a) {\n  return ($1)$n1.wrapper(new function.Ceil(a));\n}",
    "$1 SQRT(final $2 a) {\n  return ($1)$n1.wrapper(new function.Sqrt(a));\n}",
    "$1 EXP(final $2 a) {\n  return ($1)$n1.wrapper(new function.Exp(a));\n}",
    "$1 LN(final $2 a) {\n  return ($1)$n1.wrapper(new function.Ln(a));\n}",
    "$1 LOG2(final $2 a) {\n  return ($1)$n1.wrapper(new function.Log2(a));\n}",
    "$1 LOG10(final $2 a) {\n  return ($1)$n1.wrapper(new function.Log10(a));\n}",
    "$1 SIN(final $2 a) {\n  return ($1)$n1.wrapper(new function.Sin(a));\n}",
    "$1 ASIN(final $2 a) {\n  return ($1)$n1.wrapper(new function.Asin(a));\n}",
    "$1 COS(final $2 a) {\n  return ($1)$n1.wrapper(new function.Cos(a));\n}",
    "$1 ACOS(final $2 a) {\n  return ($1)$n1.wrapper(new function.Acos(a));\n}",
    "$1 TAN(final $2 a) {\n  return ($1)$n1.wrapper(new function.Tan(a));\n}",
    "$1 ATAN(final $2 a) {\n  return ($1)$n1.wrapper(new function.Atan(a));\n}"
  };

  private static final String[] doubleParamFunctions = {
    "$1 POW(final $2 a, final $3 b) {\n  return ($1)$n1.wrapper(new function.Pow(a, b));\n}",
    "$1 MOD(final $2 a, final $3 b) {\n  return ($1)$n1.wrapper(new function.Mod(a, b));\n}",
    "$1 LOG(final $2 a, final $3 b) {\n  return ($1)$n1.wrapper(new function.Log(a, b));\n}",
    "$1 ATAN2(final $2 a, final $3 b) {\n  return ($1)$n1.wrapper(new function.Atan2(a, b));\n}"
  };

  private static final String[] numericExpressionsDirect = {
    "$1 ADD(final $2 a, final $3 b) {\n  return ($1)$n1.wrapper(new NumericExpression(Operator.PLUS, a, b));\n}",
    "$1 SUB(final $2 a, final $3 b) {\n  return ($1)$n1.wrapper(new NumericExpression(Operator.MINUS, a, b));\n}",
    "$1 MUL(final $2 a, final $3 b) {\n  return ($1)$n1.wrapper(new NumericExpression(Operator.MULTIPLY, a, b));\n}"
  };

  private static final String[] numericExpressionsScaled = {
    "$1 DIV(final $2 a, final $3 b) {\n  return ($1)$n1.wrapper(new NumericExpression(Operator.DIVIDE, a, b));\n}"
  };

  private static String getName(final Class<?> cls) {
    if (cls == Float.class)
      return "float";

    if (cls == Double.class)
      return "double";

    if (cls == Byte.class)
      return "byte";

    if (cls == Short.class)
      return "short";

    if (cls == Integer.class)
      return "int";

    if (cls == Long.class)
      return "long";

    if (cls == BigInteger.class || cls == BigDecimal.class)
      return cls.getSimpleName();

    int index = cls.getName().indexOf("type$");
    final String canonicalName = cls.getCanonicalName();
    if (index != -1)
      return canonicalName.substring(index) + (cls == data.Numeric.class ? "<?>" : "");

    index = cls.getName().indexOf("UNS");
    return canonicalName.substring(index);
  }

  private static String newInstance(final Class<?> a, final Class<?> b, final Class<?> c) {
    if (a == data.FLOAT.class || a == data.DOUBLE.class || a == data.DECIMAL.class) {
      if (b == data.FLOAT.class || b == data.DOUBLE.class || b == data.DECIMAL.class) {
        if (c == null || c == data.FLOAT.class || c == data.DOUBLE.class || c == data.DECIMAL.class) {
          final String ub = c == data.FLOAT.class || c == data.DOUBLE.class || c == data.DECIMAL.class ? " && b.unsigned()" : "";
          return "(a.unsigned()" + ub + " ? new " + getName(a) + ".UNSIGNED($p) : new " + getName(a) + "($p))";
        }
      }
    }

    return "new " + getName(a) + "($p)";
  }

  private static String compile(final String function, final Class<?> a, final Class<?> b, final Class<?> c, final boolean checkBothUnsigned) {
    final boolean bIsNumeric = data.Numeric.class.isAssignableFrom(b);
    String compiled = "public static final " + function.replace("$n1", newInstance(a, b, checkBothUnsigned ? c : null)).replace("$1", getName(a)).replace("$2", getName(b)) + "\n";
    if (c != null)
      compiled = compiled.replace("$3", getName(c));

    final String numericVar = bIsNumeric ? "a" : "b";
    return a == data.DECIMAL.class ? compiled.replace("$p", numericVar + ".precision(), " + numericVar + ".scale()") : data.ExactNumeric.class.isAssignableFrom(a) ? compiled.replace("$p", numericVar + ".precision()") : compiled.replace("$p", "");
  }

  private static void printSingles() {
    for (final String function : singleParamFunctions)
      for (final Map.Entry<Class<?>,Class<?>> entry : singleMap.entrySet())
        System.out.println(compile(function, entry.getValue(), entry.getKey(), null, false));
  }

  private static void printDoubles() {
    for (final String function : doubleParamFunctions) {
      for (final Map.Entry<Args,Class<?>> entry : scaledMap.entrySet()) {
        System.out.println(compile(function, entry.getValue(), entry.getKey().a, entry.getKey().b, false));
      }
    }
  }

  private static void printNumericExpressions() {
    for (final String function : numericExpressionsDirect)
      for (final Map.Entry<Args,Class<?>> entry : directMap.entrySet()) {
        System.out.println(compile(function, entry.getValue(), entry.getKey().a, entry.getKey().b, true));
      }

    for (final String function : numericExpressionsScaled)
      for (final Map.Entry<Args,Class<?>> entry : scaledMap.entrySet()) {
        System.out.println(compile(function, entry.getValue(), entry.getKey().a, entry.getKey().b, true));
      }
  }

  private static void filter(final Map<Args,Class<?>> map) {
    final Set<Args> removes = new HashSet<>();
    for (final Map.Entry<Args,Class<?>> entry : map.entrySet()) {
      final Args args = entry.getKey();
      if (!data.Numeric.class.isAssignableFrom(args.b)) {
        if (args.b == Float.class && map.get(new Args(args.a, Double.class)) == entry.getValue())
          removes.add(args);
        if (args.b == Byte.class && map.get(new Args(args.a, Short.class)) == entry.getValue())
          removes.add(args);
        if (args.b == Short.class && map.get(new Args(args.a, Integer.class)) == entry.getValue())
          removes.add(args);
        if (args.b == Integer.class && map.get(new Args(args.a, Long.class)) == entry.getValue())
          removes.add(args);
      }
    }

    for (final Args args : removes)
      map.remove(args);
  }

  private static void trans(final Map<Args,Class<?>> map) {
    final Map<Args,Class<?>> trans = new HashMap<>();
    for (final Map.Entry<Args,Class<?>> entry : map.entrySet()) {
      final Args args = entry.getKey();
      trans.put(new Args(args.b, args.a), entry.getValue());
    }

    map.putAll(trans);
  }

  public static void main(final String[] args) {
    filter(scaledMap);
    trans(scaledMap);
    filter(directMap);
    trans(directMap);

//    int total = 0;
//    for (final Map.Entry<Args,Class<?>> entry : scaledMap.entrySet())
//      System.out.println(getName(entry.getValue()) + " (" + getName(entry.getKey().a) + ", " + getName(entry.getKey().b) + ")");

//    System.err.println(scaledMap.size());
//    printSingles();
    printDoubles();
//    printNumericExpressions();
  }
}