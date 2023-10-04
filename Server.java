//Importamos las librerias a usar
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class Server {
    private static final int PORT = 2099; // Declaramos el puerto donde se correra el servidor
    private static Vector<Socket> clients = new Vector<Socket>(); // Vector donde se guardaran los sockets de los clientes que esten conectados
    private static HashMap<Socket, String> userNames = new HashMap<>(); // HashMap para almacenar los nombres de usuario asociados a los sockets

    //Funcion que inicializa el servidor
    private ServerSocket startServer() {
        try {
            ServerSocket sSocket = new ServerSocket(PORT);
            return sSocket;
        } catch (IOException ioe) {
            System.err.println("No se puede abrir el puerto" + ioe.getMessage());
        }
        return null;
    }

    // Constructor de la case Server
    public Server() {
        ServerSocket welcomeSocket = startServer(); // Crea una instancia de ServerSocket
        System.out.println("Servidor iniciado en el puerto: " + PORT);
        System.out.println("Ctrl + C para detener");
        if (welcomeSocket != null) {
            while (true) {
                try {
                    Socket socket = welcomeSocket.accept(); // Esperamos conexiones y las aceptamos
                    clients.add(socket); //Agregamos el socket del cliente al vector 
                    System.out.println("Conexión iniciada");

                    chatThread hiloChat = new chatThread(socket, clients, userNames); //Creamos un hilo para que se comunique con el cliente
                    Thread thread = new Thread(hiloChat); // Iniciamos el hilo 
                    thread.start();
                } catch (IOException ioe) {
                    System.err.println("Hay un error en la creación de conexiones, se cerrará el servidor");
                    System.err.println(ioe.getMessage());
                }

            }
        }
    }

    //Funcion principal donde creamos el servidor 
    public static void main(String[] args) {
        new Server();
    }
}