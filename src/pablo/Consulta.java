package pablo;

import java.net.*;

import es.uvigo.det.ro.simpledns.Message;
import es.uvigo.det.ro.simpledns.RRType;

public class Consulta {
	String modo;
	InetAddress servidordns;
	String rrtipo;
	String nombre;
	DatagramSocket socket;
	InetAddress resp;
	boolean noresp;
	
	public Consulta(String modo, InetAddress servidordns, String rrtipo, String nombre,	DatagramSocket socket) throws Exception{
		super();
		this.modo = modo;
		this.servidordns = servidordns;
		this.rrtipo = rrtipo;
		this.nombre = nombre;
		this.socket = socket;
		System.out.println(this);
		consulta();
	}
	@Override
	public String toString() {
		return "Q: " + modo + " " + servidordns.toString() + " " + rrtipo + " " + nombre;
	}
	public void consulta() throws Exception{
		InetAddress respcache = Cache.exists(nombre, rrtipo);
		if(respcache != null){
		resp = respcache;
		return;
		}
		Message mensaje = new Message(nombre,RRType.valueOf(rrtipo),false);
		byte[] msj = mensaje.toByteArray();
		DatagramPacket paquete = new DatagramPacket(msj, msj.length,servidordns, Dnsclient.puerto);
		socket.send(paquete);
		byte[] mensajerecibido = new byte[10000];
		DatagramPacket respuesta = new DatagramPacket(mensajerecibido, mensajerecibido.length);
		socket.receive(respuesta);
		byte[] rmsj = respuesta.getData();
		Message rmensaje = new Message(rmsj);
		if(rrtipo.equals("A")){
			RespuestaA r = new RespuestaA(rmensaje,servidordns,modo, nombre,socket,null);
			r.procesar();
			if(r.noresp) {
				noresp = true;
				return;
			}
			resp = r.getResp();
		}
		if(rrtipo.equals("NS")){
			RespuestaNS r = new RespuestaNS(rmensaje, servidordns, modo, nombre, socket);
			r.procesar();
			if(r.noresp) {
				noresp = true;
				return;
			}
		}
	}
	public InetAddress getResp() {
		return resp;
	}
	public void setResp(InetAddress resp) {
		this.resp = resp;
	}
}