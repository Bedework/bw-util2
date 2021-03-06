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

import net.fortuna.ical4j.vcard.Group;
import net.fortuna.ical4j.vcard.GroupRegistry;
import net.fortuna.ical4j.vcard.Parameter;
import net.fortuna.ical4j.vcard.ParameterFactory;
import net.fortuna.ical4j.vcard.ParameterFactoryRegistry;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.PropertyFactory;
import net.fortuna.ical4j.vcard.PropertyFactoryRegistry;
import net.fortuna.ical4j.vcard.property.Xproperty;
import net.fortuna.ical4j.vcard.property.Xproperty.ExtendedFactory;

import java.util.ArrayList;
import java.util.List;

/** Vcard property builder
 *
 * @author douglm
 *
 */
public class PropertyBuilder {
  private static final GroupRegistry groupRegistry = new GroupRegistry();

  private static final PropertyFactoryRegistry propertyFactoryRegistry =
    new PropertyFactoryRegistry();

  private static final ParameterFactoryRegistry parameterFactoryRegistry =
    new ParameterFactoryRegistry();

  private PropertyBuilder() {
  }

  /**
   * @param name property name
   * @param value its value
   * @return Property or null
   * @throws RuntimeException on fatal error
   */
  public static Property getProperty(final String name,
                                     final String value) {
    try {
      final PropertyFactory<?> factory =
              propertyFactoryRegistry.getFactory(name);

      if (factory != null) {
        return factory.createProperty(new ArrayList<>(), value);
      }

      final ExtendedFactory xfactory = (ExtendedFactory)Xproperty.FACTORY;

      return xfactory.createProperty(name,
                                     new ArrayList<>(),
                                     value);
    } catch (final Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * @param groupVal
   * @param name
   * @param value
   * @return Property or null
   * @throws RuntimeException on fatal error
   */
  public static Property getProperty(final String groupVal,
                                     final String name,
                                     final String value) {
    final Group group = groupRegistry.getGroup(groupVal);
    final PropertyFactory<?> factory = propertyFactoryRegistry.getFactory(name);

    if (factory == null) {
        return null;
    }

    try {
      return factory.createProperty(group, new ArrayList<>(), value);
    } catch (final Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * @param groupVal
   * @param paramName
   * @param paramValue
   * @param name
   * @param value
   * @return Property or null
   * @throws RuntimeException on fatal error
   */
  public static Property getProperty(final String groupVal,
                                     final String paramName,
                                     final String paramValue,
                                     final String name,
                                     final String value) {
    final Group group = groupRegistry.getGroup(groupVal);

    final List<Parameter> parameters = new ArrayList<>();

    final ParameterFactory<? extends Parameter> paramFactory =
      parameterFactoryRegistry.getFactory(paramName.toUpperCase());

    if (paramFactory == null) {
      return null;
    }

    parameters.add(paramFactory.createParameter(paramValue));

    final PropertyFactory<?> factory = propertyFactoryRegistry.getFactory(name);

    if (factory == null) {
      return null;
    }

    try {
      return factory.createProperty(group, parameters, value);
    } catch (final Throwable t) {
      throw new RuntimeException(t);
    }
  }
}
