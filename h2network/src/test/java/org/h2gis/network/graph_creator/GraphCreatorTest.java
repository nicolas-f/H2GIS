/**
 * GDMS-Topology is a library dedicated to graph analysis. It is based on the
 * JGraphT library available at <http://www.jgrapht.org/>. It enables computing
 * and processing large graphs using spatial and alphanumeric indexes.
 *
 * This version is developed at French IRSTV Institute as part of the EvalPDU
 * project, funded by the French Agence Nationale de la Recherche (ANR) under
 * contract ANR-08-VILL-0005-01 and GEBD project funded by the French Ministry
 * of Ecology and Sustainable Development.
 *
 * GDMS-Topology is distributed under GPL 3 license. It is produced by the
 * "Atelier SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR
 * 2488.
 *
 * Copyright (C) 2009-2013 IRSTV (FR CNRS 2488)
 *
 * GDMS-Topology is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * GDMS-Topology is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * GDMS-Topology. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://wwwc.orbisgis.org/> or contact
 * directly: info_at_orbisgis.org
 */
package org.h2gis.network.graph_creator;

import junit.framework.Assert;
import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.network.SpatialFunctionTest;
import org.javanetworkanalyzer.data.VDijkstra;
import org.javanetworkanalyzer.model.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Tests the graph creators under all possible configurations.
 *
 * @author Adam Gouge
 */
public class GraphCreatorTest {

