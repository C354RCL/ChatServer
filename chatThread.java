import java.io.*;
import java.net.*;
import java.util.*;


public class chatThread implements Runnable{
	//Declaracion de variables globales
	private Map<String, Socket> clientsMap; //Mapa que guarda los sockets de los clientes conectados
	private Socket socket; //Socket del cliente actual que el hilo esta manejando
	private DataInputStream netIn; //Flujo de entrada para recibir los mensajes del cliente
	private DataOutputStream netOut; //Flujo de datos para enviar archivos
	public chatThread(Socket socket, Map<String, Socket> clientsMap){
		this.clientsMap = clientsMap;
		this.socket = socket;
	}
	
	//Metodo que start el flujo de entrada para recibir datos del cliente actual
	public void start(){
		try{
			netIn = new DataInputStream(socket.getInputStream());
		}catch (IOException ioe){
			System.err.println("Problema al crear el flujo start()");
			System.err.println(ioe.getMessage());
		}
	}

	//Metodo que envia el mensaje a todos los clientes conectados
	public void sendMsg(String msg){
		// DataOutputStream netOut; 
		try{
			//Recorremos toda la lista de clientes y envia el mensaje 
			for (Socket socketTmp : clientsMap.values()){			
				netOut = new DataOutputStream(socketTmp.getOutputStream());
				netOut.writeUTF(msg);
			}
		}catch (IOException ioe){ //Cachamos el error
			System.err.println("Problema: NO se pueden crear flujos enviaMensaje()");
			System.err.println(ioe.getMessage());
		}
	}

	//Metodo que envia archivos
	public void sendFile(String fileName) throws IOException{
		try{
			//Creamos un nuevo objeto tipo File
			File file = new File(fileName);
			//Comprobamos si existe el archivo
			if(file.exists()){
				int totalLen = (int) file.length();
				netOut.writeUTF(fileName);
				//Creamos los flujos de datos
				BufferedInputStream bis;
				BufferedOutputStream bos;
				bis = new BufferedInputStream(new FileInputStream(fileName));
				bos = new BufferedOutputStream(socket.getOutputStream());
				//Creamos un arreglo de bytes
				byte[] buffer = new byte[1024];
				int bytesRead = 0;
				int length = 1024;
				int rest = 0;
				while(true){
					rest = totalLen - bytesRead;
					if(rest > length) {
						int read = bis.read(buffer, 0, length);
						bos.write(buffer, 0, read);
					} else {
						int read = bis.read(buffer, 0, rest);
						bos.write(buffer, 0 ,read);
						bos.flush();
						bis.close();
						return;
					}
					bytesRead += 1024;
				} 
			} else {
				String res = "No se pudo enviar el archivo";
				netOut.writeUTF(res);
			}
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}
	
	//Metodo que inicia un hilo para manejar la comunicacion con el cliente
	public void run(){
		//Llamamos al metodo start para el flujo de entrada
		start();
		try{
			while(true){
				String msg = netIn.readUTF();
				//Se dividen los mensajes en tokens
				StringTokenizer st = new StringTokenizer(msg, "^");
				if (st.countTokens() >= 3){
					String command = st.nextToken();
					//Si el comando es 'm' se envia el mensaje
					if (command.equalsIgnoreCase("m")){
						sendMsg(msg);
					} else if (command.equalsIgnoreCase("request_ip_port")) {
						// Cliente solicita dirección IP y puerto de otro cliente
                        String targetIP = st.nextToken(); // IP del cliente destino
                        Socket targetSocket = clientsMap.get(targetIP);
                        if (targetSocket != null) {
                            // Envía la dirección IP y puerto del cliente destino al cliente solicitante
                            String response = "peer_info^" + targetSocket.getInetAddress().getHostAddress() + "^" + targetSocket.getPort();
                            netOut.writeUTF(response);
                        }
					}
					// else {
					// 	String res = "m^Server@";
					// 	InetAddress ip = InetAddress.getLocalHost();
					// 	res += ip.getHostAddress() + "^-^";
					// 	String aliasIP = st.nextToken();
					// 	res += aliasIP.substring(0, aliasIP.indexOf("@")); //Obtiene el alias
					// 	String ipCliente = aliasIP.substring(aliasIP.indexOf("@") + 1);
					// 	//Si el comando es igual a 'j' se manda mensaje de que alguien se unio
					// 	if (command.equalsIgnoreCase("j")){
					// 		res += " joined from " + ipCliente + "^";
					// 		sendMsg(res);
					// 	} else { //Si el cliente se desconecta
					// 		res += " parted from " + ipCliente + "^";
					// 		clientsMap.remove(socket);
					// 		sendMsg(res);
					// 		return;
					// 	}
						
					// }
				}
			}			
		}catch (IOException ioe){
			System.out.println("problema en run()");
			System.out.println(ioe.getMessage());
			
		}
	}

}

