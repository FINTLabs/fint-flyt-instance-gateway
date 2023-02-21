package no.fintlabs.gateway.instance.validation.constraints


import spock.lang.Specification

class Base64ValidatorSpec extends Specification {

    private final Base64Validator validator = new Base64Validator()

    def "valid Base64 string should pass validation"() {
        when:
        def result = validator.isValid("YW55IGNhcm5hbCBwbGVhc3Vy", null)

        then:
        result
    }

    def "invalid Base64 string should fail validation"() {
        when:
        def result = validator.isValid("not a valid base64 string", null)

        then:
        !result
    }

    def "null value should fail validation"() {
        when:
        def result = validator.isValid(null, null)

        then:
        !result
    }
}
