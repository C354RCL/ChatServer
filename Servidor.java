import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
// import java.util.Vector;

public class Servidor {
    //Declaramos en que puerto estara escuchando el servidor
    private static final int PORT = 2099;
    //Creamos un vector para poner a los clientes
	// private static Vector<Socket> clients = new Vector<Socket>();
    private static Map<String, Socket> clientsMap = new HashMap<>();

    //Creamos la funcion que inicaliza el servidor
    private ServerSocket inicializaServer(){
        try{
            //Creamos un nuevo socket y le pasamos como parametro el puerto
            ServerSocket sSocket = new ServerSocket(PORT);            
            return sSocket;
        } catch (IOException ioe){ //Error que muestra si no es posible crear el socket en el puerto deseado
            System.err.println("No se puede abrir el puerto" + ioe.getMessage());
        }
        return null;
    }

    public Servidor(){
        //Inicialiazamos un socket
        ServerSocket welcomeSocket = inicializaServer();
        System.out.println("Servidor iniciado en el puerto: " + PORT);
        System.out.println("Ctrl + C para detener");
        if (welcomeSocket != null){
            while (true){
                try{
                    //Aceptamos las conexiones
                    Socket socket = welcomeSocket.accept();
                    //Añadimos el cliente al Map
                    clientsMap.put(socket.getInetAddress().getHostAddress(), socket);
					// clients.add(socket);
                    System.out.println("Conexión iniciada desde " + socket.getInetAddress());
                    //Creamos un hilo para el cliente
                    chatThread chatThread = new chatThread(socket, clientsMap);
                    Thread thread = new Thread(chatThread);
                    //Iniciamos el hilo
                    thread.start();
                } catch (IOException ioe){ //Cachamos la excepcion
                    System.err.println("Hay un error en la creación de conexiones, se cerrara el servidor");
                    System.err.println(ioe.getMessage());
                }
                
            }
        }

    }

    public static void main(String [] args){
        //Creamos el servidor
        new Servidor();
    }
}

