// Importamos las librerias a usar 
import java.io.*;
import java.net.*;
import java.util.*;

public class chatThread implements Runnable {
    
    // Declaramos las variables 
    private Vector<Socket> clients; // Vector que almacena los sockets de los clientes
    private Socket socket; // Socket con el que se está comunicando el hilo
    private InputStream inputStream; // Flujo de datos para recibir los datos 
    private OutputStream outputStream; // Flujo de datos para enviar los datos 
    private HashMap<Socket, String> userNames = new HashMap<>(); // HashMap para almacenar los nombres de usuario asociados a los sockets

    // Constructor que toma un socket y un vector de sockets para inicializar las variables 
    public chatThread(Socket socket, Vector<Socket> clients, HashMap<Socket, String> userNames) {
        this.clients = clients;
        this.socket = socket;
        this.userNames = userNames;
    }

    // Método que obtiene los flujos de entrada y salida del socket del cliente
    public void initialize() {
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException ioe) {
            System.err.println("Problema al crear los flujos initialize()");
            System.err.println(ioe.getMessage());
        }
    }

    // Método que se usa para enviar un mensaje a todos los clientes conectados que toma un arreglo de bytes como parámetro
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

    // Método que obtiene el nombre del usuario
    public String getUserName(String message) {
        StringTokenizer tokens = new StringTokenizer(message, "^"); // Separamos el mensaje por tokens
        String command = tokens.nextToken();
        String msg = tokens.nextToken();
        if (command.equalsIgnoreCase("u") && userNames.get(socket) == null) {
            userNames.put(socket, msg); // Asignamos valor a userName en el HashMap
        }
        return userNames.get(socket);
    }

    // Método que implementa la lógica del hilo
    public void run() {
        initialize();  // Llamamos el método para preparar los flujos de entrada-salida
        String userName = "";
        try {
            while (socket.isConnected()) { 
                InputStream inputStream = socket.getInputStream(); // Leemos los datos del cliente a través de un arreglo de bytes
                byte[] data = new byte[1024];
                int bytesRead = inputStream.read(data); // Leemos los datos y obtenemos la cantidad de bytes leídos
    
                if (bytesRead == -1) {
                    // El cliente se desconectó, salimos del bucle
                    break;
                }
    
                String message = new String(data, 0, bytesRead); // Convertimos el arreglo de bytes en un String
    
                StringTokenizer tokens = new StringTokenizer(message, "^"); // Separamos el mensaje por tokens 
                String command = tokens.nextToken(); // Obtenemos token que contiene el comando

                if (command.equalsIgnoreCase("u")) {
                    userName = getUserName(message); // Llamamos el método para obtener el nombre de usuario
                    int port = socket.getPort(); // Obtenemos el puerto que usa el socket
                    String portString = String.valueOf(port);
                    String clientIfo = userName+"^"+portString; // Lo concatetamos al nombre de usuario
                    userNames.put(socket, clientIfo); // Asociamos el nombre de usuario al socket en el HashMap
                }

                // Imprimir los valores en clientInfo
                System.out.println("Clientes conectados: " + userNames.values());

                if (command.equalsIgnoreCase("m")) {
                    String msg = tokens.nextToken(); // Obtenemos el token que contiene el mensaje
                    String formattedMessage = userName + ": " + msg.trim();
                    byte[] resArray = formattedMessage.getBytes(); // Convertimos a res en un arreglo de bytes para poder enviarlo
                    sendMessage(resArray); // Llamamos el método para mandar el mensaje a todos los clientes
                }
            }
        } catch (IOException ioe) {
            System.out.println("Problema en run()");
            System.out.println(ioe.getMessage());
        } finally { // Aseguramos que el socket se cierre correctamente y se elimina de la lista de clientes 
            try { 
                socket.close(); // Cerramos el socket
                clients.remove(socket); // Movemos el socket del vector
                userNames.remove(socket); // Removemos el nombre de usuario asociado al socket
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


