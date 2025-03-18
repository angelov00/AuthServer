package com.angelov00.server.repository.impl;

import com.angelov00.server.model.entity.User;
import com.angelov00.server.model.enums.Role;
import com.angelov00.server.repository.UserRepository;
import com.angelov00.server.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {

    private static final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    @Override
    public void save(User user) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
        }
    }

    @Override
    public void update(User user) {
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(user);
            tx.commit();
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        try (Session session = sessionFactory.openSession()) {
            User user = session.createQuery("from User where username = :username", User.class)
                    .setParameter("username", username)
                    .uniqueResult();
            return Optional.ofNullable(user);
        }
    }

    @Override
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

    @Override
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

    @Override
    public boolean exists(String username) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery("select count(u) from User u where u.username = :username", Long.class)
                    .setParameter("username", username)
                    .uniqueResult();
            return count != null && count > 0;
        }
    }

    @Override
    public void deleteUser(String username) {
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            int deletedRows = session.createMutationQuery("delete from User where username = :username")
                    .setParameter("username", username).executeUpdate();
            tx.commit();
        }
    }

    @Override
    public long adminCount() {
        try(Session session = sessionFactory.openSession()) {
            return session.createQuery("select count(u) from User u where u.role == 'ADMIN'", Long.class).uniqueResult();
        }
    }

    @Override
    public int getFailedLoginAttempts(String username) {
        try(Session session = sessionFactory.openSession()) {
            return session.createQuery("select u.failedLoggedAttempts from User u where u.username = :username", Integer.class)
                    .setParameter("username", username)
                    .uniqueResult();
        }
    }

    @Override
    public boolean isTimeouted(String username) {
        try(Session session = sessionFactory.openSession()) {
            LocalDateTime timeout = session.createQuery("select timeout from User u where u.username = :username ", LocalDateTime.class)
                    .setParameter("username", username)
                    .uniqueResult();

            if(timeout == null) return false;

            return timeout.isAfter(LocalDateTime.now());
        }
    }

    public void removeTimeout(String username) {
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            User user = session.createQuery("from User where username = :username", User.class)
                    .setParameter("username", username)
                    .uniqueResult();
            if(user == null) {
                return;
            }

            user.setFailedLoggedAttempts(0);
            user.setTimeout(null);

            session.merge(user);
            tx.commit();
        }
    }

    public void incrementFailedLoginAttempts(String username) {
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            User user = session.createQuery("from User where username = :username", User.class)
                    .setParameter("username", username)
                    .uniqueResult();
            if(user == null) {
                return;
            }

            user.setFailedLoggedAttempts(user.getFailedLoggedAttempts() + 1);
            user.setTimeout(null);

            session.merge(user);
            tx.commit();
        }
    }

    public void timeoutUser(String username, LocalDateTime localDateTime) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            User user = session.createQuery("from User where username = :username", User.class)
                    .setParameter("username", username)
                    .uniqueResult();
            if (user == null) {
                return;
            }

            user.setTimeout(localDateTime);
            session.merge(user);
            tx.commit();
        }
    }
}
