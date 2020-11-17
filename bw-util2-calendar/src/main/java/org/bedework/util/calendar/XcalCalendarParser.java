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

import org.bedework.util.xml.XmlUtil;
import org.bedework.util.xml.tagdefs.XcalTags;

import net.fortuna.ical4j.data.CalendarParser;
import net.fortuna.ical4j.data.ContentHandler;
import net.fortuna.ical4j.data.ParserException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
public class XcalCalendarParser implements CalendarParser {
  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  //private Logger log = Logger.getLogger(XmlCalendarBuilder.class);

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
      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);

      final DocumentBuilder builder = factory.newDocumentBuilder();

      final Document doc = builder.parse(new InputSource(in));

      process(doc, handler);
    } catch (final Throwable t) {
      throw new ParserException(t.getMessage(), 0, t);
    }
  }

  private void process(final Document doc,
                       final ContentHandler handler)
          throws ParserException {
    // start = element icalendar { vcalendar+ }

    final Element root = doc.getDocumentElement();

    if (!XmlUtil.nodeMatches(root, XcalTags.icalendar)) {
      // error
      throw new ParserException("Expected " + XcalTags.icalendar +
                                " found " + root, 0);
    }

    for (final Element el: getChildren(root)) {
      // Expect vcalendar

      if (!XmlUtil.nodeMatches(el, XcalTags.vcalendar)) {
        // error
        throw new ParserException("Expected " + XcalTags.vcalendar +
                                  " found " + el, 0);
      }

      processVcalendar(el, handler);
    }
  }

  private void processVcalendar(final Element el,
                                final ContentHandler handler)
          throws ParserException {
    handler.startCalendar();

    try {
      final Collection<Element> els = XmlUtil.getElements(el);
      /*
          vcalendar = element vcalendar {
            type-calprops,
            type-component
          }
       */
      final Iterator<Element> elit = els.iterator();

      Element vcel = null;

      if (elit.hasNext()) {
        vcel = elit.next();
      }

      /*
        type-calprops = element properties {
            property-prodid &
            property-version &
            property-calscale? &
            property-method?
        }
       */
      if (XmlUtil.nodeMatches(vcel, XcalTags.properties)) {
        processProperties(vcel, handler);

        if (elit.hasNext()) {
          vcel = elit.next();
        } else {
          vcel = null;
        }
      }

      if (XmlUtil.nodeMatches(vcel, XcalTags.components)) {
        processCalcomps(vcel, handler);

        if (elit.hasNext()) {
          vcel = elit.next();
        } else {
          vcel = null;
        }
      }

      if (vcel != null) {
        throw new ParserException("Unexpected element: found " + vcel, 0);
      }
    } catch (final RuntimeException e) {
      throw new ParserException(e.getMessage(), 0, e);
    }
  }

  private void processProperties(final Element el,
                                 final ContentHandler handler)
          throws ParserException {
    try {
      for (final Element e: XmlUtil.getElements(el)) {
        processProperty(e, handler);
      }
    } catch (final RuntimeException e) {
      throw new ParserException(e.getMessage(), 0, e);
    }
  }

  private void processCalcomps(final Element el,
                               final ContentHandler handler)
          throws ParserException {
    /*
      type-component = element components {
          (
              component-vevent |
              component-vtodo |
              component-vjournal |
              component-vfreebusy |
              component-vtimezone
          )*
      }
     */
    try {
      for (final Element e: XmlUtil.getElements(el)) {
        processComponent(e, handler);
      }
    } catch (final RuntimeException e) {
      throw new ParserException(e.getMessage(), 0, e);
    }
  }

  private void processComponent(final Element el,
                                final ContentHandler handler)
          throws ParserException {
    try {
      handler.startComponent(el.getLocalName().toUpperCase());

      for (final Element e: XmlUtil.getElements(el)) {
        if (XmlUtil.nodeMatches(e, XcalTags.properties)) {
          processProperties(e, handler);
        } else if (XmlUtil.nodeMatches(e, XcalTags.components)) {
          for (final Element ce: XmlUtil.getElements(e)) {
            processComponent(ce, handler);
          }
        } else {
          throw new ParserException("Unexpected element: found " + e, 0);
        }
      }

      handler.endComponent(el.getLocalName().toUpperCase());
    } catch (final RuntimeException e) {
      throw new ParserException(e.getMessage(), 0, e);
    }
  }

  private void processProperty(final Element el,
                               final ContentHandler handler)
          throws ParserException {
    try {
      handler.startProperty(el.getLocalName());

      for (final Element e: XmlUtil.getElements(el)) {
        if (XmlUtil.nodeMatches(e, XcalTags.parameters)) {
          for (final Element par: XmlUtil.getElements(e)) {
            handler.parameter(par.getLocalName(),
                              XmlUtil.getElementContent(par));
          }
        }

        if (!processValue(e, handler)) {
          throw new ParserException("Bad property " + el, 0);
        }
      }

      handler.endProperty(el.getLocalName());
    } catch (final Throwable t) {
      throw new ParserException(t.getMessage(), 0, t);
    }
  }

  private boolean processValue(final Element el,
                               final ContentHandler handler)
          throws ParserException {
    try {
      if (XmlUtil.nodeMatches(el, XcalTags.recurVal)) {
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
        final StringBuilder sb = new StringBuilder();

        String delim = "";

        for (final Element re: XmlUtil.getElements(el)) {
          sb.append(delim);
          delim = ";";
          sb.append(re.getLocalName().toUpperCase());
          sb.append("=");
          sb.append(XmlUtil.getElementContent(re));
        }

        handler.propertyValue(sb.toString());

        return true;
      }

      if (XmlUtil.nodeMatches(el, XcalTags.binaryVal) ||
          XmlUtil.nodeMatches(el, XcalTags.booleanVal) ||
          XmlUtil.nodeMatches(el, XcalTags.calAddressVal) ||
          XmlUtil.nodeMatches(el, XcalTags.dateVal) ||
          XmlUtil.nodeMatches(el, XcalTags.dateTimeVal) ||
          XmlUtil.nodeMatches(el, XcalTags.durationVal) ||
          XmlUtil.nodeMatches(el, XcalTags.floatVal) ||
          XmlUtil.nodeMatches(el, XcalTags.integerVal) ||
          XmlUtil.nodeMatches(el, XcalTags.periodVal) ||
          XmlUtil.nodeMatches(el, XcalTags.textVal) ||
          XmlUtil.nodeMatches(el, XcalTags.timeVal) ||
          XmlUtil.nodeMatches(el, XcalTags.uriVal) ||
          XmlUtil.nodeMatches(el, XcalTags.utcOffsetVal)) {
        handler.propertyValue(XmlUtil.getElementContent(el));
        return true;
      }

      return false;
    } catch (final Throwable t) {
      throw new ParserException(t.getMessage(), 0, t);
    }
  }

  /* ====================================================================
   *                   XmlUtil wrappers
   * ==================================================================== */

  boolean icalElement(final Element el) {
    if (el == null) {
      return false;
    }

    final String ns = el.getNamespaceURI();

    if ((ns == null) || !ns.equals(XcalTags.namespace)) {
      return false;
    }

    return true;
  }

  boolean icalElement(final Element el, final String name) {
    if (!icalElement(el)) {
      return false;
    }

    final String ln = el.getLocalName();

    if (ln == null) {
      return false;
    }

    return ln.equals(name);
  }

  protected Collection<Element> getChildren(final Node nd)
          throws ParserException {
    try {
      return XmlUtil.getElements(nd);
    } catch (final Throwable t) {
      throw new ParserException(t.getMessage(), 0);
    }
  }

  protected Element[] getChildrenArray(final Node nd)
          throws ParserException {
    try {
      return XmlUtil.getElementsArray(nd);
    } catch (final Throwable t) {
      throw new ParserException(t.getMessage(), 0);
    }
  }

  protected Element getOnlyChild(final Node nd) throws ParserException {
    try {
      return XmlUtil.getOnlyElement(nd);
    } catch (final Throwable t) {
      throw new ParserException(t.getMessage(), 0);
    }
  }

  protected String getElementContent(final Element el) throws ParserException {
    try {
      return XmlUtil.getElementContent(el);
    } catch (final Throwable t) {
      throw new ParserException(t.getMessage(), 0);
    }
  }

  protected boolean isEmpty(final Element el) throws ParserException {
    try {
      return XmlUtil.isEmpty(el);
    } catch (final Throwable t) {
      throw new ParserException(t.getMessage(), 0);
    }
  }
}
