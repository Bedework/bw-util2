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

import org.bedework.util.calendar.PropertyIndex.ParameterInfoIndex;
import org.bedework.util.calendar.PropertyIndex.PropertyInfoIndex;
import org.bedework.util.misc.Util;

import ietf.params.xml.ns.icalendar_2.ActionPropType;
import ietf.params.xml.ns.icalendar_2.AltrepParamType;
import ietf.params.xml.ns.icalendar_2.ArrayOfComponents;
import ietf.params.xml.ns.icalendar_2.ArrayOfParameters;
import ietf.params.xml.ns.icalendar_2.ArrayOfProperties;
import ietf.params.xml.ns.icalendar_2.AttendeePropType;
import ietf.params.xml.ns.icalendar_2.BaseComponentType;
import ietf.params.xml.ns.icalendar_2.BaseParameterType;
import ietf.params.xml.ns.icalendar_2.BasePropertyType;
import ietf.params.xml.ns.icalendar_2.CategoriesPropType;
import ietf.params.xml.ns.icalendar_2.ClassPropType;
import ietf.params.xml.ns.icalendar_2.CnParamType;
import ietf.params.xml.ns.icalendar_2.CommentPropType;
import ietf.params.xml.ns.icalendar_2.CompletedPropType;
import ietf.params.xml.ns.icalendar_2.ContactPropType;
import ietf.params.xml.ns.icalendar_2.CreatedPropType;
import ietf.params.xml.ns.icalendar_2.CutypeParamType;
import ietf.params.xml.ns.icalendar_2.DateDatetimePropertyType;
import ietf.params.xml.ns.icalendar_2.DaylightType;
import ietf.params.xml.ns.icalendar_2.DelegatedFromParamType;
import ietf.params.xml.ns.icalendar_2.DelegatedToParamType;
import ietf.params.xml.ns.icalendar_2.DescriptionPropType;
import ietf.params.xml.ns.icalendar_2.DirParamType;
import ietf.params.xml.ns.icalendar_2.DtendPropType;
import ietf.params.xml.ns.icalendar_2.DtstampPropType;
import ietf.params.xml.ns.icalendar_2.DtstartPropType;
import ietf.params.xml.ns.icalendar_2.DuePropType;
import ietf.params.xml.ns.icalendar_2.DurationPropType;
import ietf.params.xml.ns.icalendar_2.ExrulePropType;
import ietf.params.xml.ns.icalendar_2.FbtypeParamType;
import ietf.params.xml.ns.icalendar_2.FreebusyPropType;
import ietf.params.xml.ns.icalendar_2.FreqRecurType;
import ietf.params.xml.ns.icalendar_2.GeoPropType;
import ietf.params.xml.ns.icalendar_2.IcalendarType;
import ietf.params.xml.ns.icalendar_2.LanguageParamType;
import ietf.params.xml.ns.icalendar_2.LastModifiedPropType;
import ietf.params.xml.ns.icalendar_2.LocationPropType;
import ietf.params.xml.ns.icalendar_2.MemberParamType;
import ietf.params.xml.ns.icalendar_2.MethodPropType;
import ietf.params.xml.ns.icalendar_2.ObjectFactory;
import ietf.params.xml.ns.icalendar_2.OrganizerPropType;
import ietf.params.xml.ns.icalendar_2.PartstatParamType;
import ietf.params.xml.ns.icalendar_2.PercentCompletePropType;
import ietf.params.xml.ns.icalendar_2.PeriodType;
import ietf.params.xml.ns.icalendar_2.PriorityPropType;
import ietf.params.xml.ns.icalendar_2.ProdidPropType;
import ietf.params.xml.ns.icalendar_2.RdatePropType;
import ietf.params.xml.ns.icalendar_2.RecurType;
import ietf.params.xml.ns.icalendar_2.RecurrenceIdPropType;
import ietf.params.xml.ns.icalendar_2.RelatedParamType;
import ietf.params.xml.ns.icalendar_2.RelatedToPropType;
import ietf.params.xml.ns.icalendar_2.ReltypeParamType;
import ietf.params.xml.ns.icalendar_2.RepeatPropType;
import ietf.params.xml.ns.icalendar_2.ResourcesPropType;
import ietf.params.xml.ns.icalendar_2.RoleParamType;
import ietf.params.xml.ns.icalendar_2.RrulePropType;
import ietf.params.xml.ns.icalendar_2.ScheduleStatusParamType;
import ietf.params.xml.ns.icalendar_2.SentByParamType;
import ietf.params.xml.ns.icalendar_2.SequencePropType;
import ietf.params.xml.ns.icalendar_2.StandardType;
import ietf.params.xml.ns.icalendar_2.StatusPropType;
import ietf.params.xml.ns.icalendar_2.SummaryPropType;
import ietf.params.xml.ns.icalendar_2.TranspPropType;
import ietf.params.xml.ns.icalendar_2.TriggerPropType;
import ietf.params.xml.ns.icalendar_2.TzidParamType;
import ietf.params.xml.ns.icalendar_2.UidPropType;
import ietf.params.xml.ns.icalendar_2.UntilRecurType;
import ietf.params.xml.ns.icalendar_2.UrlPropType;
import ietf.params.xml.ns.icalendar_2.ValarmType;
import ietf.params.xml.ns.icalendar_2.VcalendarType;
import ietf.params.xml.ns.icalendar_2.VersionPropType;
import ietf.params.xml.ns.icalendar_2.VeventType;
import ietf.params.xml.ns.icalendar_2.VfreebusyType;
import ietf.params.xml.ns.icalendar_2.VjournalType;
import ietf.params.xml.ns.icalendar_2.VtimezoneType;
import ietf.params.xml.ns.icalendar_2.VtodoType;
import ietf.params.xml.ns.icalendar_2.XBedeworkCostPropType;
import ietf.params.xml.ns.icalendar_2.XBedeworkWrappedNameParamType;
import ietf.params.xml.ns.icalendar_2.XBedeworkWrapperPropType;
import ietf.params.xml.ns.icalendar_2.XBwCategoriesPropType;
import ietf.params.xml.ns.icalendar_2.XBwContactPropType;
import ietf.params.xml.ns.icalendar_2.XBwLocationPropType;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.NumberList;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TextList;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.Daylight;
import net.fortuna.ical4j.model.component.Standard;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.component.VJournal;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.parameter.TzId;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Categories;
import net.fortuna.ical4j.model.property.FreeBusy;
import net.fortuna.ical4j.model.property.Geo;
import net.fortuna.ical4j.model.property.PercentComplete;
import net.fortuna.ical4j.model.property.Priority;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Repeat;
import net.fortuna.ical4j.model.property.Resources;
import net.fortuna.ical4j.model.property.Sequence;
import net.fortuna.ical4j.model.property.XProperty;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;

