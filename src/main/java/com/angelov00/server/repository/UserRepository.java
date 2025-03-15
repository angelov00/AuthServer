package com.angelov00.server.repository;

import com.angelov00.server.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class UserRepository {

    private static final SessionFactory sessionFactory = new Configuration()
            .configure()
            .addAnnotatedClass(User.class)
            .buildSessionFactory();

    public void save(User user) {
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
        }
    }

    public User findByUsername(String username) {
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            final String query = "from User where username = :username";
            return session.createQuery(query, User.class).setParameter("username", username).uniqueResult();
        }
    }

}
