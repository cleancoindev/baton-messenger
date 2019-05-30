/**
 *   Copyright 2019, Dapps Incorporated.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.baton.dao
import org.springframework.stereotype.Component
import io.baton.user.User
import io.baton.user.UserRepository

@Component
class UserDao(
        private val userRepository: UserRepository
) {
    fun getUserById(userId: String) =
            userRepository.findById(userId)

    fun createUser(userId: String) =
            userRepository.save(User(userId = userId))
}