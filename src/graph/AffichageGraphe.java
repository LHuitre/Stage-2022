package graph;

import java.awt.Dimension;
import java.util.Map;

import javax.swing.JApplet;

import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;


@SuppressWarnings("deprecation")
public class AffichageGraphe extends JApplet{
	private static final long serialVersionUID = 1L;
	private static Graph<Node, Edge> listenableGraph;
	private static final Dimension DEFAULT_SIZE = new Dimension(1500, 500);
	
	public AffichageGraphe(Graph<Node, Edge> listenableGraph) throws Exception {
		AffichageGraphe.listenableGraph = listenableGraph;
	}
	
	
	@Override
    public void init() {
        JGraphXAdapter<Node, Edge> jgxAdapter = new JGraphXAdapter<Node, Edge>(listenableGraph);

        Map<String, Object> stylesheet = jgxAdapter.getStylesheet().getDefaultEdgeStyle();
        stylesheet.put(mxConstants.STYLE_TEXT_OPACITY, 0);
        
        setPreferredSize(DEFAULT_SIZE);
        mxGraphComponent component = new mxGraphComponent(jgxAdapter);
        component.setConnectable(false);
        component.getGraph().setAllowDanglingEdges(false);
        add(component);
        //resize(DEFAULT_SIZE);
        
        // positioning via jgraphx layouts
        mxCompactTreeLayout layout = new mxCompactTreeLayout(jgxAdapter);
        layout.setHorizontal(true);
        layout.execute(jgxAdapter.getDefaultParent());
    }
}
