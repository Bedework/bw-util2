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
package org.bedework.util.calendar;

import net.fortuna.ical4j.data.CalendarParser;
import net.fortuna.ical4j.data.ContentHandler;
import net.fortuna.ical4j.data.DefaultContentHandler;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.function.Consumer;

/**
 * Parses and builds an iCalendar model from an xml input stream.
 * Note that this class is not thread-safe.
 *
 * @version 1.0
 * @author Mike Douglass
 *
 * Created: Sept 8, 2010
 *
 */
public class XmlCalendarBuilder implements Consumer<Calendar> {
  //private Logger log = Logger.getLogger(XmlCalendarBuilder.class);

  private final CalendarParser parser;

  private final ContentHandler contentHandler;

  private final TimeZoneRegistry tzRegistry;

  /**
   * The calendar instance created by the builder.
   */
  private Calendar calendar;

  /**
   */
  public XmlCalendarBuilder() {
    this(TimeZoneRegistryFactory.getInstance().createRegistry());
  }

  /**
   * @param tzRegistry a custom timezone registry
   */
  public XmlCalendarBuilder(final TimeZoneRegistry tzRegistry) {
    this.parser = new XcalCalendarParser();
    this.tzRegistry = tzRegistry;
    this.contentHandler = new DefaultContentHandler(this, tzRegistry);
  }

  @Override
  public void accept(final Calendar calendar) {
    this.calendar = calendar;
  }

  /**
   * Builds an iCalendar model from the specified input stream.
   * @param in an input stream to read calendar data from
   * @return a calendar parsed from the specified input stream
   * @throws IOException where an error occurs reading data from the specified stream
   * @throws ParserException where an error occurs parsing data from the stream
   */
  public Calendar build(final InputStream in)
          throws IOException, ParserException {
    parser.parse(in, contentHandler);

    return calendar;
  }

  /**
   * Build an iCalendar model by parsing data from the specified reader.
   *
   * @param in an unfolding reader to read data from
   * @return a calendar parsed from the specified reader
   * @throws IOException where an error occurs reading data from the specified reader
   * @throws ParserException where an error occurs parsing data from the reader
   */
  public Calendar build(final Reader in)
          throws IOException, ParserException {
    parser.parse(in, contentHandler);

    return calendar;
  }

  /**
   * Returns the timezone registry used in the construction of calendars.
   * @return a timezone registry
   */
  public final TimeZoneRegistry getRegistry() {
    return tzRegistry;
  }
}
