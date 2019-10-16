package pablo;

import java.util.*;
import java.io.*;
import java.net.*;

import es.uvigo.det.ro.simpledns.*;

public class RespuestaA {

	Message mensaje;
	List<ResourceRecord> answers;
	List<ResourceRecord> authorities;
	List<ResourceRecord> additionals;
	InetAddress ipservidor;
	String modo;
	String nombre;
	DatagramSocket socket;
	InetAddress resp;
	boolean noresp;

	public RespuestaA(Message mensaje, InetAddress ipservidor, String modo, String nombre, DatagramSocket socket,
			InetAddress lastresp) {
		this.mensaje = mensaje;
		this.ipservidor = ipservidor;
		this.modo = modo;
		this.nombre = nombre;
		this.socket = socket;
		this.resp = lastresp;
	}

	public void procesar() throws Exception {
		boolean salir = false; // booleano para salir del bucle de authorities y
								// additionals
		answers = mensaje.getAnswers();
		authorities = mensaje.getNameServers();
		additionals = mensaje.getAdditonalRecords();
		if (!answers.isEmpty()) {
			ResourceRecord ans = answers.get(0);
			if (answers.get(0).getRRType().equals(RRType.A)) {
				AResourceRecord answ = (AResourceRecord) ans;
				Cache.insertar(ans);
				resp = answ.getAddress();
				noresp = false;
				System.out.println(
						"A: " + ipservidor + " " + answ.getRRType() + " " + answ.getTTL() + " " + answ.getAddress()); // Respuesta final
			} else {
				if (ans.getRRType().equals(RRType.CNAME)) {
					System.out.println("A: " + ipservidor + " CNAME");
				}
			}

		} else {
			if (authorities.isEmpty()) {
				System.out.println("Sin Respuesta");
				noresp = true;
				return;
			} else {
				for (ResourceRecord aut : authorities) {
					if (aut.getRRType().equals(RRType.NS)) {
						NSResourceRecord auth = (NSResourceRecord) aut;
						DomainName dominio = auth.getNS();
						boolean authadditional = true; // booleano para el caso
														// en el que ninguna
														// additional coincide
														// con esta authority
						if (!additionals.isEmpty()) {
							for (ResourceRecord i : additionals) {
								if (i.getRRType().equals(RRType.A)) {
									AResourceRecord j = (AResourceRecord) i;
									if (j.getDomain().equals(dominio)) {
										System.out.println("A: " + ipservidor + " " + auth.getRRType() + " "
												+ auth.getTTL() + " " + auth.getNS());
										System.out.println("A: " + ipservidor + " " + j.getRRType() + " " + j.getTTL()
												+ " " + j.getAddress());
										authadditional = false;
										Consulta k = new Consulta(modo, j.getAddress(), "A", nombre, socket); // Nueva
																												// consulta
																												// al
																												// servidor
																												// de
																												// la
																												// authority
										if (k.noresp) { // Si no hay respuesta,
														// se acabó la consulta
											this.noresp = true;
											return;
										}
										resp = k.getResp();
										this.noresp = false;
										salir = true;
										break;
									}
								}
							}
							if (salir)
								break;
							if (authadditional) { // ninguna additional
													// coincide con esta
													// authority
								System.out.println("A: " + ipservidor + " " + auth.getRRType() + " " + auth.getTTL()
										+ " " + auth.getNS());
								System.out.println(
										"El siguiente dominio del campo authority no contiene la ip en el campo additional :"
												+ auth.getNS());
								Consulta c = new Consulta(modo, Dnsclient.ipservidor, "A", auth.getNS().toString(),
										socket);
								if (c.noresp) {
									this.noresp = true;
									return;
								} else {
									if (!c.getResp().equals(null)) {
										InetAddress solucion = c.getResp();
										System.out.println("Hemos obtenido la ip de " + auth.getNS() + " : "
												+ solucion.toString());
										Consulta m = new Consulta(modo, solucion, "A", nombre, socket);
										if (m.noresp) {
											this.noresp = true;
											return;
										}
										resp = m.getResp();
										this.noresp = false;
									}
								}
								break;
							}

						} else { // No hay additionals
							System.out.println("A: " + ipservidor + " " + auth.getRRType() + " " + auth.getTTL() + " "
									+ auth.getNS());
							System.out.println(
									"El siguiente dominio del campo authority no contiene la ip en el campo additional :"
											+ auth.getNS());
							Consulta c = new Consulta(modo, Dnsclient.ipservidor, "A", auth.getNS().toString(), socket);
							if (c.noresp) {
								this.noresp = true;
								return;
							} else {
								if (!c.getResp().equals(null)) {
									InetAddress solucion = c.getResp();
									System.out.println(
											"Hemos obtenido la ip de " + auth.getNS() + " : " + solucion.toString());
									Consulta m = new Consulta(modo, solucion, "A", nombre, socket);
									if (m.noresp) {
										this.noresp = true;
										return;
									}
									resp = m.getResp();
									this.noresp = false;
								}
							}
							break;
						}
					}
				}
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
