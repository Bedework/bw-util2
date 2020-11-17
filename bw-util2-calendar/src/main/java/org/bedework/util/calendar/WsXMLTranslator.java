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

import org.bedework.util.calendar.PropertyIndex.ComponentInfoIndex;
import org.bedework.util.logging.BwLogger;

import ietf.params.xml.ns.icalendar_2.ArrayOfComponents;
import ietf.params.xml.ns.icalendar_2.ArrayOfParameters;
import ietf.params.xml.ns.icalendar_2.ArrayOfProperties;
import ietf.params.xml.ns.icalendar_2.AttachPropType;
import ietf.params.xml.ns.icalendar_2.BaseComponentType;
import ietf.params.xml.ns.icalendar_2.BaseParameterType;
import ietf.params.xml.ns.icalendar_2.BasePropertyType;
import ietf.params.xml.ns.icalendar_2.CalAddressListParamType;
import ietf.params.xml.ns.icalendar_2.CalAddressParamType;
import ietf.params.xml.ns.icalendar_2.CalAddressPropertyType;
import ietf.params.xml.ns.icalendar_2.CalscalePropType;
import ietf.params.xml.ns.icalendar_2.DateDatetimePropertyType;
import ietf.params.xml.ns.icalendar_2.DatetimePropertyType;
import ietf.params.xml.ns.icalendar_2.DurationParameterType;
import ietf.params.xml.ns.icalendar_2.DurationPropType;
import ietf.params.xml.ns.icalendar_2.FreebusyPropType;
import ietf.params.xml.ns.icalendar_2.GeoPropType;
import ietf.params.xml.ns.icalendar_2.IcalendarType;
import ietf.params.xml.ns.icalendar_2.IntegerPropertyType;
import ietf.params.xml.ns.icalendar_2.RangeParamType;
import ietf.params.xml.ns.icalendar_2.RecurPropertyType;
import ietf.params.xml.ns.icalendar_2.RecurType;
import ietf.params.xml.ns.icalendar_2.RequestStatusPropType;
import ietf.params.xml.ns.icalendar_2.TextListPropertyType;
import ietf.params.xml.ns.icalendar_2.TextParameterType;
import ietf.params.xml.ns.icalendar_2.TextPropertyType;
import ietf.params.xml.ns.icalendar_2.TriggerPropType;
import ietf.params.xml.ns.icalendar_2.UntilRecurType;
import ietf.params.xml.ns.icalendar_2.UriParameterType;
import ietf.params.xml.ns.icalendar_2.UriPropertyType;
import ietf.params.xml.ns.icalendar_2.UtcDatetimePropertyType;
import ietf.params.xml.ns.icalendar_2.UtcOffsetPropertyType;
import ietf.params.xml.ns.icalendar_2.VcalendarType;
import net.fortuna.ical4j.data.ContentHandler;
import net.fortuna.ical4j.data.DefaultContentHandler;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.parameter.Value;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

/** Convert to/from Web Services style XML. On input this has been parsed by the
 * framework. For output we need to build a JAXB compliant structure.
 *
 * <p>For the moment we build an ical4j structure and use that to create events
 *
 * @author douglm
 */
public class WsXMLTranslator implements Consumer<Calendar> {
  //private Logger log = Logger.getLogger(WsXMLTranslator.class);

  private final TimeZoneRegistry tzRegistry;
  private ContentHandler handler;

  /**
   * The calendar instance created by the builder.
   */
  private Calendar calendar;

  /**
   * @param tzRegistry for timezones
   */
  public WsXMLTranslator(final TimeZoneRegistry tzRegistry) {
    this.tzRegistry = tzRegistry;
  }

  @Override
  public void accept(final Calendar calendar) {
    this.calendar = calendar;
  }

  /**
   * @param ical xCal Icalendar object
   * @return Calendar object or null for no data
   * @throws RuntimeException on fatal error
   */
  public Calendar fromXcal(final IcalendarType ical) {
    handler = new DefaultContentHandler(this, tzRegistry);

    final List<VcalendarType> vcts = ical.getVcalendar();
    if (vcts.size() == 0) {
      return null;
    }

    if (vcts.size() > 1) {
      throw new RuntimeException("More than one vcalendar");
    }

    processVcalendar(vcts.get(0), handler);

    return calendar;
  }

