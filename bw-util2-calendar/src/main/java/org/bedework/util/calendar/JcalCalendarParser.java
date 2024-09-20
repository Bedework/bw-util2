/* ********************************************************************
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
*/
/*
 * Copyright (c) 2010, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 *  o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.bedework.util.calendar;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import net.fortuna.ical4j.data.CalendarParser;
import net.fortuna.ical4j.data.ContentHandler;
import net.fortuna.ical4j.data.ParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

/**
 * Parses and builds an iCalendar model from a json input stream.
 * Note that this class is not thread-safe.
 *
 * @version 1.0
 * @author Mike Douglass
 *
 * Created: Sept 8, 2010
 *
 */
public class JcalCalendarParser implements CalendarParser {
  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  private final static JsonFactory jsonFactory;

  static {
    jsonFactory = new JsonFactory();
    jsonFactory.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    jsonFactory.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
  }

  //private Logger log = Logger.getLogger(XmlCalendarBuilder.class);

  private String lastComponent;
  private String lastProperty;

  @Override
  public void parse(final InputStream in,
                    final ContentHandler handler)
          throws ParserException {
    parse(new InputStreamReader(in, DEFAULT_CHARSET),
          handler);
  }

  @Override
  public void parse(final Reader in,
                    final ContentHandler handler)
          throws ParserException {
    try {
      final JsonParser parser = jsonFactory.createParser(in);

      process(parser, handler);
    } catch (final Throwable t) {
      throw new ParserException(t.getMessage(), 0, t);
    }
  }

  private void process(final JsonParser parser,
                       final ContentHandler handler)
          throws ParserException {
    /* ["vcalendar",
          [ <properties> ],
          [ <components> ]
      ]
      */

    try {
      arrayStart(parser);
      final String ctype = textField(parser);

      if (!ctype.equals("vcalendar")) {
        // error
        throwException("Expected vcalendar: found " + ctype, parser);
      }

      lastComponent = "vcalendar";

      processVcalendar(parser, handler);

      arrayEnd(parser);
    } catch (final Throwable t) {
      handleException(t, parser);
    }
  }

  private void processVcalendar(final JsonParser parser,
                                final ContentHandler handler)
          throws ParserException {
    handler.startCalendar();

    /* Properties first */
    processProperties(parser, handler);

    /* Now components */
    processCalcomps(parser, handler);

    try {
      handler.endCalendar();
    } catch (final Throwable t) {
      throw new RuntimeException(t);
    }
  }

  private void processProperties(final JsonParser parser,
                                 final ContentHandler handler)
          throws ParserException {
    arrayStart(parser);

    while (!testArrayEnd(parser)) {
      processProperty(parser, handler);
    }
  }

  private void processCalcomps(final JsonParser parser,
                               final ContentHandler handler)
          throws ParserException {
    arrayStart(parser);

    while (!testArrayEnd(parser)) {
      processComponent(parser, handler);
    }
  }

  private void processComponent(final JsonParser parser,
                                final ContentHandler handler)
          throws ParserException {
    currentArrayStart(parser);

    final String cname = textField(parser).toUpperCase();
    lastComponent = cname;
    handler.startComponent(cname);

    /* Properties first */
    processProperties(parser, handler);

    /* Now components */
    processCalcomps(parser, handler);

    handler.endComponent(cname);

    arrayEnd(parser);
  }

  private void processProperty(final JsonParser parser,
                               final ContentHandler handler)
          throws ParserException {
    /* Each individual iCalendar property is represented in jCal by an array
      with three fixed elements, followed by at one or more additional
      elements, depending on if the property is a multi-value property as
      described in Section 3.1.2 of [RFC5545].

      The array consists of the following fixed elements:
      1. The name of the property as a string, but in lowercase.
      2. An object containing the parameters as described in Section 3.5.
      3. The type identifier string of the value, in lowercase.

      The remaining elements of the array are used for the value of the
      property. For single-value properties, the array MUST have exactly
      four elements, for multi-valued properties as described in
      Section 3.4.1.1 there can be any number of additional elements.

      array start should be current token
    */

    currentArrayStart(parser);

    final String name = textField(parser);
    lastProperty = name;
    handler.startProperty(name);

    processParameters(parser, handler);

    final boolean parseArrayEnd =
            processValue(parser, handler, textField(parser));

    try {
      handler.endProperty(name);
    } catch (final Throwable t) {
      handleException(t, parser);
    }

    if (parseArrayEnd) {
      arrayEnd(parser);
    }
  }

