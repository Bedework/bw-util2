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
package org.bedework.util.calendar.diff;

import org.bedework.util.calendar.XcalUtil;
import org.bedework.util.xml.tagdefs.XcalTags;

import ietf.params.xml.ns.icalendar_2.ActionPropType;
import ietf.params.xml.ns.icalendar_2.AttachPropType;
import ietf.params.xml.ns.icalendar_2.CalAddressListParamType;
import ietf.params.xml.ns.icalendar_2.CalAddressParamType;
import ietf.params.xml.ns.icalendar_2.CalAddressPropertyType;
import ietf.params.xml.ns.icalendar_2.CalscalePropType;
import ietf.params.xml.ns.icalendar_2.CategoriesPropType;
import ietf.params.xml.ns.icalendar_2.CutypeParamType;
import ietf.params.xml.ns.icalendar_2.DateDatetimePropertyType;
import ietf.params.xml.ns.icalendar_2.DatetimePropertyType;
import ietf.params.xml.ns.icalendar_2.DurationPropType;
import ietf.params.xml.ns.icalendar_2.EncodingParamType;
import ietf.params.xml.ns.icalendar_2.FbtypeParamType;
import ietf.params.xml.ns.icalendar_2.FreebusyPropType;
import ietf.params.xml.ns.icalendar_2.GeoPropType;
import ietf.params.xml.ns.icalendar_2.IntegerPropertyType;
import ietf.params.xml.ns.icalendar_2.PartstatParamType;
import ietf.params.xml.ns.icalendar_2.PeriodType;
import ietf.params.xml.ns.icalendar_2.RangeParamType;
import ietf.params.xml.ns.icalendar_2.RecurPropertyType;
import ietf.params.xml.ns.icalendar_2.RecurType;
import ietf.params.xml.ns.icalendar_2.RelatedParamType;
import ietf.params.xml.ns.icalendar_2.ReltypeParamType;
import ietf.params.xml.ns.icalendar_2.RequestStatusPropType;
import ietf.params.xml.ns.icalendar_2.RoleParamType;
import ietf.params.xml.ns.icalendar_2.RsvpParamType;
import ietf.params.xml.ns.icalendar_2.ScheduleAgentParamType;
import ietf.params.xml.ns.icalendar_2.ScheduleForceSendParamType;
import ietf.params.xml.ns.icalendar_2.StatusPropType;
import ietf.params.xml.ns.icalendar_2.TextListPropertyType;
import ietf.params.xml.ns.icalendar_2.TextParameterType;
import ietf.params.xml.ns.icalendar_2.TextPropertyType;
import ietf.params.xml.ns.icalendar_2.TranspPropType;
import ietf.params.xml.ns.icalendar_2.TriggerPropType;
import ietf.params.xml.ns.icalendar_2.UriParameterType;
import ietf.params.xml.ns.icalendar_2.UriPropertyType;
import ietf.params.xml.ns.icalendar_2.UtcDatetimePropertyType;
import ietf.params.xml.ns.icalendar_2.UtcOffsetPropertyType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

/** This class allows comparison of calendaring values. Each value is
 * represented by an iCalendar value-type element (e.g. text, integer).
 * <p>
 * They can be added to the ValueMatcher object and then that object can be
 * compared to another to determine if the values are equal.
 *
 * @author Mike Douglass
 */
public class ValueMatcher {
  /**
   * @author douglm
   *
   * @param <T>
   */
  public interface ValueConverter<T> {
    /** Called to convert an object of a registered class. Converters implementing
     * this interface are registered with a value matcher
     * <p></p>
     * Note that standard value types (those defined in the standard schema)
     * are all registered once only at system initialization.
     *
     * @param val to convert
     * @return a ValueComparator
     */
    ValueComparator convert(T val);

    /** Called to get a property or parameter object containing only the value.
     * The property or parameter object is a new instance. Its value is copied.
     *
     * @param val for new element
     * @return property object containing value only
     */
    T getElementAndValue(T val);

    /** Return either a single valued set or a set with the values split into
     * separate objects
     *
     * @param val to normalize
     * @return list containing the object or the split object
     */
    List<T> getNormalized(T val);
  }

