package com.pms;

import com.pms.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void hashThenVerifyRoundTrip() throws Exception {
        char[] pw = "CorrectHorse123!".toCharArray();
        String stored = PasswordUtil.hash(pw);
        assertTrue(stored.startsWith("PBKDF2WithHmacSHA256$210000$"));
        assertTrue(PasswordUtil.verify("CorrectHorse123!".toCharArray(), stored));
    }

    @Test
    void wrongPasswordFails() throws Exception {
        String stored = PasswordUtil.hash("RightPassword1".toCharArray());
        assertFalse(PasswordUtil.verify("WrongPassword1".toCharArray(), stored));
    }

    @Test
    void saltsAreUnique() throws Exception {
        String a = PasswordUtil.hash("SamePassword1".toCharArray());
        String b = PasswordUtil.hash("SamePassword1".toCharArray());
        assertNotEquals(a, b);
        assertTrue(PasswordUtil.verify("SamePassword1".toCharArray(), a));
        assertTrue(PasswordUtil.verify("SamePassword1".toCharArray(), b));
    }

    @Test
    void malformedStoredValueFails() throws Exception {
        assertFalse(PasswordUtil.verify("anything".toCharArray(), "not-a-valid-hash"));
    }
}
