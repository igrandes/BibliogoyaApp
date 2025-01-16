package dao.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import dao.entity.*;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();
    
    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration().configure();
            
            // Agregar las clases de entidad
            configuration.addAnnotatedClass(Libro.class);
            configuration.addAnnotatedClass(Usuario.class);
            configuration.addAnnotatedClass(Prestamo.class);
            configuration.addAnnotatedClass(Reserva.class);
            
            return configuration.buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Error al crear SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }
    
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    
    public static void shutdown() {
        getSessionFactory().close();
    }
}