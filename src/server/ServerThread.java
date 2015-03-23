/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import server.Server;

/**
 *
 * @author Gerryflap
 */
public class ServerThread extends Thread{
    
    public void run(){
        Server.main(new String[0]);
    }
}
