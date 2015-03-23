package mygame;

import com.jme3.scene.Geometry;
import com.jme3.terrain.GeoMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import objects.GameObject;
import ship.Ship;

public class World implements StepListener {

    private static World instance = null;

    public static World getInstance() {
        if (instance == null) {
            instance = new World();
        }
        return instance;
    }

    private World() {}

    private Map<Integer, GameObject> entityMap = new HashMap<Integer, GameObject>();
    private Map<Integer, GameObject> queue = new HashMap<Integer, GameObject>();
    private static final int FRAME_OBJECT_CAP = 100;

    public synchronized void register(Integer id, GameObject entity) {
        queue.put(id, entity);
    }
    
    public synchronized void step() {
        ArrayList<Integer> delete = new ArrayList<Integer>();
        for (Integer qid : queue.keySet()) {
            entityMap.put(qid, queue.get(qid));
            Main.app.attachToRootNode(queue.get(qid).getSpatial());
            delete.add(qid);
            if (delete.size() > FRAME_OBJECT_CAP) break;
        }
        for (Integer id : delete) {
            queue.remove(id);
        }
        for (GameObject entity: this.entityMap.values()){
            if (entity instanceof Ship){
                System.out.println("Step");
                ((Ship) entity).step();
            }
        }
    }

    public GameObject getEntityById(int identifier) {
        return entityMap.get(identifier);
    }

    public float[] getSpawnLocation() {
        return new float[3];
    }
    
    public synchronized void resetId(int oldid, int newid) {
        queue.put(newid, entityMap.get(oldid));
        entityMap.remove(oldid);
    }

    public Map<Integer, GameObject> getEntityMap() {
        return entityMap;
    }

}