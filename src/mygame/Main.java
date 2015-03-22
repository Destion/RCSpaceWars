package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FogFilter;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.jme3.ui.Picture;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import input.ShipKeyBoardListener;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import objects.Meteor;
import objects.MeteorFactory;
import ship.Ship;

/**
 * test
 * @author normenhansen
 */
public class Main extends SimpleApplication {
    public MeteorFactory meteorFactory;
    Geometry geom;
    float count;
    Ship mainship;
    CameraNode camNode;
    ShipKeyBoardListener skbListener;  
    private Socket sock;
    private Thread thread;
    private NetworkManager net;
    
    private ArrayList<StepListener> steplisteners = new ArrayList<StepListener>();
    
    private BitmapText hudText;
    private Picture deathScreen;
    private Picture leaveScreen;
    
    private static ArrayList<Spatial> geomMeteorList;
    private static Node metNode;
    
    
    public static Main app;
    public static final String HOST = "130.89.89.27";
    public static final int PORT = 6969;
    public static final String[] USERNAMES = { "Button", "EBOLA.EXE", "OneManCheeseBurgerApocalypse", "BlackMesa", "Microsoft_GLa-DoS", "ZeroCool", "CrashOverride", "AcidBurn", "CerealKiller", "ThaPhreak" };
    
    public static void main(String[] args) {
        app = new Main();  
        
        geomMeteorList = new ArrayList<Spatial>();
        metNode = new Node();
        
        app.setShowSettings(true);
        AppSettings settings = new AppSettings(true);
        

        settings.setSettingsDialogImage("Textures/dialog.jpg");
        settings.setTitle("Space Vector");
        
        settings.setFullscreen(true);
        settings.setVSync(true);
        settings.setResolution(1024, 720);
        
        app.setDisplayStatView(false);
        app.setDisplayFps(false);
        app.setSettings(settings);
        
        app.start();
    }

    @Override
    public void simpleInitApp() {  
        AudioNode audioNode = new AudioNode(assetManager, "Music/Space.wav");
        audioNode.setPositional(false);
        audioNode.setLooping(true);
        audioNode.setVolume(3);
        rootNode.attachChild(audioNode);
        audioNode.play();
        
        meteorFactory = new MeteorFactory(this);

        
        this.flyCam.setEnabled(false);
        
        //// LIGHTING ////
        
        // SUN // TODO: Needs direction
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(-.5f,-.5f,-.5f).normalizeLocal());
        rootNode.addLight(sun);
        
