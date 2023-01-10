package eu.europeana.set.web.service.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.ProfileConstants;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.VisibilityTypes;
import eu.europeana.set.web.config.UserSetI18nConstants;
import eu.europeana.set.web.model.WebUserSetImpl;
import eu.europeana.set.web.service.UserSetService;

@ExtendWith(MockitoExtension.class)
public class BaseRestTest {

    private BaseRest baseRest;

    @Mock
    private UserSetService userSetService;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setup() {
        baseRest = new BaseRest();
        baseRest.setUserSetService(userSetService);
    }

    @Test
    public void testParsePreferHeader() {
        Map<String, String> preferHeader = baseRest.parsePreferHeader("handling=lenient; wait=100; respond-async");
        assertNotNull(preferHeader);
        assertTrue(preferHeader.size() == 2);

        preferHeader.clear();
        preferHeader = baseRest.parsePreferHeader("handling=lenient; wait=100; respond-async=true");
        assertNotNull(preferHeader);
        assertTrue(preferHeader.size() == 3);
    }

    @Test
    public void testGetHeaderProfileSuccess() throws HttpException {
        String preferHeader = "include=" + ProfileConstants.VALUE_LD_MINIMAL + "; wait=100";
        Mockito.when(request.getHeader(Mockito.any())).thenReturn(preferHeader);

        LdProfiles profile = baseRest.getHeaderProfile(request);

        assertNotNull(profile);
        assertTrue(StringUtils.equals(ProfileConstants.VALUE_LD_MINIMAL, profile.getHeaderValue()));
    }

    @Test
    public void testGetHeaderProfileInvalidHeaderFormat() {
        String preferHeader = "handling=lenient; wait=100";
        Mockito.when(request.getHeader(Mockito.any())).thenReturn(preferHeader);

        HttpException thrown = assertThrows(
                HttpException.class,
                () -> baseRest.getHeaderProfile(request),
                "Something went wrong, check preferHeader "
        );

        assertTrue(StringUtils.equals(thrown.getMessage(), UserSetI18nConstants.INVALID_HEADER_FORMAT));
    }

    @Test
    public void testGetHeaderProfileInvalidHeaderValue() {
        String preferHeader = "include=testing; wait=100";
        Mockito.when(request.getHeader(Mockito.any())).thenReturn(preferHeader);

        HttpException thrown = assertThrows(
                HttpException.class,
                () -> baseRest.getHeaderProfile(request),
                "Something went wrong, check preferHeader value "
        );

        assertTrue(StringUtils.equals(thrown.getMessage(), UserSetI18nConstants.INVALID_HEADER_VALUE));
    }

    @Test
    public void testGetProfile() throws HttpException {
        String preferHeader = "include=" + ProfileConstants.VALUE_LD_ITEM_DESCRIPTIONS + "; wait=100";
        Mockito.when(request.getHeader(Mockito.any())).thenReturn(preferHeader);

        LdProfiles profile = baseRest.getProfile(LdProfiles.ITEMDESCRIPTIONS.getHeaderValue(), request);

        assertNotNull(profile);
        assertTrue(StringUtils.equals(ProfileConstants.VALUE_LD_ITEM_DESCRIPTIONS, profile.getHeaderValue()));
    }

    @Test
    public void testSerialiseUserSet() throws IOException {
        UserSet userSet = new WebUserSetImpl();
        userSet.setVisibility(VisibilityTypes.PUBLIC.getJsonValue());
        userSet.setType(UserSetTypes.COLLECTION.getJsonValue());
        List<String> items = new ArrayList<>();
        items.add("http://data.europeana.eu/item/000000/1");
        items.add("http://data.europeana.eu/item/000000/2");
        userSet.setItems(items);
        
        //Mockito.when(baseRest.getUserSetService().applyProfile(Mockito.any(), Mockito.any())).thenReturn(userSet);
        baseRest.getUserSetService().applyProfile(userSet, LdProfiles.STANDARD);

        String serialisedUserSet = baseRest.serializeUserSet(LdProfiles.MINIMAL, userSet);
        assertNotNull(serialisedUserSet);
    }

}
