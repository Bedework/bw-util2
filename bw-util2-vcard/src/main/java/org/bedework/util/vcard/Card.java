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
package org.bedework.util.vcard;

import org.bedework.util.misc.GeoUri;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser.Feature;
import net.fortuna.ical4j.data.FoldingWriter;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Encodable;
import net.fortuna.ical4j.model.property.LastModified;
import net.fortuna.ical4j.util.Strings;
import net.fortuna.ical4j.vcard.Parameter;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.Property.Id;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;
import net.fortuna.ical4j.vcard.parameter.Type;
import net.fortuna.ical4j.vcard.property.Accessible;
import net.fortuna.ical4j.vcard.property.Address;
import net.fortuna.ical4j.vcard.property.Geo;
import net.fortuna.ical4j.vcard.property.Revision;
import net.fortuna.ical4j.vcard.property.Uid;
import net.fortuna.ical4j.vcard.property.Url;
import net.fortuna.ical4j.vcard.property.Version;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** A vcard and properties for cardDAV
 *
 * @author douglm
 *
 */
public class Card {
  private int version;

  private String uid;

  private String name;

  private String created;

  private VCard vcard;

  private String strForm;

  private String jsonStrForm;

  private String prevLastmod;

  private final static JsonFactory jsonFactory;

  static {
    jsonFactory = new JsonFactory();
    jsonFactory.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    jsonFactory.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
    jsonFactory.configure(Feature.ALLOW_COMMENTS, true);
  }

  /** Create Card with a new embedded VCard
   *
   */
  public Card() {
    this(new VCard());
    vcard.getProperties().add(Version.VERSION_4_0);

    version = 4;
  }

  /** Create card with supplied vcard
   *
   * @param vcard the card
   */
  public Card(final VCard vcard) {
    this.vcard = vcard;
    prevLastmod = getLastmod();
  }

  /**
   * @return Major part of version number - only 3 or 4 supported
   */
  public int getVersion() {
    return version;
  }

  /** Set the name
  *
  * @param val    String name
  */
 public void setName(final String val) {
   name = val;
 }

 /** Get the name
  *
  * @return String   name
  */
 public String getName() {
   return name;
 }

  /**
   * @param val created
   */
  public void setCreated(final String val) {
    created = val;

    changed();
  }

  /**
   * @return String created
   */
  public String getCreated() {
    return created;
  }

  @SuppressWarnings(value = "unused")
  public void setLastmod() {
    final DateTime dt = new DateTime(true);

    setLastmod(new LastModified(dt).getValue());

    changed();
  }

