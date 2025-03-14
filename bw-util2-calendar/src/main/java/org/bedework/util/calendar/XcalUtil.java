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

import org.bedework.base.exc.BedeworkException;
import org.bedework.util.xml.tagdefs.XcalTags;

import ietf.params.xml.ns.icalendar_2.ArrayOfParameters;
import ietf.params.xml.ns.icalendar_2.BaseComponentType;
import ietf.params.xml.ns.icalendar_2.BaseParameterType;
import ietf.params.xml.ns.icalendar_2.BasePropertyType;
import ietf.params.xml.ns.icalendar_2.DateDatetimePropertyType;
import ietf.params.xml.ns.icalendar_2.DaylightType;
import ietf.params.xml.ns.icalendar_2.IcalendarType;
import ietf.params.xml.ns.icalendar_2.ObjectFactory;
import ietf.params.xml.ns.icalendar_2.StandardType;
import ietf.params.xml.ns.icalendar_2.TzidParamType;
import ietf.params.xml.ns.icalendar_2.UntilRecurType;
import ietf.params.xml.ns.icalendar_2.ValarmType;
import ietf.params.xml.ns.icalendar_2.VcalendarType;
import ietf.params.xml.ns.icalendar_2.VeventType;
import ietf.params.xml.ns.icalendar_2.VfreebusyType;
import ietf.params.xml.ns.icalendar_2.VjournalType;
import ietf.params.xml.ns.icalendar_2.VtimezoneType;
import ietf.params.xml.ns.icalendar_2.VtodoType;
import jakarta.xml.bind.JAXBElement;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

/**
 * @author douglm
 *
 */
public class XcalUtil {
  private static final ObjectFactory icalOf = new ObjectFactory();
  static Map<Class<?>, QName> compNames = new HashMap<>();

  /** */
  public static final Integer UnknownKind = -1;

  /** */
  public static final Integer OuterKind = 0;
  /** */
  public static final Integer RecurringKind = 1;
  /** */
  public static final Integer UidKind = 2;
  /** */
  public static final Integer AlarmKind = 3;
  /** */
  public static final Integer TzKind = 4;
  /** */
  public static final Integer TzDaylight = 5;
  /** */
  public static final Integer TzStandard = 6;

  static Map<QName, Integer> compKinds = new HashMap<>();

  static {
    // Outer container
    addInfo(XcalTags.vcalendar,
            OuterKind,
            VcalendarType.class);

    // Recurring style uid + optional recurrence id
    addInfo(XcalTags.vtodo,
            RecurringKind,
            VtodoType.class);
    addInfo(XcalTags.vjournal,
            RecurringKind,
            VjournalType.class);
    addInfo(XcalTags.vevent,
            RecurringKind,
            VeventType.class);

    // Uid only
    addInfo(XcalTags.vfreebusy,
            UidKind,
            VfreebusyType.class);

    addInfo(XcalTags.valarm,
            AlarmKind,
            ValarmType.class);

    // Timezones
    addInfo(XcalTags.standard,
            TzStandard,
            StandardType.class);
    addInfo(XcalTags.vtimezone,
            TzKind,
            VtimezoneType.class);
    addInfo(XcalTags.daylight,
            TzDaylight,
            DaylightType.class);
  }

  /** Initialize the DateDatetimeProperty
   * @param dt to be initialized
   * @param dtval date string
   * @param tzid timezone id
   * @throws BedeworkException on bad date
   */
  public static void initDt(final DateDatetimePropertyType dt,
                            final String dtval,
                            final String tzid) {
    final XMLGregorianCalendar xgc = fromDtval(dtval);

    if (dtval.length() == 8) {
      dt.setDate(xgc);
      return;
    }

    dt.setDateTime(xgc);

    if (dtval.endsWith("Z") || (tzid == null)) {
      return;
    }

    final TzidParamType tz = new TzidParamType();
    tz.setText(tzid);

    ArrayOfParameters aop = dt.getParameters();

    if (aop == null) {
      aop = new ArrayOfParameters();
      dt.setParameters(aop);
    }

    aop.getBaseParameter().add(icalOf.createTzid(tz));
  }

