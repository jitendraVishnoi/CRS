package crs.paypal.ssl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.KeyManager;

public class Keys {
	
	private static final Map<String, KeyManager[]> KEYMANAGERS = Collections
			.synchronizedMap(new HashMap<String, KeyManager[]>());

	public static void registerKeys(String id, KeyManager[] keymanagers) {
		if (id == null) {
			throw new IllegalArgumentException("ID is null");
		}
		if (keymanagers == null) {
			throw new IllegalArgumentException("Key managers is null");
		}
		KEYMANAGERS.put(id, keymanagers);
	}

	public static void unregisterKeys(String id) {
		if (id == null) {
			throw new IllegalArgumentException("ID is null");
		}
		KEYMANAGERS.remove(id);
	}

	public static boolean containsKey(String id) {
		if (id == null) {
			throw new IllegalArgumentException("ID is null");
		}
		return KEYMANAGERS.containsKey(id);
	}

	public static KeyManager[] getKeyManagers(String id)
			throws IllegalStateException {
		if (id == null) {
			throw new IllegalArgumentException("ID is null");
		}
		return (KeyManager[]) KEYMANAGERS.get(id);
	}
}