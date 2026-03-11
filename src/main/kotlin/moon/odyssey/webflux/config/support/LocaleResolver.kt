package moon.odyssey.webflux.config.support

import org.springframework.context.i18n.LocaleContext
import org.springframework.context.i18n.SimpleLocaleContext
import org.springframework.http.HttpHeaders
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.i18n.LocaleContextResolver
import java.util.Locale

class LocaleResolver : LocaleContextResolver {
    override fun resolveLocaleContext(exchange: ServerWebExchange): LocaleContext {
        val language = exchange.request.queryParams.getFirst("lang")
            ?: exchange.request.headers.getFirst(HttpHeaders.ACCEPT_LANGUAGE)

        val targetLocale = if (!language.isNullOrBlank()) {
            Locale.forLanguageTag(language.replace("_", "-"))
        } else {
            Locale.getDefault()
        }

        return SimpleLocaleContext(targetLocale)
    }

    override fun setLocaleContext(exchange: ServerWebExchange, localeContext: LocaleContext?) {
    }
}
