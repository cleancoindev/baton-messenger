package io.adappt.webserver.dao

import io.baton.repository.UserRepository
import org.springframework.stereotype.Component
import io.baton.user.User
import io.baton.user.UserRepository

@Component
class UserDao(
        private val userRepository: UserRepository
) {
    fun getUserById(id: String) =
            userRepository.findById(id)

    fun getUsersByName(name: String) =
            userRepository.findByNameLike(name)

    fun createUser(name: String) =
            userRepository.save(User(name = name))
}