/** Conversion to XML from ical4j
 * @author douglm
 */
public class IcalToXcal {
  static ObjectFactory of = new ObjectFactory();

  /**
   * @param cal an ical4j calendar
   * @param pattern - allows specification of a subset to be returned.
   * @return icalendar in XML
   */
  public static IcalendarType fromIcal(final Calendar cal,
                                       final BaseComponentType pattern,
                                       final boolean wrapXprops) {
    return fromIcal(cal, pattern, false, wrapXprops);
  }

  /**
   * @param cal an ical4j calendar
   * @param pattern - allows specification of a subset to be returned.
   * @param doTimezones false to skip timezones
   * @return Internal XML representation of iCalendar object
   */
  public static IcalendarType fromIcal(final Calendar cal,
                                       final BaseComponentType pattern,
                                       final boolean doTimezones,
                                       final boolean wrapXprops) {
    final IcalendarType ical = new IcalendarType();
    final VcalendarType vcal = new VcalendarType();

    ical.getVcalendar().add(vcal);

    processProperties(cal.getProperties(), vcal,
                      pattern, wrapXprops);

    final ComponentList<? extends CalendarComponent> icComps =
            cal.getComponents();

    if (icComps == null) {
      return ical;
    }

    final ArrayOfComponents aoc = new ArrayOfComponents();
    vcal.setComponents(aoc);

    for (final var o: icComps) {
      if (!doTimezones && (o instanceof VTimeZone)) {
        // Skip these
        continue;
      }
      aoc.getBaseComponent().add(toComponent(o,
                                             pattern,
                                             wrapXprops));
    }

    return ical;
  }

