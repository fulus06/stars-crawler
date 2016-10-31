package com.jotak.stars;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * @author Joel Takvorian
 */
public class RunningHost {

    private static String HOST_ID;

    public static String getHostId() {
        if (HOST_ID != null) {
            return HOST_ID;
        }
        try {
            HOST_ID = System.getenv("HOSTNAME");
            if (HOST_ID != null && !HOST_ID.isEmpty()) {
                return HOST_ID;
            }
        } catch (SecurityException e) {
            // Ignore; try next
        }
		try {
            HOST_ID = InetAddress.getLocalHost().getHostName();
            if (HOST_ID != null && !HOST_ID.isEmpty()) {
                return HOST_ID;
            }
		} catch (UnknownHostException e) {
            // Ignore; try next
		}
        HOST_ID = UUID.randomUUID().toString();
        return HOST_ID;
    }
}
