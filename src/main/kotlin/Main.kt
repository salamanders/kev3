import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.runBlocking
import java.net.URL

fun main(args: Array<String>) {
    println("Hello World! (${args.joinToString()})")

    runBlocking {
        URL("kev3bot.firestore.com/points").toScript()
            .distinctUntilChanged()
            .collectIndexed { index, (x,y) ->
                println("$index: $x,$y")
            }
    }

}


