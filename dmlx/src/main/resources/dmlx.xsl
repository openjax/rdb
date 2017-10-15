<!--
  Copyright (c) 2016 lib4j
  
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:
  
  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.
  
  You should have received a copy of The MIT License (MIT) along with this
  program. If not, see <http://opensource.org/licenses/MIT/>.
-->
<xsl:stylesheet
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:ddlx="http://rdb.libx4j.org/ddlx.xsd"
  xmlns:dmlx="http://rdb.libx4j.org/dmlx.xsd"
  xmlns:sqlx="http://rdb.libx4j.org/sqlx.xsd"
  xmlns:jsql="http://rdb.libx4j.org/jsql.xsd"
  xmlns:function="http://rdb.libx4j.org/dmlx.xsl"
  xmlns:ext="http://exslt.org/common" 
  xmlns:math="http://www.w3.org/2005/xpath-functions/math"
  xmlns:saxon="http://saxon.sf.net/"
  xmlns:annox="http://annox.dev.java.net"
  xmlns:jaxb="http://java.sun.com/xml/ns/jaxb"
  exclude-result-prefixes="ext function math saxon ddlx xs xsi"
  version="2.0">
  
  <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
  
  <xsl:variable name="namespace">
    <xsl:text>dmlx.</xsl:text>
    <xsl:value-of select="//ddlx:schema/@name"/>
  </xsl:variable>  
  
  <xsl:variable name="xmlns">
    <xsl:element name="ns:x" namespace="{$namespace}"/>
  </xsl:variable>
  
  <xsl:variable name="noTables">
    <xsl:value-of select="count(/ddlx:schema/ddlx:table)"/>
  </xsl:variable>
  
  <!-- This function is used to sort the tables in topological dependency order -->
  <xsl:function name="function:computeWeight" as="xs:integer*">
    <xsl:param name="node"/>
    <xsl:param name="i"/>
    <!-- generate a sequence containing the weights of each node I reference -->
    <xsl:variable name="weights" as="xs:integer*">
      <xsl:sequence select="0"/>
      <xsl:for-each select="$node/ddlx:column/ddlx:foreignKey/@references">
        <xsl:value-of select="function:computeWeight(/ddlx:schema/ddlx:table[@name=current() and $node/@name!=current()], $i + 1)"/>
      </xsl:for-each>
    </xsl:variable>
    <!-- make my weight higher than any of the nodes I reference -->
    <xsl:choose>
      <xsl:when test="$noTables > $i">
        <xsl:value-of select="max($weights) + 1"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">ERROR: Loop detected in FOREIGN KEY relationship</xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="function:getPrimaryKey">
    <xsl:param name="node" as="element()"/>
    <xsl:variable name="primaryKey" select="$node/ddlx:constraints/ddlx:primaryKey"/>
    <xsl:choose>
      <xsl:when test="$primaryKey">
        <xsl:sequence select="$primaryKey"/>
      </xsl:when>
      <xsl:when test="$node/@extends">
        <xsl:for-each select="$node/@extends">
          <xsl:sequence select="function:getPrimaryKey(/ddlx:schema/ddlx:table[@name=current()])"/>
        </xsl:for-each>
      </xsl:when>
    </xsl:choose>
  </xsl:function>
  
  <xsl:function name="function:camel-case">
    <xsl:param name="string"/>
    <xsl:value-of select="string-join(for $s in tokenize($string, '\W+') return concat(upper-case(substring($s, 1, 1)), substring($s, 2)), '')"/>
  </xsl:function>
  
  <xsl:function name="function:instance-case">
    <xsl:param name="string"/>
    <xsl:value-of select="concat(lower-case(substring($string, 1, 1)), substring(function:camel-case($string), 2))"/>
  </xsl:function>
  
  <xsl:function name="function:precision-scale">
    <xsl:param name="precision"/>
    <xsl:param name="max"/>
    <xsl:choose>
      <xsl:when test="$max = -1 or math:pow(10, $precision) &lt; $max">
        <xsl:value-of select="substring('99999999999999999999', 1, $precision)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$max"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  <xsl:template match="/ddlx:schema">
    <xs:schema
      elementFormDefault="qualified"
      xmlns:ddlx="http://rdb.libx4j.org/ddlx.xsd"
      xmlns:xs="http://www.w3.org/2001/XMLSchema">
      
      <xsl:copy-of select="ext:node-set($xmlns)/*/namespace::*[.=$namespace]"/>
      
      <xsl:attribute name="targetNamespace">
        <xsl:value-of select="$namespace"/>
      </xsl:attribute>
      
      <xsl:attribute name="jaxb:version">2.1</xsl:attribute>
      <xsl:attribute name="jaxb:extensionBindingPrefixes">annox</xsl:attribute>
      
      <xs:import namespace="http://rdb.libx4j.org/sqlx.xsd" schemaLocation="http://rdb.libx4j.org/sqlx.xsd"/>
      <xs:import namespace="http://rdb.libx4j.org/dmlx.xsd" schemaLocation="http://rdb.libx4j.org/dmlx.xsd"/>
      
      <xsl:for-each select="ddlx:table">
        <xs:complexType>
          <xsl:attribute name="name">
            <xsl:value-of select="@name"/>
          </xsl:attribute>
          <xsl:if test="not(@abstract='true')">
            <xs:annotation>
              <xs:appinfo>
                <annox:annotate>
                  <xsl:value-of select="concat('@org.libx4j.rdb.ddlx.Table(name = &quot;', @name, '&quot;)')"/>
                </annox:annotate>
              </xs:appinfo>
            </xs:annotation>
          </xsl:if>
          <xs:complexContent>
            <xs:extension>
              <xsl:attribute name="base">
                <xsl:choose>
                  <xsl:when test="@extends">
                    <xsl:value-of select="concat('ns:', @extends)"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:text>dmlx:row</xsl:text>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:attribute>
              <xsl:variable name="tableName" select="@name"/>
              <xsl:for-each select="ddlx:column">
                <xs:attribute>
                  <xsl:attribute name="name">
                    <xsl:value-of select="function:instance-case(@name)"/>
                  </xsl:attribute>
                  <xsl:if test="@null='false' and not(@default) and not(@jsql:generateOnInsert)">
                    <xsl:attribute name="use">required</xsl:attribute>
                  </xsl:if>
                  <xsl:if test="@default">
                    <xsl:attribute name="default">
                      <xsl:value-of select="@default"/>
                    </xsl:attribute>
                  </xsl:if>
                  <xsl:if test="@xsi:type='boolean'">
                    <xsl:attribute name="type">sqlx:boolean</xsl:attribute>
                  </xsl:if>
                  <xsl:if test="@xsi:type='date'">
                    <xsl:attribute name="type">sqlx:date</xsl:attribute>
                  </xsl:if>
                  <xsl:if test="@xsi:type='dateTime'">
                    <xsl:attribute name="type">sqlx:dateTime</xsl:attribute>
                  </xsl:if>
                  <xsl:if test="@xsi:type='time'">
                    <xsl:attribute name="type">sqlx:time</xsl:attribute>
                  </xsl:if>
                  <xs:annotation>
                    <xs:appinfo>
                      <annox:annotate>
                        <xsl:value-of select="concat('@org.libx4j.rdb.ddlx.Column(name = &quot;', @name, '&quot;)')"/>
                      </annox:annotate>
                    </xs:appinfo>
                  </xs:annotation>
                  <xsl:if test="@xsi:type='binary' or @xsi:type='blob'">
                    <xs:simpleType>
                      <xs:restriction>
                        <xsl:attribute name="base">sqlx:<xsl:value-of select="@xsi:type"/></xsl:attribute>
                        <xs:maxLength>
                          <xsl:attribute name="value">
                            <xsl:value-of select="@length * 2"/>
                          </xsl:attribute>
                        </xs:maxLength>
                        <xsl:if test="not(@varying='true') and not(@xsi:type='blob')">
                          <xs:minLength>
                            <xsl:attribute name="value">
                              <xsl:value-of select="@length * 2"/>
                            </xsl:attribute>
                          </xs:minLength>
                        </xsl:if>
                      </xs:restriction>
                    </xs:simpleType>
                  </xsl:if>
                  <xsl:if test="@xsi:type='char' or @xsi:type='clob'">
                    <xs:simpleType>
                      <xs:restriction>
                        <xsl:attribute name="base">sqlx:<xsl:value-of select="@xsi:type"/></xsl:attribute>
                        <xs:maxLength>
                          <xsl:attribute name="value">
                            <xsl:value-of select="@length"/>
                          </xsl:attribute>
                        </xs:maxLength>
                        <xsl:if test="not(@varying='true') and not(@xsi:type='clob')">
                          <xs:minLength>
                            <xsl:attribute name="value">
                              <xsl:value-of select="@length"/>
                            </xsl:attribute>
                          </xs:minLength>
                        </xsl:if>
                      </xs:restriction>
                    </xs:simpleType>
                  </xsl:if>
                  <xsl:if test="@xsi:type='float'">
                    <xs:simpleType>
                      <xs:restriction base="sqlx:float">
                        <xsl:if test="@max">
                          <xs:maxInclusive>
                            <xsl:attribute name="value">
                              <xsl:value-of select="@max"/>
                            </xsl:attribute>
                          </xs:maxInclusive>
                        </xsl:if>
                        <xsl:choose>
                          <xsl:when test="@min">
                            <xs:minInclusive>
                              <xsl:attribute name="value">
                                <xsl:value-of select="@min"/>
                              </xsl:attribute>
                            </xs:minInclusive>
                          </xsl:when>
                          <xsl:when test="@unsigned='true'">
                            <xs:minInclusive value="0"/>
                          </xsl:when>
                        </xsl:choose>
                      </xs:restriction>
                    </xs:simpleType>
                  </xsl:if>
                  <xsl:if test="@xsi:type='double'">
                    <xs:simpleType>
                      <xs:restriction base="sqlx:double">
                        <xsl:if test="@max">
                          <xs:maxInclusive>
                            <xsl:attribute name="value">
                              <xsl:value-of select="@max"/>
                            </xsl:attribute>
                          </xs:maxInclusive>
                        </xsl:if>
                        <xsl:choose>
                          <xsl:when test="@min">
                            <xs:minInclusive>
                              <xsl:attribute name="value">
                                <xsl:value-of select="@min"/>
                              </xsl:attribute>
                            </xs:minInclusive>
                          </xsl:when>
                          <xsl:when test="@unsigned='true'">
                            <xs:minInclusive value="0"/>
                          </xsl:when>
                        </xsl:choose>
                      </xs:restriction>
                    </xs:simpleType>
                  </xsl:if>
                  <xsl:if test="@xsi:type='decimal'">
                    <xs:simpleType>
                      <xs:restriction base="sqlx:decimal">
                        <xs:fractionDigits>
                          <xsl:attribute name="value">
                            <xsl:value-of select="@scale"/>
                          </xsl:attribute>
                        </xs:fractionDigits>
                        <xs:maxInclusive>
                          <xsl:attribute name="value">
                            <xsl:choose>
                              <xsl:when test="@max">
                                <xsl:value-of select="@max"/>
                              </xsl:when>
                              <xsl:otherwise>
                                <xsl:value-of select="function:precision-scale(@precision - @scale, -1)"/>
                              </xsl:otherwise>
                            </xsl:choose>
                          </xsl:attribute>
                        </xs:maxInclusive>
                        <xs:minInclusive>
                          <xsl:attribute name="value">
                            <xsl:choose>
                              <xsl:when test="@min">
                                <xsl:value-of select="@min"/>
                              </xsl:when>
                              <xsl:otherwise>
                                <xsl:choose>
                                  <xsl:when test="not(@unsigned='true')">
                                    <xsl:value-of select="concat('-', function:precision-scale(@precision - @scale, -1))"/>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <xsl:value-of select="0"/>
                                  </xsl:otherwise>
                                </xsl:choose>
                              </xsl:otherwise>
                            </xsl:choose>
                          </xsl:attribute>
                        </xs:minInclusive>
                      </xs:restriction>
                    </xs:simpleType>
                  </xsl:if>
                  <xsl:if test="@xsi:type='enum'">
                    <xs:simpleType>
                      <xs:restriction base="sqlx:enum">
                        <xsl:for-each select="tokenize(replace(@values, '\\ ', '\\`'), ' ')">
                          <xs:enumeration>
                            <xsl:attribute name="value">
                              <xsl:value-of select="replace(., '\\`', ' ')"/>
                            </xsl:attribute>
                          </xs:enumeration>
                        </xsl:for-each>
                      </xs:restriction>
                    </xs:simpleType>
                  </xsl:if>
                  <xsl:if test="@xsi:type='tinyint' or @xsi:type='smallint' or @xsi:type='int' or @xsi:type='bigint'">
                    <xs:simpleType>
                      <xs:restriction>
                        <xsl:attribute name="base">
                          <xsl:value-of select="concat('sqlx:', @xsi:type)"/>
                        </xsl:attribute>
                        <xs:maxInclusive>
                          <xsl:attribute name="value">
                            <xsl:choose>
                              <xsl:when test="@max">
                                <xsl:value-of select="@max"/>
                              </xsl:when>
                              <xsl:otherwise>
                                <xsl:choose>
                                  <xsl:when test="not(@unsigned='true')">
                                    <xsl:value-of select="function:precision-scale(@precision, if (@xsi:type='tinyint') then 127 else if (@xsi:type='smallint') then 32767 else if (@xsi:type='int') then 2147483647 else 9223372036854775807)"/>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <xsl:value-of select="function:precision-scale(@precision, if (@xsi:type='tinyint') then 255 else if (@xsi:type='smallint') then 64535 else if (@xsi:type='int') then 4294967295 else 18446744073709551615)"/>
                                  </xsl:otherwise>
                                </xsl:choose>
                              </xsl:otherwise>
                            </xsl:choose>
                          </xsl:attribute>
                        </xs:maxInclusive>
                        <xs:minInclusive>
                          <xsl:attribute name="value">
                            <xsl:choose>
                              <xsl:when test="@min">
                                <xsl:value-of select="@min"/>
                              </xsl:when>
                              <xsl:otherwise>
                                <xsl:choose>
                                  <xsl:when test="not(@unsigned='true')">
                                    <xsl:value-of select="concat('-', function:precision-scale(@precision, if (@xsi:type='tinyint') then 128 else if (@xsi:type='smallint') then 32768 else if (@xsi:type='int') then 2147483648 else 9223372036854775808))"/>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <xsl:value-of select="0"/>
                                  </xsl:otherwise>
                                </xsl:choose>
                              </xsl:otherwise>
                            </xsl:choose>
                          </xsl:attribute>
                        </xs:minInclusive>
                      </xs:restriction>
                    </xs:simpleType>
                  </xsl:if>
                </xs:attribute>
              </xsl:for-each>
            </xs:extension>
          </xs:complexContent>
        </xs:complexType>
      </xsl:for-each>
      
      <xs:complexType name="insert">
        <xs:complexContent>
          <xs:extension base="dmlx:insert">
            <xs:sequence>
              <xsl:for-each select="ddlx:table">
                <xsl:sort select="function:computeWeight(., 0)" data-type="number"/>
                <xsl:if test="not(@abstract='true')">
                  <xs:element minOccurs="0" maxOccurs="unbounded">
                    <xsl:attribute name="name">
                      <xsl:value-of select="function:instance-case(@name)"/>
                    </xsl:attribute>
                    <xsl:attribute name="type">
                      <xsl:value-of select="concat('ns:', @name)"/>
                    </xsl:attribute>
                  </xs:element>
                </xsl:if>
              </xsl:for-each>
            </xs:sequence>
          </xs:extension>
        </xs:complexContent>
      </xs:complexType>
      
      <xs:complexType>
        <xsl:attribute name="name">
          <xsl:value-of select="function:instance-case(@name)"/>
        </xsl:attribute>
        <xs:annotation>
          <xs:appinfo>
            <annox:annotate>
              <xsl:value-of select="concat('@org.libx4j.rdb.ddlx.Schema(name = &quot;', @name, '&quot;)')"/>
            </annox:annotate>
          </xs:appinfo>
        </xs:annotation>
        <xs:complexContent>
          <xs:extension base="dmlx:database">
          <xs:sequence>
            <xs:element name="insert" type="ns:insert" minOccurs="0" maxOccurs="1"/>
          </xs:sequence>
          </xs:extension>
        </xs:complexContent>
      </xs:complexType>
      
      <xs:element>
        <xsl:attribute name="name">
          <xsl:value-of select="function:instance-case(@name)"/>
        </xsl:attribute>
        <xsl:attribute name="type">
          <xsl:value-of select="concat('ns:', function:instance-case(@name))"/>
        </xsl:attribute>
        <xsl:for-each select="ddlx:table[not(@abstract='true')]">
          <xsl:variable name="primaryKey" select="function:getPrimaryKey(current())"/>
          <xsl:if test="$primaryKey">
            <xs:key>
              <xsl:attribute name="name">
                <xsl:value-of select="concat('key', function:instance-case(@name))"/>
              </xsl:attribute>
              <xs:selector>
                <xsl:attribute name="xpath">
                  <xsl:value-of select="concat('ns:insert/ns:', function:instance-case(@name))"/>
                </xsl:attribute>
              </xs:selector>
              <xsl:for-each select="$primaryKey/ddlx:column">
                <xs:field>
                  <xsl:attribute name="xpath">
                    <xsl:value-of select="concat('@', function:instance-case(@name))"/>
                  </xsl:attribute>
                </xs:field>
              </xsl:for-each>
            </xs:key>
          </xsl:if>
          <xsl:for-each select="ddlx:column">
            <xsl:if test="ddlx:foreignKey">
              <xs:keyref>
                <xsl:attribute name="refer">
                  <xsl:value-of select="concat('ns:key', function:instance-case(ddlx:foreignKey/@references))"/>
                </xsl:attribute>
                <xsl:attribute name="name">
                  <xsl:value-of select="concat('keyRef', function:instance-case(../@name), function:instance-case(ddlx:foreignKey/@references), function:instance-case(ddlx:foreignKey/@column))"/>
                </xsl:attribute>
                <xs:selector>
                  <xsl:attribute name="xpath">
                    <xsl:value-of select="concat('ns:insert/ns:', function:instance-case(../@name))"/>
                  </xsl:attribute>
                </xs:selector>
                <xs:field>
                  <xsl:attribute name="xpath">
                    <xsl:value-of select="concat('@', function:instance-case(@name))"/>
                  </xsl:attribute>
                </xs:field>
              </xs:keyref>
            </xsl:if>
          </xsl:for-each>
        </xsl:for-each>
      </xs:element>
    </xs:schema>
  </xsl:template>
    
</xsl:stylesheet>