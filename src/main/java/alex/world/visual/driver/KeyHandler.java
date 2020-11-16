package alex.world.visual.driver;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import alex.geo.angle.Angle;
import alex.geo.angle.Degrees;
import alex.geo.angle.rotate.RayRotator;
import alex.geo.force.Force;
import alex.geo.render.camera.Camera;
import alex.geo.trig.Vertex;
import alex.world.visual.wrappers.EnvironmentWrapper;


public class KeyHandler {
	
	public static enum MOVE_MODE{SPACESHIP, AIRPLANE, HELICOPTER, JETPACK};
	
	public static double move = 1.0;
	public static Angle turnUD = new Degrees(3);
	public static Angle turnLR = new Degrees(3);

	//keep track of which keys are pressed--might be holding down multiple
	private Map<Integer, Boolean> keyMap = new HashMap<Integer, Boolean>();
		
	private Camera camera;
	private EnvironmentWrapper environment;
	
	private MOVE_MODE moveMode = MOVE_MODE.AIRPLANE;
	
	
	public KeyHandler(Camera camera, EnvironmentWrapper environment){
		this.camera = camera;
		this.environment = environment;
	}
	
	public void setKey(int key, boolean isPressed){
		this.keyMap.put(key, isPressed);
	}
	
	public void setMoveMode(String moveStr){
		if(moveStr.toLowerCase().equals("airplane")){
			this.moveMode = MOVE_MODE.AIRPLANE;
		}
		else if(moveStr.toLowerCase().equals("helicopter")){
			this.moveMode = MOVE_MODE.HELICOPTER;
		}
		else if(moveStr.toLowerCase().equals("jetpack")){
			this.moveMode = MOVE_MODE.JETPACK;
		}
		else if(moveStr.toLowerCase().equals("spaceship")){
			this.moveMode = MOVE_MODE.SPACESHIP;
		}
		else{
			this.moveMode = MOVE_MODE.AIRPLANE;
		}
	}
	
	/**
	 * Turn off all keys
	 * */
	public void clearKeys(){
		this.keyMap.clear();
	}
	
	/**
	 * Return TRUE if a key is in pressed-mode
	 * */
	private boolean keyDown(int keyCode){
		return (keyMap.get(keyCode) != null && keyMap.get(keyCode) != false);
	}
	
	/**
	 * Act (mostly camera) based on which keys are being held down
	 * */
	public void handleKeys(){
		if(keyDown(KeyEvent.VK_1)){
			this.moveMode = MOVE_MODE.AIRPLANE;
		}
		if(keyDown(KeyEvent.VK_2)){
			this.moveMode = MOVE_MODE.HELICOPTER;
		}
		if(keyDown(KeyEvent.VK_3)){
			this.moveMode = MOVE_MODE.JETPACK;
		}
		if(keyDown(KeyEvent.VK_4)){
			this.moveMode = MOVE_MODE.SPACESHIP;
		}
		
		Force force = new Force(new Vertex(0,0,0));
		switch(this.moveMode){
			case AIRPLANE:
				force = this.get_airplane_mode();
				break;
			case HELICOPTER:
				force = this.get_helicopter_mode();
				break;
			case JETPACK:
				force = this.get_jetpack_mode();
				break;
			case SPACESHIP:
				force = this.get_spaceship_mode();
				break;
			default:
				force = this.get_airplane_mode();	
		}
		
		applyForce(force);
	}
	
	private void applyForce(Force cameraForce){
		//don't need to check walls if camera is holding still
		if(cameraForce.asVertex().length() > 0.01){
			cameraForce = environment.getNewForce(camera.getEye(), cameraForce);
			camera.translate(cameraForce.asVertex());
		}
	}
	
