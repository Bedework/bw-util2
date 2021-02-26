/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.util.calendar;

import org.bedework.util.timezones.Timezones;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.TreeSet;

/**
 * User: mike Date: 2/26/21 Time: 14:22
 */
public class IcalendarUtil {
  /** Make a new Calendar with default properties
   *
   * @param methodType - ical method
   * @return Calendar
   */
  public static Calendar newIcal(final int methodType,
                                 final String prodId) {
    final Calendar cal = new Calendar();

    final PropertyList<Property> pl = cal.getProperties();

    pl.add(new ProdId(prodId));
    pl.add(Version.VERSION_2_0);

    if ((methodType > ScheduleMethods.methodTypeNone) &&
            (methodType < ScheduleMethods.methodTypeUnknown)) {
      pl.add(new Method(ScheduleMethods.methods[methodType]));
    }

    return cal;
  }

  /** Create a Calendar object from the named timezone
   *
   * @param tzid       String timezone id
   * @return Calendar
   */
  public static Calendar getTzCalendar(final String tzid,
                                       final String prodId) {
    final Calendar cal = newIcal(ScheduleMethods.methodTypeNone,
                                 prodId);

    addIcalTimezone(cal, tzid, null, Timezones.getTzRegistry());

    return cal;
  }

  public static void addIcalTimezone(
          final Calendar cal,
          final String tzid,
          final TreeSet<String> added,
          final TimeZoneRegistry tzreg) {
    VTimeZone vtz = null;

    if ((tzid == null) ||
            ((added != null) && added.contains(tzid))) {
      return;
    }

    //if (debug()) {
    //  debug("Look for timezone with id " + tzid);
    //}

    final TimeZone tz = tzreg.getTimeZone(tzid);

    if (tz != null) {
      vtz = tz.getVTimeZone();
    }

    if (vtz != null) {
      //if (debug()) {
      //  debug("found timezone with id " + tzid);
      //}
      cal.getComponents().add(vtz);
    }

    if (added != null) {
      added.add(tzid);
    }
  }

  /** Create a Calendar object from the named timezone and convert to
   * a String representation
   *
   * @param tzid       String timezone id
   * @return String
   * @throws RuntimeException on fatal error
   */
  public static String toStringTzCalendar(final String tzid,
                                          final String prodId) {
    final Calendar ical = getTzCalendar(tzid, prodId);

    final CalendarOutputter calOut = new CalendarOutputter(true);

    final StringWriter sw = new StringWriter();

    try {
      calOut.output(ical, sw);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }

    return sw.toString();
  }

  /** Convert a Calendar to it's string form
   *
   * @param cal Calendar to convert
   * @return String representation
   * @throws RuntimeException on fatal error
   */
  public static String toIcalString(final Calendar cal) {
    final Writer wtr =  new StringWriter();
    writeCalendar(cal, wtr);

    return wtr.toString();
  }

  /** Write a Calendar
   *
   * @param cal Calendar to convert
   * @param wtr Writer for output
   * @throws RuntimeException on fatal error
   */
  public static void writeCalendar(final Calendar cal,
                                   final Writer wtr) {
    final CalendarOutputter co = new CalendarOutputter(false, 74);

    try {
      co.output(cal, wtr);
    } catch (final Throwable t) {
      throw new RuntimeException(t);
    }
  }
}
