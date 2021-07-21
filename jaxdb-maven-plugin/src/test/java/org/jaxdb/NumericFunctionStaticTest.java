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

package org.jaxdb;

import static org.jaxdb.jsql.DML.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;

import org.jaxdb.jsql.DML.IS;
import org.jaxdb.jsql.RowIterator;
import org.jaxdb.jsql.Select;
import org.jaxdb.jsql.Transaction;
import org.jaxdb.jsql.classicmodels;
import org.jaxdb.jsql.data;
import org.jaxdb.jsql.types;
import org.jaxdb.runner.Derby;
import org.jaxdb.runner.MySQL;
import org.jaxdb.runner.Oracle;
import org.jaxdb.runner.PostgreSQL;
import org.jaxdb.runner.SQLite;
import org.jaxdb.runner.VendorSchemaRunner;
import org.jaxdb.runner.VendorSchemaRunner.Schema;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libj.math.SafeMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(VendorSchemaRunner.class)
public abstract class NumericFunctionStaticTest {
  @VendorSchemaRunner.Vendor(value=Derby.class, parallel=2)
  @VendorSchemaRunner.Vendor(SQLite.class)
  public static class IntegrationTest extends NumericFunctionStaticTest {
  }

  @VendorSchemaRunner.Vendor(MySQL.class)
  @VendorSchemaRunner.Vendor(PostgreSQL.class)
  @VendorSchemaRunner.Vendor(Oracle.class)
  public static class RegressionTest extends NumericFunctionStaticTest {
  }

  private static final Logger logger = LoggerFactory.getLogger(NumericFunctionStaticTest.class);

  private static Select.untyped.SELECT<data.Entity<?>> selectVicinity(final double latitude, final double longitude, final double distance, final int limit) {
    final classicmodels.Customer c = classicmodels.Customer();
    final data.DOUBLE d = data.DOUBLE();

    return SELECT(c, MUL(3959 * 2, ATAN2(
      SQRT(ADD(
        POW(SIN(DIV(MUL(SUB(c.latitude, latitude), PI()), 360)), 2),
        MUL(MUL(
          COS(DIV(MUL(c.latitude, PI()), 180)),
          COS(DIV(MUL(latitude, PI()), 180))),
          POW(SIN(DIV(MUL(SUB(c.longitude, longitude), PI()), 360)), 2)))),
      SQRT(ADD(
        SUB(1, POW(SIN(DIV(MUL(SUB(c.latitude, latitude), PI()), 360)), 2)),
        MUL(MUL(
          COS(DIV(MUL(latitude, PI()), 180)),
          COS(DIV(MUL(c.latitude, PI()), 180))),
          POW(SIN(DIV(MUL(SUB(c.longitude, longitude), PI()), 360)), 2)))))).
        AS(d)).
      FROM(c).
      GROUP_BY(c).
      HAVING(LT(d, distance)).
      ORDER_BY(DESC(d)).
      LIMIT(limit);
  }

  @Test
  public void testVicinity(@Schema(classicmodels.class) final Transaction transaction) throws IOException, SQLException {
    try (final RowIterator<? extends data.Entity<?>> rows = selectVicinity(37.78536811469731, -122.3931884765625, 10, 1)
      .execute(transaction)) {
      while (rows.nextRow()) {
        final classicmodels.Customer c = (classicmodels.Customer)rows.nextEntity();
        assertEquals("Mini Wheels Co.", c.companyName.get());
        final data.DECIMAL d = (data.DECIMAL)rows.nextEntity();
        assertEquals(2.22069, d.get().doubleValue(), 0.00001);
      }
    }
  }

