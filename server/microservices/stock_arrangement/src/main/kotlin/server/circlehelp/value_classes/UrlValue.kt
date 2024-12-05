package server.circlehelp.value_classes

import org.springframework.validation.ValidationUtils
import server.circlehelp.api.serializers.matchesUrl
import java.util.regex.Pattern

@JvmInline
value class UrlValue(val url: String) {

    init {
        if (Pattern.matches(matchesUrl, url).not())
            throw IllegalArgumentException("Input is not an url.")
    }
}