  private void processParameters(final JsonParser parser,
                                 final ContentHandler handler)
          throws ParserException {
    objectStart(parser);

    while (!testObjectEnd(parser)) {
      processParameter(parser, handler);
    }
  }

  private void processParameter(final JsonParser parser,
                                final ContentHandler handler)
          throws ParserException {
    try {
      handler.parameter(currentFieldName(parser),
                        textField(parser));
    } catch (final Throwable t) {
      handleException(t, parser);
    }
  }

  private boolean processValue(final JsonParser parser,
                               final ContentHandler handler,
                               final String type)
          throws ParserException {
    var parseArrayEnd = true;

    try {
      if (lastProperty.equals("geo")) {
        // 2 floats in an array
        arrayStart(parser);

        final StringBuilder sb = new StringBuilder();

        sb.append(floatField(parser));
        sb.append(",");
        sb.append(floatField(parser));

        arrayEnd(parser);

        handler.propertyValue(sb.toString());

        return true;
      }

      if (lastProperty.equals("request-status")) {
        arrayStart(parser);

        final StringBuilder sb = new StringBuilder();

        sb.append(textField(parser));
        sb.append(",");
        sb.append(textField(parser));

        if (!testArrayEnd(parser)) {
          sb.append(",");
          sb.append(currentTextField(parser));

          arrayEnd(parser);
        }

        handler.propertyValue(sb.toString());

        return true;
      }

      final StringBuilder sb;
      switch (type) {
        case "recur":
          /*
            value-recur = element recur {
              type-freq,
              (type-until | type-count)?,
              element interval  { text }?,
              element bysecond  { text }*,
              element byminute  { text }*,
              element byhour    { text }*,
              type-byday*,
              type-bymonthday*,
              type-byyearday*,
              type-byweekno*,
              element bymonth   { text }*,
              type-bysetpos*,
              element wkst { type-weekday }?
          }

          */
          sb = new StringBuilder();

          String delim = "";

          objectStart(parser);

          while (!testObjectEnd(parser)) {
            sb.append(delim);
            delim = ";";
            final String recurEl = currentFieldName(parser);
            sb.append(recurEl.toUpperCase());
            sb.append("=");
            sb.append(recurElVal(parser, recurEl));
          }

          handler.propertyValue(sb.toString());

          break;

        case "boolean":
          handler.propertyValue(String.valueOf(
                  booleanField(parser)));

          break;

        case "utc-offset":
          handler.propertyValue(
                  XcalUtil.getIcalUtcOffset(textField(parser)));

          break;

        case "binary":
        case "cal-address":
        case "duration":
        case "text":
        case "uri":
          sb = new StringBuilder();
          delim = "";

          while (!testArrayEnd(parser)) {
            sb.append(delim);
            delim = ",";
            sb.append(currentTextField(parser));
          }

          handler.propertyValue(sb.toString());

          parseArrayEnd = false;
          break;

        case "integer":
          handler.propertyValue(String.valueOf(intField(
                  parser)));

          break;

        case "float":
          handler.propertyValue(String.valueOf(intField(
                  parser)));

          break;

        case "date":
        case "date-time":
          handler.propertyValue(
                  XcalUtil.getIcalFormatDateTime(
                          textField(parser)));
          break;

        case "time":
          handler.propertyValue(
                  XcalUtil.getIcalFormatTime(textField(parser)));
          break;

        case "period":
          final String[] parts = textField(parser).split("/");

          sb = new StringBuilder();

          sb.append(XcalUtil.getIcalFormatDateTime(parts[0]));

          if (parts[1].toUpperCase().startsWith("P")) {
            sb.append(parts[1]);
          } else {
            sb.append(XcalUtil.getIcalFormatDateTime(parts[1]));
          }

          handler.propertyValue(sb.toString());

          break;

        default:
          throwException("Bad property", parser);
          return false;
      }
    } catch (final URISyntaxException |
            ParseException |
            IOException e) {
      throw new ParserException(e.getMessage(), 0, e);
    }

    return parseArrayEnd;
  }