  /**
   * @param val lastmod
   */
  public void setLastmod(final String val) {
    try {
      replaceProperty(new Revision(new ArrayList<>(), val));
    } catch (final Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * @return String
   */
  public String getLastmod() {
    final Revision rev = (Revision)findProperty(Id.REV);

    if (rev == null) {
      return null;
    }

    return rev.getValue();
  }

  /** Lastmod before any changes were made
   *
   * @return String
   */
  public String getPrevLastmod() {
    return prevLastmod;
  }

  /**
   * @param val String uid
   */
  public void setUid(final String val) {
    uid = val;
  }

  /**
   * @return String
   */
  public String getUid() {
    return uid;
  }

  /**
   * @param poBox post office box address component
   * @param extended extended address component
   * @param street street address component
   * @param locality locality address component
   * @param region region address component
   * @param postcode postal code address component
   * @param country country address component
   * @param types optional address types
   */
  public void setAddress(final String poBox,
                         final String extended,
                         final String street,
                         final String locality,
                         final String region,
                         final String postcode,
                         final String country,
                         final Type...types) {
    final Address addr = new Address(poBox, extended, street,
                                     locality, region, postcode,
                                     country, types);
    addProperty(addr);
  }

  public void setAccessible(final boolean value) {
    addProperty(new Accessible(value));
  }

  public void setUrl(final String value) {
    try {
      addProperty(new Url(Collections.emptyList(), value));
    } catch (final URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public void setGeoUri(final String value) {
    if (value == null) {
      removeProperty(Id.GEO);
      return;
    }

    final GeoUri gu = GeoUri.parse(value);
    final Geo geo = new Geo(BigDecimal.valueOf(gu.getCoordA()),
                            BigDecimal.valueOf(gu.getCoordB()));
    addProperty(geo);
  }

  /**
   * @return vcard or null
   */
  public VCard getVcard() {
    return vcard;
  }

  /**
   * @param val property
   */
  public void addProperty(final Property val) {
    if ((val.getId() != Id.VERSION) ||
        (findProperty(Id.VERSION) == null)) {
      vcard.getProperties().add(val);
    }

    changed();
  }

  /**
   * @param id of property
   * @return property or null
   */
  public Property findProperty(final Id id) {
    return vcard.getProperty(id);
  }

  /**
   * @param id of property
   */
  public void removeProperty(final Id id) {
    final Property p = findProperty(id);
    if (p != null) {
      vcard.getProperties().remove(p);
    }
  }

  /**
   * @param name or property
   * @return property or null
   */
  public Property findProperty(final String name) {
    Id id = null;

    for (final Id i: Id.values()) {
      if (i.toString().equals(name)) {
        id = i;
        break;
      }
    }

    if (id != null) {
      return vcard.getProperty(id);
    }

    return vcard.getExtendedProperty(name);
  }

  /**
   * @param name or property
   * @return property or null
   */
  public List<Property> findProperties(final String name) {
    Id id = null;

    for (final Id i: Id.values()) {
      if (i.toString().equals(name)) {
        id = i;
        break;
      }
    }

    if (id != null) {
      return vcard.getProperties(id);
    }

    return vcard.getExtendedProperties(name);
  }

  /**
   * @param rdr a Reader
   * @throws RuntimeException for parse error
   */
  public void parse(final Reader rdr) {
    try {
      vcard = new VCardBuilder(rdr).build();
    } catch (final Throwable t) {
      throw new RuntimeException(t);
    }

    changed();

    /* Make sure we have a UID - this may be difficult */
    final Version vp = (Version)vcard.getProperty(Id.VERSION);

    if (vp == null) {
      throw new RuntimeException("Missing VERSION");
    }

    final String v = vp.getValue();

    if (v.equals("3.0")) {
      version = 3;
    } else if (v.equals("4.0")) {
      version = 4;
    } else {
      throw new RuntimeException("Unsupported VERSION: " + v);
    }

    final Uid uidp = (Uid)findProperty(Id.UID);

    if (uidp != null) {
      uid = uidp.getValue();
    } else {
      final Property xuidp = vcard.getExtendedProperty("X-ABUID");
      if (xuidp == null) {
        throw new RuntimeException("No uid property found");
      }

      uid = xuidp.getValue();
    }
  }

  /**
   * @return String
   * @throws RuntimeException for fatal error
   */
  public String outputVcard(final String version) {
    if (strForm != null) {
      return strForm;
    }

    final StringWriter sw = new StringWriter();

    try (final FoldingWriter wtr =
                 new FoldingWriter(sw,
                                   FoldingWriter.REDUCED_FOLD_LENGTH)) {
      wtr.write("BEGIN:VCARD");
      wtr.write(Strings.LINE_SEPARATOR);

      /* Version should come before anything else. */
      boolean version4 = false;

      if (version != null) {
        version4 = version.equals("4.0");
      } else {
        final Version v = (Version)vcard.getProperty(Id.VERSION);

        if (v != null) {
          version4 = v.equals(Version.VERSION_4_0);
        }
      }

      if (version4) {
        wtr.write(Version.VERSION_4_0.toString());
      } else {
        wtr.write(new Version("3.0").toString());
      }

      for (final Property prop : vcard.getProperties()) {
        if (prop.getId() == Id.VERSION) {
          continue;
        }

        if (version4) {
          wtr.write(prop.toString());
          continue;
        }

        /* Attempt to downgrade by turning some properties into x-props. */
        appendDowngraded(wtr, prop);
      }

      wtr.write("END:VCARD");
      wtr.write(Strings.LINE_SEPARATOR);
    } catch (final Throwable t) {
      throw new RuntimeException(t);
    }

    strForm = sw.toString();

    return strForm;
  }

  /**
   * @param indent true for pretty
   * @param version V4 or V3
   * @return String json value
   * @throws RuntimeException for fatal error
   */
  public String outputJson(final boolean indent,
                           final String version) {
    if (jsonStrForm != null) {
      return jsonStrForm;
    }

    final StringWriter sw = new StringWriter();

    try (final JsonGenerator jgen = jsonFactory.createGenerator(sw)) {
      if (indent) {
        jgen.useDefaultPrettyPrinter();
      }

      jgen.writeStartArray(); // for vcard

      jgen.writeString("vcard");
      jgen.writeStartArray();       // Array of properties

      /* Version should come before anything else. */
      boolean version4 = false;

      if (version != null) {
        version4 = version.equals("4.0");
      } else {
        final Version v = (Version)vcard.getProperty(Id.VERSION);

        if (v != null) {
          version4 = v.equals(Version.VERSION_4_0);
        }
      }

      final Property pversion;

      if (version4) {
        pversion = Version.VERSION_4_0;
      } else {
        pversion = new Version("3.0");
      }

      JsonProperty.addFields(jgen, pversion);

      final Set<String> pnames = VcardDefs.getPropertyNames();

      /* Output known properties first */

      for (final String pname: pnames) {
        if ("VERSION".equals(pname)) {
          continue;
        }

        final List<Property> props = findProperties(pname);

        if (!props.isEmpty()) {
          for (final Property p: props) {
            JsonProperty.addFields(jgen, p);
          }
        }
      }

      /* Now output any extra unknown properties */

      final List<Property> props = vcard.getProperties();

      if (props != null) {
        for (final Property p: props) {
          if (!pnames.contains(p.getId().toString())) {
            JsonProperty.addFields(jgen, p);
          }
        }
      }

      jgen.writeEndArray(); // End event properties

      jgen.writeEndArray(); // for vcard

      jgen.flush();
    } catch (final Throwable t) {
      throw new RuntimeException(t);
    }

    jsonStrForm = sw.toString();

    return jsonStrForm;
  }

  @Override
  public String toString() {
    try {
      return outputVcard(null);
    } catch (final Throwable t) {
      return t.getMessage();
    }
  }

  private void replaceProperty(final Property val) {
    final List<Property> ps = vcard.getProperties();

    final Property p = vcard.getProperty(val.getId());

    if (p != null) {
      ps.remove(p);
    }

    ps.add(val);

    changed();
  }

  private void appendDowngraded(final Writer wtr,
                                final Property prop) throws IOException {
    /* From rfc6350 - vcard 4.0

    Appendix A. Differences from RFCs 2425 and 2426
     This appendix contains a high-level overview of the major changes
     that have been made in the vCard specification from RFCs 2425 and
     2426.  It is incomplete, as it only lists the most important changes.

    A.1. New Structure
     o  [RFC2425] and [RFC2426] have been merged.

     o  vCard is now not only a MIME type but a stand-alone format.

     o  A proper MIME type registration form has been included.

     o  UTF-8 is now the only possible character set.

     o  New vCard elements can be registered from IANA.

    A.2. Removed Features
     o  The CONTEXT and CHARSET parameters are no more.

     o  The NAME, MAILER, LABEL, and CLASS properties are no more.

     o  The "intl", "dom", "postal", and "parcel" TYPE parameter values
        for the ADR property have been removed.

     o  In-line vCards (such as the value of the AGENT property) are no
        longer supported.

    A.3. New Properties and Parameters
     o  The KIND, GENDER, LANG, ANNIVERSARY, XML, and CLIENTPIDMAP
        properties have been added.

     o  [RFC2739], which defines the FBURL, CALADRURI, CAPURI, and CALURI
        properties, has been merged in.

     o  [RFC4770], which defines the IMPP property, has been merged in.

     o  The "work" and "home" TYPE parameter values are now applicable to
        many more properties.

     o  The "pref" value of the TYPE parameter is now a parameter of its
        own, with a positive integer value indicating the level of
        preference.

     o  The ALTID and PID parameters have been added.

     o  The MEDIATYPE parameter has been added and replaces the TYPE
        parameter when it was used for indicating the media type of the
        property's content.
     */

    if (v3Ok(prop)) {
      wtr.write(prop.toString());
      return;
    }

    /* x-prop already? */

//    if (Property.Id.EXTENDED == prop.getId()) {
//      if (skipXprops.contains(Extend))
//      wtr.write(prop.toString());
//      return;
//    }

    /* Output as x-prop */
    if (prop.getGroup() != null) {
      wtr.write(prop.getGroup().toString());
      wtr.write('.');
    }

    wtr.write(VcardDefs.v4AsXpropPrefix);
    wtr.write(prop.getId().getPropertyName());

    for (final Parameter param : prop.getParameters()) {
      wtr.write(';');

      /* Watch for non v3 */
      wtr.write(param.toString());
    }
    wtr.write(':');

    if (prop instanceof Encodable) {
      wtr.write(Strings.escape(Strings.valueOf(prop.getValue())));
    }
    else {
      wtr.write(Strings.valueOf(prop.getValue()));
    }

    wtr.write(Strings.LINE_SEPARATOR);
  }

  private static final Set<Id> notV3Ok = new TreeSet<>();

  private static final Set<String> skipXprops = new TreeSet<>();

  static {
    notV3Ok.add(Id.KIND);
    notV3Ok.add(Id.GENDER);
    notV3Ok.add(Id.LANG);
    notV3Ok.add(Id.ANNIVERSARY);
    notV3Ok.add(Id.XML);
    notV3Ok.add(Id.CLIENTPIDMAP);

    /* Treat UID specially - we know some mappings */
    //notV3Ok.add(Property.Id.UID);

    skipXprops.add("X-ABUID");
  }

  /* Return true if this property should be OK for v3 */
  private boolean v3Ok(final Property prop) {
    final Id id = prop.getId();

    if (notV3Ok.contains(id)) {
      return false;
    }

    /* altid and pid parameters were added in v.4 */

    Parameter par = prop.getParameter(Parameter.Id.ALTID);
    if (par != null) {
      return false;
    }

    par = prop.getParameter(Parameter.Id.PID);
    return par == null;
  }

  private void changed() {
    jsonStrForm = null;
    strForm = null;
  }
}
