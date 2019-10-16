package pablo;

import java.net.*;
import java.util.*;

import es.uvigo.det.ro.simpledns.*;

public class Cache {
	public static HashMap<Integer, ArrayList<ResourceRecord>> cache = new HashMap<Integer, ArrayList<ResourceRecord>>();

	public static void insertar(ResourceRecord rr) {
		if (rr.getRRType().equals(RRType.A)) {
			AResourceRecord arr = (AResourceRecord) rr;
			int longitudDominio = arr.getDomain().toString().split(".").length;
			if (cache.containsKey(longitudDominio)) {
				cache.get(longitudDominio).add(arr);
			} else {
				ArrayList<ResourceRecord> a = new ArrayList<ResourceRecord>();
				a.add(arr);
				cache.put(longitudDominio, a);
			}
		}
		if (rr.getRRType().equals(RRType.NS)) {

		}
	}

	public static InetAddress exists(String s, String rrType) {
		if (rrType.equals("A")) {
			s = s + ".";
			int longitud = s.split(".").length;
			if (!cache.isEmpty()) {
				if (cache.containsKey(longitud)) {
					Iterator<ResourceRecord> it = cache.get(longitud).iterator();
					while (it.hasNext()) {
						ResourceRecord rr = it.next();
						if (rr.getRRType().equals(RRType.A)) {
							AResourceRecord arr = (AResourceRecord) rr;
							if (rr.getDomain().equals(s)) {
								System.out.println(
										"A: cache " + arr.getRRType() + " " + arr.getTTL() + " " + arr.getAddress());
								return arr.getAddress();
							}
						}
					}
				}
			}
		}
		return null;
	}

}
