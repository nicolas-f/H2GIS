/*
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
package org.h2gis.drivers.dbf;

import org.h2gis.drivers.dbf.internal.DBFDriver;
import org.h2gis.drivers.dbf.internal.DbaseFileHeader;
import org.h2gis.drivers.shp.SHPEngineTest;
import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.h2spatialapi.DriverFunction;
import org.h2gis.h2spatialapi.EmptyProgressVisitor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Nicolas Fortin
 */
public class DBFImportExportTest {
    private static Connection connection;
    private static final String DB_NAME = "DBFImportExportTest";

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new DBFRead(), "");
        CreateSpatialExtension.registerFunction(connection.createStatement(), new DBFWrite(), "");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void exportTableTestGeomEnd() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File dbfFile = new File("target/area_export.dbf");
        stat.execute("DROP TABLE IF EXISTS AREA");
        stat.execute("create table area(idarea int primary key, value DOUBLE, descr CHAR(50))");
        stat.execute("insert into area values(1, 4.9406564584124654, 'main area')");
        stat.execute("insert into area values(2, 2.2250738585072009, 'second area')");
        // Create a shape file using table area
        stat.execute("CALL DBFWrite('target/area_export.dbf', 'AREA')");
        // Read this shape file to check values
        assertTrue(dbfFile.exists());
        DBFDriver dbfDriver = new DBFDriver();
        dbfDriver.initDriverFromFile(dbfFile);
        assertEquals(3, dbfDriver.getFieldCount());
        assertEquals(2, dbfDriver.getRowCount());
        assertEquals(50, dbfDriver.getDbaseFileHeader().getFieldLength(2));
        Object[] row = dbfDriver.getRow(0);
        assertEquals(1, row[0]);
        assertEquals(4.9406564584124654, (Double) row[1], 1e-12);
        assertEquals("main area", row[2]);
        row = dbfDriver.getRow(1);
        assertEquals(2, row[0]);
        assertEquals(2.2250738585072009, (Double) row[1], 1e-12);
        assertEquals("second area", row[2]);
    }

    @Test
    public void importTableTestGeomEnd() throws SQLException, IOException {
        Statement st = connection.createStatement();
        final String path = SHPEngineTest.class.getResource("waternetwork.dbf").getPath();
        DriverFunction driver = new DBFDriverFunction();
        st.execute("DROP TABLE IF EXISTS waternetwork");
        driver.importFile(connection, "WATERNETWORK", new File(path), new EmptyProgressVisitor());
        // Query declared Table columns
        ResultSet rs = st.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'WATERNETWORK'");
        assertTrue(rs.next());
        assertEquals("TYPE_AXE",rs.getString("COLUMN_NAME"));
        assertEquals("VARCHAR", rs.getString("TYPE_NAME"));
        assertEquals(254, rs.getInt("CHARACTER_MAXIMUM_LENGTH"));
        assertTrue(rs.next());
        assertEquals("GID",rs.getString("COLUMN_NAME"));
        assertEquals("BIGINT", rs.getString("TYPE_NAME"));
        assertTrue(rs.next());
        assertEquals("LENGTH",rs.getString("COLUMN_NAME"));
        assertEquals("DOUBLE",rs.getString("TYPE_NAME"));
        rs.close();
        // Check content
        rs = st.executeQuery("SELECT * FROM WATERNETWORK");
        assertTrue(rs.next());
        assertEquals("river",rs.getString("type_axe"));
        assertEquals(9.492402903934545, rs.getDouble("length"), 1e-12);
        assertEquals(1, rs.getInt(2)); // gid
        assertTrue(rs.next());
        assertEquals("ditch", rs.getString("type_axe"));
        assertEquals(261.62989135452983, rs.getDouble("length"), 1e-12);
        assertEquals(2, rs.getInt(2)); // gid
        rs.close();
        // Computation
        rs = st.executeQuery("SELECT SUM(length) sumlen FROM WATERNETWORK");
        assertTrue(rs.next());
        assertEquals(28469.778049948833, rs.getDouble(1), 1e-12);
        rs.close();
        st.execute("drop table WATERNETWORK");
    }

    @Test
    public void testStringLength() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS TOTO;" +
                "CREATE CACHED TABLE PUBLIC.TOTO(" +
                "    THE_GEOM GEOMETRY," +
                "    OSM_ID CHAR(11)," +
                "    NAME CHAR(48)," +
                "    TYPE_OBJ CHAR(16)," +
                "    WIDTH BIGINT" +
                ");     \n" +
                "INSERT INTO PUBLIC.TOTO(THE_GEOM, OSM_ID, NAME, TYPE_OBJ, WIDTH) VALUES\n" +
                "(X'00000000050000000100000000020000000641539b81a033f81541501050c2d2297241539b82876cf8e64150104f3fc4e51" +
                "a41539b84099ed7134150104be401534141539b8473eba6d54150104a183d1fdf41539b85331f646f4150103cc981d34741539b" +
                "8414101d7e41501033f859de94'::Geometry, '5369251', STRINGDECODE('M\\u00e4t\\u00e4joki'), 'stream', 0),\n" +
                "(X'00000000050000000100000000020000000241539b9fa9dd222041500ffd53c62b8041539b9b09530b5541500ff2db311567'" +
                "::Geometry, '5369270', STRINGDECODE('M\\u00e4t\\u00e4joki'), 'stream', 0),\n" +
                "(X'00000000050000000100000000020000000241539b9fef14d24e4150102505e99bc041539ba1d368116c4150102128c5fd7c'" +
                "::Geometry, '5369285', STRINGDECODE('M\\u00e4t\\u00e4joki'), 'stream', 0);    \n");
        File dbfFile = new File("target/testStringLength.dbf");
        st.execute("CALL DBFWrite('"+dbfFile+"', 'TOTO')");
        // Read this shape file to check values
        assertTrue(dbfFile.exists());
        DBFDriver dbfDriver = new DBFDriver();
        dbfDriver.initDriverFromFile(dbfFile);
        DbaseFileHeader header = dbfDriver.getDbaseFileHeader();
        assertEquals(4, header.getNumFields());
        assertEquals(11, header.getFieldLength(0));
        assertEquals(48, header.getFieldLength(1));
        assertEquals(16, header.getFieldLength(2));
    }
}
