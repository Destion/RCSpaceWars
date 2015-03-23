/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import ship.Ship;

/**
 *
 * @author Rick Lubbers
 */
public class NetworkManager implements Runnable {

    private Main app;
    private Socket socket;
    public BufferedReader in;
    public BufferedWriter out;
    private String username;
    private int controlId;
    
    public NetworkManager(Main app, Socket socket, String username) {
        this.username = username;
        try {
            this.app = app;
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
    
    public void run() {
        try {
            while (true) {
                String input = null;
                if ((input = in.readLine()) != null) {
                    
                    Command cmd = Command.parseCommand(input);
                    if (cmd != null) {
                        if (cmd.getCommandType() == Command.CommandType.OBJECT) {
                            String[] args = cmd.getArguments();
                            updateObject(args);
                        } else if (cmd.getCommandType() == Command.CommandType.CONTROL) {
                            controlId = Integer.parseInt(cmd.getArguments()[0]);
                        } else if(cmd.getCommandType() == Command.CommandType.UPDATE){
                            System.err.println("New UPDATE!");
                            int id = Integer.parseInt(cmd.getArguments()[0]);
                            
                            if(id != controlId && World.getInstance().getEntityById(id) != null){
                                System.err.println("New UPDATE on an Existing Entity");
                                System.err.println(id + ", " + controlId);
                                World.getInstance().getEntityById(id).setPosition(new Vector3f(
                                        Float.parseFloat(cmd.getArguments()[2]), 
                                        Float.parseFloat(cmd.getArguments()[3]), 
                                        Float.parseFloat(cmd.getArguments()[4])));
                                World.getInstance().getEntityById(id).setDirection(new Vector3f(
                                        Float.parseFloat(cmd.getArguments()[5]), 
                                        Float.parseFloat(cmd.getArguments()[6]), 
                                        Float.parseFloat(cmd.getArguments()[7])));
                            }
                           
                        }else {
                            // TODO: implement other commands
                        }
                    }
                    input = null;
                }
            }
        } catch (Exception e) {
            System.err.println("Something went terribly wrong with the socket");
            e.printStackTrace();
        }
    }
    
    public void send(Command cmd) throws IOException {
        out.write(cmd.toString());
        out.newLine();
        out.flush();
    }
    
    public void updateObject(String[] args) {
        int id = Integer.parseInt(args[0]);
        if ("Meteor".equals(args[1])) {
            float[] coords = { Float.parseFloat(args[2]), Float.parseFloat(args[3]), Float.parseFloat(args[4]) };
            float[] rotation = { Float.parseFloat(args[5]), Float.parseFloat(args[6]), Float.parseFloat(args[7]) };
            float radius = Float.parseFloat(args[8]);
            app.getMeteorFactory().addMeteor(id, coords, rotation, radius);
        } else if ("Ship".equals(args[1])) {
            if (id == controlId) {
                app.enableControls();
                app.getShip().setId(controlId);
                World.getInstance().resetId(-5, controlId);
            } else {
                World.getInstance().register(id, new Ship(id, new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), false, this.app, new Node(), "Swek"));
            }
        } else {
            // TODO: implement other objects
        }
    }
    
    public Socket getSock(){
        return this.socket;
    }
    
}