  /** Make a BaseComponentType component from an ical4j object. This may produce a
   * VEvent, VTodo or VJournal.
   *
   * @param val the component
   * @param pattern - if non-null limit returned components and values to those
   *                  supplied in the pattern.
   * @return JAXBElement<? extends BaseComponentType>
   */
  public static JAXBElement<? extends BaseComponentType>
                    toComponent(final Component val,
                                final BaseComponentType pattern,
                                final boolean wrapXprops) {
    if (val == null) {
      return null;
    }

    final PropertyList icprops = val.getProperties();
    ComponentList<? extends Component> icComps = null;

    if (icprops == null) {
      // Empty component
      return null;
    }

    final JAXBElement<? extends BaseComponentType> el;

    if (val instanceof VEvent) {
      el = of.createVevent(new VeventType());
      icComps = ((VEvent)val).getAlarms();
    } else if (val instanceof VToDo) {
      el = of.createVtodo(new VtodoType());
      icComps = ((VToDo)val).getAlarms();
    } else if (val instanceof VJournal) {
      el = of.createVjournal(new VjournalType());
    } else if (val instanceof VFreeBusy) {
      el = of.createVfreebusy(new VfreebusyType());
    } else if (val instanceof VAlarm) {
      el = of.createValarm(new ValarmType());
    } else if (val instanceof VTimeZone) {
      el = of.createVtimezone(new VtimezoneType());
      icComps = ((VTimeZone)val).getObservances();
    } else if (val instanceof Daylight) {
      el = of.createDaylight(new DaylightType());
    } else if (val instanceof Standard) {
      el = of.createStandard(new StandardType());
    } else {
      throw new RuntimeException("org.bedework.invalid.entity.type" +
          val.getClass().getName());
    }

    final BaseComponentType comp = el.getValue();

    processProperties(val.getProperties(), comp,
                      pattern, wrapXprops);

    if (Util.isEmpty(icComps)) {
      return el;
    }

    /* Process any sub-components */
    final ArrayOfComponents aoc = new ArrayOfComponents();
    comp.setComponents(aoc);

    for (final var o: icComps) {
      final JAXBElement<? extends BaseComponentType> subel =
              toComponent(o,
                          pattern, wrapXprops);
      aoc.getBaseComponent().add(subel);
    }

    return el;
  }

  /**
   * @param icprops property list
   * @param comp xml base component
   * @param pattern pattern
   */
  public static void processProperties(final PropertyList icprops,
                                       final BaseComponentType comp,
                                       final BaseComponentType pattern,
                                       final boolean wrapXprops) {
    if ((icprops == null) || icprops.isEmpty()) {
      return;
    }

    comp.setProperties(new ArrayOfProperties());
    final List<JAXBElement<? extends BasePropertyType>> pl =
            comp.getProperties().getBasePropertyOrTzid();

    for (final Object icprop : icprops) {
      final Property prop = (Property)icprop;

      final PropertyInfoIndex pii = PropertyInfoIndex
              .fromName(prop.getName());

      if ((pii != null) &&
              !emit(pattern, comp.getClass(), pii.getXmlClass())) {
        continue;
      }

      final JAXBElement<? extends BasePropertyType> xmlprop =
              doProperty(prop, pii, wrapXprops);

      if (xmlprop != null) {
        processParameters(prop.getParameters(), xmlprop.getValue());
        pl.add(xmlprop);
      }
    }
  }