    private static Connection connection;
    private static final String DB_NAME = "GraphCreatorTest";

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GraphCreatorTest.class);
    private static final int[] EDGE_ORIENTATIONS =
            new int[]{GraphCreator.DIRECTED_EDGE,
                      GraphCreator.REVERSED_EDGE,
                      GraphCreator.UNDIRECTED_EDGE};
    private static final double[] EDGE_WEIGHTS = new double[]{2.3, 4.2, 5.7};
    private static final double TOLERANCE = 0.0;

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME, true);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_Graph(), "");
        SpatialFunctionTest.registerCormenGraph(connection);
    }

    private void checkEdge(KeyedGraph<VDijkstra, Edge> graph, int id, int source, int dest) {
        checkEdge(graph, id, source, dest, 1.0);
    }

    private void checkEdge(KeyedGraph<VDijkstra, Edge> graph, int id, int source, int dest, double weight) {
        final Edge edge = graph.getEdge(graph.getVertex(source), graph.getVertex(dest));
        assertEquals(id, edge.getID());
        assertTrue(graph.containsEdge(edge));
        assertEquals(weight, graph.getEdgeWeight(edge), TOLERANCE);
    }

    private void checkVertices(KeyedGraph<VDijkstra, Edge> graph, int... vertices) {
        for (int i : vertices) {
            assertTrue(graph.containsVertex(graph.getVertex(i)));
        }
    }

    @Test
    public void testD() throws SQLException {
        GraphCreator<VDijkstra, Edge> graphCreator =
                new GraphCreator<VDijkstra, Edge>(connection,
                        "cormen_edges",
                        null,
                        GraphFunctionParser.DIRECTED,
                        null,
                        VDijkstra.class, Edge.class);
        final KeyedGraph<VDijkstra,Edge> graph = graphCreator.prepareGraph();
        assertTrue(graph instanceof DirectedPseudoG);
        assertEquals(5, graph.vertexSet().size());
        Assert.assertEquals(10, graph.edgeSet().size());
        checkVertices(graph, 1, 2, 3, 4, 5);
        checkEdge(graph, 1, 1, 2);
        checkEdge(graph, 2, 2, 3);
        checkEdge(graph, 3, 2, 4);
        checkEdge(graph, 4, 4, 2);
        checkEdge(graph, 5, 1, 4);
        checkEdge(graph, 6, 4, 3);
        checkEdge(graph, 7, 4, 5);
        checkEdge(graph, 8, 3, 5);
        checkEdge(graph, 9, 5, 3);
        checkEdge(graph, 10, 5, 1);
    }

    @Test
    public void testDO() throws SQLException {
        GraphCreator<VDijkstra, Edge> graphCreator =
                new GraphCreator<VDijkstra, Edge>(connection,
                        "cormen_edges",
                        null,
                        GraphFunctionParser.DIRECTED,
                        "edge_orientation",
                        VDijkstra.class, Edge.class);
        final KeyedGraph<VDijkstra,Edge> graph = graphCreator.prepareGraph();
        assertTrue(graph instanceof DirectedPseudoG);
        assertEquals(5, graph.vertexSet().size());
        Assert.assertEquals(11, graph.edgeSet().size());
        checkVertices(graph, 1, 2, 3, 4, 5);
        checkEdge(graph, 1, 1, 2);
        checkEdge(graph, 2, 3, 2);
        checkEdge(graph, 3, 2, 4);
        checkEdge(graph, 4, 4, 2);
        checkEdge(graph, 5, 1, 4);
        checkEdge(graph, 6, 4, 3);
        checkEdge(graph, 7, 4, 5);
        checkEdge(graph, 8, 3, 5);
        checkEdge(graph, 9, 5, 3);
        checkEdge(graph, 10, 5, 1);
        checkEdge(graph, -10, 1, 5);
    }

    @Test
    public void testR() throws SQLException {
        GraphCreator<VDijkstra, Edge> graphCreator =
                new GraphCreator<VDijkstra, Edge>(connection,
                        "cormen_edges",
                        null,
                        GraphFunctionParser.REVERSED,
                        null,
                        VDijkstra.class, Edge.class);
        final KeyedGraph<VDijkstra,Edge> graph = graphCreator.prepareGraph();
        assertTrue(graph instanceof DirectedPseudoG);
        assertEquals(5, graph.vertexSet().size());
        Assert.assertEquals(10, graph.edgeSet().size());
        checkVertices(graph, 1, 2, 3, 4, 5);
        checkEdge(graph, 1, 2, 1);
        checkEdge(graph, 2, 3, 2);
        checkEdge(graph, 3, 4, 2);
        checkEdge(graph, 4, 2, 4);
        checkEdge(graph, 5, 4, 1);
        checkEdge(graph, 6, 3, 4);
        checkEdge(graph, 7, 5, 4);
        checkEdge(graph, 8, 5, 3);
        checkEdge(graph, 9, 3, 5);
        checkEdge(graph, 10, 1, 5);
    }

    @Test
    public void testRO() throws SQLException {
        GraphCreator<VDijkstra, Edge> graphCreator =
                new GraphCreator<VDijkstra, Edge>(connection,
                        "cormen_edges",
                        null,
                        GraphFunctionParser.REVERSED,
                        "edge_orientation",
                        VDijkstra.class, Edge.class);
        final KeyedGraph<VDijkstra,Edge> graph = graphCreator.prepareGraph();
        assertTrue(graph instanceof DirectedPseudoG);
        assertEquals(5, graph.vertexSet().size());
        Assert.assertEquals(11, graph.edgeSet().size());
        checkVertices(graph, 1, 2, 3, 4, 5);
        checkEdge(graph, 1, 2, 1);
        checkEdge(graph, 2, 2, 3);
        checkEdge(graph, 3, 4, 2);
        checkEdge(graph, 4, 2, 4);
        checkEdge(graph, 5, 4, 1);
        checkEdge(graph, 6, 3, 4);
        checkEdge(graph, 7, 5, 4);
        checkEdge(graph, 8, 5, 3);
        checkEdge(graph, 9, 3, 5);
        checkEdge(graph, 10, 1, 5);
        checkEdge(graph, -10, 5, 1);
    }

    @Test
    public void testU() throws SQLException {
        GraphCreator<VDijkstra, Edge> graphCreator =
                new GraphCreator<VDijkstra, Edge>(connection,
                        "cormen_edges",
                        null,
                        GraphFunctionParser.UNDIRECTED,
                        null,
                        VDijkstra.class, Edge.class);
        final KeyedGraph<VDijkstra,Edge> graph = graphCreator.prepareGraph();
        assertTrue(graph instanceof PseudoG);
        assertEquals(5, graph.vertexSet().size());
        Assert.assertEquals(10, graph.edgeSet().size());
        checkVertices(graph, 1, 2, 3, 4, 5);
        checkEdge(graph, 1, 1, 2);
        checkEdge(graph, 2, 2, 3);
        final Set<Edge> edges24 = graph.getAllEdges(graph.getVertex(2), graph.getVertex(4));
        Assert.assertEquals(2, edges24.size());
        for (Edge e : edges24) {
            if (e.getID() != 3) {
                assertEquals(4, e.getID());
            }
        }
        checkEdge(graph, 5, 1, 4);
        checkEdge(graph, 6, 4, 3);
        checkEdge(graph, 7, 4, 5);
        final Set<Edge> edges35 = graph.getAllEdges(graph.getVertex(3), graph.getVertex(5));
        Assert.assertEquals(2, edges24.size());
        for (Edge e : edges35) {
            if (e.getID() != 8) {
                assertEquals(9, e.getID());
            }
        }
        checkEdge(graph, 10, 5, 1);
    }

    @Test
    public void testWD() throws SQLException {
        GraphCreator<VDijkstra, Edge> graphCreator =
                new GraphCreator<VDijkstra, Edge>(connection,
                        "cormen_edges",
                        "weight",
                        GraphFunctionParser.DIRECTED,
                        null,
                        VDijkstra.class, Edge.class);
        final KeyedGraph<VDijkstra,Edge> graph = graphCreator.prepareGraph();
        assertTrue(graph instanceof DirectedWeightedPseudoG);
        assertEquals(5, graph.vertexSet().size());
        Assert.assertEquals(10, graph.edgeSet().size());
        checkVertices(graph, 1, 2, 3, 4, 5);
        checkEdge(graph, 1, 1, 2, 10.0);
        checkEdge(graph, 2, 2, 3, 1.0);
        checkEdge(graph, 3, 2, 4, 2.0);
        checkEdge(graph, 4, 4, 2, 3.0);
        checkEdge(graph, 5, 1, 4, 5.0);
        checkEdge(graph, 6, 4, 3, 9.0);
        checkEdge(graph, 7, 4, 5, 2.0);
        checkEdge(graph, 8, 3, 5, 4.0);
        checkEdge(graph, 9, 5, 3, 6.0);
        checkEdge(graph, 10, 5, 1, 7.0);
    }

    @Test
    public void testWDO() throws SQLException {
        GraphCreator<VDijkstra, Edge> graphCreator =
                new GraphCreator<VDijkstra, Edge>(connection,
                        "cormen_edges",
                        "weight",
                        GraphFunctionParser.DIRECTED,
                        "edge_orientation",
                        VDijkstra.class, Edge.class);
        final KeyedGraph<VDijkstra,Edge> graph = graphCreator.prepareGraph();
        assertTrue(graph instanceof DirectedWeightedPseudoG);
        assertEquals(5, graph.vertexSet().size());
        Assert.assertEquals(11, graph.edgeSet().size());
        checkVertices(graph, 1, 2, 3, 4, 5);
        checkEdge(graph, 1, 1, 2, 10.0);
        checkEdge(graph, 2, 3, 2, 1.0);
        checkEdge(graph, 3, 2, 4, 2.0);
        checkEdge(graph, 4, 4, 2, 3.0);
        checkEdge(graph, 5, 1, 4, 5.0);
        checkEdge(graph, 6, 4, 3, 9.0);
        checkEdge(graph, 7, 4, 5, 2.0);
        checkEdge(graph, 8, 3, 5, 4.0);
        checkEdge(graph, 9, 5, 3, 6.0);
        checkEdge(graph, 10, 5, 1, 7.0);
        checkEdge(graph, -10, 1, 5, 7.0);
    }

    @Test
    public void testWR() throws SQLException {
        GraphCreator<VDijkstra, Edge> graphCreator =
                new GraphCreator<VDijkstra, Edge>(connection,
                        "cormen_edges",
                        "weight",
                        GraphFunctionParser.REVERSED,
                        null,
                        VDijkstra.class, Edge.class);
        final KeyedGraph<VDijkstra,Edge> graph = graphCreator.prepareGraph();
        assertTrue(graph instanceof DirectedWeightedPseudoG);
        assertEquals(5, graph.vertexSet().size());
        Assert.assertEquals(10, graph.edgeSet().size());
        checkVertices(graph, 1, 2, 3, 4, 5);
        checkEdge(graph, 1, 2, 1, 10.0);
        checkEdge(graph, 2, 3, 2, 1.0);
        checkEdge(graph, 3, 4, 2, 2.0);
        checkEdge(graph, 4, 2, 4, 3.0);
        checkEdge(graph, 5, 4, 1, 5.0);
        checkEdge(graph, 6, 3, 4, 9.0);
        checkEdge(graph, 7, 5, 4, 2.0);
        checkEdge(graph, 8, 5, 3, 4.0);
        checkEdge(graph, 9, 3, 5, 6.0);
        checkEdge(graph, 10, 1, 5, 7.0);
    }

    @Test
    public void testWRO() throws SQLException {
        GraphCreator<VDijkstra, Edge> graphCreator =
                new GraphCreator<VDijkstra, Edge>(connection,
                        "cormen_edges",
                        "weight",
                        GraphFunctionParser.REVERSED,
                        "edge_orientation",
                        VDijkstra.class, Edge.class);
        final KeyedGraph<VDijkstra,Edge> graph = graphCreator.prepareGraph();
        assertTrue(graph instanceof DirectedWeightedPseudoG);
        assertEquals(5, graph.vertexSet().size());
        Assert.assertEquals(11, graph.edgeSet().size());
        checkVertices(graph, 1, 2, 3, 4, 5);
        checkEdge(graph, 1, 2, 1, 10.0);
        checkEdge(graph, 2, 2, 3, 1.0);
        checkEdge(graph, 3, 4, 2, 2.0);
        checkEdge(graph, 4, 2, 4, 3.0);
        checkEdge(graph, 5, 4, 1, 5.0);
        checkEdge(graph, 6, 3, 4, 9.0);
        checkEdge(graph, 7, 5, 4, 2.0);
        checkEdge(graph, 8, 5, 3, 4.0);
        checkEdge(graph, 9, 3, 5, 6.0);
        checkEdge(graph, 10, 1, 5, 7.0);
        checkEdge(graph, -10, 5, 1, 7.0);
    }

    @Test
    public void testWU() throws SQLException {
        GraphCreator<VDijkstra, Edge> graphCreator =
                new GraphCreator<VDijkstra, Edge>(connection,
                        "cormen_edges",
                        "weight",
                        GraphFunctionParser.UNDIRECTED,
                        null,
                        VDijkstra.class, Edge.class);
        final KeyedGraph<VDijkstra,Edge> graph = graphCreator.prepareGraph();
        assertTrue(graph instanceof WeightedPseudoG);
        assertEquals(5, graph.vertexSet().size());
        Assert.assertEquals(10, graph.edgeSet().size());
        checkVertices(graph, 1, 2, 3, 4, 5);
        checkEdge(graph, 1, 1, 2, 10.0);
        checkEdge(graph, 2, 2, 3, 1.0);
        final Set<Edge> edges24 = graph.getAllEdges(graph.getVertex(2), graph.getVertex(4));
        Assert.assertEquals(2, edges24.size());
        for (Edge e : edges24) {
            if (e.getID() == 3) {
                assertEquals(2.0, graph.getEdgeWeight(e), TOLERANCE);
            } else {
                assertEquals(4, e.getID());
                assertEquals(3.0, graph.getEdgeWeight(e), TOLERANCE);
            }
        }
        checkEdge(graph, 5, 1, 4, 5.0);
        checkEdge(graph, 6, 4, 3, 9.0);
        checkEdge(graph, 7, 4, 5, 2.0);
        final Set<Edge> edges35 = graph.getAllEdges(graph.getVertex(3), graph.getVertex(5));
        Assert.assertEquals(2, edges24.size());
        for (Edge e : edges35) {
            if (e.getID() == 8) {
                assertEquals(4.0, graph.getEdgeWeight(e), TOLERANCE);
            } else {
                assertEquals(9, e.getID());
                assertEquals(6.0, graph.getEdgeWeight(e), TOLERANCE);
            }
        }
        checkEdge(graph, 10, 5, 1, 7.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullOrientation() throws SQLException {
        testOrientation("NULL");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidOrientation() throws SQLException {
        testOrientation("2");
    }

    private void testOrientation(String newOrientation) throws SQLException {
        final Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS copy; CREATE TABLE copy AS SELECT * FROM cormen_edges");
        st.execute("UPDATE copy SET edge_orientation=" + newOrientation + " WHERE edge_id=1");
        GraphCreator<VDijkstra, Edge> graphCreator =
                new GraphCreator<VDijkstra, Edge>(connection,
                        "copy",
                        "weight",
                        GraphFunctionParser.DIRECTED,
                        "edge_orientation",
                        VDijkstra.class, Edge.class);
        try {
            graphCreator.prepareGraph();
        } finally {
            st.execute("DROP TABLE copy");
            st.close();
        }
    }
}