  private String recurElVal(final JsonParser parser,
                            final String el) throws ParserException {
    if (el.equals("freq")) {
      return textField(parser);
    }

    if (el.equals("wkst")) {
      return textField(parser);
    }

    if (el.equals("until")) {
      return textField(parser);
    }

    if (el.equals("count")) {
      return String.valueOf(intField(parser));
    }

    if (el.equals("interval")) {
      return String.valueOf(intField(parser));
    }

    if (el.equals("bymonth")) {
      return intList(parser);
    }

    if (el.equals("byweekno")) {
      return intList(parser);
    }

    if (el.equals("byyearday")) {
      return intList(parser);
    }

    if (el.equals("bymonthday")) {
      return intList(parser);
    }

    if (el.equals("byday")) {
      return textList(parser);
    }

    if (el.equals("byhour")) {
      return intList(parser);
    }

    if (el.equals("byminute")) {
      return intList(parser);
    }

    if (el.equals("bysecond")) {
      return intList(parser);
    }

    if (el.equals("bysetpos")) {
      return intList(parser);
    }

    throwException("Unexpected recur field " + el, parser);
    return null;
  }

  /* ====================================================================
   *                   XmlUtil wrappers
   * ==================================================================== */

  private void throwException(final String msg,
                              final JsonParser parser) throws ParserException {
    final StringBuilder augmented = new StringBuilder(msg);

    if (lastComponent != null) {
      augmented.append("; last component=");
      augmented.append(lastComponent);
    }

    if (lastProperty != null) {
      augmented.append("; last property=");
      augmented.append(lastProperty);
    }

    handleException(new Throwable(augmented.toString()), parser);
  }

  private Object handleException(final Throwable t,
                                 final JsonParser parser) throws ParserException {
    if (t instanceof ParserException) {
      throw (ParserException)t;
    }

    try {
      final int lnr = parser.getCurrentLocation().getLineNr();
      throw new ParserException(t.getLocalizedMessage(), lnr);
    } catch (final ParserException pe) {
      throw pe;
    } catch (final Throwable t1) {
      throw new ParserException(t.getLocalizedMessage(), -1);
    }
  }

  private void arrayStart(final JsonParser parser)
          throws ParserException {
    expectToken(parser, JsonToken.START_ARRAY,
                "Expected array start");
  }

  private void arrayEnd(final JsonParser parser)
          throws ParserException {
    expectToken(parser, JsonToken.END_ARRAY,
                "Expected array end");
  }

  private void currentArrayStart(final JsonParser parser)
          throws ParserException {
    expectCurrentToken(parser, JsonToken.START_ARRAY,
                       "Expected array start");
  }

  private boolean testNextArrayStart(final JsonParser parser)
          throws ParserException {
    return testToken(parser, JsonToken.START_ARRAY);
  }

  private boolean testArrayEnd(final JsonParser parser)
          throws ParserException {
    return testToken(parser, JsonToken.END_ARRAY);
  }

  private void objectStart(final JsonParser parser)
          throws ParserException {
    expectToken(parser, JsonToken.START_OBJECT,
                "Expected object start");
  }

  private boolean testObjectEnd(final JsonParser parser)
          throws ParserException {
    return testToken(parser, JsonToken.END_OBJECT);
  }

