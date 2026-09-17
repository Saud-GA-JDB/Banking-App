package Bank.System;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

public class AppSystemTest {
    AppSystem appSystem;

    @BeforeEach
    public void setUp() {
        appSystem = new AppSystem("Test Bank");
    }

    @Test
    @DisplayName("When a user is locked then access is blocked during the lockout time")
    public final void whenUserIsLockedThenAccessIsBlockedDuringLockoutTime() {
        appSystem.lockedUserAnsTimeMap.put("123456789", LocalTime.now());
        Assertions.assertTrue(appSystem.checkIsUserLocked("123456789"));
    }

    @Test
    @DisplayName("When the lockout time has passed then the user is unlocked")
    public final void whenLockoutTimeHasPassedThenUserIsUnlocked() {
        appSystem.lockedUserAnsTimeMap.put("123456789", LocalTime.now().minusMinutes(2));
        Assertions.assertFalse(appSystem.checkIsUserLocked("123456789"));
        Assertions.assertFalse(appSystem.lockedUserAnsTimeMap.containsKey("123456789"));
    }

    @Test
    @DisplayName("When a user has not been locked then access is not blocked")
    public final void whenUserHasNotBeenLockedThenAccessIsNotBlocked() {
        Assertions.assertFalse(appSystem.checkIsUserLocked("123456789"));
    }
}
