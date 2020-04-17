package moon.odyssey.webflux.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.DelegatingWebFluxConfiguration;
import org.springframework.web.server.i18n.LocaleContextResolver;

import moon.odyssey.webflux.config.support.LocaleResolver;

@Configuration
public class LocaleConfig extends DelegatingWebFluxConfiguration {

    @Override
    protected LocaleContextResolver createLocaleContextResolver() {
        return new LocaleResolver();
    }

}
