/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.h2gis.spatialut;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import org.h2.value.ValueGeometry;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Assert with Geometry type
 *
 * @author Nicolas Fortin
 */
public class GeometryAsserts {
    /**
     * Default, Epsilon value for metric projections unit test
     */
    private static final double EPSILON = .01;

    /**
     * Check Geometry type,X,Y,Z and SRID
     *
     * @param expectedWKT Expected value, in WKT
     * @param valueWKB    Test value, in WKB ex rs.getBytes()
     * @throws SQLException If WKT or WKB is not valid
     */
    public static void assertGeometryEquals(String expectedWKT, byte[] valueWKB) throws SQLException {
        if (expectedWKT == null) {
            assertNull(valueWKB);
        } else {
            assertGeometryEquals(expectedWKT, ValueGeometry.get(valueWKB).getObject());
        }
    }

    /**
     * Check Geometry type,X,Y,Z
     *
     * @param expectedWKT Expected value, in WKT
     * @param valueObject Test value geometry ex rs.getObject(i)
     * @throws SQLException If WKT or WKB is not valid
     */
    public static void assertGeometryEquals(String expectedWKT, Object valueObject) throws SQLException {
        assertGeometryEquals(expectedWKT, 0, valueObject);
    }


    /**
     * Check Geometry type,X,Y,Z and SRID
     *
     * @param expectedWKT Expected value, in WKT
     * @param expectedSRID Expected SRID code,
     * @param valueObject Test value geometry ex rs.getObject(i)
     * @throws SQLException If WKT or WKB is not valid
     */
    public static void assertGeometryEquals(String expectedWKT,int expectedSRID, Object valueObject) throws SQLException {
        if (expectedWKT == null) {
            assertNull(valueObject);
        } else {
            ValueGeometry expected = ValueGeometry.get(expectedWKT, expectedSRID);
            ValueGeometry actual = ValueGeometry.tryGet(valueObject);
            assertEquals("Expected:\n" + expected.getString() + "\nActual:\n" + actual.getString(), expected, actual);
        }
    }
    /**
     * Check only X,Y and geometry type
     *
     * @param expectedWKT Expected value, in WKT
     * @param valueWKT    Test value, in WKT ex rs.getString()
     * @throws SQLException
     */
    public static void assertGeometryEquals(String expectedWKT, String valueWKT) throws SQLException {
        assertGeometryEquals(expectedWKT, ValueGeometry.get(valueWKT).getBytes());
    }

    /**
     * Equals test with epsilon error acceptance.
     *
     * @param expectedWKT     Expected value, in WKT
     * @param resultSetObject Geometry, rs.getObject(i)
     */
    public static void assertGeometryBarelyEquals(String expectedWKT, Object resultSetObject) {
        assertGeometryBarelyEquals(expectedWKT, resultSetObject, EPSILON);
    }

    /**
     * Equals test with epsilon error acceptance.
     * @param expectedWKT Expected value, in WKT
     * @param resultSetObject Geometry, rs.getObject(i)
     * @param epsilon epsilon error acceptance
     */
    public static void assertGeometryBarelyEquals(String expectedWKT, Object resultSetObject, double epsilon) {
        assertGeometryBarelyEquals(expectedWKT, 0, resultSetObject, epsilon);
    }

    /**
     * Equals test with epsilon error acceptance and SRID.
     * @param expectedWKT Expected value, in WKT
     * @param expectedSRID Expected SRID Value
     * @param resultSetObject
     * @param epsilon epsilon error acceptance
     */
    public static void assertGeometryBarelyEquals(String expectedWKT,int expectedSRID, Object resultSetObject, double epsilon) {
        assertTrue(resultSetObject instanceof Geometry);
        Geometry expectedGeometry = (Geometry)ValueGeometry.get(expectedWKT, expectedSRID).getObject();
        Geometry result = (Geometry) resultSetObject;
        assertEquals(expectedGeometry.getGeometryType(), result.getGeometryType());
        assertEquals(expectedGeometry.getNumPoints(), result.getNumPoints());
        Coordinate[] expectedCoordinates = expectedGeometry.getCoordinates();
        Coordinate[] resultCoordinates = result.getCoordinates();
        for (int idPoint = 0; idPoint < expectedCoordinates.length; idPoint++) {
            assertEquals(expectedCoordinates[idPoint].x, resultCoordinates[idPoint].x, epsilon);
            assertEquals(expectedCoordinates[idPoint].y, resultCoordinates[idPoint].y, epsilon);
            assertEquals(expectedCoordinates[idPoint].z, resultCoordinates[idPoint].z, epsilon);
        }
    }
}
