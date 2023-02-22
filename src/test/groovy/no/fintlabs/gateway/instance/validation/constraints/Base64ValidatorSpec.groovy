package no.fintlabs.gateway.instance.validation.constraints

import spock.lang.Specification

class Base64ValidatorSpec extends Specification {

    Base64Validator validator = new Base64Validator()

    def "empty value should be invalid"() {
        when:
        boolean isValid = validator.isValid("", null)

        then:
        !isValid
    }

    def "null value should be invalid"() {
        when:
        boolean isValid = validator.isValid(null, null)

        then:
        !isValid
    }

    def "blank value should be invalid"() {
        when:
        boolean isValid = validator.isValid("   ", null)

        then:
        !isValid
    }

    def "invalid base64 value should be invalid"() {
        when:
        boolean isValid = validator.isValid("invalid-base64", null)

        then:
        !isValid
    }

    def "valid base64 value with one padding char should be valid"() {
        when:
        boolean isValid = validator.isValid("Zm9vYg==", null)

        then:
        isValid
    }

    def "valid base64 value with two padding chars should be valid"() {
        when:
        boolean isValid = validator.isValid("Zm9vYg===", null)

        then:
        !isValid
    }

    def "valid base64 value without padding should be invalid"() {
        when:
        boolean isValid = validator.isValid("Zm9vYg", null)

        then:
        !isValid
    }

    def "valid base64 value with more than two padding chars should be invalid"() {
        when:
        boolean isValid = validator.isValid("Zm9vYg====", null)

        then:
        !isValid
    }

}
