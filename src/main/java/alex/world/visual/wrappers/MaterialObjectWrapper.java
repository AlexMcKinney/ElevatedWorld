package alex.world.visual.wrappers;

import alex.buffer.BufferableList;
import alex.world.env.Material;
import alex.world.env.obj.MaterialObject;

public class MaterialObjectWrapper extends VisualWrapper{
	private MaterialObject obj;
	
	
	public MaterialObjectWrapper(MaterialObject obj){
		this.obj = obj;
		this.updateVisual();
	}
	
	@Override
	public void updateVisual() {
		BufferableList visual = new BufferableList();
		for(Material mat : this.obj){
			if(mat.hasTouchableState()){
				visual.add(new MaterialWrapper(mat));
			}
		}
		this.visual = visual;
	}

}
