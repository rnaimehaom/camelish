package foo

import spock.lang.Specification


class SayHelloSpecification extends Specification {

    def "Check our name is recognised"() {

        setup:
        def hello = new SayHello()
        def msg1 = hello.salutation('Spock')
        def msg2 = hello.salutation(null)

        expect:
        msg1 == 'Hello Spock!'
        msg2 == 'Hello World!'
    }
}
