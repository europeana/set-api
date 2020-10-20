package eu.europeana.set.web.model;

import eu.europeana.api.commons.definitions.vocabulary.Role;
import eu.europeana.api.commons.web.model.vocabulary.Operations;
import eu.europeana.set.web.model.vocabulary.Roles;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RolesTest {

    private static final String ANONYMOUS = "ANONYMOUS";
    private static final String EDITOR = "EDITOR";
    private static final String ADMIN = "ADMIN";

    @Test
    public void testAdminRole() {
        Role role = Roles.getRoleByName(ADMIN);
        assertNotNull(role);
        assertTrue(StringUtils.equals(ADMIN, role.getName()));

        List<String> permissionList = Arrays.asList(role.getPermissions());

        assertTrue(permissionList.size() > 0);
        assertTrue(permissionList.contains(Operations.ADMIN_ALL));
        assertTrue(permissionList.contains(Operations.RETRIEVE));
        assertTrue(permissionList.contains(Operations.CREATE));
        assertTrue(permissionList.contains(Operations.DELETE));
        assertTrue(permissionList.contains(Operations.UPDATE));
    }

    @Test
    public void testAnonymousRole() {
        Role role = Roles.getRoleByName(ANONYMOUS);
        assertNotNull(role);
        assertTrue(StringUtils.equals(ANONYMOUS, role.getName()));

        List<String> permissionList = Arrays.asList(role.getPermissions());

        assertTrue(permissionList.size() > 0);
        assertTrue(permissionList.contains(Operations.RETRIEVE));
        assertFalse(permissionList.contains(Operations.CREATE));
        assertFalse(permissionList.contains(Operations.DELETE));
        assertFalse(permissionList.contains(Operations.UPDATE));
    }

    @Test
    public void testEditorRole() {
        Role role = Roles.getRoleByName(EDITOR);
        assertNotNull(role);
        assertTrue(StringUtils.equals(EDITOR, role.getName()));

        List<String> permissionList = Arrays.asList(role.getPermissions());

        assertTrue(permissionList.size() > 0);
        assertFalse(permissionList.contains(Operations.ADMIN_ALL));
        assertTrue(permissionList.contains(Operations.RETRIEVE));
        assertTrue(permissionList.contains(Operations.CREATE));
        assertTrue(permissionList.contains(Operations.DELETE));
        assertTrue(permissionList.contains(Operations.UPDATE));
    }
}