  /** Initialize the recur property
   * @param dt to be initialized
   * @param dtval to convert
   */
  public static void initUntilRecur(final UntilRecurType dt,
                                    final String dtval) {
    final XMLGregorianCalendar xgc = fromDtval(dtval);

    if (dtval.length() == 8) {
      dt.setDate(xgc);
      return;
    }

    dt.setDateTime(xgc);
  }

  /**
   * @param dtval to convert
   * @return XMLGregorianCalendar
   * @throws BedeworkException on bad date or new instance failure
   */
  public static XMLGregorianCalendar fromDtval(final String dtval) {
    final DatatypeFactory dtf;
    try {
      dtf = DatatypeFactory.newInstance();
    } catch (final DatatypeConfigurationException dce) {
      throw new BedeworkException(dce);
    }

    return dtf.newXMLGregorianCalendar(getXmlFormatDateTime(dtval));
  }

  /**
   * @param dur string duration
   * @return Duration
   */
  public static Duration makeXmlDuration(final String dur) {
    final DatatypeFactory dtf;
    try {
      dtf = DatatypeFactory.newInstance();
    } catch (final DatatypeConfigurationException dce) {
      throw new BedeworkException(dce);
    }

    return dtf.newDuration(dur);
  }

  /**
   * @param dtval to convert
   * @return utc
   */
  public static XMLGregorianCalendar getXMlUTCCal(final String dtval) {
    final DatatypeFactory dtf;
    try {
      dtf = DatatypeFactory.newInstance();
    } catch (final DatatypeConfigurationException dce) {
      throw new BedeworkException(dce);
    }

    return dtf.newXMLGregorianCalendar(getXmlFormatDateTime(dtval));
  }

  /** */
  public static class DtTzid {
    /** yyyymmdd or yyyymmddThhmmss or yyyymmddThhmmssZ */
    public String dt;

    /** true if dt represents a date */
    public boolean dateOnly;

    /** null or tzid from param */
    public String tzid;
  }

  /**
   * Class allowing fetch of timezones
   */
  public interface TzGetter {
    /**
     * @param id of timezone
     * @return A timezone or null if non found
     * @throws Throwable on fatal error
     */
    TimeZone getTz(String id) throws Throwable;
  }

  /** For date only values and floating convert to local UTC. For UTC just return
   * the value. For non-floating convert.
   *
   * @param dt to convert
   * @param tzs tz getter
   * @return string UTC value
   */
  public static String getUTC(final DateDatetimePropertyType dt,
                              final TzGetter tzs) {
    final var dtz = getDtTzid(dt);

    if ((dtz.dt.length() == 18) && (dtz.dt.charAt(17) == 'Z')) {
      return dtz.dt;
    }

    try {
      TimeZone tz = null;
      if (dtz.tzid != null) {
        tz = tzs.getTz(dtz.tzid);
      }

      final DateTime dtim = new DateTime(dtz.dt, tz);

      dtim.setUtc(true);

      return dtim.toString();
    } catch (final Throwable t) {
      throw new BedeworkException(t);
    }
  }

  /**
   * @param dt date time property
   * @return DtTzid filled in
   */
  public static DtTzid getDtTzid(final DateDatetimePropertyType dt) {
    final var res = new DtTzid();

    final ArrayOfParameters aop = dt.getParameters();

    if (aop != null) {
      for (final var e: aop.getBaseParameter()) {
        if (e.getName().equals(XcalTags.tzid)) {
          res.tzid = ((TzidParamType)e.getValue()).getText();
          break;
        }
      }
    }

    res.dateOnly = dt.getDate() != null;
    if (res.dateOnly) {
      res.dt = getIcalFormatDateTime(dt.getDate().toString());
    } else {
      res.dt = getIcalFormatDateTime(dt.getDateTime().toString());
    }

    return res;
  }

