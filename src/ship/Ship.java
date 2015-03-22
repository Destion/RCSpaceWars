package ship;

import com.jme3.material.Material;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import mygame.Main;
import objects.GameObject;
/**
 * 
 * @author Destion
 */
public class Ship extends GameObject{
    
    private int id; 
    
    private Vector3f speeds;
    private float[] angles;
    private int inverted;
    private int health;
    
    private Main app;
    
    private String username;

    public static Material mat;
    public static Spatial globalSpatial;
    
    private Spatial spatial;
    private Node node;
    
    private static final int DEFAULTFIREPOWER = 50;
    private static final int DEFAULTRELOADTIME = 5000;
    private static final int DEFAULTAMMO = 8;
    
  
    
    private final float SPEED = 0.008f;
    
    Weapon weapon;
    
    
    
    public Ship(int id, Vector3f position, Vector3f direction, boolean invert, Main app, Node node, String username){
        this(id, position, direction, invert, Ship.DEFAULTRELOADTIME, Ship.DEFAULTFIREPOWER, 500, Ship.DEFAULTAMMO, app, node, username);

    }
    
    public Ship(int id, Vector3f position, Vector3f direction, boolean invert, int seperation, int firePower, int reloadTime, int ammo, Main app, Node node, String username){
        super(id, position, direction);
        this.speeds = new Vector3f(0, 0, 0);
        this.inverted = (invert) ? -1 : 1;
        this.angles = new float[]{0, 0, 0};
        this.health = 100;        
        this.app = app;        
        this.weapon = new Weapon(seperation, firePower, reloadTime, ammo);
        if(globalSpatial == null){
            globalSpatial = app.getAssetManager().loadModel("Models/ship/ship.j3o");
            globalSpatial.scale(5);
            globalSpatial.setMaterial(mat);
        } 
        this.spatial = globalSpatial.clone();
        node.attachChild(spatial);
        this.node = node;
        
        
    }
    
    public Weapon getWep(){
        return this.weapon;
    }
    
    //Getters for position and speed
    public float getX(){
        return this.position.x;
    }
    public float getY(){
        return this.position.y;
    }
    public float getZ(){
        return this.position.z;
    }
    
    public Vector3f getLoc(){
        //This is here for backwards compability
        return getPosition();
    }
    
    public float[] getAngles(){
        return this.angles;
    }
    
    
    public float getSpeed(){
        return this.SPEED;
    }
    
    public void step(){
        if(this.app.meteorFactory.doesCollide(spatial.getWorldBound())){
            System.out.println("Collison!");
        }
        node.move(this.app.getCamera().getDirection().normalizeLocal().mult(new Vector3f(10f, 10f, 10f))); // 0.1 = speed        
        this.setPosition(node.getLocalTranslation());
        
        this.spatial.setLocalRotation(Quaternion.IDENTITY);
        
        //System.out.println(String.format("x: %s, y: %s, z: %s", angles[0], angles[1], angles[2]));
        //System.out.println(String.format("x: %s, y: %s, z: %s", Math.sin(this.angles[2]) * Math.cos(this.angles[1]) , Math.sin(this.angles[0]) * Math.cos(this.angles[2]), Math.sin(this.angles[1]) * Math.cos(this.angles[0])));

    }
    
    //Getter for identification number
    public int getId(){
        return this.id;
    }
    
    public Vector3f getSpeeds(){
        return this.speeds;
    }
    
    //Setters for position, identification number and speedc
    public void setX(float x){
        this.position.x =x;
    }
    public void setY(float y){
        this.position.y = y;
    }
    public void setZ(float z){
        this.position.z = z;
    }
    public void setId(int id){
        this.id = id;
    }
    
    public void wPressed(float force){
        app.setNodeDir(node, force * inverted * 0.03f, 0, 0);
    }
    
    public void sPressed(float force){
        app.setNodeDir(node, force * -1 * (inverted * 0.03f), 0, 0);        
    }
    
    public void aPressed(float force) {
        app.setNodeDir(node, 0, 0, force * -1 * 0.04f);
    }
    
    public void dPressed(float force) {
        app.setNodeDir(node, 0, 0, force * 0.04f);
    }
    
    public Spatial getSpatial(){
        return this.spatial;
    }
         
    public void shoot(){
        int fire = 0;
        int nofire = 0;
        if(this.weapon.fire()){
            this.weapon.ammunition -= 1;
            fire += 1;
            Vector3f directionXYZ = this.direction;
            Vector3f positionXYZ = this.position;

            Ray ray = new Ray(directionXYZ, positionXYZ);

            if (this.app.meteorFactory.doesCollide(ray)){
                System.out.println("Lol");
                this.app.meteorFactory.collideObject(ray).setCullHint(CullHint.Always);
                System.out.println("Hoi");
            }
        } else {
        }
        
    }
     
    public void hit(int damage){
         this.health -= damage;
     }     
     
    //Weapon carries the reloadtime and damage of the weapon this ship is carrying, enabling multiple types of ship
    public class Weapon{
        
        int seperation;
        int firePower;
        int reloadTime;
        int ammunition;
        
        long lastFire;
        long reloadStart;
        
        boolean reloading;
        boolean reloaded;
        
        public Weapon(int reloadTime, int firePower, int seperation, int ammunition){
            this.seperation = seperation;
            this.firePower = firePower;
            this.reloadTime = reloadTime;
            this.ammunition = ammunition;
            this.reloadStart = 0;
            this.lastFire = 0;
            this.reloading = false;
            this.reloaded = false;
            this.reloadTime = 4;
        }

            //Use fire when firing a bullet, canFire checks reloadtime
        /*public boolean canFire(){
            long currentTime = System.currentTimeMillis();
            if ((currentTime - this.reloadStart) > this.reloadTime){
                
                if(this.ammunition == 0){
                    this.ammunition = 8;
                }
                
                if ((currentTime - this.lastFire) > this.seperation){
                    return true;
                }
                else {
                    return false;
                }
            }
            return false;
        }
        */
        public void reload(){
            if(this.reloading){
                
                if ((System.currentTimeMillis() - this.reloadStart) > reloadStart){
                    this.reloaded = true;
                    this.reloading = false;
                    System.out.println("Gerben");
                } 
            }
            if (this.reloaded){
                this.ammunition = 8;
                this.reloaded = false;
            }
        }
        
        public boolean fire(){
            if (this.ammunition > 0){
                this.lastFire = System.currentTimeMillis();
                this.reload();
                return true;
            } else if (this.ammunition == 0){
                this.reload();
                this.reloadStart = System.currentTimeMillis();
                this.reloading = true;
                System.out.println("Hoi");
                return false;
            } else {
                return false;
            }
        }
        
        public int getAmmo(){
            return this.ammunition;
        }
    }
}
