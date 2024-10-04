package server.circlehelp.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import server.circlehelp.api.response.StockItem

@RestController
class ShelvesController {

    @GetMapping("/api/shelves/get")
    fun getStocks(@RequestParam(value = "shelf") shelfNumber: Int,
                  @RequestParam(value = "row") rowNumber: Int,
                  @RequestParam(value = "compartment") compartmentNumber: Int) : StockItem {

    }

    @PutMapping("/api/shelves/automove")
    fun autoMove()
}