  static JAXBElement<? extends BasePropertyType> doProperty(
          final Property prop,
          final PropertyInfoIndex pii,
          final boolean wrapXprops) {
    if (prop instanceof XProperty) {
      if (!wrapXprops) {
        return null;
      }

      final XBedeworkWrapperPropType wrapper =
              new XBedeworkWrapperPropType();

      wrapper.setText(prop.getValue());

      //processParameters(prop.getParameters(), wrapper);

      final XBedeworkWrappedNameParamType wnp =
              new XBedeworkWrappedNameParamType();
      wnp.setText(prop.getName());
      if (wrapper.getParameters() == null) {
        wrapper.setParameters(new ArrayOfParameters());
      }
      wrapper.getParameters().getBaseParameter().add(
              of.createXBedeworkWrappedName(wnp));

      return of.createXBedeworkWrapper(wrapper);
    }

    switch (pii) {
      case ACTION:
        /* ------------------- Action: Alarm -------------------- */

        final ActionPropType a = new ActionPropType();
        a.setText(prop.getValue());
        return of.createAction(a);

      case ATTACH:
        /* ------------------- Attachments -------------------- */
        //          pl.add(setAttachment(att));
        return null;

      case ATTENDEE:
        /* ------------------- Attendees -------------------- */

        final AttendeePropType att = new AttendeePropType();
        att.setCalAddress(prop.getValue());
        return of.createAttendee(att);

      case BUSYTYPE:
        return null;

      case CATEGORIES:
        /* ------------------- Categories -------------------- */

        // LANG - filter on language - group language in one cat list?

        final CategoriesPropType c = new CategoriesPropType();
        final TextList cats = ((Categories)prop).getCategories();

        final Iterator<String> pit = cats.iterator();
        while (pit.hasNext()) {
          c.getText().add(pit.next());
        }

        return of.createCategories(c);

      case CLASS:
        /* ------------------- Class -------------------- */

        final ClassPropType cl = new ClassPropType();
        cl.setText(prop.getValue());
        return of.createClass(cl);

      case COMMENT:
        /* ------------------- Comments -------------------- */

        final CommentPropType cm = new CommentPropType();
        cm.setText(prop.getValue());
        return of.createComment(cm);

      case COMPLETED:
        /* ------------------- Completed -------------------- */

        final CompletedPropType cmp = new CompletedPropType();
        cmp.setUtcDateTime(XcalUtil.getXMlUTCCal(prop.getValue()));
        return of.createCompleted(cmp);

      case CONTACT:
        /* ------------------- Contact -------------------- */

        // LANG
        final ContactPropType ct = new ContactPropType();
        ct.setText(prop.getValue());

        return of.createContact(ct);

      case CREATED:
        /* ------------------- Created -------------------- */

        final CreatedPropType created = new CreatedPropType();
        created.setUtcDateTime(XcalUtil.getXMlUTCCal(prop.getValue()));
        return of.createCreated(created);

      case DESCRIPTION:
        /* ------------------- Description -------------------- */

        final DescriptionPropType desc = new DescriptionPropType();
        desc.setText(prop.getValue());
        return of.createDescription(desc);

      case DTEND:
        /* ------------------- DtEnd -------------------- */

        final DtendPropType dtend =
                (DtendPropType)makeDateDatetime(new DtendPropType(),
                                                prop);
        return of.createDtend(dtend);

      case DTSTAMP:
        /* ------------------- DtStamp -------------------- */

        final DtstampPropType dtstamp = new DtstampPropType();
        dtstamp.setUtcDateTime(XcalUtil.getXMlUTCCal(prop.getValue()));
        return of.createDtstamp(dtstamp);

      case DTSTART:
        /* ------------------- DtStart -------------------- */

        final DtstartPropType dtstart =
                (DtstartPropType)makeDateDatetime(new DtstartPropType(),
                                                  prop);
        return of.createDtstart(dtstart);

      case DUE:
        /* ------------------- Due -------------------- */

        final DuePropType due =
                (DuePropType)makeDateDatetime(new DuePropType(),
                                              prop);
        return of.createDue(due);

      case DURATION:
        /* ------------------- Duration -------------------- */

        final DurationPropType dur = new DurationPropType();

        dur.setDuration(prop.getValue());
        return of.createDuration(dur);

      case EXDATE:
        /* ------------------- ExDate --below------------ */
        return null;

      case EXRULE:
        /* ------------------- ExRule --below------------- */

        final ExrulePropType er = new ExrulePropType();
        er.setRecur(doRecur(((RRule)prop).getRecur()));

        return of.createExrule(er);

      case FREEBUSY:
        /* ------------------- freebusy -------------------- */

        final FreeBusy icfb = (FreeBusy)prop;
        final PeriodList fbps = icfb.getPeriods();

        if (Util.isEmpty(fbps)) {
          return null;
        }

        final FreebusyPropType fb = new FreebusyPropType();

        final String fbtype = paramVal(prop, Parameter.FBTYPE);

        if (fbtype != null) {
          final ArrayOfParameters pars = getAop(fb);

          final FbtypeParamType f = new FbtypeParamType();

          f.setText(fbtype);
          final JAXBElement<FbtypeParamType> param = of.createFbtype(f);
          pars.getBaseParameter().add(param);
        }

        final List<PeriodType> pdl =  fb.getPeriod();

        for (final Object o: fbps) {
          final Period p = (Period)o;
          final PeriodType np = new PeriodType();

          np.setStart(XcalUtil.getXMlUTCCal(p.getStart().toString()));
          np.setEnd(XcalUtil.getXMlUTCCal(p.getEnd().toString()));
          pdl.add(np);
        }

        return of.createFreebusy(fb);

      case GEO:
        /* ------------------- Geo -------------------- */

        final Geo geo = (Geo)prop;
        final GeoPropType g = new GeoPropType();

        g.setLatitude(geo.getLatitude().floatValue());
        g.setLatitude(geo.getLongitude().floatValue());
        return of.createGeo(g);

      case LAST_MODIFIED:
        /* ------------------- LastModified -------------------- */

        final LastModifiedPropType lm = new LastModifiedPropType();
        lm.setUtcDateTime(XcalUtil.getXMlUTCCal(prop.getValue()));
        return of.createLastModified(lm);

      case LOCATION:
        /* ------------------- Location -------------------- */

        final LocationPropType l = new LocationPropType();
        l .setText(prop.getValue());

        return of.createLocation(l);

      case METHOD:
        /* ------------------- Method -------------------- */

        final MethodPropType m = new MethodPropType();

        m.setText(prop.getValue());
        return of.createMethod(m);

      case ORGANIZER:
        /* ------------------- Organizer -------------------- */

        final OrganizerPropType org = new OrganizerPropType();
        org.setCalAddress(prop.getValue());
        return of.createOrganizer(org);

      case PERCENT_COMPLETE:
        /* ------------------- PercentComplete -------------------- */

        final PercentCompletePropType p = new PercentCompletePropType();
        p.setInteger(BigInteger.valueOf(
                ((PercentComplete)prop).getPercentage()));

        return of.createPercentComplete(p);

      case PRIORITY:
        /* ------------------- Priority -------------------- */

        final PriorityPropType pr = new PriorityPropType();
        pr.setInteger(BigInteger.valueOf(((Priority)prop).getLevel()));

        return of.createPriority(pr);

      case PRODID:
        /* ------------------- Prodid -------------------- */
        final ProdidPropType prod = new ProdidPropType();
        prod.setText(prop.getValue());
        return of.createProdid(prod);

      case RDATE:
        /* ------------------- RDate ------------------- */

        final RdatePropType rdate =
                (RdatePropType)makeDateDatetime(new RdatePropType(),
                                                prop);
        return of.createRdate(rdate);

      case RECURRENCE_ID:
        /* ------------------- RecurrenceID -------------------- */

        final RecurrenceIdPropType ri = new RecurrenceIdPropType();
        String strval = prop.getValue();

        if (dateOnly(prop)) {
          // RECUR - fix all day recurrences sometime
          if (strval.length() > 8) {
            // Try to fix up bad all day recurrence ids. - assume a local timezone
            strval = strval.substring(0, 8);
          }

          ri.setDate(XcalUtil.fromDtval(strval));
        } else {
          XcalUtil.initDt(ri, strval, getTzid(prop));
        }

        return of.createRecurrenceId(ri);

      case RELATED_TO:
        /* ------------------- RelatedTo -------------------- */

        final RelatedToPropType rt = new RelatedToPropType();

        final String relType = paramVal(prop, Parameter.RELTYPE);
        final String value = paramVal(prop, Parameter.VALUE);

        if ((value == null) || "uid".equalsIgnoreCase(value)) {
          rt.setUid(prop.getValue());
        } else if ("uri".equalsIgnoreCase(value)) {
          rt.setUri(prop.getValue());
        } else {
          rt.setText(prop.getValue());
        }

        if (relType != null) {
          final ArrayOfParameters pars = getAop(rt);

          final ReltypeParamType r = new ReltypeParamType();
          r.setText(relType);
          final JAXBElement<ReltypeParamType> param = of.createReltype(r);
          pars.getBaseParameter().add(param);
        }

        return of.createRelatedTo(rt);

      case REPEAT:
        /* ------------------- Repeat Alarm -------------------- */
        final Repeat rept = (Repeat)prop;
        final RepeatPropType rep = new RepeatPropType();
        rep.setInteger(BigInteger.valueOf(rept.getCount()));

        return of.createRepeat(rep);

      case REQUEST_STATUS:
        /* ------------------- RequestStatus -------------------- */

        // XXX Later
        return null;

      case RESOURCES:
        /* ------------------- Resources -------------------- */

        final ResourcesPropType r = new ResourcesPropType();

        final List<String> rl = r.getText();
        final TextList rlist = ((Resources)prop).getResources();

        final Iterator<String> rlit = rlist.iterator();
        while (rlit.hasNext()) {
          rl.add(rlit.next());
        }

        return of.createResources(r);

      case RRULE:
        /* ------------------- RRule ------------------- */

        final RrulePropType rrp = new RrulePropType();
        rrp.setRecur(doRecur(((RRule)prop).getRecur()));

        return of.createRrule(rrp);

      case SEQUENCE:
        /* ------------------- Sequence -------------------- */

        final SequencePropType s = new SequencePropType();
        s.setInteger(BigInteger.valueOf(((Sequence)prop).getSequenceNo()));

        return of.createSequence(s);

      case STATUS:
        /* ------------------- Status -------------------- */

        final StatusPropType st = new StatusPropType();

        st.setText(prop.getValue());
        return of.createStatus(st);

      case SUMMARY:
        /* ------------------- Summary -------------------- */

        final SummaryPropType sum = new SummaryPropType();
        sum.setText(prop.getValue());
        return of.createSummary(sum);

      case TRIGGER:
        /* ------------------- Trigger - alarm -------------------- */
        final TriggerPropType trig = new TriggerPropType();

        final String valType = paramVal(prop, Parameter.VALUE);

        if ((valType == null) ||
            (valType.equalsIgnoreCase(Value.DURATION.getValue()))) {
          trig.setDuration(prop.getValue());
          final String rel = paramVal(prop, Parameter.RELATED);
          if (rel != null) {
            final ArrayOfParameters pars = getAop(trig);

            final RelatedParamType rpar = new RelatedParamType();
            rpar.setText(IcalDefs.alarmTriggerRelatedEnd);
            final JAXBElement<RelatedParamType> param = of.createRelated(rpar);
            pars.getBaseParameter().add(param);
          }
        } else if (valType.equalsIgnoreCase(Value.DATE_TIME.getValue())) {
          //t.setDateTime(val.getTrigger());
          trig.setDateTime(XcalUtil.getXMlUTCCal(prop.getValue()));
        }

        return of.createTrigger(trig);

      case TRANSP:
        /* ------------------- Transp -------------------- */

        final TranspPropType t = new TranspPropType();
        t.setText(prop.getValue());
        return of.createTransp(t);

      case TZID:
      case TZNAME:
      case TZOFFSETFROM:
      case TZOFFSETTO:
      case TZURL:
        return null;

      case UID:
        /* ------------------- Uid -------------------- */

        final UidPropType uid = new UidPropType();
        uid.setText(prop.getValue());
        return of.createUid(uid);

      case URL:
        /* ------------------- Url -------------------- */

        final UrlPropType u = new UrlPropType();

        u.setUri(prop.getValue());
        return of.createUrl(u);

      case VERSION:
        /* ------------------- Version - vcal only -------------------- */

        final VersionPropType vers = new VersionPropType();
        vers.setText(prop.getValue());
        return of.createVersion(vers);

      case XBEDEWORK_COST:
        /* ------------------- Cost -------------------- */

        final XBedeworkCostPropType cst =
                new XBedeworkCostPropType();

        cst.setText(prop.getValue());
        return of.createXBedeworkCost(cst);

      case X_BEDEWORK_CATEGORIES:
        /* ------------------- Categories -------------------- */

        final XBwCategoriesPropType xpcat =
                new XBwCategoriesPropType();

        xpcat.getText().add(prop.getValue());
        return of.createXBedeworkCategories(xpcat);

      case X_BEDEWORK_CONTACT:
        /* ------------------- Categories -------------------- */

        final XBwContactPropType xpcon =
                new XBwContactPropType();

        xpcon.setText(prop.getValue());
        return of.createXBedeworkContact(xpcon);

      case X_BEDEWORK_LOCATION:
        /* ------------------- Categories -------------------- */

        final XBwLocationPropType xploc =
                new XBwLocationPropType();

        xploc.setText(prop.getValue());
        return of.createXBedeworkLocation(xploc);

      default:
        if (prop instanceof XProperty) {
          /* ------------------------- x-property --------------------------- */

          /*
          final PropertyInfoIndex xpii =
                  PropertyInfoIndex.fromName(prop.getName());

          if (xpii == null) {
            return null;
          }

          return null;*/

          if (!wrapXprops) {
            return null;
          }

          final XBedeworkWrapperPropType wrapper =
                  new XBedeworkWrapperPropType();

          processParameters(prop.getParameters(), wrapper);

          return of.createXBedeworkWrapper(wrapper);
        }

    } // switch (pii)

    return null;
  }