  /**
   * @param val ical format or xml format date or datetime
   * @return XML formatted
   */
  public static String getXmlFormatDateTime(final String val) {
    if (val.charAt(4) == '-') {
      // XML format
      return val;
    }

    if (val.length() < 8) {
      throw new BedeworkException("Bad date: " + val);
    }

    final StringBuilder sb = new StringBuilder();

    sb.append(val, 0, 4)
      .append("-").append(val, 4, 6)
      .append("-").append(val, 6, 8);

    if (val.length() > 8) {
      sb.append("T").append(val, 9, 11)
        .append(":").append(val, 11, 13)
        .append(":").append(val.substring(13));
    }

    return sb.toString();
  }

  /**
   * @param dt to reformat to ical format
   * @return rfc5545 date or date/time
   */
  public static String getIcalFormatDateTime(final XMLGregorianCalendar dt) {
    if (dt == null) {
      return null;
    }

    return getIcalFormatDateTime(dt.toXMLFormat());
  }

  private  final static Pattern xmlDatePattern =
          Pattern.compile(
                  "(\\d\\d\\d\\d-\\d\\d-\\d\\d)" + // date
                          "(T\\d\\d:\\d\\d:\\d\\dZ?)?" +  // [time]
                          "(.*)");        // trailing junk

  private  final static Pattern icalDatePattern =
          Pattern.compile(
                  "(\\d{8})" + // date
                          "(T\\d{6}Z?)?" +  // [time]
                          "(.*)");        // trailing junk

  /**
   * @param dt to reformat to ical format
   * @return rfc5545 date or date/time or null for none or invalid
   */
  public static String getIcalFormatDateTime(final String dt) {
    if (dt == null) {
      return null;
    }

    if (dt.length() <= 16) {
      final Matcher m = icalDatePattern.matcher(dt);
      if (m.matches()) {
        // Already ical?
        final String g3 = m.group(3);

        if ((g3 != null) && (!g3.isEmpty())) {
          return null; // trailing junk
        }

        return dt;
      }
    }

    final Matcher m = xmlDatePattern.matcher(dt);
    if (!m.matches()) {
      return null;
    }

    final String g3 = m.group(3);

    if ((g3 != null) && (!g3.isEmpty())) {
      return null; // trailing junk
    }

    final StringBuilder sb = new StringBuilder()
            .append(dt, 0, 4)
            .append(dt, 5, 7)
            .append(dt, 8, 10);

    if (dt.length() > 10) {
      sb.append("T")
        .append(dt, 11, 13)
        .append(dt, 14, 16)
        .append(dt, 17, 19);

      if (dt.endsWith("Z")) {
        sb.append("Z");
      }
    }

    return sb.toString();
  }

  /**
   * @param val ical format or xml format time
   * @return XML formatted
   */
  public static String getXmlFormatTime(final String val) {
    if (val.charAt(2) == ':') {
      // XML format
      return val;
    }

    return val.substring(0, 2) +
            ":" +
            val.substring(2, 4) +
            ":" +
            val.substring(4);
  }

  /**
   * @param tm to convert
   * @return rfc5545 time
   */
  public static String getIcalFormatTime(final String tm) {
    if (tm == null) {
      return null;
    }

    if (tm.charAt(2) != ':') {
      // Already Ical format
      return tm;
    }

    return tm.substring(0, 2) +
            tm.substring(3, 5) +
            tm.substring(6);
  }

  /**
   * @param tm date time value
   * @return rfc5545 time
   */
  public static String getIcalUtcOffset(final String tm) {
    if (tm == null) {
      return null;
    }

    if (tm.charAt(3) != ':') {
      // Already Ical format
      return tm;
    }

    return tm.substring(0, 3) +
            tm.substring(4);
  }

  /**
   * @param val ical format or xml format date or datetime
   * @return XML formatted
   */
  public static String getXmlFormatUtcOffset(final String val) {
    if (val.charAt(3) == ':') {
      // XML format
      return val;
    }

    return val.substring(0, 3) +
            ":" +
            val.substring(3);
  }

