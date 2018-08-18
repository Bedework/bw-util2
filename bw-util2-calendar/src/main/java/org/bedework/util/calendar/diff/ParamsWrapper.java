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

import org.bedework.util.xml.tagdefs.XcalTags;

import ietf.params.xml.ns.icalendar_2.BaseParameterType;
import org.oasis_open.docs.ws_calendar.ns.soap.ParameterReferenceType;
import org.oasis_open.docs.ws_calendar.ns.soap.ParameterSelectionType;
import org.oasis_open.docs.ws_calendar.ns.soap.ParametersSelectionType;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.JAXBElement;

/** This class wraps an array of parameters.
 *
 * @author Mike Douglass
 */
class ParamsWrapper extends BaseSetWrapper<ParamWrapper, PropWrapper,
                                          JAXBElement<? extends BaseParameterType>>
                    implements Comparable<ParamsWrapper> {
  ParamsWrapper(final PropWrapper parent,
                final List<JAXBElement<? extends BaseParameterType>> plist) {
    super(parent, XcalTags.parameters, plist);
  }

  @Override
  ParamWrapper[] getTarray(final int len) {
    return new ParamWrapper[len];
  }

  @Override
  Set<ParamWrapper> getWrapped(final JAXBElement<? extends BaseParameterType> el) {
    /* We skip certain properties as they only appear on one side
     * If this is one we skip return null.
     */
    if (skipThis(el.getValue())) {
      return null;
    }

    Set<ParamWrapper> res = new TreeSet<ParamWrapper>();

    res.add(new ParamWrapper(this, el.getName(), el.getValue()));

    return res;
  }

  /** Return a list of differences between this (the new object) and that (the
   * old object)
   *
   * @param that - the old form.
   * @return changes
   */
  public ParametersSelectionType diff(final ParamsWrapper that) {
    ParametersSelectionType sel = null;

    int thatI = 0;
    int thisI = 0;

    while ((thisI < size()) && (thatI < that.size())) {
      ParamWrapper thisOne = getTarray()[thisI];
      ParamWrapper thatOne = that.getTarray()[thatI];

      if (thisOne.equals(thatOne)) {
        thisI++;
        thatI++;
        continue;
      }

      /* I think it's true that we can only have a single occurence of a
       * parameter name. We can make use of that here
       */

      int ncmp = thisOne.compareNames(thatOne);

      if (ncmp == 0) {
        /* names match - scan down that side to see if we can find a matching
         * parameter. All the intermediate ones would then be marked for
         * deletion.
         *
         * If that doesn't find a match, scan down this side to see if we can
         * match thatOne. If we find a match the intermediate ones would then be
         * marked for addition.
         *
         * We only need to scan so far. The items are all ordered so we should be
         * able to do a simple comparison as an initial test.
         *
         * To complicate issues - we should diff the non-matching parameters to
         * see if it's a value change.
         *
         * A value change is an update to the parameter.
         */

        if (((thisI + 1) == size()) &&
                ((thatI + 1) == that.size())) {
          // No more on this side and that side - call it an update
          sel = select(sel, thisOne.diff(thatOne));
          thisI++;
          thatI++;
          continue;
        }

        /* More on one or both sides. This allows the possibility that an
         * extra multivalued value has been inserted or one deleted.
         *
         * We should check further down both sides.
         *
         * There are a number of possibilities - looking at the values only:
         *
         * 1. changed
         * new  old
         * a    b
         *
         * 2. changed
         * a    b
         * c    c
         *
         * 3. added
         * a    b
         * b
         *
         * 4. removed
         * b    a
         *      b
         *
         * 5. Multiple changes
         * a    b
         * c    d
         * e    e
         *
         * So the process is
         *  1. Try to find a match for the new value.
         *
         *  2. If we do then we have case 4 - remove value advance old
         *
         *  3. Try to find a match for the old value
         *
         *  4. If we do then we have case 3 - add value advance new
         *
         *  5. else we have a changed property - diff and advance both
         */

        //NOTE: because the values are ordered we can probably terminate early


        // We known the 2 current values don't match. Try this one with the next old
        int nextThatI = thatI + 1;
        boolean matchFound = false;

        if (debug) {
          if (thatOne.getMappedName().equals(PropWrapper.XBedeworkWrapperQNAME)) {
            debug("At wrapped x-prop");
          }
        }

        //try to match new to remaining old

        while (nextThatI < that.size()) {
          ParamWrapper nextThatOne = that.getTarray()[nextThatI];

          if (thisOne.compareNames(nextThatOne) != 0) {
            // Into the next property
            break;
          }

          if (thisOne.equals(nextThatOne)) {
            matchFound = true;
            break;
          }

          nextThatI++;
        }

        if (matchFound) {
          /*
            nextThatI is positioned at the next matching property or off the end.
            Remove the extras
           */
          while (thatI < nextThatI) {
            thatOne = that.getTarray()[thatI];
            sel = remove(sel, thatOne.makeRef());
            thatI++;
          }

          continue;
        }

        //try to match old to remaining new
        int nextThisI = thisI + 1;

        while (nextThisI < this.size()) {
          ParamWrapper nextThisOne = getTarray()[nextThisI];

          if (nextThisOne.compareNames(thatOne) != 0) {
            // Into the next property
            break;
          }

          if (nextThisOne.equals(thatOne)) {
            matchFound = true;
            break;
          }

          nextThisI++;
        }

        if (matchFound) {
          /*
            nextThisI is positioned at the next matching property or off the end.
            Add the extras
           */
          while (thisI < nextThisI) {
            thisOne = getTarray()[thisI];
            sel = add(sel, thisOne.makeRef());
            thisI++;
          }

          continue;
        }

        // No match found. Diff the current ones and move on

        sel = select(sel, thisOne.diff(thatOne));
        thisI++;
        thatI++;
      } else if (ncmp < 0) {
        // in this but not that - addition
        sel = add(sel, thisOne.makeRef());
        thisI++;
      } else {
        // in that but not this - deletion
        sel = remove(sel, thatOne.makeRef());
        thatI++;
      }
    }

    while (thisI < size()) {
      // Extra ones in the source

      ParamWrapper thisOne = getTarray()[thisI];
      sel = add(sel, thisOne.makeRef());
      thisI++;
    }

    while (thatI < that.size()) {
      // Extra ones in the target

      ParamWrapper thatOne = that.getTarray()[thatI];
      sel = remove(sel, thatOne.makeRef());
      thatI++;
    }

    return sel;
  }

  ParametersSelectionType getSelect(final ParametersSelectionType val) {
    if (val != null) {
      return val;
    }

    return new ParametersSelectionType();
  }

  ParametersSelectionType add(final ParametersSelectionType sel,
                              final ParameterReferenceType val) {
    ParametersSelectionType csel = getSelect(sel);

    csel.getAdd().add(val);

    return csel;
  }

  ParametersSelectionType remove(final ParametersSelectionType sel,
                                 final ParameterReferenceType val) {
    ParametersSelectionType csel = getSelect(sel);

    csel.getRemove().add(val);

    return csel;
  }

  ParametersSelectionType select(final ParametersSelectionType sel,
                                 final ParameterSelectionType val) {
    ParametersSelectionType csel = getSelect(sel);

    csel.getParameter().add(val);

    return csel;
  }

  @Override
  public int compareTo(final ParamsWrapper that) {
    if (size() < that.size()) {
      return -1;
    }

    if (size() > that.size()) {
      return 1;
    }

    Iterator<ParamWrapper> it = that.getEls().iterator();

    for (ParamWrapper p: getEls()) {
      ParamWrapper thatP = it.next();

      int res = p.compareTo(thatP);

      if (res != 0) {
        return res;
      }
    }

    return 0;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ParamsWrapper{");

    super.toStringSegment(sb);
    sb.append("}");

    return sb.toString();
  }
}