  private static class ValueMatcherRegistry {
    private static final Map<Class<?>, ValueConverter<?>> standardConverters =
        new HashMap<>();

    private Map<Class<?>, ValueConverter<?>> nonStandardConverters;

    /** Register a non-standard converter. This can override standard converters.
     *
     * @param cl the class
     * @param vc converter
     */
    public void registerConverter(final Class<?> cl,
                                  final ValueConverter<?> vc) {
      if (nonStandardConverters == null) {
        nonStandardConverters = new HashMap<>();
      }

      nonStandardConverters.put(cl, vc);
    }

    private void registerStandardConverter(final Class<?> cl,
                                           final ValueConverter<?> vc) {
      standardConverters.put(cl, vc);
    }

    private ValueConverter<?>getConverter(final Object o) {
      ValueConverter<?> vc;
      final Class<?> cl = o.getClass();

      if (nonStandardConverters != null) {
        vc = findConverter(cl, nonStandardConverters);

        if (vc != null) {
          return vc;
        }
      }

      vc = findConverter(cl, standardConverters);
      if (vc == null) {
        throw new RuntimeException("ValueMatcher: No converter for class " + cl);
      }

      return vc;
    }

    static ValueConverter<?> findConverter(final Class<?> cl,
                                           final Map<Class<?>, ValueConverter<?>> converters) {
      Class<?> lcl = cl;

      while (lcl != null) {
        final ValueConverter<?> vc = converters.get(lcl);

        if (vc != null) {
          return vc;
        }

        lcl = lcl.getSuperclass();
      }

      return null;
    }
  }

  private static final ValueMatcherRegistry registry = new ValueMatcherRegistry();

  private Map<Class<?>, ValueConverter<?>> instanceConverters;

  /**
   */
  public ValueMatcher() {
  }

  /**
   * @param val - value to match
   * @return comparator
   */
  public <T> ValueComparator getComparator(final T val) {
    return getConverter(val).convert(val);
  }

  /** Called to get a property or parameter object containing only the value.
   * The property or parameter object is a new instance. Its value is copied.
   *
   * @param val for new element
   * @return property object containing value only
   */
  public Object getElementAndValue(final Object val) {
    return getConverter(val).getElementAndValue(val);
  }

  /**
   * @param val to normalize
   * @return normalized set of objects
   */
  public List getNormalized(final Object val) {
    return getConverter(val).getNormalized(val);
  }

  /** Register a converter used by all instances of the value matcher.
   *
   * @param cl Class key
   * @param vc Converter
   */
  public static void registerGlobalConverter(final Class<?> cl,
                                             final ValueConverter<?> vc) {
    registry.registerConverter(cl, vc);
  }

  /** Register a converter used only by this instance of the value matcher.
   *
   * @param cl Class key
   * @param vc Converter
   */
  public void registerInstanceConverter(final Class<?> cl,
                                        final ValueConverter<?> vc) {
    if (instanceConverters != null) {
      instanceConverters = new HashMap<>();
    }

    instanceConverters.put(cl, vc);
  }

  private <T> ValueConverter<T> getConverter(final T o) {
    final ValueConverter<T> vc;
    final Class<?> cl = o.getClass();

    if (instanceConverters != null) {
      vc = (ValueConverter<T>)ValueMatcherRegistry.findConverter(cl, instanceConverters);

      if (vc != null) {
        return vc;
      }
    }

    return (ValueConverter<T>)registry.getConverter(o);
  }