  /**
   * @param val to clone
   * @return cloned empty component
   * @throws BedeworkException for illegal access exception
   */
  public static BaseComponentType cloneComponent(final BaseComponentType val) {
    try {
      return val.getClass().newInstance();
    } catch (final Throwable t) {
      throw new BedeworkException(t);
    }
  }

  /**
   * @param val to clone
   * @return cloned empty property
   */
  public static BasePropertyType cloneProperty(final BasePropertyType val) {
    try {
      return val.getClass().newInstance();
    } catch (final Throwable t) {
      throw new BedeworkException(t);
    }
  }

  /**
   * @param val to clone
   * @return cloned empty parameter
   */
  public static BaseParameterType cloneProperty(final BaseParameterType val) {
    try {
      return val.getClass().newInstance();
    } catch (final Throwable t) {
      throw new BedeworkException(t);
    }
  }

  /**
   * @param ical Icalendar value
   * @param name of component
   * @return null or first matching component
   */
  public static BaseComponentType findComponent(final IcalendarType ical,
                                                final QName name) {
    for (final var v: ical.getVcalendar()) {
      if (name.equals(XcalTags.vcalendar)) {
        return v;
      }

      final var bc = findComponent(v, name);
      if (bc != null) {
        return bc;
      }
    }

    return null;
  }

  /** Get enclosed components for the supplied component.
   *
   * @param c component
   * @return list of components or null for none or unrecognized class.
   */
  public static List<JAXBElement<? extends BaseComponentType>> getComponents(final BaseComponentType c) {
    if (c.getComponents() == null) {
      return null;
    }

    return new ArrayList<>(c.getComponents().getBaseComponent());
  }

  /**
   * @param bcPar component
   * @param name to find
   * @return null or first matching component
   */
  public static BaseComponentType findComponent(final BaseComponentType bcPar,
                                                final QName name) {
    final var cs = getComponents(bcPar);
    if (cs == null) {
      return null;
    }

    for (final var bcel: cs) {
      if (bcel.getName().equals(name)) {
        return bcel.getValue();
      }

      final var bc = findComponent(bcel.getValue(), name);
      if (bc != null) {
        return bc;
      }
    }

    return null;
  }

  /**
   * @param ical entity
   * @return null or first contained entity
   */
  public static BaseComponentType findEntity(final IcalendarType ical) {
    if (ical == null) {
      return null;
    }

    for (final var v: ical.getVcalendar()) {
      final var cs = v.getComponents();
      if (cs == null) {
        continue;
      }

      for (final var bcel: cs.getBaseComponent()) {
        return bcel.getValue();
      }
    }

    return null;
  }

  /** Searches this entity for the named property. Does not recurse down.
   *
   * @param bcPar entity
   * @param name to find
   * @return null or first matching property
   */
  public static BasePropertyType findProperty(final BaseComponentType bcPar,
                                              final QName name) {
    if (bcPar == null) {
      return null;
    }

    final var ps = bcPar.getProperties();
    if (ps == null) {
      return null;
    }

    for (final var bpel: ps.getBasePropertyOrTzid()) {
      if (bpel.getName().equals(name)) {
        return bpel.getValue();
      }
    }

    return null;
  }

  /** Searches the property for the named parameter.
   *
   * @param prop property
   * @param name to find
   * @return null or first matching property
   */
  public static BaseParameterType findParam(final BasePropertyType prop,
                                            final QName name) {
    if (prop == null) {
      return null;
    }

    final var ps = prop.getParameters();
    if (ps == null) {
      return null;
    }

    for (final var bpel: ps.getBaseParameter()) {
      if (bpel.getName().equals(name)) {
        return bpel.getValue();
      }
    }

    return null;
  }

  /**
   * @param cl class
   * @return QName for component
   */
  public static QName getCompName(final Class<?> cl) {
    return compNames.get(cl);
  }

  /**
   * @param name for component
   * @return component kind
   */
  public static int getCompKind(final QName name) {
    return compKinds.get(name);
  }

  private static void addInfo(final QName nm,
                              final Integer kind,
                              final Class<?> cl) {
    compNames.put(cl, nm);
    compKinds.put(nm, kind);
  }
}
