package foo

/** Dummy file
 */
class SayHello {

    String salutation(String toWho) {
        "Hello ${toWho ?: 'World'}!"
    }
}
