/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; 
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.mesh;

import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.h2gis.unitTest.GeometryAsserts.assertGeometryEquals;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests of package spatial mesh
 * @author Erwan Bocher
 * @author Nicolas Fortin
 */
public class MeshFunctionTest {
    private static Connection connection;
    private Statement st;

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(MeshFunctionTest.class.getSimpleName());
    }

    @AfterAll
    public static void tearDown() throws Exception {
        connection.close();
    }

    @BeforeEach
    public void setUpStatement() throws Exception {
        st = connection.createStatement();
    }

    @AfterEach
    public void tearDownStatement() throws Exception {
        st.close();
    }

    @Test
    public void testVoronoiPoints() throws Exception {

    }

    @Test
    public void test_ST_DelaunayNullValue() throws Exception {
        try (ResultSet rs = st.executeQuery("SELECT ST_Delaunay(null);")) {
            rs.next();
            assertTrue(rs.getObject(1)==null);
        }
    }

    @Test
    public void test_ST_DelaunayWithPoints1() throws Exception {
        try (ResultSet rs = st.executeQuery("SELECT ST_Delaunay('MULTIPOINTZ ((0 0 1), (10 0 1), (10 10 1))'::GEOMETRY);")) {
            rs.next();
            assertGeometryEquals("MULTIPOLYGONZ (((0 0 1, 10 0 1, 10 10 1, 0 0 1)))",rs.getBytes(1));
        }
    }

    @Test
    public void test_ST_DelaunayWithPoints2() throws Exception {
        try (ResultSet rs = st.executeQuery("SELECT ST_Delaunay('MULTIPOINTZ ((0 0 1), (10 0 1), (10 10 1), (5 5 1))'::GEOMETRY);")) {
            rs.next();
            assertGeometryEquals("MULTIPOLYGONZ (((5 5 1, 0 0 1, 10 0 1, 5 5 1)), ((5 5 1, 10 0 1, 10 10 1, 5 5 1)))",  rs.getBytes(1));
        }
    }

    @Test
    public void test_ST_DelaunayWithCollection() throws Exception {
        try (ResultSet rs = st.executeQuery("SELECT ST_Delaunay('GEOMETRYCOLLECTIONZ (POLYGONZ ((150 380 1, 110 230 1, 180 190 1, 230 300 1, 320 280 1, 320 380 1, 150 380 1)),"
                + "  LINESTRINGZ (70 330 1, 280 220 1))'::GEOMETRY);")) {
            rs.next();
            assertGeometryEquals("MULTIPOLYGONZ (((70 330 1, 150 380 1, 110 230 1, 70 330 1)), ((110 230 1, 150 380 1, 230 300 1, 110 230 1)), ((110 230 1, 230 300 1, 180 190 1, 110 230 1)), ((150 380 1, 320 380 1, 230 300 1, 150 380 1)), ((180 190 1, 230 300 1, 280 220 1, 180 190 1)), ((230 300 1, 320 280 1, 280 220 1, 230 300 1)), ((230 300 1, 320 380 1, 320 280 1, 230 300 1)))", rs.getBytes(1));
        }
    }

    @Test
    public void test_ST_DelaunayWithLines() throws Exception {
        try (ResultSet rs = st.executeQuery("SELECT ST_Delaunay('MULTILINESTRINGZ ((1.1 8 1, 8 8 1), (2 3.1 1, 8 5.1 1))'::GEOMETRY);")) {
            rs.next();
            assertGeometryEquals("MULTIPOLYGONZ (((1.1 8 1, 2 3.1 1, 8 5.1 1, 1.1 8 1)), ((1.1 8 1, 8 5.1 1, 8 8 1, 1.1 8 1)))", rs.getBytes(1));
        }
    }

    @Test
    public void test_ST_DelaunayAsMultiPolygon() throws Exception {
        try (ResultSet rs = st.executeQuery("SELECT ST_Delaunay('POLYGONZ ((1.1 9 1, 1.1 3 1, 5.1 1.1 1, 9.5 6.4 1, 8.8 9.9 1, 5 8 1, 1.1 9 1))'::GEOMETRY, 0);")) {
            rs.next();
            assertGeometryEquals("MULTIPOLYGONZ (((5 8 1, 1.1 3 1, 5.1 1.1 1, 5 8 1)), ((5 8 1, 5.1 1.1 1, 9.5 6.4 1, 5 8 1)), ((1.1 9 1, 1.1 3 1, 5 8 1, 1.1 9 1)), ((8.8 9.9 1, 5 8 1, 9.5 6.4 1, 8.8 9.9 1)), ((1.1 9 1, 5 8 1, 8.8 9.9 1, 1.1 9 1)))\n",  rs.getBytes(1));
        }
    }

    @Test
    public void test_ST_DelaunayAsMultiLineString() throws Exception {
        try (ResultSet rs = st.executeQuery("SELECT ST_Delaunay('POLYGONZ ((1.1 9 1, 1.1 3 1, 5.1 1.1 1, 9.5 6.4 1, 8.8 9.9 1, 5 8 1, 1.1 9 1))'::GEOMETRY, 1);")) {
            rs.next();
            assertGeometryEquals("MULTILINESTRINGZ ((5.1 1.1 1, 9.5 6.4 1), (1.1 3 1, 1.1 9 1), (1.1 9 1, 8.8 9.9 1), (1.1 3 1, 5 8 1), (1.1 9 1, 5 8 1), (5 8 1, 8.8 9.9 1), (8.8 9.9 1, 9.5 6.4 1), (1.1 3 1, 5.1 1.1 1), (5 8 1, 5.1 1.1 1), (5 8 1, 9.5 6.4 1))", rs.getBytes(1));
        }
    }

    @Test
    public void test_ST_ConstrainedDelaunayNullValue() throws Exception {
        try (ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay(null);")) {
            rs.next();
            assertTrue(rs.getObject(1)==null);
        }
    }

    @Test
    public void test_ST_ConstrainedDelaunayWithPoints() throws Exception {
        try (ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay('MULTIPOINTZ ((0 0 1), (10 0 1), (10 10 1), (5 5 1))'::GEOMETRY);")) {
            rs.next();
            assertGeometryEquals("MULTIPOLYGONZ (((5 5 1, 0 0 1, 10 0 1, 5 5 1)), ((5 5 1, 10 0 1, 10 10 1, 5 5 1)))", rs.getBytes(1));
        }
    }

    @Test
    public void test_ST_ConstrainedDelaunayWithPolygon() throws Exception {
        try (ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay('POLYGON ((1.9 8, 2.1 2.2, 7.1 2.2, 4.9 3.5, 7.5 8.1, 3.2 6, 1.9 8))'::GEOMETRY);")) {
            rs.next();
            assertGeometryEquals("MULTIPOLYGON (((4.9 3.5, 2.1 2.2, 7.1 2.2, 4.9 3.5)), ((3.2 6, 2.1 2.2, 4.9 3.5, 3.2 6)), ((1.9 8, 2.1 2.2, 3.2 6, 1.9 8)), ((4.9 3.5, 7.1 2.2, 7.5 8.1, 4.9 3.5)), ((3.2 6, 4.9 3.5, 7.5 8.1, 3.2 6)), ((1.9 8, 3.2 6, 7.5 8.1, 1.9 8)))", rs.getBytes(1));
        }
    }

    @Test
    public void test_ST_ConstrainedDelaunayWithLines() throws Exception {
        try (ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay('MULTILINESTRING ((2 7, 6 7), (3.2 4.6, 5 9),(4.1 5, 6 5))'::GEOMETRY, 1);")) {
            rs.next();
            assertGeometryEquals("MULTILINESTRING ((4.181818181818182 7, 6 7), (3.2 4.6, 4.1 5), (3.2 4.6, 6 5), (5 9, 6 7), (2 7, 3.2 4.6), (4.181818181818182 7, 5 9), (4.1 5, 6 5), (3.2 4.6, 4.181818181818182 7), (2 7, 5 9), (4.1 5, 4.181818181818182 7), (2 7, 4.181818181818182 7), (6 5, 6 7), (4.181818181818182 7, 6 5))", rs.getBytes(1));
        }
    }

    @Test
    public void test_ST_ConstrainedDelaunayWithLines2() throws Exception {
        try (ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay('MULTILINESTRING ((2 7, 6 7), (3.2 4.6, 5 9),(4.1 5, 6 5))'::GEOMETRY, 0);")) {
            rs.next();
            assertGeometryEquals("MULTIPOLYGON (((6 5, 4.1 5, 3.2 4.6, 6 5)), ((4.181818181818182 7, 4.1 5, 6 5, 4.181818181818182 7)), ((3.2 4.6, 4.1 5, 4.181818181818182 7, 3.2 4.6)), ((2 7, 3.2 4.6, 4.181818181818182 7, 2 7)), ((4.181818181818182 7, 6 5, 6 7, 4.181818181818182 7)), ((5 9, 4.181818181818182 7, 6 7, 5 9)), ((2 7, 4.181818181818182 7, 5 9, 2 7)))", rs.getBytes(1));
        }
    }

    @Test
    public void test_ST_ConstrainedDelaunayWithLines3() throws Exception {
        try (ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay('LINESTRINGZ (0 0 0, 10 0 0, 10 10 0, 0 10 0, 0 0 0)'::GEOMETRY, 1);")) {
            rs.next();
            assertGeometryEquals("MULTILINESTRINGZ ((0 10 0, 10 10 0), (0 0 0, 10 0 0), (0 10 0, 10 0 0), (0 0 0, 0 10 0), (10 0 0, 10 10 0))", rs.getBytes(1));
        }
    }

    @Test
    public void test_ST_ConstrainedDelaunayWithLines4() throws Exception {
        try (ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay('LINESTRINGZ (0 0 1, 10 0 1, 10 10 1, 0 10 1, 0 0 1)'::GEOMETRY, 1);")) {
            rs.next();
            assertGeometryEquals("MULTILINESTRINGZ ((0 10 1, 10 10 1), (0 0 1, 10 0 1), (0 10 1, 10 0 1), (0 0 1, 0 10 1), (10 0 1, 10 10 1))", rs.getBytes(1));
        }
    }

    @Test
    public void test_ST_ConstrainedDelaunayWithCollection() throws Exception {
        try (ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay('GEOMETRYCOLLECTION (POLYGON ((150 380, 110 230, 180 190, 230 300, 320 280, 320 380, 150 380)), \n"
                + "  LINESTRING (70 330, 280 220))'::GEOMETRY);")) {
            rs.next();
            assertGeometryEquals("MULTIPOLYGON (((210.2447552447553 256.5384615384615, 180 190, 280 220, 210.2447552447553 256.5384615384615)), ((110 230, 180 190, 210.2447552447553 256.5384615384615, 110 230)), ((128.4958217270195 299.3593314763231, 110 230, 210.2447552447553 256.5384615384615, 128.4958217270195 299.3593314763231)), ((230 300, 210.2447552447553 256.5384615384615, 280 220, 230 300)), ((230 300, 280 220, 320 280, 230 300)), ((128.4958217270195 299.3593314763231, 210.2447552447553 256.5384615384615, 230 300, 128.4958217270195 299.3593314763231)), ((70 330, 110 230, 128.4958217270195 299.3593314763231, 70 330)), ((150 380, 128.4958217270195 299.3593314763231, 230 300, 150 380)), ((70 330, 128.4958217270195 299.3593314763231, 150 380, 70 330)), ((230 300, 320 280, 320 380, 230 300)), ((150 380, 230 300, 320 380, 150 380)))", rs.getBytes(1));
        }
    }

    @Test
    public void test_ST_ConstrainedDelaunayWithCollection1() throws Exception {
        try (ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay('GEOMETRYCOLLECTIONZ(POINTZ (0 0 1), POINTZ (10 0 1), POINTZ (10 10 1), POINTZ (5 5 1))'::GEOMETRY);")) {
            rs.next();
            assertGeometryEquals("MULTIPOLYGONZ (((5 5 1, 0 0 1, 10 0 1, 5 5 1)), ((5 5 1, 10 0 1, 10 10 1, 5 5 1)))", rs.getBytes(1));
        }
    }

    @Test
    public void test_ST_VORONOI() throws Exception {
        st.execute("drop table if exists pts;\n" +
                "create table pts as select ST_MakePoint(A.X + (COS(B.X)), B.X - (SIN(A.X)), ROUND(LOG10(1 + A.X *" +
                " (5 * B.X)),2)) THE_GEOM from SYSTEM_RANGE(0,50) A,SYSTEM_RANGE(30,50) B;\n" +
                "drop table if exists voro;\n" +
                "create table voro as select ST_VORONOI(st_delaunay(st_accum(the_geom)), 1, " +
                "ST_ENVELOPE(ST_ACCUM(the_geom))) the_geom from PTS;");
        try (ResultSet rs = st.executeQuery("select ST_NUMGEOMETRIES(the_geom) cpt,st_length(the_geom) lngth," +
                "st_npoints(the_geom) numpts  from voro;")) {
            assertTrue(rs.next());
            assertEquals(3211, rs.getInt(1));
            assertEquals(2249.43, rs.getDouble(2), 1e-3);
            assertEquals(6425, rs.getInt(3));
        }
    }

    @Test
    public void test_ST_VORONOIPTS() throws Exception {
        st.execute("drop table if exists pts;\n" +
                "create table pts as select ST_MakePoint(A.X + (COS(B.X)), B.X - (SIN(A.X)), ROUND(LOG10(1 + A.X *" +
                " (5 * B.X)),2)) THE_GEOM from SYSTEM_RANGE(0,50) A,SYSTEM_RANGE(30,50) B;\n" +
                "drop table if exists voro;\n" +
                "create table voro as select ST_VORONOI(st_accum(the_geom), 2, " +
                "ST_ENVELOPE(ST_ACCUM(the_geom))) the_geom from PTS;");
        try (ResultSet rs = st.executeQuery("select  ST_NUMGEOMETRIES(the_geom) cpt,st_perimeter(the_geom) lngth," +
                "st_npoints(the_geom) numpts  from voro;")) {
            assertTrue(rs.next());
            assertEquals(1071, rs.getInt(1));
            assertEquals(4350.87, rs.getDouble(2), 1e-2);
            assertEquals(7390, rs.getInt(3));
        }
    }

    @Test
    public void test_EdgeST_VORONOI() throws Exception {
        try (ResultSet rs = st.executeQuery("select ST_VORONOI('MULTIPOINT(1 5, 4 5)'::geometry, 1) the_geom")) {
            assertTrue(rs.next());
            assertGeometryEquals("MULTILINESTRING ((-2 2, -2 8), (-2 2, 2.5 2), (-2 8, 2.5 8), (2.5 2, 2.5 8), (2.5 2, 7 2), (2.5 8, 7 8), (7 2, 7 8))", rs.getObject(1));
        }
    }

    @Test
    /**
     * Construction of voronoi with three coplanar points make three polygons.
     */
    public void test_ST_VORONOIJTSInvalid() throws Exception {
        st.execute("drop table if exists pts;\n" +
                "create table pts as select ST_GeomFromText('MULTIPOINT(0 0, 1 0, 2 0)') the_geom;\n" +
                "drop table if exists voro;");
        try (ResultSet rs = st.executeQuery("select ST_NUMGEOMETRIES(ST_VORONOI(st_accum(the_geom), 2, " +
                "ST_ENVELOPE(ST_ACCUM(the_geom)))) num from PTS;")) {
            assertTrue(rs.next());
            assertEquals(3, rs.getInt("num"));
        }
    }

    @Test
    /**
     * Construction of Voronoi without input triangles is not possible.
     */
    public void test_ST_VORONOIInvalid() throws Exception {
        st.execute("drop table if exists pts;\n" +
                "create table pts as select ST_GeomFromText('MULTIPOINTZ(0 0 0, 1 0 0, 2 0 0)') the_geom;\n" +
                "drop table if exists voro;");
        try (ResultSet rs = st.executeQuery("select ST_VORONOI(ST_DELAUNAY(st_accum(the_geom)), 2, " +
                "ST_ENVELOPE(ST_ACCUM(the_geom))) the_geom from PTS;")) {
            assertTrue(rs.next());
            assertEquals("MULTIPOLYGON EMPTY", rs.getString("the_geom"));
        }
    }

    @Test
    public void testSimpleST_TESSELLATE() throws Exception {
        try (ResultSet rs = st.executeQuery("SELECT ST_TESSELLATE('POLYGON ((-6 -2, -8 2, 0 8, -8 -7, -10 -1, -6 -2))') the_geom")) {
            assertTrue(rs.next());
            assertGeometryEquals("MULTIPOLYGON (((-10 -1, -8 -7, -6 -2, -10 -1)), ((0 8, -6 -2, -8 -7, 0 8))," +
                    " ((-8 2, -6 -2, 0 8, -8 2)))", rs.getObject(1));
        }
    }

    @Test
    public void testMultiST_TESSELLATE() throws Exception {
        try (ResultSet rs = st.executeQuery("SELECT ST_TESSELLATE(ST_UNION(ST_EXPAND('POINT(0 0)',5, 5),ST_EXPAND('POINT(15 0)',5, 5))) the_geom")) {
            assertTrue(rs.next());
            assertGeometryEquals("MULTIPOLYGON (((-5 5, 5 -5, 5 5, -5 5)), ((-5 5, -5 -5, 5 -5, -5 5)), ((10 5, 20 -5, 20 5, 10 5)), ((10 5, 10 -5, 20 -5, 10 5)))", rs.getObject(1));
        }
    }

    @Test
    public void testEmptyST_TESSELLATE() throws Exception {
        try (ResultSet rs = st.executeQuery("SELECT ST_TESSELLATE('MULTIPOLYGON EMPTY') the_geom")) {
            assertTrue(rs.next());
            assertGeometryEquals("MULTIPOLYGON EMPTY", rs.getObject(1));
        }
    }

    @Test
    public void testInvalid2ST_TESSELLATE() {
        assertThrows(SQLException.class, ()-> {
            try (ResultSet rs = st.executeQuery("SELECT ST_TESSELLATE('POINT(1 1)') the_geom")) {
                assertTrue(rs.next());
                assertGeometryEquals("MULTIPOLYGON (((-5 -5, -5 5, 5 5, -5 -5)), ((-5 -5, 5 5, 5 -5, -5 -5)), ((10 -5, 10 5, 20 5, 10 -5)), ((10 -5, 20 5, 20 -5, 10 -5)))", rs.getObject(1));
            }
        });
    }
}
