import com.security.AuthorizationService;
import com.security.PasswordUtil;
import com.security.Permission;

import java.util.List;

/**
 * Standalone unit checks for the RBAC decision matrix and password hashing.
 * Run from the project root after a build:
 *   java -cp "build/web/WEB-INF/classes;build/web/WEB-INF/lib/jbcrypt-0.4.jar" SecuritySelfTest
 */
public final class SecuritySelfTest {

    private static int failures;

    public static void main(String[] args) {
        for (Permission permission : Permission.values()) {
            check(AuthorizationService.allows("ADMIN", permission),
                    "ADMIN allows " + permission);
        }

        check(AuthorizationService.allows("WARDEN", Permission.HOSTEL_READ),
                "WARDEN reads hostels");
        check(AuthorizationService.allows("WARDEN", Permission.ROOM_READ),
                "WARDEN reads rooms");
        check(AuthorizationService.allows("WARDEN", Permission.ROOM_CREATE),
                "WARDEN creates rooms");
        check(AuthorizationService.allows("WARDEN", Permission.ROOM_UPDATE),
                "WARDEN updates rooms");
        check(!AuthorizationService.allows("WARDEN", Permission.HOSTEL_CREATE),
                "WARDEN cannot create hostels");
        check(!AuthorizationService.allows("WARDEN", Permission.HOSTEL_UPDATE),
                "WARDEN cannot update hostels");
        check(!AuthorizationService.allows("WARDEN", Permission.HOSTEL_DELETE),
                "WARDEN cannot delete hostels");
        check(!AuthorizationService.allows("WARDEN", Permission.ROOM_DELETE),
                "WARDEN cannot delete rooms");
        check(!AuthorizationService.allows("WARDEN", Permission.USER_MANAGE),
                "WARDEN cannot manage users");
        check(AuthorizationService.allows("WARDEN", Permission.ROOM_SEARCH),
                "WARDEN searches rooms");
        check(AuthorizationService.allows("WARDEN",
                Permission.ROOM_REQUEST_CREATE),
                "WARDEN creates room requests");
        check(AuthorizationService.allows("WARDEN",
                Permission.ROOM_REQUEST_READ_OWN),
                "WARDEN reads own room requests");
        check(AuthorizationService.allows("WARDEN",
                Permission.ROOM_REQUEST_MANAGE),
                "WARDEN manages room requests");
        check(AuthorizationService.allows("WARDEN",
                Permission.ROOM_ALLOCATION_CONFIRM),
                "WARDEN confirms room allocations");

        check(AuthorizationService.allows("VIEWER", Permission.HOSTEL_READ),
                "VIEWER reads hostels");
        check(AuthorizationService.allows("VIEWER", Permission.ROOM_READ),
                "VIEWER reads rooms");
        check(AuthorizationService.allows("VIEWER", Permission.ROOM_SEARCH),
                "VIEWER searches rooms");
        check(AuthorizationService.allows("VIEWER",
                Permission.ROOM_REQUEST_CREATE),
                "VIEWER creates room requests");
        check(AuthorizationService.allows("VIEWER",
                Permission.ROOM_REQUEST_READ_OWN),
                "VIEWER reads own room requests");
        check(!AuthorizationService.allows("VIEWER",
                Permission.ROOM_REQUEST_MANAGE),
                "VIEWER cannot manage room requests");
        check(!AuthorizationService.allows("VIEWER",
                Permission.ROOM_ALLOCATION_CONFIRM),
                "VIEWER cannot confirm room allocations");
        for (Permission permission : Permission.values()) {
            if (permission != Permission.HOSTEL_READ
                    && permission != Permission.ROOM_READ
                    && permission != Permission.ROOM_SEARCH
                    && permission != Permission.ROOM_REQUEST_CREATE
                    && permission != Permission.ROOM_REQUEST_READ_OWN) {
                check(!AuthorizationService.allows("VIEWER", permission),
                        "VIEWER denied " + permission);
            }
        }

        check(!AuthorizationService.allows("UNKNOWN", Permission.HOSTEL_READ),
                "unknown role denied");
        check(!AuthorizationService.allows(null, Permission.HOSTEL_READ),
                "null role denied");
        check(!AuthorizationService.allows("ADMIN", null),
                "null permission denied");
        check(AuthorizationService.allowsAny(
                List.of("VIEWER", "WARDEN"), Permission.ROOM_CREATE),
                "allowsAny honors multiple roles");
        check("ADMIN".equals(AuthorizationService.highestRole(
                List.of("VIEWER", "ADMIN", "WARDEN"))),
                "highestRole prefers ADMIN");
        check("WARDEN".equals(AuthorizationService.highestRole(
                List.of("VIEWER", "WARDEN"))),
                "highestRole prefers WARDEN");

        String hash = PasswordUtil.hash("CorrectHorse1");
        check(hash.startsWith("$2"), "BCrypt hash format");
        check(!hash.contains("CorrectHorse1"), "hash does not contain password");
        check(PasswordUtil.verify("CorrectHorse1", hash),
                "correct password verifies");
        check(!PasswordUtil.verify("WrongHorse1", hash),
                "wrong password fails");
        check(!PasswordUtil.verify("CorrectHorse1", "not-a-hash"),
                "malformed hash fails safely");
        check(PasswordUtil.strengthProblem("short1") != null,
                "short password rejected");
        check(PasswordUtil.strengthProblem("alllettersonly") != null,
                "password without digit rejected");
        check(PasswordUtil.strengthProblem("1234567890") != null,
                "password without letter rejected");
        check(PasswordUtil.strengthProblem("CorrectHorse1") == null,
                "strong password accepted");

        if (failures == 0) {
            System.out.println("SecuritySelfTest: ALL CHECKS PASSED");
        } else {
            System.out.println("SecuritySelfTest: " + failures
                    + " CHECK(S) FAILED");
            System.exit(1);
        }
    }

    private static void check(boolean condition, String name) {
        if (condition) {
            System.out.println("PASS  " + name);
        } else {
            failures++;
            System.out.println("FAIL  " + name);
        }
    }
}