  @Test
  public void testRound0(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<data.DOUBLE> rows =
      SELECT(
        t.doubleType,
        ROUND(t.doubleType, 0)).
      FROM(t).
      WHERE(GT(t.doubleType, 10)).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = Math.round(rows.nextEntity().get());
      assertEquals(expected, rows.nextEntity().get(), Math.ulp(expected) * 100);
    }
  }

  @Test
  public void testRound1(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<data.DOUBLE> rows =
      SELECT(
        t.doubleType,
        ROUND(t.doubleType, 1)).
      FROM(t).
      WHERE(GT(t.doubleType, 10)).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = SafeMath.round(rows.nextEntity().get(), 1);
      assertEquals(expected, rows.nextEntity().get(), Math.ulp(expected) * 100);
    }
  }

  @Test
  public void testSign(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        SIGN(t.doubleType)).
      FROM(t).
      WHERE(IS.NOT.NULL(t.doubleType)).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      assertEquals(Math.signum(rows.nextEntity().get().doubleValue()), rows.nextEntity().get().intValue(), 0);
    }
  }

  @Test
  public void testFloor(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        FLOOR(t.doubleType)).
      FROM(t).
      WHERE(IS.NOT.NULL(t.doubleType)).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = Math.floor(rows.nextEntity().get().doubleValue());
      assertEquals(expected, rows.nextEntity().get().doubleValue(), Math.ulp(expected) * 100);
    }
  }

  @Test
  public void testCeil(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        CEIL(t.doubleType)).
      FROM(t).
      WHERE(IS.NOT.NULL(t.doubleType)).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = Math.ceil(rows.nextEntity().get().doubleValue());
      assertEquals(expected, rows.nextEntity().get().doubleValue(), Math.ulp(expected) * 100);
    }
  }

  @Test
  public void testSqrt(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        SQRT(t.doubleType)).
      FROM(t).
      WHERE(GT(t.doubleType, 10)).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = Math.sqrt(rows.nextEntity().get().doubleValue());
      assertEquals(expected, rows.nextEntity().get().doubleValue(), Math.ulp(expected) * 100);
    }
  }

  @Test
  public void testDegrees(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        DEGREES(t.doubleType)).
      FROM(t).
      WHERE(NE(t.doubleType, 0)).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = SafeMath.toDegrees(rows.nextEntity().get().doubleValue());
      assertEquals(expected, rows.nextEntity().get().doubleValue(), Math.ulp(expected) * 100);
    }
  }

  @Test
  public void testRadians(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        RADIANS(t.doubleType)).
      FROM(t).
      WHERE(NE(t.doubleType, 0)).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = SafeMath.toRadians(rows.nextEntity().get().doubleValue());
      assertEquals(expected, rows.nextEntity().get().doubleValue(), Math.ulp(expected) * 100);
    }
  }

  @Test
  public void testSin(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        SIN(t.doubleType)).
      FROM(t).
      WHERE(AND(GT(t.doubleType, 0), LT(t.doubleType, 1))).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = StrictMath.sin(rows.nextEntity().get().doubleValue());
      assertEquals(expected, rows.nextEntity().get().doubleValue(), Math.ulp(expected) * 100);
    }
  }

  @Test
  public void testAsin(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        ASIN(t.doubleType)).
      FROM(t).
      WHERE(AND(GT(t.doubleType, 0), LT(t.doubleType, 1))).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = StrictMath.asin(rows.nextEntity().get().doubleValue());
      assertEquals(expected, rows.nextEntity().get().doubleValue(), Math.ulp(expected) * 100);
    }
  }

  @Test
  public void testCos(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        COS(t.doubleType)).
      FROM(t).
      WHERE(AND(GT(t.doubleType, 0), LT(t.doubleType, 1))).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = StrictMath.cos(rows.nextEntity().get().doubleValue());
      assertEquals(expected, rows.nextEntity().get().doubleValue(), Math.ulp(expected) * 100);
    }
  }

  @Test
  public void testAcos(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        ACOS(t.doubleType)).
      FROM(t).
      WHERE(AND(GT(t.doubleType, 0), LT(t.doubleType, 1))).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = StrictMath.acos(rows.nextEntity().get().doubleValue());
      assertEquals(expected, rows.nextEntity().get().doubleValue(), Math.ulp(expected) * 100);
    }
  }

  @Test
  public void testTan(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        TAN(t.doubleType)).
      FROM(t).
      WHERE(AND(GT(t.doubleType, 0), LT(t.doubleType, 1))).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = StrictMath.tan(rows.nextEntity().get().doubleValue());
      assertEquals(expected, rows.nextEntity().get().doubleValue(), Math.ulp(expected) * 100);
    }
  }

  @Test
  public void testAtan(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        ATAN(t.doubleType)).
      FROM(t).
      WHERE(AND(GT(t.doubleType, 0), LT(t.doubleType, 1))).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = StrictMath.atan(rows.nextEntity().get().doubleValue());
      assertEquals(expected, rows.nextEntity().get().doubleValue(), Math.ulp(expected) * 100);
    }
  }

  @Test
  public void testModInt1(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.intType,
        MOD(t.intType, 3)).
      FROM(t).
      WHERE(IS.NOT.NULL(t.intType)).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      assertEquals(rows.nextEntity().get().intValue() % 3, rows.nextEntity().get().intValue());
    }
  }

  @Test
  public void testModInt2(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.intType,
        MOD(t.intType, -3)).
      FROM(t).
      WHERE(IS.NOT.NULL(t.intType)).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      assertEquals(rows.nextEntity().get().intValue() % -3, rows.nextEntity().get().intValue());
    }
  }

  @Test
  public void testModInt3(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        t.intType,
        MOD(t.doubleType, t.intType)).
      FROM(t).
      WHERE(AND(IS.NOT.NULL(t.doubleType), NE(t.intType, 0))).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      assertEquals(rows.nextEntity().get().intValue() % rows.nextEntity().get().intValue(), rows.nextEntity().get().intValue());
    }
  }

  @Test
  @VendorSchemaRunner.Unsupported(SQLite.class)
  public void testModDouble1(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        MOD(t.doubleType, 1.2)).
      FROM(t).
      WHERE(AND(IS.NOT.NULL(t.doubleType), LT(ABS(t.doubleType), 100))).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = rows.nextEntity().get().doubleValue() % 1.2;
      assertEquals(expected, rows.nextEntity().get().doubleValue(), Math.ulp(expected) * 1000);
    }
  }

  @Test
  @VendorSchemaRunner.Unsupported(SQLite.class)
  public void testModDouble2(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        MOD(t.doubleType, -1.2)).
      FROM(t).
      WHERE(AND(IS.NOT.NULL(t.doubleType), LT(ABS(t.doubleType), 100))).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = rows.nextEntity().get().doubleValue() % -1.2;
      assertEquals(expected, rows.nextEntity().get().doubleValue(), Math.ulp(expected) * 1000);
    }
  }

  @Test
  @VendorSchemaRunner.Unsupported({SQLite.class, Oracle.class})
  public void testModDouble3(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        t.floatType,
        MOD(t.doubleType, t.floatType)).
      FROM(t).
      WHERE(AND(
        IS.NOT.NULL(t.doubleType),
        GT(ABS(t.floatType), 10),
        LT(ABS(t.floatType), 100),
        GT(ABS(t.doubleType), 10),
        LT(ABS(t.doubleType), 100))).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      // FIXME: Is there something wrong with DMOD() for Derby?
      final double expected = rows.nextEntity().get().doubleValue() % rows.nextEntity().get().floatValue();
      final double actual = rows.nextEntity().get().doubleValue();
      if (Math.abs(expected - actual) > 0.000001)
        logger.warn("Math.abs(expected - actual) > 0.000001: " + Math.abs(expected - actual));

      assertEquals(expected, actual, 0.003);
    }
  }

  @Test
  public void testExp(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        EXP(MUL(t.doubleType, -1))).
      FROM(t).
      WHERE(AND(
        IS.NOT.NULL(t.doubleType),
        LT(ABS(t.doubleType), 100))).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = StrictMath.exp(-rows.nextEntity().get().doubleValue());
      assertEquals(expected, rows.nextEntity().get().doubleValue(), Math.ulp(expected) * 100);
    }
  }

  @Test
  public void testPowX3(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        POW(t.doubleType, 3)).
      FROM(t).
      WHERE(AND(GT(t.doubleType, 0), LT(t.doubleType, 10))).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = StrictMath.pow(rows.nextEntity().get().doubleValue(), 3);
      assertEquals(expected, rows.nextEntity().get().doubleValue(), Math.ulp(expected) * 100);
    }
  }

  @Test
  public void testPow3X(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        POW(3, MUL(t.doubleType, -1))).
      FROM(t).
      WHERE(AND(IS.NOT.NULL(t.doubleType), LT(ABS(t.doubleType), 100))).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = StrictMath.pow(3, -rows.nextEntity().get().doubleValue());
      assertEquals(expected, rows.nextEntity().get().doubleValue(), Math.ulp(expected) * 100);
    }
  }

  @Test
  public void testPowXX(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        t.doubleType,
        POW(t.doubleType, t.doubleType)).
      FROM(t).
      WHERE(AND(GT(t.doubleType, 0), LT(t.doubleType, 10))).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = StrictMath.pow(rows.nextEntity().get().doubleValue(), rows.nextEntity().get().doubleValue());
      assertEquals(expected, rows.nextEntity().get().doubleValue(), Math.ulp(expected) * 100);
    }
  }

  @Test
  public void testLog3X(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        LOG(3, t.doubleType)).
      FROM(t).
      WHERE(GT(t.doubleType, 0)).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = SafeMath.log(3, rows.nextEntity().get().doubleValue());
      assertEquals(expected, rows.nextEntity().get().doubleValue(), Math.ulp(expected) * 100);
    }
  }

  @Test
  public void testLogX3(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        LOG(t.doubleType, 3)).
      FROM(t).
      WHERE(GT(t.doubleType, 0)).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = SafeMath.log(rows.nextEntity().get().doubleValue(), 3);
      assertEquals(expected, rows.nextEntity().get().doubleValue(), Math.ulp(expected) * 100);
    }
  }

  @Test
  public void testLogXX(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        t.intType,
        LOG(t.intType, t.doubleType)).
      FROM(t).
      WHERE(AND(GT(t.intType, 1), GT(t.doubleType, 0), GT(t.doubleType, 1), LT(t.doubleType, 10))).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = StrictMath.log(rows.nextEntity().get().doubleValue()) / StrictMath.log(rows.nextEntity().get().intValue());
      assertEquals(expected, rows.nextEntity().get().doubleValue(), Math.ulp(expected) * 100);
    }
  }

  @Test
  public void testLn(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        LN(t.doubleType)).
      FROM(t).
      WHERE(GT(t.doubleType, 0)).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = StrictMath.log(rows.nextEntity().get().doubleValue());
      assertEquals(expected, rows.nextEntity().get().doubleValue(), Math.ulp(expected) * 100);
    }
  }

  @Test
  public void testLog2(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        LOG2(t.doubleType)).
      FROM(t).
      WHERE(GT(t.doubleType, 0)).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = SafeMath.log2(rows.nextEntity().get().doubleValue());
      assertEquals(expected, rows.nextEntity().get().doubleValue(), Math.ulp(expected) * 100);
    }
  }

  @Test
  public void testLog10(@Schema(types.class) final Transaction transaction) throws IOException, SQLException {
    final types.Type t = types.Type();
    try (final RowIterator<? extends data.Numeric<?>> rows =
      SELECT(
        t.doubleType,
        LOG10(t.doubleType)).
      FROM(t).
      WHERE(GT(t.doubleType, 0)).
      LIMIT(1)
        .execute(transaction)) {
      assertTrue(rows.nextRow());
      final double expected = StrictMath.log10(rows.nextEntity().get().doubleValue());
      assertEquals(expected, rows.nextEntity().get().doubleValue(), Math.ulp(expected) * 100);
    }
  }
}