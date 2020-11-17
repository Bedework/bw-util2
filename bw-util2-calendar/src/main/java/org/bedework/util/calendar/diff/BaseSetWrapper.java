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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;

/** This class wraps an array of components.
 *
 * @author Mike Douglass
 *
 * @param <T>
 * @param <ParentT>
 * @param <ListT>
 */
abstract class BaseSetWrapper<T extends BaseWrapper<?>,
                              ParentT extends BaseWrapper<?>, ListT>
        extends BaseWrapper<ParentT> {
  private final Set<T> els = new TreeSet<>();

  private T[] tarray;

  BaseSetWrapper(final ParentT parent,
                 final QName name,
                 final List<ListT> elsList) {
    super(parent, name);

    if (elsList == null) {
      return;
    }

    for (final ListT el: elsList) {
      final Set<T> t = getWrapped(el);

      if (t == null) {
        // Skip this one
        continue;
      }

      els.addAll(t);
    }

    /* The set is ordered - use that to produce ordered array */

    tarray = getTarray(els.size());
    int i = 0;

    for (final T t: els) {
      getTarray()[i] = t;
      i++;
    }
  }

  /** Some multi-valued elements, e.g. categories, are better handled as a set
   * of separate values. In general this method will just return the wrapped
   * object. For those special cases it will return a set of the wrapped
   * objects.
   *
   * @param el List type
   * @return null if the property is skipped, one or more wrapped objects otherwise.
   */
  abstract Set<T> getWrapped(ListT el);

  abstract T[] getTarray(int len);

  Set<T> getEls() {
    return els;
  }

  int size() {
    return els.size();
  }

  public T[] getTarray() {
    return tarray;
  }

  T find(final QName nm) {
    for (final T t: els) {
      if (t.getName().equals(nm)) {
        return t;
      }
    }

    return null;
  }

  List<T> findAll(final QName nm) {
    final List<T> found = new ArrayList<>();

    for (final T t: els) {
      if (t.getName().equals(nm)) {
        found.add(t);
      }
    }

    return found;
  }

  @Override
  protected void toStringSegment(final ToString ts) {
    ts.append("size", size());

    for (final T t: els) {
      ts.newLine();
      ts.append(t.toString());
    }
  }

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    toStringSegment(ts);

    return ts.toString();
  }
}