  private void expectToken(final JsonParser parser,
                           final JsonToken expected,
                           final String message) throws ParserException {
    try {
      final JsonToken t = parser.nextToken();

      if (t != expected) {
        throwException(message, parser);
      }
    } catch (final ParserException pe) {
      throw pe;
    } catch (final Throwable t) {
      handleException(t, parser);
    }
  }

  private void expectCurrentToken(final JsonParser parser,
                                  final JsonToken expected,
                                  final String message) throws ParserException {
    try {
      final JsonToken t = parser.getCurrentToken();

      if (t != expected) {
        throwException(message, parser);
      }
    } catch (final ParserException pe) {
      throw pe;
    } catch (final Throwable t) {
      handleException(t, parser);
    }
  }

  private boolean testCurrentToken(final JsonParser parser,
                                   final JsonToken expected) throws ParserException {
    try {
      final JsonToken t = parser.getCurrentToken();

      return t == expected;
    } catch (final Throwable t) {
      handleException(t, parser);
      return false;
    }
  }

  private boolean testToken(final JsonParser parser,
                            final JsonToken expected) throws ParserException {
    try {
      final JsonToken t = parser.nextToken();

      return t == expected;
    } catch (final Throwable t) {
      return (Boolean)handleException(t, parser);
    }
  }

  private String textField(final JsonParser parser) throws ParserException {

    expectToken(parser, JsonToken.VALUE_STRING,
                "Expected string field");
    try {
      return parser.getText();
    } catch (final Throwable t) {
      return (String)handleException(t, parser);
    }
  }

  private String currentTextField(final JsonParser parser) throws ParserException {
    try {
      return parser.getText();
    } catch (final Throwable t) {
      return (String)handleException(t, parser);
    }
  }

  private int currentIntField(final JsonParser parser) throws ParserException {
    try {
      return parser.getIntValue();
    } catch (final Throwable t) {
      return (Integer)handleException(t, parser);
    }
  }

  private int intField(final JsonParser parser) throws ParserException {
    expectToken(parser, JsonToken.VALUE_NUMBER_INT,
                "Expected integer field");
    try {
      return parser.getIntValue();
    } catch (final Throwable t) {
      return (Integer)handleException(t, parser);
    }
  }

  private float floatField(final JsonParser parser) throws ParserException {
    expectToken(parser, JsonToken.VALUE_NUMBER_FLOAT,
                "Expected float field");
    try {
      return parser.getFloatValue();
    } catch (final Throwable t) {
      return (Float)handleException(t, parser);
    }
  }

  private boolean booleanField(final JsonParser parser) throws ParserException {
    try {
      if (parser.getCurrentToken() == JsonToken.VALUE_FALSE) {
        return false;
      }

      if (parser.getCurrentToken() == JsonToken.VALUE_TRUE) {
        return true;
      }

      throwException("expected boolean constant", parser);
      return false;
    } catch (final Throwable t) {
      return (Boolean)handleException(t, parser);
    }
  }

  private String currentFieldName(final JsonParser parser) throws ParserException {
    expectCurrentToken(parser, JsonToken.FIELD_NAME,
                "Expected field name");
    try {
      return parser.getText();
    } catch (final Throwable t) {
      return (String)handleException(t, parser);
    }
  }

  private String textList(final JsonParser parser) throws ParserException {
    if (!testNextArrayStart(parser)) {
      // Single textt value
      return currentTextField(parser);
    }

    final StringBuilder sb = new StringBuilder();
    String delim = "";
    while (!testArrayEnd(parser)) {
      sb.append(delim);
      delim = ",";
      sb.append(currentTextField(parser));
    }

    return sb.toString();
  }

  private String intList(final JsonParser parser) throws ParserException {
    if (!testNextArrayStart(parser)) {
      // Single int value
      return String.valueOf(currentIntField(parser));
    }

    final StringBuilder sb = new StringBuilder();
    String delim = "";
    while (!testArrayEnd(parser)) {
      sb.append(delim);
      delim = ",";
      sb.append(currentIntField(parser));
    }

    return sb.toString();
  }
}
