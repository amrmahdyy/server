package HTTPServer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Middleware {
    private static Middleware middleware=new Middleware();
     String  resourceTarget="";
     Socket socket;
     private static String CRLF="\r\n";
     public static  Middleware Middleware(){
         return middleware;
     }
    public void applyCaching(Socket socket, String resourceTarget) throws IOException {
        this.resourceTarget=resourceTarget;
        this.socket=socket;
        sendToClientLastModifiedDate();
    }

    private  String getLastModifiedDate(){
        String lastModifiedDate=Extensions.getLastModifiedDate(resourceTarget).toString();
        return lastModifiedDate;
    }

    private  void sendToClientLastModifiedDate() throws IOException {
        OutputStreamWriter outputStreamWriter=new OutputStreamWriter(socket.getOutputStream());
        BufferedWriter bufferedWriter=new BufferedWriter(outputStreamWriter);

        bufferedWriter.write("Last-Modified: "+getLastModifiedDate()+CRLF);

        bufferedWriter.flush();
        outputStreamWriter.flush();
    }

}