  /**
   * @param comp xCal component
   * @return Calendar object or null for no data
   * @throws RuntimeException on fatal error
   */
  public Calendar fromXcomp(final JAXBElement<
          ? extends BaseComponentType> comp) {
    final IcalendarType ical = new IcalendarType();

    final List<VcalendarType> vcts = ical.getVcalendar();

    final VcalendarType vcal = new VcalendarType();
    vcts.add(vcal);

    final ArrayOfComponents aop = new ArrayOfComponents();

    vcal.setComponents(aop);
    aop.getBaseComponent().add(comp);

    return fromXcal(ical);
  }

  private void processVcalendar(final VcalendarType vcal,
                                final ContentHandler handler) {
    handler.startCalendar();

    processProperties(vcal.getProperties(), handler);

    processCalcomps(vcal, handler);
  }

  private void processProperties(final ArrayOfProperties aop,
                                 final ContentHandler handler) {
    if ((aop == null) || (aop.getBasePropertyOrTzid().size() == 0)) {
      return;
    }

    for (final JAXBElement<?> e: aop.getBasePropertyOrTzid()) {
      processProperty((BasePropertyType)e.getValue(),
                      e.getName(), handler);
    }
  }

  /* Process all the sub-components of the supplied component */
  private void processCalcomps(final BaseComponentType c,
                               final ContentHandler handler) {
    final List<JAXBElement<? extends BaseComponentType>> comps =
      XcalUtil.getComponents(c);

    if (comps == null) {
      return;
    }

    for (final JAXBElement<? extends BaseComponentType> el: comps) {
      processComponent(el.getValue(), handler);
    }
  }

  private void processComponent(final BaseComponentType comp,
                                final ContentHandler handler) {
    final ComponentInfoIndex cii = ComponentInfoIndex.fromXmlClass(comp.getClass());

    if (cii == null) {
      throw new RuntimeException("Unknown component " + comp.getClass());
    }

    final String name = cii.getPname();
    handler.startComponent(name);

    processProperties(comp.getProperties(), handler);

    processCalcomps(comp, handler);

    handler.endComponent(name);
  }

