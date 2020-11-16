package alex.world.visual.wrappers;

import java.util.List;

import alex.buffer.Bufferable;
import alex.buffer.render.RenderingTriangle;

public abstract class VisualWrapper implements Bufferable{
	protected Bufferable visual;
	
	public abstract void updateVisual();
	
	public List<RenderingTriangle> getRenderingTriangles(){
		return this.visual.getRenderingTriangles();
	}

}
