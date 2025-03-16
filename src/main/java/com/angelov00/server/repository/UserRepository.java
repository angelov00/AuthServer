package com.angelov00.server.repository;

import com.angelov00.server.model.entity.User;
import com.angelov00.server.model.enums.Role;
import com.angelov00.server.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.Optional;

public class UserRepository {

    private static final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    public void save(User user) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
        }
    }

    public void update(User user) {
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(user);
            tx.commit();
        }
    }

    public Optional<User> findByUsername(String username) {
        try (Session session = sessionFactory.openSession()) {
            User user = session.createQuery("from User where username = :username", User.class)
                    .setParameter("username", username)
                    .uniqueResult();
            return Optional.ofNullable(user);
        }
    }

    public void promoteToAdmin(String username) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            User user = session.createQuery("from User where username = :username", User.class)
                    .setParameter("username", username)
                    .uniqueResult();
            if (user != null) {
                user.setRole(Role.ADMIN);
                session.merge(user);
            }
            tx.commit();
        }
    }

    public void demoteToUser(String username) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            User user = session.createQuery("from User where username = :username", User.class)
                    .setParameter("username", username)
                    .uniqueResult();
            if (user != null) {
                user.setRole(Role.USER);
                session.merge(user);
            }
            tx.commit();
        }
    }

    public boolean exists(String username) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery("select count(u) from User u where u.username = :username", Long.class)
                    .setParameter("username", username)
                    .uniqueResult();
            return count != null && count > 0;
        }
    }

    public void deleteUser(String username) {
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            int deletedRows = session.createMutationQuery("delete from User where username = :username")
                    .setParameter("username", username).executeUpdate();
            tx.commit();
        }
    }
}