  static {
    /* ======================================================================
     *          Register property values
     * ====================================================================== */

    registry.registerStandardConverter(ActionPropType.class,
                           new ActionPropConverter());

    registry.registerStandardConverter(FreebusyPropType.class,
                           new FreebusyPropConverter());

    registry.registerStandardConverter(RequestStatusPropType.class,
                           new RequestStatusPropConverter());

    registry.registerStandardConverter(GeoPropType.class,
                           new GeoPropConverter());

    registry.registerStandardConverter(StatusPropType.class,
                           new StatusPropConverter());

    registry.registerStandardConverter(TranspPropType.class,
                           new TranspPropConverter());

    registry.registerStandardConverter(CalscalePropType.class,
                           new CalscalePropConverter());

    registry.registerStandardConverter(TriggerPropType.class,
                           new TriggerPropConverter());

    registry.registerStandardConverter(DurationPropType.class,
                           new DurationPropConverter());

    registry.registerStandardConverter(AttachPropType.class,
                           new AttachPropConverter());

    registry.registerStandardConverter(DateDatetimePropertyType.class,
                           new DateDatetimePropConverter());

    registry.registerStandardConverter(DatetimePropertyType.class,
                           new DatetimePropConverter());

    registry.registerStandardConverter(UtcDatetimePropertyType.class,
                           new UtcDatetimePropConverter());

    registry.registerStandardConverter(CalAddressPropertyType.class,
                           new CalAddressPropConverter());

    registry.registerStandardConverter(UtcOffsetPropertyType.class,
                           new UtcOffsetPropConverter());

    registry.registerStandardConverter(TextListPropertyType.class,
                           new TextListPropConverter());

    registry.registerStandardConverter(TextPropertyType.class,
                           new TextPropConverter());

    registry.registerStandardConverter(RecurPropertyType.class,
                           new RecurPropConverter());

    registry.registerStandardConverter(IntegerPropertyType.class,
                           new IntegerPropConverter());

    registry.registerStandardConverter(UriPropertyType.class,
                           new UriPropConverter());

    /* ========================================================================
     *          Parameter values
     * ======================================================================== */

    registry.registerStandardConverter(CalAddressParamType.class,
                           new CalAddressParamConverter());

    registry.registerStandardConverter(CalAddressListParamType.class,
                           new CalAddressListParamConverter());

    registry.registerStandardConverter(TextParameterType.class,
                           new TextParamConverter());

    registry.registerStandardConverter(UriParameterType.class,
                           new UriParamConverter());

    registry.registerStandardConverter(CutypeParamType.class,
                           new CutypeParamConverter());

    registry.registerStandardConverter(EncodingParamType.class,
                           new EncodingParamConverter());

    registry.registerStandardConverter(FbtypeParamType.class,
                           new FbtypeParamConverter());

    registry.registerStandardConverter(PartstatParamType.class,
                           new PartstatParamConverter());

    registry.registerStandardConverter(RangeParamType.class,
                           new RangeParamConverter());

    registry.registerStandardConverter(RelatedParamType.class,
                           new RelatedParamConverter());

    registry.registerStandardConverter(ReltypeParamType.class,
                           new ReltypeParamConverter());

    registry.registerStandardConverter(RoleParamType.class,
                           new RoleParamConverter());

    registry.registerStandardConverter(RsvpParamType.class,
                           new RsvpParamConverter());

    registry.registerStandardConverter(ScheduleAgentParamType.class,
                           new ScheduleAgentParamConverter());

    registry.registerStandardConverter(ScheduleForceSendParamType.class,
                           new ScheduleForceSendParamConverter());
  }

  private static abstract class DefaultConverter<T>
          implements ValueConverter<T> {
    @Override
    public List<T> getNormalized(final T val) {
      final List<T> res = new ArrayList<>();

      res.add(val);

      return res;
    }
  }

  /* ========================================================================
   *          Property values
   * ======================================================================== */

  private static class ActionPropConverter extends DefaultConverter<ActionPropType> {
    @Override
    public ValueComparator convert(final ActionPropType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.textVal,
                       val.getText());

      return vc;
    }

