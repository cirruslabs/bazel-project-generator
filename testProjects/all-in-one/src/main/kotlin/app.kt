import com.google.gson.Gson
import foo.Foo

fun main(args: Array<String>) {
    println("Hello, ${Foo.fooName.name}")
    println(Gson().toJson(Foo))
}