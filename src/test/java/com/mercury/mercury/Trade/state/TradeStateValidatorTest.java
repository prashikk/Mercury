package com.mercury.mercury.Trade.state;

import com.mercury.mercury.Trade.Enum.TradeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TradeStateValidatorTest {

    private TradeStateValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TradeStateValidator();
    }

    @Test
    @DisplayName("Should pass when moving from NEW to VALIDATED")
    void testNewToValidatedShouldPass() {
        assertDoesNotThrow(() -> validator.validateTransition(TradeStatus.NEW, TradeStatus.VALIDATED));
    }

    @Test
    @DisplayName("Should fail when moving from SETTLED back to NEW")
    void testSettledToNewShouldFail() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                validator.validateTransition(TradeStatus.SETTLED, TradeStatus.NEW)
        );

        assertTrue(exception.getMessage().contains("Invalid Trade State Transition"));
    }

    @Test
    @DisplayName("Should pass when moving from VALIDATED to SETTLED")
    void testValidatedToSettledShouldPass() {
        assertDoesNotThrow(() -> validator.validateTransition(TradeStatus.VALIDATED, TradeStatus.SETTLED));
    }

    @Test
    @DisplayName("Should fail when moving from FAILED to VALIDATED")
    void testFailedToValidatedShouldFail() {
        assertThrows(IllegalArgumentException.class, () ->
                validator.validateTransition(TradeStatus.FAILED, TradeStatus.VALIDATED)
        );
    }
}
