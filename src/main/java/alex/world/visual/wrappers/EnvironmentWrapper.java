package alex.world.visual.wrappers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import alex.buffer.BufferMaster;
import alex.buffer.Bufferable;
import alex.buffer.BufferableList;
import alex.buffer.render.Color;
import alex.geo.force.Force;
import alex.geo.shape.Cylinder;
import alex.geo.shape.Prism;
import alex.geo.shape.Rectangle;
import alex.geo.shape.Shape;
import alex.geo.trig.Vertex;
import alex.world.driver.Environment;
import alex.world.driver.Wall;
import alex.world.env.Material;
import alex.world.env.agent.Agent;

public class EnvironmentWrapper {
	private Environment env;
	
	//private List<Wall> cameraWalls = new ArrayList<Wall>();
		
	private List<MaterialWrapper> worldMatImages = new ArrayList<MaterialWrapper>();
	private List<AgentWrapper<?>> agentImages = new ArrayList<AgentWrapper<?>>();
	
	//use to record which objects have already been added
	private Set<Material> addedMaterials = new HashSet<Material>();
	private Set<Agent> addedAgents = new HashSet<Agent>();
	
	//private Shape ground;
	//private Prism bounds;
		
	private boolean readyToRender = false;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public EnvironmentWrapper(BufferMaster master, Environment env){
		this.env = env;
		
		double WORLD_BOUNDS = Environment.WORLD_BOUNDS;
		Rectangle ground = new Rectangle(
				new Vertex(-WORLD_BOUNDS, -1, -WORLD_BOUNDS), 
				new Vertex(-WORLD_BOUNDS, -1, WORLD_BOUNDS),
				new Vertex(WORLD_BOUNDS, -1, WORLD_BOUNDS),
				new Vertex(WORLD_BOUNDS, -1, -WORLD_BOUNDS), Color.BROWN);
		//Rectangle sky = (Rectangle) ground.clone();
		//sky.translate(0, 100, 0);
		//sky.setColor(Color.BLUE);
		
		Color[] colors = new Color[]{Color.WHITE.mult(0.8), Color.WHITE.mult(0.7), Color.CYAN.mult(0.5),
				Color.WHITE.mult(0.6), Color.WHITE.mult(0.5), Color.WHITE.mult(0.4)};		
		
		Prism bounds = new Prism(new Vertex(0, 200/2 - 2.0, 0), WORLD_BOUNDS*2, WORLD_BOUNDS*2, 200, colors);
		master.add(bounds);
		master.add(ground);
		
		//master.add(ground);
		//master.add(sky);
		
		//this.ground = ground.clone();
		//this.ground = ground;
		//this.cameraWalls.add(new Wall(ground, true));
		

		//this.bounds = new Prism(ground, sky, colors);
		//this.bounds.translate(0, -1.0, 0);
		
		
		
		for(Material mat : env.getResources()){
			this.addedMaterials.add(mat);
			this.worldMatImages.add(new MaterialWrapper(mat));
		}
		for(Agent<?> agent : env.getAgents()){
			this.addedAgents.add(agent);
			this.agentImages.add(new AgentWrapper<Agent<?>>(agent));
		}
				
		for(AgentWrapper<?> ag : this.agentImages){
			master.add(ag);
		}
		for(MaterialWrapper mat : this.worldMatImages){
			mat.updateVisual();
			master.add(mat);
		}
		
		readyToRender = true;
	}
	
	public void visualTimestep(BufferMaster mast){
		Date start = new Date();
		if(readyToRender == true){
			//check for newly added Materials
			for(Material mat : this.env.getResources()){
				if(this.addedMaterials.contains(mat) == false){
					this.addedMaterials.add(mat);
					MaterialWrapper mw = new MaterialWrapper(mat);
					this.worldMatImages.add(mw);
					mast.add(mw);
				}
			}
			//check for newly added Agents
			for(Agent<?> agent : this.env.getAgents()){
				if(this.addedAgents.contains(agent) == false){
					this.addedAgents.add(agent);
					AgentWrapper<Agent<?>> aw = new AgentWrapper<Agent<?>>(agent);
					this.agentImages.add(aw);
					mast.add(aw);
				}
			}

			//update any changed Materials
			for(MaterialWrapper mat : this.worldMatImages){
				if(env.isActiveMaterial(mat.mat) == false && mast.isVisible(mat) == true){
					mast.setVisible(mat, false);
					mast.update(mat);
				}
			}
			
			//update any changed Agents (assumed to be all living agents)
			for(AgentWrapper<?> agent : this.agentImages){
				if(agent.agent.isDead() == false){
					//Date sub = new Date();
					agent.updateVisual();
					//System.out.println("a1 "+(new Date().getTime() - sub.getTime())+" MS");
					mast.update(agent);	
					//System.out.println("agent "+(new Date().getTime() - sub.getTime())+" MS");
				}
				else{
					mast.setVisible(agent, false);
					mast.update(agent);	
				}
			}
		}
		//System.out.println("Vis "+(new Date().getTime() - start.getTime())+" MS");
	}
	
	
	public Force getNewForce(Vertex point, Force oldForce) {
		Force newForce = oldForce;
		/*
		for(Wall wall : this.cameraWalls){
			newForce = wall.getNewForce(point, newForce);
		}
		*/
		return newForce;	
	}
}