	private void applyTurn(){

		if(keyDown(KeyEvent.VK_UP)){
			camera.turnUp(turnUD);
		}
		if(keyDown(KeyEvent.VK_DOWN)){
			camera.turnDown(turnUD);
		}
		if(keyDown(KeyEvent.VK_LEFT)){
			camera.turnLeft(turnLR);
		}
		if(keyDown(KeyEvent.VK_RIGHT)){
			camera.turnRight(turnLR);
		}
	}

	private Force get_helicopter_mode(){
		this.applyTurn();
		
		double lr = 0.0;
		double fb = 0.0;
		double ud = 0.0;
				
		if(keyDown('W')){
			fb -= move;
		}
		if(keyDown('S')){
			fb += move;
		}
		if(keyDown('A')){
			lr += move;
		}
		if(keyDown('D')){
			lr -= move;
		}
		if(keyDown('R')){
			ud += move;
		}
		if(keyDown('F')){
			ud -= move;
		}

		Vertex eye = camera.getEye();
		Vertex aim = camera.getAim();
		
		RayRotator temp = new RayRotator(eye, new Vertex(aim.x, eye.y, aim.z));
		
		Vertex v_lr = eye.minus(eye.movedForth(lr, 
				temp.getLeftRight()[0]));
		Vertex v_ud = eye.translated(new Vertex(0,ud,0)).minus(eye);
		Vertex v_fb = eye.minus(eye.movedForth(fb, new Vertex(aim.x, eye.y, aim.z)));
		
		return new Force(v_lr.plus(v_ud).plus(v_fb));		
	}
	
	private Force get_airplane_mode(){
		this.applyTurn();
		
		double lr = 0.0;
		double fb = 0.0;
		double ud = 0.0;
		
		if(keyDown('W')){
			fb -= move;
		}
		if(keyDown('S')){
			fb += move;
		}
		if(keyDown('A')){
			lr -= move;
		}
		if(keyDown('D')){
			lr += move;
		}
		if(keyDown('R')){
			ud += move;
		}
		if(keyDown('F')){
			ud -= move;
		}

		Vertex eye = camera.getEye();
		Vertex aim = camera.getAim();
		
		RayRotator temp = new RayRotator(eye, new Vertex(aim.x, eye.y, aim.z));
		
		Vertex v_lr = eye.minus(eye.movedForth(lr, 
				temp.getLeftRight()[0]));
		Vertex v_ud = eye.translated(new Vertex(0,ud,0)).minus(eye);
		Vertex v_fb = eye.minus(eye.movedForth(fb, aim));
		
		return new Force(v_lr.plus(v_ud).plus(v_fb));		
	}
	
	private Force get_jetpack_mode(){
		this.applyTurn();
		
		double lr = 0.0;
		double fb = 0.0;
		double ud = -0.05;
		
		if(keyDown('W')){
			fb -= move;
		}
		if(keyDown('S')){
			fb += move;
		}
		if(keyDown('A')){
			lr += move;
		}
		if(keyDown('D')){
			lr -= move;
		}
		if(keyDown(' ')){
			ud += 2.0*move;
		}

		Vertex eye = camera.getEye();
		Vertex aim = camera.getAim();
		
		RayRotator temp = new RayRotator(eye, new Vertex(aim.x, eye.y, aim.z));
		
		Vertex v_lr = eye.minus(eye.movedForth(lr, 
				temp.getLeftRight()[0]));
		Vertex v_ud = eye.translated(new Vertex(0, ud, 0)).minus(eye);
		Vertex v_fb = eye.minus(eye.movedForth(fb, aim));
		
		return new Force(v_lr.plus(v_ud).plus(v_fb));		
	}

	private Force get_spaceship_mode(){
		this.applyTurn();

		if(keyDown(' ')){
			Vertex eye = camera.getEye();
			Vertex aim = camera.getAim();
			return new Force(eye.movedForth(move, aim).minus(eye));
		}
		return new Force(new Vertex(0,0,0));
	}
	
	protected static void println(Object ob){
		System.out.println(ob);
	}
}
