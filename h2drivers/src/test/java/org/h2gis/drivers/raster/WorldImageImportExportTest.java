/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.drivers.raster;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2.util.RasterUtils;
import org.h2.util.StringUtils;
import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Test of Raster drivers
 * @author Erwan Bocher
 * @author Nicolas Fortin
 */
public class WorldImageImportExportTest {

    private Statement st;
    private static Connection connection;
    private static final String DB_NAME = "WorldFileImageImportExportTest";

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_WorldFileImageRead(), "");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Before
    public void setUpStatement() throws Exception {
        st = connection.createStatement();
    }
    
     @After
    public void tearDownStatement() throws Exception {
        st.close();
    }

    @Test
    public void importRasterFile1() throws SQLException, IOException {
        st.execute("drop table if exists remote_sensing");
        st.execute("create table remote_sensing(id serial, the_raster raster) as select null, ST_WorldFileImageRead(" +
                StringUtils.quoteStringSQL(WorldImageImportExportTest.class.getResource("remote_sensing.png").getPath())
                + ")");
        ResultSet rs = st.executeQuery("select the_raster from remote_sensing;");
        assertTrue(rs.next());
        // Read metadata from WKB raster stream
        InputStream is = rs.getBinaryStream(1);
        try {
            RasterUtils.RasterMetaData metaData = RasterUtils.RasterMetaData.fetchMetaData(is, true);
            assertNotNull(metaData);
            assertEquals(461, metaData.width);
            assertEquals(346, metaData.height);
            assertEquals(3, metaData.numBands);
            assertEquals(319190.95, metaData.ipX, 1e-2);
            assertEquals(2250332.35, metaData.ipY, 1e-2);
            assertEquals(2.5, metaData.scaleX, 1e-2);
            assertEquals(-2.5, metaData.scaleY, 1e-2);
            assertEquals(0., metaData.skewX, 1e-6);
            assertEquals(0., metaData.skewY, 1e-6);
        } finally {
            is.close();
        }
        rs.close();

    }
}