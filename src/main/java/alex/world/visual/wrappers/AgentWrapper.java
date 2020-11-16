package alex.world.visual.wrappers;

import java.util.ArrayList;
import java.util.List;

import alex.buffer.Bufferable;
import alex.buffer.BufferableList;
import alex.buffer.render.Color;
import alex.buffer.render.RenderingTriangle;
import alex.geo.angle.Degrees;
import alex.geo.shape.Cylinder;
import alex.geo.shape.Prism;
import alex.geo.shape.Pyramid;
import alex.geo.shape.Rectangle;
import alex.geo.shape.Sphere;
import alex.geo.trig.Vertex;
import alex.world.env.agent.Agent;
import alex.world.env.agent.sensory.Ear;
import alex.world.env.agent.sensory.Eye;
import alex.world.env.agent.sensory.EyeReceptor;

public class AgentWrapper<T extends Agent<?>> extends VisualWrapper{
	
	public static boolean SHOW_EYE_RAYS = false;
	
	public static final double EYE_RAY_RADIUS = 0.025;
	public static final double EYE_RADIUS = 0.4;
	public static final double EAR_RADIUS = EYE_RADIUS;
	public static final int ORGAN_LEVEL = 8;
	
	
	protected BufferableList list = new BufferableList();
	
	protected T agent;

	
	public AgentWrapper(T agent){
		this.agent = agent;
		this.updateVisual();
		super.visual = list;
	}
	
	private List<Bufferable> makeRays(Eye eye){
		List<Bufferable> rays = new ArrayList<Bufferable>();
		
		Vertex base = agent.getSensorPosition(eye);
		List<Vertex> targets = agent.getEyeReceptorTargets(eye);
		List<EyeReceptor> receptors = eye.getReceptors();
		for(int i=0; i < receptors.size(); i++){
			EyeReceptor rec = receptors.get(i);
			Vertex target = targets.get(i);
			
			Color color = rec.getColorDistribution();
			double intensity = rec.getIntensity();
			color = color.mult(intensity);
			
			Cylinder ray = new Cylinder(base, target, EYE_RAY_RADIUS);
			if(intensity <= 0.000001){
				color = Color.WHITE;
			}
			ray.setColor(color);
			rays.add(ray);
		}		
		
		return rays;
	}
	
	@Override
	public void updateVisual(){
		double length = 2.0*agent.getRadius(), width = agent.getRadius(), height = agent.getRadius();
		
		Vertex back = this.agent.getLocation();
		Vertex tip = back.movedForth(length/1.5, this.agent.getFacing(), new Degrees(0));
		
		
		
		Rectangle base = new Rectangle(
				back.plus(width, height, 0), back.plus(width, -height, 0), 
				back.plus(-width, -height, 0), back.plus(-width, height, 0), Color.PURPLE);
		base.setForwards(back.clone(), tip.clone());
		base.rotateLeft(this.agent.getFacing());
		
		Rectangle rear = (Rectangle) base.clone();
		rear.setForwards(back.clone(), tip.clone());
		rear.moveForth(-length);		
		
		list.clear();
		
		double pct = agent.getHealth() / agent.getMaxHealth();
		
		//tail/health
		Vertex tail = back.movedForth(-length*1.5, tip).plus(0,-height,0);
		list.add(new Cylinder(tail, tail.plus(0,height*2*pct, 0), 0.1));
		
		//head
		list.add(new Pyramid(base, tip, Color.YELLOW));
		
		//body
		Color bodyColor = Color.PURPLE;
		if(agent.isDead()){
			bodyColor = Color.BLACK;
		}
		list.add(new Prism(base, rear, bodyColor));
		
		//eyes
		for(Eye eye : agent.getHead().getAllEyes()){
			//list.add(new Prism(this.agent.getSensorPosition(eye), EYE_RADIUS, Color.RED));
			list.add(new Sphere(this.agent.getSensorPosition(eye), EYE_RADIUS, ORGAN_LEVEL, new Color[]{Color.CYAN, Color.BLUE}));
			if(SHOW_EYE_RAYS == true){
				list.addAll(makeRays(eye));
			}
		}
		
		//ears
		for(Ear ear : agent.getHead().getAllEars()){
			list.add(new Sphere(this.agent.getSensorPosition(ear), EAR_RADIUS, ORGAN_LEVEL, new Color[]{Color.ORANGE, Color.RED}));	
		}
	}
}
