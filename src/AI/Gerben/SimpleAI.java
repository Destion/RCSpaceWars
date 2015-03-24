/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package AI.Gerben;

import AI.SimpleNetworkListener;
import AI.SimpleNetworkManager;
import com.jme3.math.Vector3f;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import mygame.Command;


/**
 *
 * @author Gerryflap
 */
public class SimpleAI implements SimpleNetworkListener{
    
    public static String HOST = "localhost";
    public static int PORT = 6969;
    public static long UPDATE_RATE = 50;
    
    private Vector3f position;
    private Vector3f direction;
    private float speed = 10;
    private float angle = 0;
    
    private long lastUpdate;
    
    private SimpleNetworkManager net;
    
    public static void main(String[] args){
        SimpleNetworkManager net = null;
        SimpleAI simpleAI = null;
         try {
            Socket sock = new Socket(HOST, PORT);
            net = new SimpleNetworkManager(sock, "GERBEN_AI");
            simpleAI = new SimpleAI(net);
            net.addListener(simpleAI);
            Thread thread = new Thread(net);
            thread.start();
        } catch (IOException e) {
            System.err.println("SOCKET FUCKUP EXCEPTION");
            e.printStackTrace();
            System.exit(1);
        }
         
         if(net != null && simpleAI != null){
            while(net.isSocketOpen()){
                net.step();
                simpleAI.step();
            }
         }
        
    }
    
    public SimpleAI(SimpleNetworkManager net){
        this.net = net;
        this.position = new Vector3f(0, 0, 0);
        this.direction = new Vector3f(0, 0, 0);
    }
    
    public void step(){
        
        //this.position = this.position.add(direction.clone().mult(speed));
        if(this.lastUpdate + UPDATE_RATE < System.currentTimeMillis()){
            this.angle += 0.001;
            if(this.angle > 2*Math.PI){
                this.angle = 0;
        }
        this.direction.y = angle;
        this.position.set(new Vector3f((float) Math.cos(angle), (float) Math.sin(angle), 0f).mult(1000));
            this.lastUpdate = System.currentTimeMillis();
            Command command = new Command(Command.CommandType.MOVE);
            command.addArgument(Float.toString(this.position.x))
                    .addArgument(Float.toString(this.position.y))
                    .addArgument(Float.toString(this.position.z))
                    .addArgument(Float.toString(this.direction.x))
                    .addArgument(Float.toString(this.direction.y))
                    .addArgument(Float.toString(this.direction.z));

            try {
                this.net.send(command);
            } catch (IOException ex) {
                Logger.getLogger(SimpleAI.class.getName()).log(Level.SEVERE, null, ex);
            }            
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(SimpleAI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void onRecieve(Command command) {
        System.out.println(command);
    }
    
    
    
}
