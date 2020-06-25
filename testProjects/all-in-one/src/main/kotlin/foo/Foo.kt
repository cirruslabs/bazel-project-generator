package foo

import model.Name
import utils.TextUtils

object Foo {
    val fooName = Name(TextUtils.capitalize("name"))
}