    @Override
    public ActionPropType getElementAndValue(final ActionPropType val) {
      try {
        final ActionPropType prop = 
                val.getClass().getDeclaredConstructor().newInstance();

        prop.setText(val.getText());

        return prop;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class FreebusyPropConverter implements ValueConverter<FreebusyPropType> {
    @Override
    public ValueComparator convert(final FreebusyPropType val) {
      final List<PeriodType> ps = val.getPeriod();
      final ValueComparator vc = new ValueComparator();

      for (final PeriodType p: ps) {
        final StringBuilder sb = 
                new StringBuilder(p.getStart().toXMLFormat());
        sb.append("\t");
        if (p.getDuration() != null) {
          sb.append(p.getDuration());
        } else {
          sb.append(p.getEnd().toXMLFormat());
        }
        vc.addValue(XcalTags.periodVal, sb.toString());
      }

      return vc;
    }

    @Override
    public FreebusyPropType getElementAndValue(final FreebusyPropType val) {
      try {
        final FreebusyPropType prop = 
                val.getClass().getDeclaredConstructor().newInstance();

        final List<PeriodType> ps = val.getPeriod();

        for (final PeriodType p: ps) {
          prop.getPeriod().add(p);
        }

        return prop;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }

    @Override
    public List<FreebusyPropType> getNormalized(final FreebusyPropType val) {
      try {
        final List<FreebusyPropType> res = new ArrayList<>();
        final List<PeriodType> ps = val.getPeriod();

        for (final PeriodType p: ps) {
          final FreebusyPropType prop = 
                  val.getClass().getDeclaredConstructor().newInstance();
          prop.getPeriod().add(p);
          res.add(prop);
          prop.setParameters(val.getParameters());
        }

        return res;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class RequestStatusPropConverter extends DefaultConverter<RequestStatusPropType> {
    @Override
    public ValueComparator convert(final RequestStatusPropType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.codeVal, val.getCode());

      if (val.getDescription() != null) {
        vc.addValue(XcalTags.descriptionVal, val.getDescription());
      }

      if (val.getExtdata() != null) {
        vc.addValue(XcalTags.extdataVal, val.getExtdata());
      }

      return vc;
    }

    @Override
    public RequestStatusPropType getElementAndValue(final RequestStatusPropType val) {
      try {
        final RequestStatusPropType prop = 
                val.getClass().getDeclaredConstructor().newInstance();

        prop.setCode(val.getCode());

        if (val.getDescription() != null) {
          prop.setDescription(val.getDescription());
        }

        if (val.getExtdata() != null) {
          prop.setExtdata(val.getExtdata());
        }

        return prop;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class GeoPropConverter extends DefaultConverter<GeoPropType> {
    @Override
    public ValueComparator convert(final GeoPropType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.latitudeVal,
                       String.valueOf(val.getLatitude()));
      vc.addValue(XcalTags.longitudeVal,
                       String.valueOf(val.getLongitude()));

      return vc;
    }

    @Override
    public GeoPropType getElementAndValue(final GeoPropType val) {
      try {
        final GeoPropType prop = 
                val.getClass().getDeclaredConstructor().newInstance();

        prop.setLatitude(val.getLatitude());
        prop.setLongitude(val.getLongitude());

        return prop;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class StatusPropConverter extends DefaultConverter<StatusPropType> {
    @Override
    public ValueComparator convert(final StatusPropType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.textVal, val.getText());

      return vc;
    }

    @Override
    public StatusPropType getElementAndValue(final StatusPropType val) {
      try {
        final StatusPropType prop = 
                val.getClass().getDeclaredConstructor().newInstance();

        prop.setText(val.getText());

        return prop;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class TranspPropConverter extends DefaultConverter<TranspPropType> {
    @Override
    public ValueComparator convert(final TranspPropType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.textVal, val.getText());

      return vc;
    }

    @Override
    public TranspPropType getElementAndValue(final TranspPropType val) {
      try {
        final TranspPropType prop =
                val.getClass().getDeclaredConstructor().newInstance();

        prop.setText(val.getText());

        return prop;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class CalscalePropConverter extends DefaultConverter<CalscalePropType> {
    @Override
    public ValueComparator convert(final CalscalePropType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.textVal,
                       val.getText().toString());

      return vc;
    }

    @Override
    public CalscalePropType getElementAndValue(final CalscalePropType val) {
      try {
        final CalscalePropType prop =
                val.getClass().getDeclaredConstructor().newInstance();

        prop.setText(val.getText());

        return prop;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class TriggerPropConverter extends DefaultConverter<TriggerPropType> {
    @Override
    public ValueComparator convert(final TriggerPropType val) {
      final ValueComparator vc = new ValueComparator();

      if (val.getDuration() != null) {
        vc.addValue(XcalTags.durationVal,
                    val.getDuration());
      } else {
        vc.addValue(XcalTags.dateTimeVal,
                         val.getDateTime().toString());
      }

      return vc;
    }

    @Override
    public TriggerPropType getElementAndValue(final TriggerPropType val) {
      try {
        final TriggerPropType prop =
                val.getClass().getDeclaredConstructor().newInstance();

        if (val.getDuration() != null) {
          prop.setDuration(val.getDuration());
        } else {
          prop.setDateTime(val.getDateTime());
        }

        return prop;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class DurationPropConverter extends DefaultConverter<DurationPropType> {
    @Override
    public ValueComparator convert(final DurationPropType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.durationVal, val.getDuration());

      return vc;
    }

    @Override
    public DurationPropType getElementAndValue(final DurationPropType val) {
      try {
        final DurationPropType prop =
                val.getClass().getDeclaredConstructor().newInstance();

        prop.setDuration(val.getDuration());

        return prop;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class AttachPropConverter extends DefaultConverter<AttachPropType> {
    @Override
    public ValueComparator convert(final AttachPropType val) {
      final ValueComparator vc = new ValueComparator();

      if (val.getBinary() !=  null) {
        vc.addValue(XcalTags.binaryVal, val.getBinary());
      } else {
        vc.addValue(XcalTags.uriVal, val.getUri());
      }

      return vc;
    }

    @Override
    public AttachPropType getElementAndValue(final AttachPropType val) {
      try {
        final AttachPropType prop =
                val.getClass().getDeclaredConstructor().newInstance();

        if (val.getBinary() !=  null) {
          prop.setBinary(val.getBinary());
        } else {
          prop.setUri(val.getUri());
        }

        return prop;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class DateDatetimePropConverter extends DefaultConverter<DateDatetimePropertyType> {
    @Override
    public ValueComparator convert(final DateDatetimePropertyType val) {
      final XcalUtil.DtTzid dtTzid = XcalUtil.getDtTzid(val);
      final ValueComparator vc = new ValueComparator();

      if (dtTzid.dateOnly) {
        vc.addValue(XcalTags.dateVal, dtTzid.dt);
      } else {
        vc.addValue(XcalTags.dateTimeVal, dtTzid.dt);
      }

      /* Note we deal with tzid separately as a parameter */

      return vc;
    }

    @Override
    public DateDatetimePropertyType getElementAndValue(final DateDatetimePropertyType val) {
      try {
        final DateDatetimePropertyType prop =
                val.getClass().getDeclaredConstructor().newInstance();

        if (val.getDate() != null) {
          prop.setDate(val.getDate());
        } else {
          prop.setDateTime(val.getDateTime());
        }

        return prop;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class DatetimePropConverter extends DefaultConverter<DatetimePropertyType> {
    @Override
    public ValueComparator convert(final DatetimePropertyType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.dateTimeVal,
                       XcalUtil.getIcalFormatDateTime(
                         val.getDateTime().toString()));

      return vc;
    }

    @Override
    public DatetimePropertyType getElementAndValue(final DatetimePropertyType val) {
      try {
        final DatetimePropertyType prop =
                val.getClass().getDeclaredConstructor().newInstance();

        prop.setDateTime(val.getDateTime());

        return prop;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class UtcDatetimePropConverter extends DefaultConverter<UtcDatetimePropertyType> {
    @Override
    public ValueComparator convert(final UtcDatetimePropertyType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.utcDateTimeVal,
                XcalUtil.getIcalFormatDateTime(
                    val.getUtcDateTime().toString()));

      return vc;
    }

    @Override
    public UtcDatetimePropertyType getElementAndValue(final UtcDatetimePropertyType val) {
      try {
        final UtcDatetimePropertyType prop =
                val.getClass().getDeclaredConstructor().newInstance();

        prop.setUtcDateTime(val.getUtcDateTime());

        return prop;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class CalAddressPropConverter extends DefaultConverter<CalAddressPropertyType> {
    @Override
    public ValueComparator convert(final CalAddressPropertyType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.calAddressVal, val.getCalAddress());

      return vc;
    }

    @Override
    public CalAddressPropertyType getElementAndValue(final CalAddressPropertyType val) {
      try {
        final CalAddressPropertyType prop =
                val.getClass().getDeclaredConstructor().newInstance();

        prop.setCalAddress(val.getCalAddress());

        return prop;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class UtcOffsetPropConverter extends DefaultConverter<UtcOffsetPropertyType> {
    @Override
    public ValueComparator convert(final UtcOffsetPropertyType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.utcOffsetVal,
                       val.getUtcOffset());

      return vc;
    }

    @Override
    public UtcOffsetPropertyType getElementAndValue(final UtcOffsetPropertyType val) {
      try {
        final UtcOffsetPropertyType prop =
                val.getClass().getDeclaredConstructor().newInstance();

        prop.setUtcOffset(val.getUtcOffset());

        return prop;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class TextListPropConverter extends DefaultConverter<TextListPropertyType> {
    @Override
    public ValueComparator convert(final TextListPropertyType val) {
      final List<String> ss = val.getText();
      final ValueComparator vc = new ValueComparator();

      for (final String s: ss) {
        vc.addValue(XcalTags.textVal, s);
      }

      return vc;
    }

    @Override
    public TextListPropertyType getElementAndValue(final TextListPropertyType val) {
      try {
        final TextListPropertyType prop =
                val.getClass().getDeclaredConstructor().newInstance();
        final List<String> ss = val.getText();

        for (final String s: ss) {
          prop.getText().add(s);
        }

        return prop;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }

    @Override
    public List<TextListPropertyType> getNormalized(final TextListPropertyType val) {
      if (!(val instanceof CategoriesPropType)) {
        return super.getNormalized(val);
      }

      try {
        final List<TextListPropertyType> res = new ArrayList<>();

        for (final String s: val.getText()) {
          final TextListPropertyType prop =
                val.getClass().getDeclaredConstructor().newInstance();
          prop.getText().add(s);
          res.add(prop);
          prop.setParameters(val.getParameters());
        }

        return res;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class TextPropConverter extends DefaultConverter<TextPropertyType> {
    @Override
    public ValueComparator convert(final TextPropertyType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.textVal, val.getText());

      return vc;
    }

    @Override
    public TextPropertyType getElementAndValue(final TextPropertyType val) {
      try {
        final TextPropertyType prop =
                val.getClass().getDeclaredConstructor().newInstance();

        prop.setText(val.getText());

        return prop;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class RecurPropConverter extends DefaultConverter<RecurPropertyType> {
    @Override
    public ValueComparator convert(final RecurPropertyType val) {
      final RecurType r = val.getRecur();
      final ValueComparator vc = new ValueComparator();

      append(vc, XcalTags.freq, r.getFreq().toString());
      append(vc, XcalTags.count, r.getCount());
      if (r.getUntil() != null) {
        final var u = r.getUntil();
        if (u.getDate() != null) {
          append(vc, XcalTags.until, u.getDate());
        } else {
          append(vc, XcalTags.until, u.getDateTime());
        }
      }
      append(vc, XcalTags.interval, r.getInterval());
      append(vc, XcalTags.bysecond, r.getBysecond());
      append(vc, XcalTags.byminute, r.getByminute());
      append(vc, XcalTags.byhour, r.getByhour());
      append(vc, XcalTags.byday, r.getByday());
      append(vc, XcalTags.byyearday, r.getByyearday());
      append(vc, XcalTags.bymonthday, r.getBymonthday());
      append(vc, XcalTags.byweekno, r.getByweekno());
      append(vc, XcalTags.bymonth, r.getBymonth());
      append(vc, XcalTags.bysetpos, r.getBysetpos());

      if (r.getWkst() != null) {
        append(vc, XcalTags.wkst, r.getWkst().toString());
      }

      return vc;
    }

    @Override
    public RecurPropertyType getElementAndValue(final RecurPropertyType val) {
      try {
        final RecurPropertyType prop = 
                val.getClass().getDeclaredConstructor().newInstance();

        prop.setRecur(val.getRecur());

        return prop;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }

    private void append(final ValueComparator vc,
                        final QName nm,
                        final List<?> val) {
      if (val == null) {
        return;
      }

      for (final Object o: val) {
        append(vc, nm, o);
      }
    }

    private void append(final ValueComparator vc,
                        final QName nm,
                        final Object val) {
      if (val == null) {
        return;
      }

      vc.addValue(nm, String.valueOf(val));
    }
  }

  private static class IntegerPropConverter extends DefaultConverter<IntegerPropertyType> {
    @Override
    public ValueComparator convert(final IntegerPropertyType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.integerVal,
                       String.valueOf(val.getInteger()));

      return vc;
    }

    @Override
    public IntegerPropertyType getElementAndValue(final IntegerPropertyType val) {
      try {
        final IntegerPropertyType prop =
                val.getClass().getDeclaredConstructor().newInstance();

        prop.setInteger(val.getInteger());

        return prop;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class UriPropConverter extends DefaultConverter<UriPropertyType> {
    @Override
    public ValueComparator convert(final UriPropertyType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.uriVal, val.getUri());

      return vc;
    }

    @Override
    public UriPropertyType getElementAndValue(final UriPropertyType val) {
      try {
        final UriPropertyType prop =
                val.getClass().getDeclaredConstructor().newInstance();

        prop.setUri(val.getUri());

        return prop;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  /* ========================================================================
   *          Parameter values
   * ======================================================================== */

  private static class CalAddressParamConverter extends DefaultConverter<CalAddressParamType> {
    @Override
    public ValueComparator convert(final CalAddressParamType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.calAddressVal, val.getCalAddress());

      return vc;
    }

    @Override
    public CalAddressParamType getElementAndValue(final CalAddressParamType val) {
      try {
        final CalAddressParamType param = val.getClass().getDeclaredConstructor().newInstance();

        param.setCalAddress(val.getCalAddress());

        return param;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class CalAddressListParamConverter extends DefaultConverter<CalAddressListParamType> {
    @Override
    public ValueComparator convert(final CalAddressListParamType val) {
      final List<String> ss = val.getCalAddress();
      final ValueComparator vc = new ValueComparator();

      for (final String s: ss) {
        vc.addValue(XcalTags.calAddressVal, s);
      }

      return vc;
    }

    @Override
    public CalAddressListParamType getElementAndValue(final CalAddressListParamType val) {
      try {
        final CalAddressListParamType param =
                val.getClass().getDeclaredConstructor().newInstance();
        final List<String> ss = val.getCalAddress();

        for (final String s: ss) {
          param.getCalAddress().add(s);
        }

        return param;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class TextParamConverter extends DefaultConverter<TextParameterType> {
    @Override
    public ValueComparator convert(final TextParameterType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.textVal, val.getText());

      return vc;
    }

    @Override
    public TextParameterType getElementAndValue(final TextParameterType val) {
      try {
        final TextParameterType param =
                val.getClass().getDeclaredConstructor().newInstance();

        param.setText(val.getText());

        return param;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class UriParamConverter extends DefaultConverter<UriParameterType> {
    @Override
    public ValueComparator convert(final UriParameterType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.uriVal, val.getUri());

      return vc;
    }

    @Override
    public UriParameterType getElementAndValue(final UriParameterType val) {
      try {
        final UriParameterType param =
                val.getClass().getDeclaredConstructor().newInstance();

        param.setUri(val.getUri());

        return param;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class CutypeParamConverter extends DefaultConverter<CutypeParamType> {
    @Override
    public ValueComparator convert(final CutypeParamType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.textVal, val.getText());

      return vc;
    }

    @Override
    public CutypeParamType getElementAndValue(final CutypeParamType val) {
      try {
        final CutypeParamType param =
                val.getClass().getDeclaredConstructor().newInstance();

        param.setText(val.getText());

        return param;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class EncodingParamConverter extends DefaultConverter<EncodingParamType> {
    @Override
    public ValueComparator convert(final EncodingParamType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.textVal, val.getText());

      return vc;
    }

    @Override
    public EncodingParamType getElementAndValue(final EncodingParamType val) {
      try {
        final EncodingParamType param =
                val.getClass().getDeclaredConstructor().newInstance();

        param.setText(val.getText());

        return param;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class FbtypeParamConverter extends DefaultConverter<FbtypeParamType> {
    @Override
    public ValueComparator convert(final FbtypeParamType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.textVal, val.getText());

      return vc;
    }

    @Override
    public FbtypeParamType getElementAndValue(final FbtypeParamType val) {
      try {
        final FbtypeParamType param =
                val.getClass().getDeclaredConstructor().newInstance();

        param.setText(val.getText());

        return param;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class PartstatParamConverter extends DefaultConverter<PartstatParamType> {
    @Override
    public ValueComparator convert(final PartstatParamType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.textVal, val.getText());

      return vc;
    }

    @Override
    public PartstatParamType getElementAndValue(final PartstatParamType val) {
      try {
        final PartstatParamType param =
                val.getClass().getDeclaredConstructor().newInstance();

        param.setText(val.getText());

        return param;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class RangeParamConverter extends DefaultConverter<RangeParamType> {
    @Override
    public ValueComparator convert(final RangeParamType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.textVal,
                       val.getText().toString());

      return vc;
    }

    @Override
    public RangeParamType getElementAndValue(final RangeParamType val) {
      try {
        final RangeParamType param =
                val.getClass().getDeclaredConstructor().newInstance();

        param.setText(val.getText());

        return param;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class RelatedParamConverter extends DefaultConverter<RelatedParamType> {
    @Override
    public ValueComparator convert(final RelatedParamType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.textVal, val.getText());

      return vc;
    }

    @Override
    public RelatedParamType getElementAndValue(final RelatedParamType val) {
      try {
        final RelatedParamType param =
                val.getClass().getDeclaredConstructor().newInstance();

        param.setText(val.getText());

        return param;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class ReltypeParamConverter extends DefaultConverter<ReltypeParamType> {
    @Override
    public ValueComparator convert(final ReltypeParamType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.textVal, val.getText());

      return vc;
    }

    @Override
    public ReltypeParamType getElementAndValue(final ReltypeParamType val) {
      try {
        final ReltypeParamType param =
                val.getClass().getDeclaredConstructor().newInstance();

        param.setText(val.getText());

        return param;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class RoleParamConverter extends DefaultConverter<RoleParamType> {
    @Override
    public ValueComparator convert(final RoleParamType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.textVal, val.getText());

      return vc;
    }

    @Override
    public RoleParamType getElementAndValue(final RoleParamType val) {
      try {
        final RoleParamType param =
                val.getClass().getDeclaredConstructor().newInstance();

        param.setText(val.getText());

        return param;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class RsvpParamConverter extends DefaultConverter<RsvpParamType> {
    @Override
    public ValueComparator convert(final RsvpParamType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.booleanVal,
                       String.valueOf(val.isBoolean()));

      return vc;
    }

    @Override
    public RsvpParamType getElementAndValue(final RsvpParamType val) {
      try {
        final RsvpParamType param =
                val.getClass().getDeclaredConstructor().newInstance();

        param.setBoolean(val.isBoolean());

        return param;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class ScheduleAgentParamConverter extends DefaultConverter<ScheduleAgentParamType> {
    @Override
    public ValueComparator convert(final ScheduleAgentParamType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.textVal, val.getText());

      return vc;
    }

    @Override
    public ScheduleAgentParamType getElementAndValue(final ScheduleAgentParamType val) {
      try {
        final ScheduleAgentParamType param =
                val.getClass().getDeclaredConstructor().newInstance();

        param.setText(val.getText());

        return param;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class ScheduleForceSendParamConverter extends DefaultConverter<ScheduleForceSendParamType> {
    @Override
    public ValueComparator convert(final ScheduleForceSendParamType val) {
      final ValueComparator vc = new ValueComparator();

      vc.addValue(XcalTags.textVal, val.getText());

      return vc;
    }

    @Override
    public ScheduleForceSendParamType getElementAndValue(final ScheduleForceSendParamType val) {
      try {
        final ScheduleForceSendParamType param =
                val.getClass().getDeclaredConstructor().newInstance();

        param.setText(val.getText());

        return param;
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }
}
