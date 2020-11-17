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

import org.bedework.util.misc.ToString;
import org.bedework.util.misc.Util;

import ietf.params.xml.ns.icalendar_2.TextParameterType;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

/** This class wraps a property.
 *
 * <p>The mappedName handles cases where we preserve a value with an x-property.
 *
 * <p>MS adds an organizer to events - even if it's not a meeting, which is out
 * of spec. We preserve that value in an x-property. The real name is the name
 * of the x-property. The mappedName is "organizer". This allows comparisons to work
 * OK and we can manufacture the correct update from the property.
 *
 * @author Mike Douglass
 *
 * @param <T>
 * @param <ParentT>
 * @param <EntityT>
 */
abstract class BaseEntityWrapper<T extends BaseEntityWrapper,
                                 ParentT extends BaseWrapper, EntityT>
        extends BaseWrapper<ParentT> {
  private QName mappedName;

  /* The entity we want to change */
  private EntityT entity;

  BaseEntityWrapper(final ParentT parent,
                    final QName name,
                    final EntityT entity) {
    super(parent, name);
    this.entity = entity;

    mappedName = getMappedName(name);

    if (mappedName == null) {
      mappedName = name;
    }
  }

  QName getMappedName() {
    return mappedName;
  }

  EntityT getEntity() {
    return entity;
  }

  @SuppressWarnings("unchecked")
  JAXBElement<? extends EntityT> getJaxbElement() {
    return new JAXBElement<EntityT>(getName(),
                                    (Class<EntityT>)entity.getClass(),
                                    getEntity());
  }

  /**
   * @param name
   * @return null for no mapping.
   */
  abstract QName getMappedName(QName name);

  /** For example, two events objects represent the same entity if the uid and
   * recurrence-ids match.
   *
   * @param val - the other wrapped entity
   * @return true if this represents the same (but possibly altered) entity.
   */
  abstract boolean sameEntity(BaseEntityWrapper<?, ?, ?> val);

  public final static QName XBedeworkWrapperQNAME =
          new QName("urn:ietf:params:xml:ns:icalendar-2.0",
                    "x-bedework-wrapper");

  public final static QName XBedeworkWrappedNameQNAME =
          new QName("urn:ietf:params:xml:ns:icalendar-2.0",
                    "x-bedework-wrapped-name");


  public int compareNames(final BaseEntityWrapper<?, ?, ?> that) {
    QName thatN = that.getMappedName();

    int res = getMappedName().getNamespaceURI().compareTo(thatN.getNamespaceURI());
    if (res != 0) {
      return res;
    }

    res = getMappedName().getLocalPart().compareTo(thatN.getLocalPart());
    if (res != 0) {
      return res;
    }

    if (!getMappedName().equals(XBedeworkWrapperQNAME)) {
      return 0;
    }

    // Name is in the params

    final PropWrapper thatProp = (PropWrapper)that;
    final PropWrapper thisProp = (PropWrapper)this;

    String thatname = null;
    String thisname = null;

    for (final ParamWrapper prop: thatProp.params.getEls()) {
      if (prop.getMappedName().equals(XBedeworkWrappedNameQNAME)) {
        thatname = ((TextParameterType)prop.getEntity()).getText();
        break;
      }
    }

    for (final ParamWrapper prop: thisProp.params.getEls()) {
      if (prop.getMappedName().equals(XBedeworkWrappedNameQNAME)) {
        thisname = ((TextParameterType)prop.getEntity()).getText();
        break;
      }
    }

    return Util.compareStrings(thisname, thatname);
  }

  public int compareNameClass(final BaseEntityWrapper<?, ?, ?> that) {
    int res = compareNames(that);
    if (res != 0) {
      return res;
    }

    return getEntity().getClass().getName().compareTo(that.getEntity().getClass().getName());
  }

  public int compareTo(final BaseEntityWrapper<?, ?, ?> o) {
    final int res = Util.compareStrings(getName().getLocalPart(),
                                        o.getName().getLocalPart());

    if (res != 0) {
      return res;
    }

    return Util.compareStrings(getName().getNamespaceURI(),
                               o.getName().getNamespaceURI());
  }

  @Override
  protected void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);

    if (!mappedName.equals(getName())) {
      ts.append("mappedName", mappedName);
    }
  }
}
