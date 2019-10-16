package pablo;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

import es.uvigo.det.ro.simpledns.AResourceRecord;
import es.uvigo.det.ro.simpledns.DomainName;
import es.uvigo.det.ro.simpledns.Message;
import es.uvigo.det.ro.simpledns.NSResourceRecord;
import es.uvigo.det.ro.simpledns.RRType;
import es.uvigo.det.ro.simpledns.ResourceRecord;

public class RespuestaNS {
	Message mensaje;
	List<ResourceRecord> answers;
	List<ResourceRecord> authorities;
	List<ResourceRecord> additionals;
	InetAddress ipservidor;
	String modo;
	String nombre;
	DatagramSocket socket;
	boolean noresp;

	public RespuestaNS(Message mensaje, InetAddress ipservidor, String modo, String nombre, DatagramSocket socket) {
		this.mensaje = mensaje;
		this.ipservidor = ipservidor;
		this.modo = modo;
		this.nombre = nombre;
		this.socket = socket;
	}

	public void procesar() throws Exception {
		boolean salir = false;
		answers = mensaje.getAnswers();
		authorities = mensaje.getNameServers();
		additionals = mensaje.getAdditonalRecords();
		if (!answers.isEmpty()) {
			NSResourceRecord ans = (NSResourceRecord) answers.get(0);
			System.out.println("A: " + ipservidor + " " + ans.getRRType() + " " + ans.getTTL() + " " + ans.getNS());
		} else {
			if (authorities.isEmpty()) {
				System.out.println("Sin Respuesta");
				noresp = true;
				return;
			}
			else {
				for (ResourceRecord aut : authorities) {
					if (aut.getRRType().equals(RRType.NS)) {
						NSResourceRecord auth = (NSResourceRecord) aut;
						DomainName dominio = auth.getNS();
						DomainName aux;
						if (!additionals.isEmpty()) {
							for (ResourceRecord i : additionals) {
								if (i.getRRType().equals(RRType.A)) {
									AResourceRecord j = (AResourceRecord) i;
									if (j.getDomain().equals(dominio)) {
										System.out.println("A: " + ipservidor + " " + auth.getRRType() + " "
												+ auth.getTTL() + " " + auth.getNS());
										System.out.println("A: " + ipservidor + " " + j.getRRType() + " " + j.getTTL()
												+ " " + j.getAddress());
										Consulta k = new Consulta(modo, j.getAddress(),"NS", nombre, socket);
										if(k.noresp)return;
										salir = true;
										break;
									}
								}
							}
							if (salir)
								break;
						} else {
							System.out.println("El siguiente dominio del campo authority no contiene la ip en el campo additional :" + auth.getNS());
							Consulta c = new Consulta(modo, Dnsclient.ipservidor, "A", auth.getNS().toString(), socket);
							if(c.noresp)return;
							if(!c.getResp().equals(null)) {
								InetAddress solucion = c.getResp();
								System.out.println(
										"Hemos obtenido la ip de " + auth.getNS() + " : " + solucion.toString());
								Consulta m = new Consulta(modo, solucion, "NS", nombre, socket);
								if(m.noresp)return;
							}
							break;
						}
					}
				}
			}
		}
	}
}
