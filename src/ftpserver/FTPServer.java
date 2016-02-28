/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ftpserver;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.omg.CORBA.INITIALIZE;
/**
 *
 * @author Dinh
 */
public class FTPServer implements Runnable{

    /**
     * @param args the command line arguments
     */
        ServerSocket PasvSocket;
        BufferedReader reader = null;
        PrintWriter writer = null;
        String response,curdir = System.getProperty("user.dir"),temp;
        StringTokenizer str;
        String[] strarr;
        Socket socket = null;
        File f = null;
        PrintWriter pwtemp = null;
        int port=0;
        InetAddress cipaddr=null;
        int i = 1;
        boolean check;     
        Socket ns;
        int ThreadCount;
        boolean done;
        Thread t;        
    public FTPServer(Socket ns,int i){        
        this.ns = ns;
        ThreadCount = i;
        t = new Thread(this);        
        t.start();
    }
    public void run() {
        // TODO code application logic here
        
        try{                        
            writer = new PrintWriter(new OutputStreamWriter(ns.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(ns.getInputStream()));
            writer.println("220 Serv-U FTP Server ready...");
            writer.flush();
            done = false;
            while (done == false){                
                response = reader.readLine();   
                System.out.println("Thread " + ThreadCount + ": "+response);
                //check user login
                if(response.startsWith("USER ")){
                //System.out.println(response);
                    if(checkUser(response)){
                        System.out.print("true\n");
                        writer.println("331 User name okay, need password.");
                        writer.flush();
                    }
                    else{
                        System.out.print("false\n");
                        writer.println("332 Need account for login.");
                        writer.flush();
                 }
                    continue;
                }   
              //check password
                if(response.startsWith("PASS ")){
                    if(checkUser(response)){
                        writer.println("230 User logged in, proceed.");
                        writer.flush();
                    }
                    else{
                        writer.println("332 Need account for login.");
                        writer.flush();
                    }
                    continue;
                }
            if(response.startsWith("QUIT")) {
                writer.println("GOOD BYE");
                writer.flush();    
                t.interrupt();
                done = true; 
            }
            //SYST
                if(response.equalsIgnoreCase("SYST")){
                    temp = checkSystem();
                    writer.println("215 " + temp +" Type");
                    writer.flush();
                    continue;
                }
                 if(response.startsWith("SIZE")){
                    response = response.substring(4);
                    response = response.trim();
                    File fp = new File(curdir,response);
                    long l = fp.length();
                    writer.print("213 " + l + "\r\n");
                    writer.flush();
                    continue;
                }
            
             //FEAT
                if(response.equalsIgnoreCase("FEAT")){
                    writer.println("211-Extensions supported\r\nUTF8\r\nOPTS MODE;MLST;UTF8\r\nCLNT\r\nCSID Name; Version;\r\n"
                            + "HOST domain\r\nSITE PSWD;SET;ZONE;CHMOD;MSG;EXEC;HELP\r\nAUTH TLS;SSL;TLS-C;TLS-P;\r\nPBSZ\r\n"
                            + "PROT\r\nCCC\r\nSSCN\r\nRMDA\r\nDSIZ\r\nAVBL\r\nEPRT\r\nEPSV\r\nMODE\r\nTHMB BMP|JPEG|GIF|TIFF|PNG"
                            + "max_width max_height pathname\r\nREST STREAM\r\nSIZE\r\nMDTM\r\nMDTM YYYYMMDDHHMMSS[+-TZ];filename\r\n"
                            + "MFMT\r\nMFCT\r\nMFF Create;Modify;\r\nXCRC filename;start;end\r\nXMD5 filename;start;end\r\n"
                            + "XSHA1 filename;start;end\r\nXSHA256 filename;start;end\r\nXSHA512 filename;start;end\r\n"
                            + "COMB target;source_list\r\nMLST Type*;Size*;Create;Modify*;Perm;Win32.ea;Win32.dt;Win32.dl\r\n"
                            + "211 End (for details use HELP command where command is the command of interest)\r\n");
                    writer.flush();
                    continue;
                }
            
              //CLNT Total Commander (UTF-8)
                if(response.startsWith("CLNT ")){
                    writer.println("200 Noted.");
                    writer.flush();
                    continue;
                }
            
                 //OPTS UTF8 ON
                if(response.startsWith("OPTS ")){
                    writer.println("200 OPTS UTF8 is set to ON.");
                    writer.flush();
                    continue;
                }
            
             //print current directory
                if(response.startsWith("PWD")){
                    //curdir = printCurrentPath();
                    writer.println("257 "+ '"' + "/" + '"' + " is current directory");
                    writer.flush();
                    continue;
                }
            
                 //Request TYPE A
                if(response.equalsIgnoreCase("TYPE A")){
                    writer.println("200 Type set to A.");
                    writer.flush();
                    continue;
                }
               if(response.equalsIgnoreCase("TYPE I")) {
                    writer.println("200 type set to I."); 
                    writer.flush();
               }
                if(response.startsWith("PASV")) {
                    String myStr;
                    // myStr = "PORT 12345";
                    
                    
                    myStr = "227 Entering Passive Mode (";
                    byte[] add = ns.getInetAddress().getAddress();      
                    
                    
                    if (PasvSocket==null){
                        PasvSocket = new ServerSocket(0);
                    }
                    int pasvport = PasvSocket.getLocalPort();                                        
                    
                    int p1 = pasvport / 256;
                    int p2 = pasvport - p1* 256;        
                    port = pasvport;
                    for (int j=0;j<4;j++) {
                        myStr += add[j];
                        myStr += ",";
                    }
                    myStr += p1;
                    myStr += ",";
                    myStr += p2;
                    myStr += ")";
                    
                    writer.println(myStr); 
                    writer.flush();
                    continue;
               }
            
                 //PORT A,B,C,D,X,Y
                
            
                
                if(response.startsWith("PORT ")){                
                    int space = response.indexOf(" ");
                    String con = response.substring(space+1);
                    str = new StringTokenizer(con,",");
                    strarr = new String[6];
                    int j = 0;                
                    while(str.hasMoreElements()){
                        strarr[j] = (String)str.nextElement();
                        j++;
                    }
                    String cipadd;
                    cipadd = strarr[0] + "." + strarr[1] + "." + strarr[2] + "." + strarr[3];
                    System.out.println(cipadd);
                    cipaddr = InetAddress.getByName(cipadd);
                    port = 256 * Integer.parseInt(strarr[4]) + Integer.parseInt(strarr[5]);                                  
                    
                    f = new File(curdir);            

                    writer.println("200 PORT command successful.");
                    writer.flush(); 
                    
                    continue;
                }          
               
             //LIST or MLSD               
                if(response.equalsIgnoreCase("LIST")){
                    if (PasvSocket!=null){
                        socket = PasvSocket.accept();
                        f = new File(curdir); 
                    }
                    else {
                        socket = new Socket(cipaddr, port);   
                    }
                    pwtemp = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                    writer.println("150 Opening ASCII mode data connection.");
                    writer.flush();                         
                    listDirectoryAndFile(f,pwtemp);                
                    writer.println("226 Transfer complete.");              
                    writer.flush();
                    pwtemp.close();                        
                    socket.close();
                    continue;
                }            
            
                //CWD
                   
                    if(response.startsWith("CWD ")){
                        curdir = changeCurrentPath(response,curdir);
                        writer.println("250 Requested file action okay, completed.");
                        writer.flush();
                        response = reader.readLine();
                        if(response.startsWith("PWD")){
                              writer.println("257 "+'"'+curdir+'"'+" directory created");
                              writer.flush();
                        }        
                        continue;
                    }   
                    //xoa file
                    if(response.startsWith("DELE ")) {
                           response = response.substring(4);
                           response = response.trim();
                           f = new File(curdir,response);
                           boolean del = f.delete();
                           writer.println("250 delete command successful"); 
                           writer.flush();
                           continue;
                    }                   
                    //Tao thu muc
                    if(response.startsWith("MKD ")) {
                           response = response.substring(4);
                           response = response.trim();
                           f = new File(curdir,response);
                           boolean del = f.mkdir();
                           writer.println("250 delete command successful"); 
                           writer.flush();
                           continue;
                    }
                    //xoa thu muc
                    if(response.startsWith("RMD ")) {
                           response = response.substring(4);
                           response = response.trim();
                           f = new File(curdir,response);   
                           String dirname = curdir +"\\" +response;
                                   
                           File fp;
                           DirDelete(dirname);
                           boolean del = f.delete();                           
                           writer.println("250 delete command successful"); 
                           writer.flush();
                           continue;
                    }
                    // Doi lai thu muc cha
                    if(response.startsWith("CDUP")) {
                           int n = curdir.lastIndexOf("\\");
                           curdir = curdir.substring(0,n);
                           writer.println("250 CWD command succesful");
                           writer.flush();
                    } 
                    if(response.startsWith("RETR ")){
                        Socket sdata;
                        if (PasvSocket!=null){
                            sdata = PasvSocket.accept();
                        }
                        else {
                            sdata = new Socket(cipaddr,port);
                        }
                        OutputStream output = sdata.getOutputStream();
                        writer.println("150 Opening BINARY mode data connection.");
                        writer.flush();                        
                        downloadFile(response,output);
                        writer.println("226 Transfer complete.");              
                        writer.flush();
                        output.close();                      
                        sdata.close();
                        continue;
                    }
                    if(response.startsWith("STOR")) {
                           writer.println("150 Binary data connection");
                           writer.flush();
                           response = response.substring(4);
                           response = response.trim();
                           System.out.println(response);
                           System.out.println(curdir);
                           RandomAccessFile inFile = new RandomAccessFile(curdir+"/"+ response ,"rw");
                           Socket t;
                           if (PasvSocket!=null){
                                t = PasvSocket.accept();
                           }
                           else {
                                t = new Socket(cipaddr,port);
                           }                           
                           InputStream in2 = t.getInputStream();
                           byte bb[] = new byte[1024];
                           int amount;
                           try {
                                while((amount = in2.read(bb,0,1024)) != -1) {
                                      inFile.write(bb, 0, amount);
                                }
                           in2.close();
                           writer.println("226 transfer complete");
                           writer.flush();
                           inFile.close();
                           t.close();
                           }   
                           catch(IOException e)                                        
                           { 
                               System.out.print(e.toString());
                           } 
                      }
            } // End While Loop
            
        } // Try catch Exception
        catch(Exception e){
            System.out.println("Error: " + e);
        }
    }
    public void CWD(String response){
             
    }
    
    public boolean checkUser(String user){
        File f = new File("src\\ftpserver\\user.txt");
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        DataInputStream dis = null;
        String[] t1 = new String[2];
        String[] t2 = new String[2];
        String temp;        
        StringTokenizer st,stt;
        int i;
        
        try{
            fis = new FileInputStream(f);
            bis = new BufferedInputStream(fis);
            dis = new DataInputStream(bis);            
            i = 0;
            st = new StringTokenizer(user);
            while(st.hasMoreElements()){
                t1[i] = (String)st.nextElement();
                //System.out.println(t[i] + "\n");
                i++;
            }
            this.curdir = dis.readLine();
            while(dis.available() != 0){
                temp = dis.readLine();
                i = 0;
                stt = new StringTokenizer(temp);
                while(stt.hasMoreElements()){
                    t2[i] = (String)stt.nextElement();
                    i++;
                }
                if(user.startsWith("USER ")){
                    if(t1[1].equalsIgnoreCase(t2[0])){
                        return true;
                    }     
                }
                else{
                    if(t1[1].equalsIgnoreCase(t2[1])){
                        return true;
                    }   
                }                           
            }           
        }
        catch(IOException e){
            e.printStackTrace();
        }
        finally{
            try{
                fis.close();
                bis.close();
                dis.close();                
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return false;
    }
    
    public String printCurrentPath(){
        String dir = System.getProperty("user.dir");    //get current directory
        System.out.println("Current working directory : " + dir);
        return dir;
    }
    
    public void listDirectoryAndFile(File f, PrintWriter pw){
        
        //System.out.println(f.getAbsoluteFile());
        //pw.println(f.getAbsoluteFile());
        //pw.flush();
        String strfile = "";
        if(f.isDirectory()){
            String[] subFile = f.list();
          //  pw.print("total " + subFile.length + "\n");
            
            
            for(String name : subFile){             
                strfile = "";
                // pw.print(name + "\n");             
                
               
                //listDirectoryAndFile(new File(f,name),pw);
                File fp = new File(f,name);
                if(fp.isDirectory()){
                    strfile += "d ";
                }
                else strfile += "f ";
                
                // strfile += fp.lastModified();
                // System.out.println(fp.getAbsoluteFile());
                strfile += name+ "\r\n";
                //pw.println(fp.getAbsoluteFile());
                pw.print(strfile);
                //pw.flush();
         
                
                
            }
            pw.flush();
           
            /*System.out.println(strfile);
            pw.println(strfile);            
            pw.flush();*/
            
        }
    }
    
    public String changeCurrentPath(String targetdir, String curdir){
        StringTokenizer stn;
        String[] t = new String[2];
        int i;        
        i = 0;
        targetdir = targetdir.substring(3);
        targetdir = targetdir.trim();               
        
        curdir = curdir + "\\" + targetdir;                      
        System.setProperty("user.dir", targetdir);        
        return curdir;
    }
    
  public void downloadFile(String response, OutputStream prtw){
        String st;
        String[] t = new String[2];
        String filename,filepath,workingdir;
        int i;
        FileInputStream fis = null;      
        workingdir = curdir;
        filename = response.substring(4);
        filename = filename.trim();
        System.out.println(filename);
        filepath =  workingdir + File.separator + filename;
        System.out.println("Final filepath : " + filepath);
        
        File file = new File(filepath);
        
        try {
            fis = new FileInputStream(file);                    
            byte[] buffer = new byte[20000];
            int byteReads = 0;
            
            while((byteReads = fis.read(buffer)) != -1){
                prtw.write(buffer,0,byteReads);
            }       
            prtw.flush();
            prtw.close();
        } 
        catch (IOException e) {
            e.printStackTrace();
        }        
    }
    
    public String checkSystem(){
        String OS,str ="";
        
        OS = System.getProperty("os.name").toLowerCase();
        
        if(OS.indexOf("win")>=0){
            System.out.println("This is Windows");
            str = "Win";            
        }
        if(OS.indexOf("mac")>=0){
            System.out.println("This is Mac");
            str = "Mac";
        }
        if(OS.indexOf("nix")>=0 || OS.indexOf("nux")>=0 || OS.indexOf("aix")>=0){
            System.out.println("This is Unix");
            str = "Unix";
        }
        if(OS.indexOf("sunos")>=0){
            System.out.println("This is Solaris");
            str = "Solaris";
        }
        return str;
    }
    public void DirDelete(String DirName) throws IOException {
        String Path;
        boolean k;            
        File f = new File(DirName);
        List<String> Filelist = new ArrayList<String>();
        List<String> Folderlist = new ArrayList<String>();
        TakeList(Filelist,Folderlist,f);
        if (f.exists()&&f.isDirectory()){
            for (String str : Filelist){
                File temp = new File(str);
                temp.delete();
            }
            for (String str : Folderlist){
                File temp = new File(str);
                temp.delete();
            }                 
            k = f.delete();
            if (k){
                System.out.println("Folder deleted successfully");
            }
            else {
                System.out.println("Unsuccessfully");
            }
        }
        else {
            System.out.println("Folder not exist");
        }
    }
    public void TakeList(List<String> filelist, List<String> folderlist, File dir){
        File[] list;
        list = dir.listFiles();
        for (File filetemp: list){
            if (filetemp.isDirectory()){
                folderlist.add(filetemp.getAbsolutePath());
                TakeList(filelist,folderlist,filetemp);                
            }
            else {
                filelist.add(filetemp.getAbsolutePath());
            }
                
        }
    }
}
