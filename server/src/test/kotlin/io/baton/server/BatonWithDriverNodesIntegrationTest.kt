/*
 * 	Corbeans Yo! Cordapp: Sample/Template project for Corbeans,
 * 	see https://manosbatsis.github.io/corbeans
 *
 * 	Copyright (C) 2018 Manos Batsis.
 * 	Parts are Copyright 2016, R3 Limited.
 *
 * 	This library is free software; you can redistribute it and/or
 * 	modify it under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance 	with the License.
 * 	You may obtain a copy of the License at
 *
 * 	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing,
 * 	software distributed under the License is distributed on an
 * 	"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * 	KIND, either express or implied.  See the License for the
 * 	specific language governing permissions and limitations
 * 	under the License.
 */
package io.baton.server

import com.github.manosbatsis.corbeans.test.integration.WithDriverNodesIT
import io.baton.server.components.BatonService
import io.baton.server.components.YoService
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * A sample integration test using ad hoc explicit network per `withDriverNodes`.
 * Alternative to [YoImplicitNetworkIntegrationTest]
 */
@SpringBootTest(
        classes = arrayOf(Application::class),
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
class BatonWithDriverNodesIntegrationTest : WithDriverNodesIT() {

    companion object {
        private val logger = LoggerFactory.getLogger(BatonWithDriverNodesIntegrationTest::class.java)

    }

    // autowire all created services, mapped by name
    @Autowired
    lateinit var services: Map<String, BatonService>

    // autowire the same services individually
    @Autowired
    @Qualifier("partyANodeService")
    lateinit var partyAService: BatonService
    @Autowired
    @Qualifier("partyBNodeService")
    lateinit var partyBService: BatonService

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `Can inject services`() {
        logger.info("Can inject services: {}", services)
        assertNotNull(this.partyAService)
        assertNotNull(this.partyBService)
        assertTrue(this.services.keys.isNotEmpty())
    }

    @Test
    fun `Can send Yo!`() {
        withDriverNodes {
            logger.info("Can send Yo!")
            val yoResponse = this.restTemplate.getForObject("/api/yo/partyA/yo?target=partyB", Map::class.java)
            logger.debug("Yo response: {}", yoResponse)
            Assertions.assertTrue(yoResponse.keys.contains("message"))
        }
    }


}