  static void processParameters(final ParameterList icparams,
                                final BasePropertyType prop) {
    if ((icparams == null) || icparams.isEmpty()) {
      return;
    }

    final Iterator<Parameter> it = icparams.iterator();

    while (it.hasNext()) {
      final Parameter param = it.next();

      final ParameterInfoIndex pii =
              ParameterInfoIndex.lookupPname(param.getName());

      if (pii == null) {
        continue;
      }

      final JAXBElement<? extends BaseParameterType> xmlprop =
          doParameter(param, pii);

      if (xmlprop != null) {
        if (prop.getParameters() == null) {
          prop.setParameters(new ArrayOfParameters());
        }

        prop.getParameters().getBaseParameter().add(xmlprop);
      }
    }
  }

  static JAXBElement<? extends BaseParameterType>
  doParameter(final Parameter param,
              final ParameterInfoIndex pii) {
    switch (pii) {
      case ALTREP:
        final AltrepParamType ar = new AltrepParamType();
        ar.setUri(param.getValue());
        return of.createAltrep(ar);

      case CN:
        final CnParamType cn = new CnParamType();
        cn.setText(param.getValue());
        return of.createCn(cn);

      case CUTYPE:
        final CutypeParamType c = new CutypeParamType();
        c.setText(param.getValue());
        return of.createCutype(c);

      case DELEGATED_FROM:
        final DelegatedFromParamType df = new DelegatedFromParamType();
        df.getCalAddress().add(param.getValue());
        return of.createDelegatedFrom(df);

      case DELEGATED_TO:
        final DelegatedToParamType dt = new DelegatedToParamType();
        dt.getCalAddress().add(param.getValue());
        return of.createDelegatedTo(dt);

      case DIR:
        final DirParamType d = new DirParamType();
        d.setUri(param.getValue());
        return of.createDir(d);

      case ENCODING:
        return null;

      case FMTTYPE:
        return null;

      case FBTYPE:
        return null;

      case LANGUAGE:
        final LanguageParamType l = new LanguageParamType();
        l.setText(param.getValue());
        return of.createLanguage(l);

      case MEMBER:
        final MemberParamType m = new MemberParamType();
        m.getCalAddress().add(param.getValue());
        return of.createMember(m);

      case PARTSTAT:
        final PartstatParamType partstat = new PartstatParamType();
        partstat.setText(param.getValue());
        return of.createPartstat(partstat);

      case RANGE:
        return null;

      case RELATED:
        return null;

      case RELTYPE:
        return null;

      case ROLE:
        final RoleParamType r = new RoleParamType();
        r.setText(param.getValue());
        return of.createRole(r);

      case RSVP:
        return null;

      case SCHEDULE_AGENT:
        return null;

      case SCHEDULE_STATUS:
        final ScheduleStatusParamType ss = new ScheduleStatusParamType();
        ss.setText(param.getValue());
        return of.createScheduleStatus(ss);

      case SENT_BY:
        final SentByParamType sb = new SentByParamType();
        sb.setCalAddress(param.getValue());
        return of.createSentBy(sb);

      case TYPE:
        return null;

      case TZID:
        final TzidParamType tzid = new TzidParamType();
        tzid.setText(param.getValue());
        return of.createTzid(tzid);

      case VALUE:
        return null;

      default:

    } // switch (pii)

    return null;
  }

