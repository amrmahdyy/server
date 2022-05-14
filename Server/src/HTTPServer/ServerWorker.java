package HTTPServer;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class ServerWorker extends Thread {

    byte[] dataBytes;
    byte[] resourceBytes;
    private Socket socket;
    String clientRequest="";
    private String httpMethod, requestedPath;
    private String notFoundPath = "/404.html";
    private HashMap<String, String> httpHeaders = new HashMap<String, String>();
    private StringBuilder httpBody = new StringBuilder();

    ServerWorker(Socket socket) {
        this.socket = socket;
    }

    private void completeGET() {
        if (requestedPath.equals("/"))
            requestedPath = "/index.html";

        Boolean resourceExists = Extensions.resourcesExists(requestedPath);
        GET(resourceExists);
    }

    private void GET(Boolean resourceExists) {
        StringBuilder responseBuilder = new StringBuilder();
        FileInputStream fileInputStream;
        String targetURI;

        if (requestedPath.equals("/"))
            requestedPath = "/index.html";

        if (resourceExists) {
            targetURI = requestedPath;
            responseBuilder.append("HTTP/1.0 200 OK\r\n");

        } else {
            targetURI = notFoundPath;
            responseBuilder.append("HTTP/1.0 404 Not Found\r\n");
        }

        try {
            fileInputStream = Extensions.getFileInputStream(targetURI);

            String contentType = Extensions.getContentType(targetURI);
            Long contentLength = fileInputStream.getChannel().size();

            responseBuilder.append("Date: " + Extensions.getCurrentDate() + "\n");
            responseBuilder.append("Server: macOS\n");
            responseBuilder.append("Last-Modified: " + Extensions.getLastModifiedDate(targetURI) + "\n");
            responseBuilder.append("Content-Length: " + contentLength.toString() + "\n");
            responseBuilder.append("Content-Type: " + contentType + "\n");
            responseBuilder.append("Connection: Closed\n");
            responseBuilder.append("\r\n");

            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(responseBuilder.toString().getBytes());
            outputStream.write(fileInputStream.readAllBytes());
            outputStream.flush();
            outputStream.close();

            SocketServer.ACTIVE_WORKERS = SocketServer.ACTIVE_WORKERS - 1;
            socket.close();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


//    This function is used by POST function where it saves the resource

    void saveResource(byte[]resourceBytes){
        try{
            File file=new File(SocketServer.resourcesDirectory+requestedPath);
            FileOutputStream fileOutputStream=new FileOutputStream(file);
            fileOutputStream.write(resourceBytes);
        }
        catch(IOException ioException){
            System.out.println("Can't save resource on Server");
            ioException.printStackTrace();
        }
    }


//    This function is responsible for copying the resource bytes from the main byte array by passing the start of resourceStartIndex
    void processPOST(int resourceStartIndex){
        int padding=1;
        int dataStartIndex=resourceStartIndex+padding;

         resourceBytes=new byte[dataBytes.length-dataStartIndex];
        for(int i=0;i<resourceBytes.length;i++){
            resourceBytes[i]=dataBytes[i+dataStartIndex];
        }
    }

    void POST(){
        saveResource(resourceBytes);
    }

    void completePOST(){
        try{
            InputStream is=socket.getInputStream();
            dataBytes=is.readAllBytes();

            String dataInString=new String(dataBytes);
            System.out.println(dataInString);
//            Search for the end of the header
            int resourceStartIndex=dataInString.indexOf("\n\r");

//            get Client request from the first line
//             Get the requested path from client

            processPOST(resourceStartIndex);
            POST();

        }
        catch(IOException ioException){
            System.out.println("Error in reading socket stream");

        }
    }

    @Override
    public void run() {
        super.run();
        try {

            InputStreamReader inStream = new InputStreamReader(socket.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inStream);
            String lineReader = bufferedReader.readLine();
            String[] lineComponents = lineReader.split(" ");

            httpMethod = lineComponents[0];
            requestedPath = lineComponents[1];

            if (httpMethod.equals("GET")) {
                completeGET();
            } else {
                completePOST();
            }

        } catch (Exception exception) {
           exception.printStackTrace();
        }
    }

}

