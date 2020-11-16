package alex.world.visual.wrappers;

import alex.buffer.BufferableList;
import alex.buffer.render.Color;
import alex.geo.shape.Sphere;
import alex.world.env.Material;
import alex.world.env.Material.Food;
import alex.world.env.Material.Neutral;
import alex.world.env.Material.Poison;

public class MaterialWrapper extends VisualWrapper{
	public static final int SPHERE_LEVEL = 8;
	
	protected Material mat;
	protected BufferableList list = new BufferableList();
	
	public MaterialWrapper(Material mat) {
		this.mat = mat;
		super.visual = this.list;
		this.updateVisual();
	}

	@Override
	public void updateVisual() {
		this.list.clear();
		if(mat.hasTouchableState()){
			Color base = Color.BLACK;		
			if(mat instanceof Food){
				base = Color.GREEN;
			}
			else if(mat instanceof Neutral){
				base = Color.BLUE;
			}
			else if(mat instanceof Poison){
				base = Color.RED;
			}
			Color[] spectrum = Color.makeSpectrum(base, new double[]{1.0, 0.9, 0.8, 0.7, 0.6, 0.5});
			this.list.add(new Sphere(mat.getLocation(), mat.getRadius(), SPHERE_LEVEL, spectrum));
		}
	}
}
