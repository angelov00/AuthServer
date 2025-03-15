package com.angelov00.server.util;

import com.angelov00.server.model.entity.User;
import com.angelov00.server.model.enums.Role;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private final static SessionFactory sessionFactory;

    static {
        try {
            sessionFactory = new Configuration()
                    .configure("hibernate.cfg.xml")
                    .addAnnotatedClass(User.class)
                    .buildSessionFactory();

            initDatabase();
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Failed to create SessionFactory");
        }
    }

    private static void initDatabase() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            String query = "from User where username = 'admin'";
            User superUser = session.createQuery(query, User.class).uniqueResult();

            if (superUser == null) {
                superUser = new User();
                superUser.setUsername("admin");
                superUser.setPassword(PasswordEncoder.hashPassword("admin_password"));
                superUser.setEmail("admin@angelov00.com");
                superUser.setFirstName("super");
                superUser.setLastName("admin");
                superUser.setRole(Role.ADMIN);
                session.persist(superUser);
            }

            session.getTransaction().commit();
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static Session getSession() {
        return sessionFactory.getCurrentSession();
    }
}
