package com.github.manosbatsis.corbeans.corda.webserver.config

import net.corda.client.jackson.JacksonSupport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jackson.JsonComponentModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter

/**
 * Configure Corda RPC ObjectMapper for Jackson
 */
@Configuration
class JacksonConfig {

    /** Force Spring/Jackson to use the provided Corda ObjectMapper for serialization */
    @Bean
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    fun mappingJackson2HttpMessageConverter(
            @Autowired jsonComponentModule: JsonComponentModule
    ): MappingJackson2HttpMessageConverter {
        var mapper = JacksonSupport.createNonRpcMapper()
        mapper.registerModule(jsonComponentModule)

        val converter = MappingJackson2HttpMessageConverter()
        converter.objectMapper = mapper
        return converter
    }
}