// Importamos las librerias a usar 
import java.io.*;
import java.net.*;
import java.util.*;

public class chatThread implements Runnable {
    
    // Declaramos las variables 
    private Vector<Socket> clients; // Vector que almacena los sockets de los clientes
    private Socket socket; // Socket con el que se esta comunicando el hilo
    private InputStream inputStream; // Flujo de datos para recibir los datos 
    private OutputStream outputStream; // Flujo de datos para enviar los datos 
    private String userName = ""; // Nombre de usuario que se mostrara en el chat

    // Cobstructor que toma un socket y un vector de sockets para inicializar las variables 
    public chatThread(Socket socket, Vector<Socket> clients) {
        this.clients = clients;
        this.socket = socket;
    }

    // Metodo que obtiene los flujos de entrada y salida del socket del cliente
    public void initialize() {
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException ioe) {
            System.err.println("Problema al crear los flujos initialize()");
            System.err.println(ioe.getMessage());
        }
    }

    // Metodo que se usa para enviar un mensaje a todos los clientes conectados que toma un arreglo de bytes como parametro
    public void sendMessage(byte[] msg) {
        try {
            for (Socket socketTmp : clients) { // Recorremos el arreglo de sockets
                OutputStream clientOutput = socketTmp.getOutputStream(); // Creamos un flujo de datos para enviar el mensaje
                clientOutput.write(msg); // Enviamos el mensaje
                clientOutput.flush();
            }
        } catch (IOException ioe) {
            System.err.println("Problema: No se pueden enviar mensajes");
            System.err.println(ioe.getMessage());
        }
    }

    // Metodo que obtiene el nombre del usuario
    public String getUserName(String message){
        StringTokenizer tokens = new StringTokenizer(message, "^"); //Separamos el mensaje por tokens
        String command = tokens.nextToken();
        String msg = tokens.nextToken();
        if(command.equalsIgnoreCase("u") && userName.isEmpty()){
            userName = msg; // Asignamos valor a userName
        }
        return userName;
    }

    // Metodo que implementa la logica del hilo
    public void run() {
        initialize();  // Llamamos el metodo para preparar los flujos de entrada-salida
        try {
            while (socket.isConnected()) { 
				InputStream inputStream = socket.getInputStream(); // Leemos los datos del cliente a traves de un arreglo de bytes
				byte[] data = new byte[1024];
				inputStream.read(data);

				String message = new String(data); // Convertimos el arreglo de bytes en un String
                System.out.println(message);

                StringTokenizer tokens = new StringTokenizer(message, "^"); //Separamos el mensaje por tokens 
                String command = tokens.nextToken(); // Obtenemos token que contiene el comando
                userName = getUserName(message); // Llamamos el metodo para obtener el nombre de usuario

                if(command.equalsIgnoreCase("m")) {
                    String msg = tokens.nextToken(); // Obtenemos el token que contiene el mensaje
                    String res = userName + ": " + msg;
                    byte [] resArray = res.getBytes(); // Convertimos a res en un arreglo de bytes para poder enviarlo
                    sendMessage(resArray); // Llamamos el metodo para mandar el mensaje a todos los clientes
                } 

            }
        } catch (IOException ioe) {
            System.out.println("Problema en run()");
            System.out.println(ioe.getMessage());
        } finally { // Aseguramos que el socket se cierre correctamente y se elimina de la lista de clientes 
            try { 
                socket.close(); // Cerramos el socket
                clients.remove(socket); // Movemos el socket del vector
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
