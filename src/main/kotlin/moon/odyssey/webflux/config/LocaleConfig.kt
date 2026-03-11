package moon.odyssey.webflux.config

import moon.odyssey.webflux.config.support.LocaleResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.DelegatingWebFluxConfiguration
import org.springframework.web.server.i18n.LocaleContextResolver

@Configuration
class LocaleConfig : DelegatingWebFluxConfiguration() {
    override fun createLocaleContextResolver(): LocaleContextResolver {
        return LocaleResolver()
    }
}
