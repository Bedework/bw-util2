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

import org.bedework.util.calendar.XcalUtil.TzGetter;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.xml.tagdefs.XcalTags;

import ietf.params.xml.ns.icalendar_2.CreatedPropType;
import ietf.params.xml.ns.icalendar_2.DtstampPropType;
import ietf.params.xml.ns.icalendar_2.IcalendarType;
import ietf.params.xml.ns.icalendar_2.LastModifiedPropType;
import ietf.params.xml.ns.icalendar_2.ProdidPropType;
import ietf.params.xml.ns.icalendar_2.VcalendarType;
import ietf.params.xml.ns.icalendar_2.VersionPropType;
import ietf.params.xml.ns.icalendar_2.XBedeworkUidParamType;
import org.oasis_open.docs.ws_calendar.ns.soap.ComponentSelectionType;
import org.oasis_open.docs.ws_calendar.ns.soap.ObjectFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** This class compares 2 components. These components may contain other
 * sub-components, for example alarms. What is produced is a set of updates
 * to generate one from the other.
 *
 * <p>For this to work, and for updates to be applicable, we need well defined
 * rules determining how we match components. For example, an event is matched
 * if the UID and RECURRENCE-ID matches.
 *
 * <p>This is a little more difficult for some components which don't have a uid,
 * alarms for example.
 *
 * @author Mike Douglass
 */
public class XmlIcalCompare implements Logged {
  /** */
  public static final List<Object> defaultSkipList;

  static {
    defaultSkipList = new ArrayList<>();

    /* Calendar properties */
    defaultSkipList.add(new ProdidPropType());
    defaultSkipList.add(new VersionPropType());

    /* Entity properties */
    defaultSkipList.add(new CreatedPropType());
    defaultSkipList.add(new DtstampPropType());
    defaultSkipList.add(new LastModifiedPropType());

    /* Parameters */
    defaultSkipList.add(new XBedeworkUidParamType());
  }

  /** Stuff to which all the generated objects need access.
   */
  static class Globals {
    Map<String, Object> skipMap;
    ObjectFactory of;
    ValueMatcher matcher;
    TzGetter tzs;

    Globals(final Map<String, Object> skipMap,
            final ObjectFactory of,
            final ValueMatcher matcher,
            final TzGetter tzs) {
      this.skipMap = skipMap;
      this.of = of;
      this.matcher = matcher;
      this.tzs = tzs;
    }
  }

  private Globals globals;

  /** The skippedEntities allow the diff process to ignore components,
   * properties and/or parameters that should not take part in the comparison.
   *
   * <p>Populate the list with empty icalendar objects.
   *
   * @param skippedEntities Objects of the class that should be skipped
   * @param tzs for fetching timezones
   */
  public XmlIcalCompare(final List<?> skippedEntities,
                        final TzGetter tzs) {
    globals = new Globals(new HashMap<>(),
                          new ObjectFactory(),
                          new ValueMatcher(),
                          tzs);

    for (Object o: skippedEntities) {
      globals.skipMap.put(o.getClass().getCanonicalName(), o);
    }
  }

  /** Compare the parameters. Return null for equal or a select element.
   *
   * @param newval possible new state
   * @param oldval the current state
   * @return SelectElementType if newval and oldval differ else null.
   */
  public ComponentSelectionType diff(final IcalendarType newval,
                                     final IcalendarType oldval) {
    VcalendarType nv = newval.getVcalendar().get(0);
    VcalendarType ov = oldval.getVcalendar().get(0);

    CompWrapper ncw = new CompWrapper(globals,
                                      XcalTags.vcalendar,
                                      nv);
    CompWrapper ocw = new CompWrapper(globals,
                                      XcalTags.vcalendar,
                                      ov);

    return ncw.diff(ocw);
  }

  /* ====================================================================
   *                   Logged methods
   * ==================================================================== */

  private BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