  /** Build recurring properties from ical recurrence.
   *
   * @param r Recur object
   * @return RecurType filled in
   */
  public static RecurType doRecur(final Recur r) {
    final RecurType rt = new RecurType();

    rt.setFreq(FreqRecurType.fromValue(r.getFrequency().name()));
    if (r.getCount() > 0) {
      rt.setCount(BigInteger.valueOf(r.getCount()));
    }

    final Date until = r.getUntil();
    if (until != null) {
      final UntilRecurType u = new UntilRecurType();
      XcalUtil.initUntilRecur(u, until.toString());
    }

    if (r.getInterval() > 0) {
      rt.setInterval(String.valueOf(r.getInterval()));
    }

    listFromNumberList(rt.getBysecond(),
                       r.getSecondList());

    listFromNumberList(rt.getByminute(),
                       r.getMinuteList());

    listFromNumberList(rt.getByhour(),
                       r.getHourList());

    if (r.getDayList() != null) {
      final List<String> l = rt.getByday();

      for (final Object o: r.getDayList()) {
        l.add(((WeekDay)o).getDay().name());
      }
    }

    listFromNumberList(rt.getByyearday(),
                       r.getYearDayList());

    intlistFromNumberList(rt.getBymonthday(),
                          r.getMonthDayList());

    listFromNumberList(rt.getByweekno(),
                       r.getWeekNoList());

    intlistFromNumberList(rt.getBymonth(),
                          r.getMonthList());

    bigintlistFromNumberList(rt.getBysetpos(),
                             r.getSetPosList());

    return rt;
  }

