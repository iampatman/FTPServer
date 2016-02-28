/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ftpserver;

/**
 *
 * @author PC
 */
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
public class Main {
    public static void main(String[] args){
        try{
            int count = 0;
            System.out.print("====================================================\n");
            System.out.print("=========THIS'S FTP SERVER APPLICATION==============\n");
            System.out.print("Server Started...\n");
            System.out.print("Waiting for connections...\n");
            ServerSocket s = new ServerSocket(21);
            while (true){
                Socket ns = s.accept();
                count++;
                System.out.println("New Client Connected with id " + count + " from " + ns.getInetAddress().getHostName()+ "\n" );
                FTPServer ftpServer = new FTPServer(ns,count);
            }
        }catch (Exception e){
            System.out.println(e.toString());
        }
    }
}
