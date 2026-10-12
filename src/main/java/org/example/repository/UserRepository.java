package org.example.repository;

import org.example.model.User;

import java.util.UUID;

public interface UserRepository extends CrudRepository<UUID, User> {
}
