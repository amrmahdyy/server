package HTTPServer;
import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
public class ServerWorker extends Thread {
    byte[]clientFileInBytes;
    private Socket socket;
    String header="";
    private String httpMethod, requestedPath;
    private String notFoundPath = "/404.html";
    private HashMap<String, String> httpHeaders = new HashMap<String, String>();
    private StringBuilder httpBody = new StringBuilder();

    ServerWorker(Socket socket) {
        this.socket = socket;
    }

    private void completeGET() throws IOException {
        if (requestedPath.equals("/"))
            requestedPath = "/index.html";

        Boolean resourceExists = Extensions.resourcesExists(requestedPath);
        GET(resourceExists);
        this.socket.close();
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

            OutputStream outStream = socket.getOutputStream();
            outStream.write(responseBuilder.toString().getBytes());
            byte[] fileBytesArr=IOUtils.toByteArray(fileInputStream);
            outStream.write(fileBytesArr);
            outStream.flush();
            outStream.close();

            SocketServer.ACTIVE_WORKERS = SocketServer.ACTIVE_WORKERS - 1;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


//    This function is used by POST function where it saves the resource

    void saveResource(){
        try{
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
        header="";
        clientFileInBytes=null;
        try{
            InputStream inputStream=socket.getInputStream();
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            int chr;
            while((chr=inputStream.read())!=-1){
                baos.write(chr);
            }
//            System.out.println(baos.size());
            byte[]responseInBytes= baos.toByteArray();
            String clientResponse=new String(responseInBytes,Charset.forName("UTF-8"));
//            System.out.println(clientResponse);
            int startIndex=clientResponse.indexOf("\n\r");
            if(startIndex>0) {
                header += clientResponse.substring(0, startIndex);
                int padding = 3;
                clientFileInBytes = new byte[responseInBytes.length - startIndex - padding];
                for (int i = 0; i < clientFileInBytes.length; i++) {
                    clientFileInBytes[i] = responseInBytes[i + startIndex + padding];
                }
//                System.out.println("File size from client : "+clientFileInBytes.length);
            }
//            System.out.println(header);
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
//                    Middleware.Middleware().applyCaching(socket,requestedPath);
                    completeGET();
                } else {
                    completePOST();
                    if(clientFileInBytes!=null)
                    saveResource();
                }
            }

        } catch (Exception exception) {
           exception.printStackTrace();
        }
    }

}

