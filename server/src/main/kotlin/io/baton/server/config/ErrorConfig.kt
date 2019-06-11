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
package io.baton.server.config

import com.github.manosbatsis.scrudbeans.error.RestExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.HandlerExceptionResolver

/**
 * Automatically handle errors by creating a REST exception response,
 * courtesy of `scrudbeans-error`, see:
 * https://manosbatsis.github.io/scrudbeans/docs/restfulservices#error-responses
 */
class ErrorConfig {

    /**
     * Register our custom `HandlerExceptionResolver`
     */
    @Bean
    fun restExceptionHandler(): HandlerExceptionResolver {
        return RestExceptionHandler()
    }
}