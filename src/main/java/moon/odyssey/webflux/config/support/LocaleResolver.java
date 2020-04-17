package moon.odyssey.webflux.config.support;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.i18n.LocaleContextResolver;

import java.util.Locale;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocaleResolver implements LocaleContextResolver {

    @Override
    public LocaleContext resolveLocaleContext(ServerWebExchange exchange) {

        String language = exchange.getRequest().getQueryParams().getFirst("lang") != null
                        ? exchange.getRequest().getQueryParams().getFirst("lang")
                          : exchange.getRequest().getHeaders().getFirst(HttpHeaders.ACCEPT_LANGUAGE);

        Locale targetLocale = language != null && !language.isEmpty()
                              ? Locale.forLanguageTag(language.replaceAll("_", "-"))
                              : Locale.getDefault();

        return new SimpleLocaleContext(targetLocale);
    }

    @Override
    public void setLocaleContext(ServerWebExchange exchange, LocaleContext localeContext) {

    }
}
