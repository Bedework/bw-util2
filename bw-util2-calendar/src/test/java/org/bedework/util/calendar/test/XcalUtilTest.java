/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.util.calendar.test;

import org.bedework.util.calendar.XcalUtil;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * User: mike Date: 1/22/20 Time: 17:11
 */
public class XcalUtilTest {
  @Test
  public void testGetIcalFormatDateTime() {
    // Good dates - if date contains "-" then next one in array
    // should be result. if not result should equal value
    final String[] gooddates = {
            "2021-11-12",
            "20211112",
            "2021-11-12T01:02:03",
            "20211112T010203",
            "2021-11-12T01:02:03Z",
            "20211112T010203Z",
            };

    final String[] baddates = {
            "bad",
            "",
            "2021-11-12junk",
            "20211112junk",
            "2021-11-xx",
            "202111xx",
            "2021-11-12T01:02",
            "20211112T0102",
            "2021-11-1201:02:03:000",
            "20211112010203000",
            "2021-11-12T01:02:03:000",
            "20211112T010203000",
            "2021-11-12T01:02:03z",
            "20211112T010203z",
            "2021-11-1201:02:03Z",
            "20211112010203Z",
            };

    for (int i = 0; i < gooddates.length; i++) {
      final String val = gooddates[i];
      final String res = XcalUtil.getIcalFormatDateTime(val);

      assertNotNull(res);

      if (val.contains("-")) {
        assertEquals(gooddates[i + 1], res);
      }
    }

    for (final String val: baddates) {
      assertNull(XcalUtil.getIcalFormatDateTime(val),
                 "Expected null for val " + val);
    }
  }
}