  private static void listFromNumberList(final List<String> l,
                                        final NumberList nl) {
    if (nl == null) {
      return;
    }

    for (final var o: nl) {
      l.add(String.valueOf(o));
    }
  }

  private static void intlistFromNumberList(final List<Integer> l,
                                            final NumberList nl) {
    if (nl == null) {
      return;
    }

    l.addAll(nl);
  }

  private static void bigintlistFromNumberList(final List<BigInteger> l,
                                            final NumberList nl) {
    if (nl == null) {
      return;
    }

    for (final var o: nl) {
      l.add(BigInteger.valueOf(o));
    }
  }

  private static String getTzid(final Property p) {
    final TzId tzidParam = (TzId)p.getParameter(Parameter.TZID);

    if (tzidParam == null) {
      return null;
    }

    return tzidParam.getValue();
  }

  private static boolean dateOnly(final Property p) {
    final Value valParam = (Value)p.getParameter(Parameter.VALUE);

    if ((valParam == null) || (valParam.getValue() == null)) {
      return false;
    }

    return valParam.getValue().toUpperCase().equals(Value.DATE);
  }

  private static String paramVal(final Property p,
                                 final String paramName) {
    final Parameter param = p.getParameter(paramName);

    if ((param == null) || (param.getValue() == null)) {
      return null;
    }

    return param.getValue();
  }