  private void processProperty(final BasePropertyType prop,
                               final QName elname,
                               final ContentHandler handler) {
    /*
    final PropertyInfoIndex pii = PropertyInfoIndex.fromXmlClass(prop.getClass());

    String name;
    if (pii == null) {
      name = elname.getLocalPart().toUpperCase();
    } else {
      name = pii.name();
    }
    */
    String name = elname.getLocalPart().toUpperCase();

    final ArrayOfParameters aop = prop.getParameters();

    final boolean wrapper = name.equals("X-BEDEWORK-WRAPPER");

    if (wrapper) {
      /* find the wrapped name parameter */
      for (final JAXBElement<? extends BaseParameterType> e:
              aop.getBaseParameter()) {
        final String parName = e.getName().getLocalPart().toUpperCase();

        if (parName.equals("X-BEDEWORK-WRAPPED-NAME")) {
          name = getParValue(e.getValue());
        }
      }
    }

    handler.startProperty(name);

    try {
      if (aop != null) {
        for (final JAXBElement<? extends BaseParameterType> e: aop
                .getBaseParameter()) {
          final String parName = e.getName().getLocalPart()
                                  .toUpperCase();

          if (parName.equals("X-BEDEWORK-WRAPPED-NAME")) {
            continue;
          }

          handler.parameter(parName, getParValue(e.getValue()));
        }
      }

      if (!processValue(prop, handler)) {
        throw new RuntimeException("Bad property " + prop);
      }

      handler.endProperty(name);
    } catch (final RuntimeException rte) {
      throw rte;
    } catch (final Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * @param rp recurrence property
   * @return iCalendar recurrence rule value
   */
  public String fromRecurProperty(final RecurPropertyType rp) {
    final RecurType r = rp.getRecur();

    final List<String> rels = new ArrayList<>();

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
    addRecurEl(rels, "FREQ", r.getFreq());

    if (r.getUntil() != null) {
      final UntilRecurType until = r.getUntil();
      if (until.getDate() != null) {
        rels.add("UNTIL=" + until.getDate());
      } else {
        rels.add("UNTIL=" + until.getDateTime());
      }
    }

    addRecurEl(rels, "COUNT", r.getCount());
    addRecurEl(rels, "INTERVAL", r.getInterval());
    addRecurEl(rels, "BYSECOND", r.getBysecond());
    addRecurEl(rels, "BYMINUTE", r.getByminute());
    addRecurEl(rels, "BYHOUR", r.getByhour());
    addRecurEl(rels, "BYDAY", r.getByday());
    addRecurEl(rels, "BYMONTHDAY", r.getBymonthday());
    addRecurEl(rels, "BYYEARDAY", r.getByyearday());
    addRecurEl(rels, "BYWEEKNO", r.getByweekno());
    addRecurEl(rels, "BYMONTH", r.getBymonth());
    addRecurEl(rels, "BYSETPOS", r.getBysetpos());
    addRecurEl(rels, "WKST", r.getWkst());

    return fromList(rels, false, ";");
  }

  private boolean processValue(final BasePropertyType prop,
                               final ContentHandler handler) {
    if (prop instanceof RecurPropertyType) {
      propVal(handler, fromRecurProperty((RecurPropertyType)prop));

      return true;
    }

    if (prop instanceof DurationPropType) {
      final DurationPropType dp = (DurationPropType)prop;

      propVal(handler, dp.getDuration());

      return true;
    }

    if (prop instanceof TextPropertyType) {
      final TextPropertyType tp = (TextPropertyType)prop;

      propVal(handler, tp.getText());

      return true;
    }

    if (prop instanceof TextListPropertyType) {
      final TextListPropertyType p = (TextListPropertyType)prop;

      propVal(handler, fromList(p.getText(), false));

      return true;
    }

    if (prop instanceof CalAddressPropertyType) {
      final CalAddressPropertyType cap = (CalAddressPropertyType)prop;

      propVal(handler, cap.getCalAddress());

      return true;
    }

    if (prop instanceof IntegerPropertyType) {
      final IntegerPropertyType ip = (IntegerPropertyType)prop;

      propVal(handler, String.valueOf(ip.getInteger()));

      return true;
    }

    if (prop instanceof UriPropertyType) {
      final UriPropertyType p = (UriPropertyType)prop;

      propVal(handler, p.getUri());

      return true;
    }

    if (prop instanceof UtcOffsetPropertyType) {
      final UtcOffsetPropertyType p = (UtcOffsetPropertyType)prop;

      propVal(handler, p.getUtcOffset());

      return true;
    }

    if (prop instanceof UtcDatetimePropertyType) {
      final UtcDatetimePropertyType p = (UtcDatetimePropertyType)prop;

      propVal(handler,
              XcalUtil.getIcalFormatDateTime(p.getUtcDateTime()
                                              .toString()));

      return true;
    }

    if (prop instanceof DatetimePropertyType) {
      final DatetimePropertyType p = (DatetimePropertyType)prop;

      propVal(handler,
              XcalUtil.getIcalFormatDateTime(p.getDateTime()
                                              .toString()));

      return true;
    }

    if (prop instanceof DateDatetimePropertyType) {
      final XcalUtil.DtTzid dtTzid =
              XcalUtil.getDtTzid((DateDatetimePropertyType)prop);

      if (dtTzid.dateOnly) {
        try {
          handler.parameter(Parameter.VALUE,
                            Value.DATE.getValue());
        } catch (final URISyntaxException e) {
          throw new RuntimeException(e);
        }
      }

      propVal(handler, dtTzid.dt);

      return true;
    }

    if (prop instanceof CalscalePropType) {
      final CalscalePropType p = (CalscalePropType)prop;

      propVal(handler, p.getText().name());

      return true;
    }

    if (prop instanceof AttachPropType) {
      final AttachPropType p = (AttachPropType)prop;

      if (p.getUri() != null) {
        propVal(handler, p.getUri());
      } else {
        propVal(handler, p.getBinary());
      }

      return true;
    }

    if (prop instanceof GeoPropType) {
      final GeoPropType p = (GeoPropType)prop;

      propVal(handler, p.getLatitude() + ";" + p.getLongitude());

      return true;
    }

    if (prop instanceof FreebusyPropType) {
      final FreebusyPropType p = (FreebusyPropType)prop;

      propVal(handler, fromList(p.getPeriod(), false));

      return true;
    }

    if (prop instanceof TriggerPropType) {
      final TriggerPropType p = (TriggerPropType)prop;

      if (p.getDuration() != null) {
        propVal(handler, p.getDuration());
      } else {
        propVal(handler, XcalUtil.getIcalFormatDateTime(p.getDateTime().toString()));
      }

      return true;
    }

    if (prop instanceof RequestStatusPropType) {
      final RequestStatusPropType p = (RequestStatusPropType)prop;

      final StringBuilder sb = new StringBuilder();

      sb.append(p.getCode());
      if (p.getDescription() != null) {
        sb.append(";");
        sb.append(p.getDescription());
      }

      if (p.getExtdata() != null) {
        sb.append(";");
        sb.append(p.getExtdata());
      }

      propVal(handler, sb.toString());

      return true;
    }

    // ClassPropType: TextPropertyType
    // StatusPropType: TextPropertyType
    // TranspPropType: TextPropertyType
    // ActionPropType: TextPropertyType

    if (getLog().isDebugEnabled()) {
      warn("Unhandled class " + prop.getClass());
    }

    return false;
  }

  private void addRecurEl(final List<String> l, final String name, final Object o) {
    if (o == null) {
      return;
    }

    final String val;

    if (o instanceof List) {
      val = fromList((List<?>)o, false);
      if (val == null) {
        return;
      }
    } else {
      val = String.valueOf(o);
    }

    l.add(name + "=" + val);
  }

  private void propVal(final ContentHandler handler,
                       final String val) {
    try {
      handler.propertyValue(val);
    } catch (final Throwable t) {
      throw new RuntimeException(t);
    }
  }

  private String getParValue(final BaseParameterType bpt) {
    if (bpt instanceof TextParameterType) {
      return ((TextParameterType)bpt).getText();
    }

    if (bpt instanceof DurationParameterType) {
      return ((DurationParameterType)bpt).getDuration().toString();
    }

    if (bpt instanceof RangeParamType) {
      return ((RangeParamType)bpt).getText().value();
    }

    if (bpt instanceof CalAddressListParamType) {
      return fromList(((CalAddressListParamType)bpt).getCalAddress(), true);
    }

    if (bpt instanceof CalAddressParamType) {
      return ((CalAddressParamType)bpt).getCalAddress();
    }

    if (bpt instanceof UriParameterType) {
      return ((UriParameterType)bpt).getUri();
    }

    throw new RuntimeException("Unsupported param type");
  }

  private String fromList(final List<?> l, final boolean quote) {
    return fromList(l, quote, ",");
  }

  private String fromList(final List<?> l,
                          final boolean quote,
                          final String delimChar) {
    if ((l == null) || l.isEmpty()) {
      return null;
    }

    final StringBuilder sb = new StringBuilder();
    String delim = "";
    String qt = "";

    if (quote) {
      qt = "\"";
    }

    for (final Object o: l) {
      sb.append(delim);
      delim = delimChar;
      sb.append(qt);
      sb.append(o);
      sb.append(qt);
    }

    return sb.toString();
  }

  /* ====================================================================
                      Private methods
     ==================================================================== */

  private static BwLogger logger;

  /**
   * @return Logger
   */
  private static BwLogger getLog() {
    if (logger != null) {
      return logger;
    }

    logger = new BwLogger();
    logger.setLoggedClass(WsXMLTranslator.class);

    return logger;
  }

  /**
   * @param t Throwable
   */
  public static void error(final Throwable t) {
    getLog().error(t);
  }

  /**
   * @param msg message
   */
  public static void warn(final String msg) {
    getLog().warn(msg);
  }
}
