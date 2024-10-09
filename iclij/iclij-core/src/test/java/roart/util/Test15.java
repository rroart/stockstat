package roart.util;

import java.io.IOException;
import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.tweetyproject.commons.ParserException;
import org.tweetyproject.graphs.util.GraphUtil;
import org.tweetyproject.graphs.DefaultGraph;
import org.tweetyproject.graphs.DirectedEdge;
import org.tweetyproject.graphs.Graph;
import org.tweetyproject.graphs.SimpleNode;
import static org.junit.Assert.assertEquals;

public class Test15 {
    
    @Test
    public void test() throws ParserException, IOException{
        Graph<SimpleNode> g = new DefaultGraph<SimpleNode>();
        SimpleNode[] nodes = new SimpleNode[10];
        for(int i = 0; i < 10; i++){
                nodes[i] = new SimpleNode("a"+i);
                g.add(nodes[i]);
        }
        g.add(new DirectedEdge<SimpleNode>(nodes[0], nodes[1]));
        g.add(new DirectedEdge<SimpleNode>(nodes[1], nodes[2]));
        g.add(new DirectedEdge<SimpleNode>(nodes[1], nodes[3]));
        Collection<SimpleNode> g0 = g.getChildren(nodes[0]);
        System.out.println("g0 " + g0);
        assertEquals(1, g0.size());
        for (SimpleNode n : g0) {
            Collection<SimpleNode> g1 = g.getChildren(n);
            assertEquals(2, g1.size());
            System.out.println("g1 " + g1);            
        }
        //g.
        //assertEquals(GraphUtil.enumerateChordlessCircuits(g).size(),4);
    }
}
