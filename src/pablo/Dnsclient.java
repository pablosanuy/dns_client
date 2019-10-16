package pablo;

import java.io.*;
import java.net.*;
import java.util.*;

import es.uvigo.det.ro.simpledns.*;

public class Dnsclient {
	public static final int puerto = 53; // puerto para UDP y TCP de los
											// servidores DNS
	public static InetAddress ipservidor;

	public static void main(String[] args) throws Exception {
		String modo = args[0];
		ipservidor = InetAddress.getByName(args[1]);
		if(modo.equals("-t")){
			System.out.println("Solo se hayan implementadas las consultas UDP");
			modo = "-u";
		}
		if (modo.equals("-u")) {
			DatagramSocket socket = new DatagramSocket();
			Scanner input = new Scanner(System.in);
			while (input.hasNext()) {
				String linea = input.nextLine();
				String [] slinea = linea.split(" +");
				String rrtipo = slinea[0];
				String nombre = slinea[1];
				Consulta c = new Consulta("UDP", ipservidor, rrtipo, nombre,socket); //imprime con el formato correcto
			}
		}
	}
}
