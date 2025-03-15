package com.angelov00.server.repository;

import com.angelov00.server.model.entity.User;
import com.angelov00.server.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class UserRepository {


    private static final SessionFactory sessionFactory = HibernateUtil.getSessionFactory(); // Използване на HibernateUtil за сесията

    public void save(User user) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
        }
    }

    public User findByUsername(String username) {
        try (Session session = sessionFactory.openSession()) {
            String query = "from User where username = :username";
            return session.createQuery(query, User.class)
                    .setParameter("username", username)
                    .uniqueResult();
        }
    }

    public boolean exists(String username) {
        return this.findByUsername(username) != null;
    }
}
