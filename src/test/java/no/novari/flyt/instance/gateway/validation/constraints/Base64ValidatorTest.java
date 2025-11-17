package no.novari.flyt.instance.gateway.validation.constraints;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Base64ValidatorTest {

    private final Base64Validator validator = new Base64Validator();

    @Test
    void nullValueShouldBeValid() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    void emptyValueShouldBeValid() {
        assertTrue(validator.isValid("", null));
    }

    @Test
    void valueWithNoPaddingCharactersShouldBeValid() {
        assertTrue(validator.isValid("AAAA", null));
    }

    @Test
    void valueWithOnePaddingCharacterShouldBeValid() {
        assertTrue(validator.isValid("AAA=", null));
    }

    @Test
    void valueWithTwoPaddingCharactersShouldBeValid() {
        assertTrue(validator.isValid("AA==", null));
    }

    @Test
    void valueNotDivisibleBy4ShouldBeValid() {
        assertTrue(validator.isValid("AAA", null));
    }

    @Test
    void blankValueShouldBeInvalid() {
        assertFalse(validator.isValid("    ", null));
    }

    @Test
    void valueWithWhitespaceShouldBeInvalid() {
        assertFalse(validator.isValid("AA A", null));
    }

    @Test
    void valueWithNonBase64CharacterShouldBeInvalid() {
        assertFalse(validator.isValid("AA-A", null));
    }

    @Test
    void valueWithThreePaddingCharactersShouldBeInvalid() {
        assertFalse(validator.isValid("A===", null));
    }

}
