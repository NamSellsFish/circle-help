package server.circlehelp.services

import org.springframework.stereotype.Service
import server.circlehelp.api.complement
import java.util.LinkedList
import java.util.stream.Collectors.reducing
import java.util.stream.Stream
import java.util.stream.Stream.concat

/**
 * All operations related to the definition of shelves depends on this class.
 */
@Service
class Blocs {

    final val map2D = (0..99).chunked(10)

    final val bloc1 = map2D.map { it.subList(0, 3) }

    final val bloc2 = map2D.subList(1, 3).map { it.subList(4, 7) }

    final val bloc3 = map2D.subList(6, 9).map { it.subList(4, 7) }

    final val bloc4 = map2D.subList(0, 3).map { it.subList(8, 10) }

    final val bloc5 = map2D.subList(6, 10).map { it.subList(8, 10) }

    final val blocs = listOf(bloc1, bloc2, bloc3, bloc4, bloc5)

    final val sequence: List<Int> = blocs
        .flatten()
        .map { it.stream() }
        .flatten()
        .toList()

    final val sequenceSet = sequence.toHashSet()

    /*
    final val blocSets: List<HashSet<Int>> = blocs.map {
        Iterable {
            it.stream()
                .map { it.stream() }
                .collect(reducing { a, b -> concat(a, b) }).get().iterator()
        }.toHashSet()
    }.toList()

     */

    /**
     * 0-based index
     */
    final val compartmentIndexBlocMap : Map<Int, Int>
    init {
        val map = HashMap<Int, Int>()

        for (bloc in blocs.withIndex()) {
            for (index in bloc.value.flatten())
                map[bloc.index] = index
        }

        compartmentIndexBlocMap = map
    }

    private fun <T> Collection<Collection<T>>.flatten() : Stream<T> {
        return this.stream().map { it.stream() }.flatten()
    }

    private fun <T> Stream<Stream<T>>.flatten() : Stream<T> {
        return this.collect(reducing { a, b -> concat(a, b) }).get()
    }

    /**
     * 0-based index
     */
    final val compartmentIndexShelfMap : Map<Int, Int>
    final val shelfCompartmentsMap: List<List<Int>>
    init {
        val map = HashMap<Int, Int>()
        val shelfCompartmentListTemp = LinkedList<List<Int>>()

        val bloc1Shelves = bloc1.flatten().toList().chunked(6)
        val bloc2Shelves = bloc2.flatten().toList().chunked(6)
        val bloc3Shelves = bloc3.flatten().toList().chunked(3)
        val bloc4Shelves = bloc4.flatten().toList().chunked(6)
        val bloc5Shelves = bloc5.flatten().toList().chunked(8)

        val shelves = listOf(
            bloc1Shelves, bloc2Shelves, bloc3Shelves, bloc4Shelves, bloc5Shelves)
            .flatten().toList().withIndex()

        for ((shelfNo, shelfCompartmentIndices) in shelves) {
            shelfCompartmentListTemp.add(shelfCompartmentIndices)
            for (index in shelfCompartmentIndices) {
                map[index] = shelfNo
            }
        }

        shelfCompartmentsMap = shelfCompartmentListTemp
        compartmentIndexShelfMap = map
    }

    fun shelfNoToCharString(shelfNo: Int): String {
        return ('A'.code + shelfNo).toChar().toString()
    }

    fun getShelfCharIndex(compartmentNo: Int): String {
        return shelfNoToCharString(compartmentIndexShelfMap[compartmentNo]!!)
    }

    fun printMap(transform: (Int) -> String, voidText: String) : String {
        return map2D.joinToString("\n") {
            it.joinToString {
                if (sequenceSet.contains(it).complement())
                    voidText
                else
                    transform(it)
            }
        }
    }
}