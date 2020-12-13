package app

object ArgsTest {

    @JvmStatic
    fun main(args: Array<String>) {
        val p1 = args[0]
        val p2 = args[1]

        println("p1: $p1 | p2: $p2")
    }
}