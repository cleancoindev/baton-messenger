package io.baton.user

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.GenerationType
import javax.persistence.GeneratedValue

@Entity
class User(
        @Id @GeneratedValue(strategy = GenerationType.AUTO)
        var userId: String = "",
        var orgId: String = "",
        var username: String = "",
        var password: String = "",
        var firstName: String = "",
        var lastName: String = "",
        var email: String = "",
        var title: String = "",
        var organization: String = ""

)