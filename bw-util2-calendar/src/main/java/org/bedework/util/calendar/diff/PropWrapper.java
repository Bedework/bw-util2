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
import org.bedework.base.ToString;

import ietf.params.xml.ns.icalendar_2.BaseParameterType;
import ietf.params.xml.ns.icalendar_2.BasePropertyType;
import ietf.params.xml.ns.icalendar_2.RecurrenceIdPropType;
import org.oasis_open.docs.ws_calendar.ns.soap.ParametersSelectionType;
import org.oasis_open.docs.ws_calendar.ns.soap.PropertyReferenceType;
import org.oasis_open.docs.ws_calendar.ns.soap.PropertySelectionType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

/** This class wraps a property.
 *
 * @author Mike Douglass
 */
class PropWrapper extends BaseEntityWrapper<PropWrapper,
                                            PropsWrapper,
                                            BasePropertyType>
            implements Comparable<PropWrapper> {
  ParamsWrapper params;

  private ValueComparator comparator;

  /* Key is the real name of the property - result is null for no mapping or a
   * QName we treat it as for comparison.
   */
  private static final Map<QName, QName> mappedNames = new HashMap<>();

  static {
    final String ns = "urn:ietf:params:xml:ns:icalendar-2.0";

    mappedNames.put(new QName(ns, "x-bedework-exsynch-organizer"),
                    new QName(ns, "organizer"));
  }

  PropWrapper(final PropsWrapper parent,
              final QName name,
              final BasePropertyType p) {
    super(parent, name, p);

    List<JAXBElement<? extends BaseParameterType>> plist = null;

    if (p.getParameters() != null) {
      plist = p.getParameters().getBaseParameter();
    }
    params = new ParamsWrapper(this, plist);
  }

  @Override
  QName getMappedName(final QName name) {
    return mappedNames.get(name);
  }

  @Override
  boolean sameEntity(final BaseEntityWrapper<?, ?, ?> val) {
    final int res = super.compareNameClass(val);
    if (res != 0) {
      return false;
    }

    return true;
  }

  PropertyReferenceType makeRef() {
    final PropertyReferenceType r = new PropertyReferenceType();

    r.setBaseProperty(getJaxbElement());
    return r;
  }

  PropertySelectionType getSelect(final PropertySelectionType val) {
    if (val != null) {
      return val;
    }

    final PropertySelectionType sel = new PropertySelectionType();

    sel.setBaseProperty(getJaxbElement());

    return sel;
  }

  /** Return a SelectElementType if the values differ. This object
   * represents the new state
   *
   * @param that - the old version
   * @return SelectElementType
   */
  @SuppressWarnings("unchecked")
  public PropertySelectionType diff(final PropWrapper that) {
    PropertySelectionType sel = null;

    if (params != null) {
      final ParametersSelectionType psel = params.diff(that.params);

      if (psel != null) {
        sel = that.getSelect(sel);

        sel.setParameters(psel);
      }
    }

    if (!equalValue(that)) {
      sel = that.getSelect(sel);
      final PropertyReferenceType ct = new PropertyReferenceType();

      final JAXBElement jel = getJaxbElement();
      jel.setValue(globals.matcher.getElementAndValue(getEntity()));
      ct.setBaseProperty(jel);

      sel.setChange(ct);
    }

    return sel;
  }

  public boolean equalValue(final PropWrapper that) {
    return getComparator().equals(that.getComparator());
  }

  public int compareValue(final PropWrapper that) {
    return getComparator().compareTo(that.getComparator());
  }

  ValueComparator getComparator() {
    if (comparator == null) {
      comparator = globals.matcher.getComparator(getEntity());
    }

    return comparator;
  }

  @Override
  public int compareTo(final PropWrapper o) {
    if (getEntity() instanceof final RecurrenceIdPropType thisRid) {
      /* Special case this one as the calculated UTC is what matters.
       */
      if (!(o.getEntity() instanceof final RecurrenceIdPropType thatRid)) {
        return getName().getLocalPart()
                        .compareTo(o.getName().getLocalPart());
      }

      /* Get UTC value to compare */

      try {
        final String thatUTC = XcalUtil.getUTC(thatRid, globals.tzs);
        final String thisUTC = XcalUtil.getUTC(thisRid, globals.tzs);

        return thatUTC.compareTo(thisUTC);
      } catch (final Throwable t) {
        throw new RuntimeException(t);
      }
    }

    int res = super.compareNameClass(o);
    if (res != 0) {
      return res;
    }

    // Try value

    res = compareValue(o);

    if (res != 0) {
      return res;
    }

    // Try params

    return params.compareTo(o.params);
  }

  @Override
  public int hashCode() {
    return getName().hashCode() * getComparator().hashCode();
  }

  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof PropWrapper)) {
      return false;
    }
    return compareTo((PropWrapper)o) == 0;
  }

  @Override
  protected void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);

    /* Just serialize the entity?
    if (params.size() > 0) {
      sb.append(", params=");
      sb.append(params);
    }

    sb.append(", value=\"");
    sb.append(getValue());
    sb.append("\"");
    */
  }
}
