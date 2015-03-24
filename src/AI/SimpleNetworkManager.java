/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package AI;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import mygame.Command;
import mygame.StepListener;
import ship.Ship;

/**
 *
 * @author Rick Lubbers
 */
public class SimpleNetworkManager implements Runnable, StepListener {

    private Socket socket;
    public BufferedReader in;
    public BufferedWriter out;
    private String username;
    private int controlId;
    private ArrayList<SimpleNetworkListener> listeners;
    private ArrayList<Command> queue;
    private boolean isSocketOpen = true;
    
    public SimpleNetworkManager(Socket socket, String username) {
        this.listeners = new ArrayList<SimpleNetworkListener>();
        this.queue = new ArrayList<Command>();
        this.username = username;
        try {
            this.socket = socket;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            send(new Command(Command.CommandType.CONNECT).addArgument(username));
        } catch (Exception e) {
            System.err.println("CHAOS AND DESTRUCTION WHILE USING SOCKET!");
        }
    }
    
    public int getId(){
        return controlId;
    }
    
    public boolean isSocketOpen(){
        return isSocketOpen;
    }
    
    public void run() {
        try {
            while (true) {
                String input = null;
                if ((input = in.readLine()) != null) {
                    
                    Command cmd = Command.parseCommand(input);
                    if (cmd != null) {
                        synchronized(queue){
                            queue.add(cmd);
                        }
                    }
                    input = null;
                }
            }
        } catch (Exception e) {
            this.isSocketOpen = false;
            System.err.println("Something went terribly wrong with the socket");
            e.printStackTrace();
        }
    }
    
    public void send(Command cmd) throws IOException {
        out.write(cmd.toString());
        out.newLine();
        out.flush();
    }
    
    public void step(){
        ArrayList<Command> toBeDeleted = new ArrayList<Command>();
        synchronized(queue){
            for(Command cmd: queue){
                for(SimpleNetworkListener listener: listeners){
                    listener.onRecieve(cmd);
                    toBeDeleted.add(cmd);
                }
            }


            for(Command cmd: toBeDeleted){
                this.queue.remove(cmd);
            }
        }
    }
    
   
    public Socket getSock(){
        return this.socket;
    }
    
    public void addListener(SimpleNetworkListener listener){
        this.listeners.add(listener);
    }
    
    public ArrayList<SimpleNetworkListener> getListeners(){
        return (ArrayList<SimpleNetworkListener>) this.listeners.clone();
    }
    
}
