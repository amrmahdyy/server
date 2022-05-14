package HTTPServer;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class ServerWorker extends Thread {


    private Socket socket;
    String header="";
    String clientFileData="";
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

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            outputStreamWriter.write(responseBuilder.toString());
            String fileInString=new String(fileInputStream.readAllBytes());
            outputStreamWriter.write(fileInString);
            outputStreamWriter.flush();
            outputStreamWriter.close();

            SocketServer.ACTIVE_WORKERS = SocketServer.ACTIVE_WORKERS - 1;
            socket.close();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


//    This function is used by POST function where it saves the resource

    void saveResource(){
        try{
            byte[]clientFileInBytes=clientFileData.getBytes();
            File file=new File(SocketServer.resourcesDirectory+requestedPath);
            FileOutputStream fileOutputStream=new FileOutputStream(file);
            fileOutputStream.write(clientFileInBytes);
        }
        catch(IOException ioException){
            System.out.println("Can't save resource on Server");
            ioException.printStackTrace();
        }
    }



    void completePOST(){
        try{
          InputStreamReader inputStreamReader=new InputStreamReader(socket.getInputStream());
          BufferedReader bufferedReader=new BufferedReader(inputStreamReader);

          Boolean isHeader=true;
          String line="";
          while((line=bufferedReader.readLine())!=null){
              if(isHeader)
                  header+=line+"\r\n";
              else
                  clientFileData+=line;
              if(line.isBlank())
                  isHeader=false;
          }

        }
        catch(IOException ioException){
            System.out.println("Error in reading client socket stream");

        }
    }

    @Override
    public void run() {
        super.run();
        try {

            InputStreamReader inStream = new InputStreamReader(socket.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inStream);
            String lineReader = bufferedReader.readLine();
            if(lineReader!=null) {
                System.out.println(lineReader);
                String[] lineComponents = lineReader.split(" ");

                httpMethod = lineComponents[0];
                requestedPath = lineComponents[1];

                if (httpMethod.equals("GET")) {
                    completeGET();
                } else {
                    completePOST();
                    saveResource();
                }
            }

        } catch (Exception exception) {
           exception.printStackTrace();
        }
    }

}

