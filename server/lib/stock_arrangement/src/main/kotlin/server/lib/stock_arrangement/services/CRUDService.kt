package server.lib.stock_arrangement.services

import java.net.URL

class CRUDService<T, ID>(private val baseUrl: String, private val cls: Class<T>) {

    fun <Return> call(urlString: String) : Return {
        val url = URL(urlString)
        val urlConnection = url.openConnection()
        urlConnection.content
    }

    fun getKey(id: ID) : T {
        val url = URL("${baseUrl}/api/${cls.simpleName}")
    }
}