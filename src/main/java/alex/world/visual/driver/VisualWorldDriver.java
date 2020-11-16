package alex.world.visual.driver;

import com.jogamp.opengl.util.FPSAnimator;

import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Date;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;

import alex.buffer.BufferMaster;
import alex.geo.angle.Degrees;
import alex.geo.render.camera.Camera;
import alex.geo.trig.Vertex;
import alex.world.driver.Environment;
import alex.world.env.agent.Agent;
import alex.world.visual.wrappers.EnvironmentWrapper;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class VisualWorldDriver
		implements GLEventListener, KeyListener, MouseListener, MouseMotionListener {

	private static final int AGENT_COUNT = 3;
	
	public static final int FRAME_WIDTH = 500, FRAME_HEIGHT = 500;
	
	private static final int DEFAULT_TIMESTEP_LENGTH = 1;
	
	//simulation paused for this long between visual rendering
	private static int timeStepLength = DEFAULT_TIMESTEP_LENGTH;
	
	private static double CAMERA_FAR = 400.0;
	
	private boolean simulation_time_active = false;
	private boolean visual_time_active = false;
	private boolean end = false;
	
	private boolean init_active = true;
	private boolean init_time_active = true;
	
	private boolean camera_follow_eye = false;
	private boolean camera_follow_aim = false;
	
	private GLU glu = new GLU();
    private FPSAnimator animator;
	
	//buffer for loading renderable images
	private BufferMaster bufferMaster = new BufferMaster(10000);
		
	//user's point-of-view
	private Camera camera;
	//camera key-movement handler
	private KeyHandler keyHandler;
	
	
	private Environment env;
	private EnvironmentWrapper visualEnv;
	
	private static VisualWorldDriver worldDriver = null;
	
		
	public static void main(String[] args){
		worldDriver = new VisualWorldDriver();
	}
	
	public VisualWorldDriver getWorldDriver(){
		return worldDriver;
	}
	
	private boolean isFinished(){
		return (env.countAgentsDead() >= 100);
	}
	
	public VisualWorldDriver(){
		println("loading Module...");

        
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);
        GLCanvas canvas = new GLCanvas(caps);
        
        canvas.addGLEventListener(this);
        canvas.addKeyListener(this);
        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        
        Toolkit t = Toolkit.getDefaultToolkit();
        Image i = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Cursor noCursor = t.createCustomCursor(i, new Point(0, 0), "none");
        
        Frame frame = new Frame("AWT Window Test");
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.add(canvas);
        frame.setVisible(true);
        frame.setCursor(noCursor);
        
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        
        frame.addMouseMotionListener(this);
        frame.addKeyListener(this);
        frame.addMouseListener(this);
        

        //FPSAnimator(GLAutoDrawable drawable, int fps, boolean scheduleAtFixedRate)
        //animator = new FPSAnimator(canvas, 60, false);
        animator = new FPSAnimator(canvas, 50);
        //animator.add(canvas);
        animator.start();
	}
		
	@Override
    public void init(GLAutoDrawable drawable) {
    	System.out.println("init()");
    	Date d1 = new Date();
    	env = new Environment(AGENT_COUNT);
    	
    	
    	println("Environment generated in "+((new Date().getTime() - d1.getTime())/1000.0)+" Seconds");
		
    	camera = new Camera(new Vertex(10,10,10), new Vertex(10,10,9));
    	camera.setFar(CAMERA_FAR);
    	
    	visualEnv = new EnvironmentWrapper(bufferMaster, env);
    	visual_time_active = init_time_active;
    	
		keyHandler = new KeyHandler(camera, visualEnv);
		
    	//setup drawing parameters
        GL2 gl = drawable.getGL().getGL2();
        
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        gl.glDepthFunc(GL2.GL_LEQUAL);
        gl.glShadeModel(GL2.GL_SMOOTH);
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
        
        gl.glClearColor(1.0f, 1.0f, 1.0f, 0.5f);
        
        println("init() done");
        
        initTimestep();
    }
    

	/******************************************************************************
	* Standard display() function
	******************************************************************************/
    @Override
    public void display(GLAutoDrawable drawable) {
    	if(visual_time_active == false){
    		return;
    	}
    	
    	GL2 gl = drawable.getGL().getGL2();
    
    	//set up view modes
    	
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();        
        camera.runPerspective(glu);
        
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        camera.runLookAt(glu);
        
        //clear screen
    	gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
    	gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

    	try{
	    	if(visual_time_active == true){
	    		visualEnv.visualTimestep(bufferMaster);
	    	}
    	}
    	catch(Exception ex){
    		System.out.println(ex);
    	}
    	
    	if(keyHandler != null){
    		keyHandler.handleKeys();
    	}
    	bufferMaster.render(gl);
    	
    	if(this.step == 0){
    		this.simulation_time_active = init_active;
    	}
    }
    
	
	public static void pause(){
		worldDriver.setActive(false);
		worldDriver.setTimeActive(false);
	}
	
	public void runTest(){
		env.runTest();
	}
	
	public void setActive(boolean active){
		this.simulation_time_active = active;
	}
	
	public void setTimeActive(boolean active){
		this.visual_time_active = active;
	}
	
	public void adjustCamera(){
		if(env.getAgents().isEmpty() == false){
			Agent<?> agent = env.getAgents().get(0);
			if(camera_follow_eye){
				Vertex tail = agent.getLocation().movedForth(-10.0, agent.getFacing(), new Degrees(0));
				camera.setEye(tail.plus(0,camera.getEye().y,0));
			}
			if(camera_follow_aim){
				camera.setAim(agent.getLocation().plus(0.1, 1.0, 0));
			}
		}
	}
	
	private int step = 0;
	
	private void timestep(){
		if(isFinished() == false){
			env.timestep();
		}

		step++;
		if(visual_time_active){
			adjustCamera();
		}
	}
	
	private void initTimestep(){
		new Thread(){
			@Override
			public void run() {
				while(end == false){
					try {
						if(visual_time_active){
							Thread.sleep(timeStepLength);
						}
						if(simulation_time_active == true){
							timestep();
						}
					}
					catch (InterruptedException e) {
					}			
				}
			}
		}.start();
	}
	
	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3,
			int arg4) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	private static void println(Object ob){
		System.out.println(ob);
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void keyPressed(KeyEvent arg0) {
		if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE){
			println("Exiting...");
			this.end = true;
			this.simulation_time_active = false;
			this.visual_time_active = false;
			this.animator.stop();
			System.exit(0);
		}
		//pause environment
		if(arg0.getKeyChar() == 'p'){
			println("Environment Paused, press P to reactivate");
			this.simulation_time_active = !this.simulation_time_active;
			//this.visual_time_active = !this.visual_time_active;
		}
		if(arg0.getKeyChar() == 'v'){
			println("Visualizer Paused, press V to reactivate");
			this.visual_time_active = !this.visual_time_active;
			if(this.visual_time_active == true){
				timeStepLength = DEFAULT_TIMESTEP_LENGTH;
			}
		}
		if(arg0.getKeyChar() == 'x'){
			println(env);
		}
		if(arg0.getKeyChar() == 'y'){
			camera_follow_aim = !camera_follow_aim;
		}
		if(arg0.getKeyChar() == 'h'){
			camera_follow_eye = !camera_follow_eye;
		}
		if(arg0.getKeyChar() == '7'){
			timeStepLength = 1000;
		}
		if(arg0.getKeyChar() == '8'){
			timeStepLength = 100;
		}
		if(arg0.getKeyChar() == '9'){
			timeStepLength = 10;
		}
		if(arg0.getKeyChar() == '0'){
			timeStepLength = 1;
		}
		if(arg0.getKeyChar() == 't'){
			runTest();
		}
		keyHandler.setKey(arg0.getKeyCode(), true);
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		keyHandler.setKey(arg0.getKeyCode(), false);
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
