package io.baton.user

import io.baton.user.User
import org.springframework.data.repository.CrudRepository

interface UserRepository : CrudRepository<User, String> {
    fun findByEmail(email: String): User

    fun findAll(userName: String): User?

    fun findByUsername(username: String): User?
}