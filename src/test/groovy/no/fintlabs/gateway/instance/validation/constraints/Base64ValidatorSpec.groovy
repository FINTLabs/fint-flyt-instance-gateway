package no.fintlabs.gateway.instance.validation.constraints


import spock.lang.Specification

class Base64ValidatorSpec extends Specification {

    Base64Validator validator = new Base64Validator()

    def "null value should be valid"() {
        when:
        boolean valid = validator.isValid(null, null)

        then:
        valid
    }

    def "empty value should be valid"() {
        when:
        boolean valid = validator.isValid("", null)

        then:
        valid
    }

    def "value with no padding characters should be valid"() {
        when:
        boolean valid = validator.isValid("AAAA", null)

        then:
        valid
    }

    def "value with one padding character should be valid"() {
        when:
        boolean valid = validator.isValid("AAA=", null)

        then:
        valid
    }

    def "value with two padding characters should be valid"() {
        when:
        boolean valid = validator.isValid("AA==", null)

        then:
        valid
    }

    def "value not divisible by 4 should be valid"() {
        when:
        boolean valid = validator.isValid("AAA", null)

        then:
        valid
    }

    def "blank value should be invalid"() {
        when:
        boolean valid = validator.isValid("    ", null)

        then:
        !valid
    }

    def "value with whitespace should be invalid"() {
        when:
        boolean valid = validator.isValid("AA A", null)

        then:
        !valid
    }

    def "value with non-base64 character should be invalid"() {
        when:
        boolean valid = validator.isValid("AA-A", null)

        then:
        !valid
    }

    def "value with three padding characters should be invalid"() {
        when:
        boolean valid = validator.isValid("A===", null)

        then:
        !valid
    }

}