        // AMBIENT //
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White);
        rootNode.addLight(al);
        
        //// OBJECTS ////
        
        // SUN //
        Material sun_mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        Texture sun_diff = assetManager.loadTexture("Textures/sun.jpg");
        sun_diff.setWrap(Texture.WrapMode.Repeat);
        sun_mat.setTexture("DiffuseMap", sun_diff);
        sun_mat.setFloat("Shininess", 255.0f);
        
        Sphere sun_sphere = new Sphere(32, 32, 800);
        Geometry sun_geo = new Geometry("planet", sun_sphere);
        sun_geo.setMaterial(sun_mat);
        rootNode.attachChild(sun_geo);
        sun_geo.move(new Vector3f(4000, -600, 0));
        
        // SHIP //
        Material ship_mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        ship_mat.setTexture("DiffuseMap", assetManager.loadTexture("Models/ship/ship.png"));
        Ship.mat = ship_mat;
        
        // SKY //
        rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/skysphere.jpg", true));
        
        //// END ////
        
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        FogFilter fog = new FogFilter();
        fog.setFogColor(ColorRGBA.BlackNoAlpha);
        fog.setFogDistance(3000);
        fog.setFogDensity(1.5f);
        fpp.addFilter(fog);
        viewPort.addProcessor(fpp);

        skbListener = new ShipKeyBoardListener(this);    
        
        Node node = new Node();
        
        mainship = new Ship(-5, new Vector3f(0,0,0), new Vector3f(0,0,0), false, this, node, "Piet");
        skbListener.setShip(mainship);
        World.getInstance().register(-5, mainship);
        
        addStepListener(World.getInstance());

        cam.setFrustumFar(5000);
        CameraNode camNode = new CameraNode("Camnode", cam);
        
        node.attachChild(camNode);        
      
        camNode.setLocalTranslation(new Vector3f(0, 50, -250));
        
        camNode.lookAt(mainship.getLoc(), Vector3f.UNIT_Y);
        camNode.setControlDir(ControlDirection.SpatialToCamera);
        meteorFactory = new MeteorFactory(this);
        meteorFactory.generateMeteors();

        
        
        this.hudText = new BitmapText(guiFont, false);
        hudText.setSize(guiFont.getCharSet().getRenderedSize()); 
        hudText.setColor(ColorRGBA.White);
        hudText.setText("Ammunition: " + mainship.getWep().getAmmo() + "/8" + " | Health: " + mainship.getHealth());
        hudText.setLocalTranslation((settings.getWidth()/2) - (hudText.getLineWidth()/2), hudText.getLineHeight(), 0);
        guiNode.attachChild(hudText);
        
        
        this.deathScreen = new Picture("Deathscreen");
        this.deathScreen.setImage(this.assetManager, "Textures/Deathscreen.png", true);
        this.deathScreen.setHeight(settings.getHeight());
        this.deathScreen.setWidth(settings.getWidth());
        this.deathScreen.setPosition(0,0);
        
        this.leaveScreen = new Picture("Leavescreen");
        this.leaveScreen.setImage(this.assetManager, "Textures/Leaving.png", true);
        this.leaveScreen.setHeight(settings.getHeight());
        this.leaveScreen.setWidth(settings.getWidth());
        this.leaveScreen.setPosition(0,0);
        
        rootNode.attachChild(metNode);
        
       try {
            this.sock = new Socket(HOST, PORT);
            this.net = new NetworkManager(this, sock, generateUsername());
            this.thread = new Thread(net);
            thread.start();
        } catch (IOException e) {
            System.err.println("SOCKET FUCKUP EXCEPTION");
            e.printStackTrace();
            System.exit(1);
        }
        
    }

    @Override
    public void simpleUpdate(float tpf) {
        this.hudText.setText("Ammunition: " + mainship.getWep().getAmmo() + "/8" + " | Health: " + mainship.getHealth());
        for (int i = 0; i < steplisteners.size(); i++) {
            steplisteners.get(i).step();
        }
        
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
    
    public Node getMetNode(){
        return metNode;
    }
    
    public ArrayList<Spatial> getGeomList(){
        return this.geomMeteorList;
    }
    
    public Vector3f getCamDir(){
        return this.cam.getDirection();
    }
    
    public Vector3f getCamLoc(){
        return this.cam.getLocation();
    }
    
    public void setNodeDir(Node node, float x, float y, float z){
        node.rotate( x , y , z );
    }
    
//    public boolean doesCollide(Collidable object){
//        CollisionResults colRes = new CollisionResults();
//        this.getMetNode().collideWith(object, colRes);
//        System.out.println(colRes.size());
//        return colRes.size() > 0; 
//    }
    
    public static String generateUsername() {
        Random rand = new Random();
        return USERNAMES[rand.nextInt(USERNAMES.length)] + (new Integer(rand.nextInt(9999)).toString());
    }

    public synchronized MeteorFactory getMeteorFactory() {
        return meteorFactory;
    }

    public synchronized void attachToRootNode(Spatial thing) {
        rootNode.attachChild(thing);
    }

    public NetworkManager getNet() {
        return net;
    }
    
    public Socket getSock() {
        return sock;
    }
    
    public Picture getDeathScreen(){
        return this.deathScreen;
    }
    public void addStepListener(StepListener sl) {
        steplisteners.add(sl);
    }
    
    public void enableControls() {
        addStepListener(mainship);
        addStepListener(mainship.getWep());
        addStepListener(skbListener);
    }
    
    public Ship getShip() {
        return mainship;
    }
    
    
    public Node getGui(){
        return this.guiNode;
    }
    
    public Picture getLeave(){
        return this.leaveScreen;
    }
}