  private static ArrayOfParameters getAop(final BasePropertyType prop) {
    ArrayOfParameters pars = prop.getParameters();

    if (pars == null) {
      pars = new ArrayOfParameters();
      prop.setParameters(pars);
    }

    return pars;
  }

  private static DateDatetimePropertyType makeDateDatetime(
          final DateDatetimePropertyType p,
          final Property prop) {
    XcalUtil.initDt(p, prop.getValue(), getTzid(prop));

    return p;
  }

  private static boolean emit(final BaseComponentType pattern,
                              final Class<?> compCl,
                              final Class<?>... cl) {
    if (pattern == null) {
      return true;
    }

    if (!compCl.getName().equals(pattern.getClass().getName())) {
      return false;
    }

    if ((cl == null) || (cl.length == 0)) {
      // Any property
      return true;
    }

    final String className = cl[0].getName();

    if (BasePropertyType.class.isAssignableFrom(cl[0])) {
      if (pattern.getProperties() == null) {
        return false;
      }

      final List<JAXBElement<? extends BasePropertyType>> patternProps =
         pattern.getProperties().getBasePropertyOrTzid();

      for (final JAXBElement<? extends BasePropertyType> jp: patternProps) {
        if (jp.getValue().getClass().getName().equals(className)) {
          return true;
        }
      }

      return false;
    }

    final List<JAXBElement<? extends BaseComponentType>> patternComps =
      XcalUtil.getComponents(pattern);

    if (patternComps == null) {
      return false;
    }

    // Check for component

    for (final JAXBElement<? extends BaseComponentType> jp: patternComps) {
      if (jp.getValue().getClass().getName().equals(className)) {
        return emit(pattern, cl[0], Arrays.copyOfRange(cl, 1, cl.length - 1));
      }
    }

    return false;
  }
}
