package com.mycompany.iotwebapp.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * JPA Utility class to manage EntityManagerFactory and EntityManager instances.
 */
public class JPAUtil {
    
    private static final String PERSISTENCE_UNIT_NAME = "IoTWebAppPU";
    private static EntityManagerFactory emFactory;
    
    static {
        try {
            emFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        } catch (Exception e) {
            System.err.println("Error creating EntityManagerFactory: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get EntityManager instance.
     */
    public static EntityManager getEntityManager() {
        if (emFactory == null) {
            throw new IllegalStateException("EntityManagerFactory is not initialized");
        }
        return emFactory.createEntityManager();
    }
    
    /**
     * Close EntityManagerFactory.
     * Call this when shutting down the application.
     */
    public static void closeEntityManagerFactory() {
        if (emFactory != null && emFactory.isOpen()) {
            emFactory.close();
        }
    }
    
    /**
     * Check if EntityManagerFactory is open.
     */
    public static boolean isOpen() {
        return emFactory != null && emFactory.isOpen();
    }
}
