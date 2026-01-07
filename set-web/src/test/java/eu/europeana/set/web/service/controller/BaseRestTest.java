package eu.europeana.set.web.service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.commons_sb3.error.EuropeanaI18nApiException;
import  jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.vocabulary.ProfileConstants;
import eu.europeana.set.definitions.model.vocabulary.SetPageProfile;
import eu.europeana.set.definitions.model.vocabulary.SetResourceProfile;
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
    public void testGetHeaderProfileSuccess() throws EuropeanaI18nApiException {
        String preferHeader = "include=" + ProfileConstants.VALUE_LD_MINIMAL + "; wait=100";
        Mockito.when(request.getHeader(Mockito.any())).thenReturn(preferHeader);

        List<SetPageProfile> profiles = baseRest.getProfilesFromRequest(null, request);

        assertNotNull(profiles);
        assertEquals(SetPageProfile.META, profiles.get(0));
    }

    @Test
    public void testGetHeaderProfileInvalidHeaderFormat() {
        String preferHeader = "handling=lenient; wait=100";
        Mockito.when(request.getHeader(Mockito.any())).thenReturn(preferHeader);

        EuropeanaI18nApiException thrown = assertThrows(
                EuropeanaI18nApiException.class,
                () -> baseRest.getProfilesFromRequest(null, request),
                "Something went wrong, check preferHeader "
        );

        //assertTrue(StringUtils.equals(thrown.getMessage(), UserSetI18nConstants.INVALID_HEADER_FORMAT));
        assertTrue(StringUtils.equals(thrown.getMessage(), UserSetI18nConstants.INVALID_HEADER_VALUE));
    }

    @Test
    public void testGetHeaderProfileInvalidHeaderValue() {
        String preferHeader = "include=testing; wait=100";
        Mockito.when(request.getHeader(Mockito.any())).thenReturn(preferHeader);

        EuropeanaI18nApiException thrown = assertThrows(
                EuropeanaI18nApiException.class,
                () -> baseRest.getProfilesFromRequest(null, request),
                "Something went wrong, check preferHeader value "
        );

        assertTrue(StringUtils.equals(thrown.getMessage(), UserSetI18nConstants.INVALID_HEADER_VALUE));
    }

    @Test
    public void testGetProfile() throws EuropeanaI18nApiException {
        String preferHeader = "include=" + ProfileConstants.VALUE_LD_ITEM_DESCRIPTIONS + "; wait=100";
        Mockito.when(request.getHeader(Mockito.any())).thenReturn(preferHeader);

        List<SetPageProfile> profiles = baseRest.getProfilesFromRequest(null, request);

        assertNotNull(profiles);
        assertTrue(StringUtils.equals(ProfileConstants.VALUE_LD_ITEM_DESCRIPTIONS, profiles.get(0).getLdPreference()));
    }

    @Test
    public void testSerialiseUserSet() throws EuropeanaApiException {
        UserSet userSet = new WebUserSetImpl();
        userSet.setVisibility(VisibilityTypes.PUBLIC.getJsonValue());
        userSet.setType(UserSetTypes.COLLECTION.getJsonValue());
        List<String> items = new ArrayList<>();
        items.add("http://data.europeana.eu/item/000000/1");
        items.add("http://data.europeana.eu/item/000000/2");
        userSet.setItems(items);
        
        //Mockito.when(baseRest.getUserSetService().applyProfile(Mockito.any(), Mockito.any())).thenReturn(userSet);
        baseRest.getUserSetService().applyProfile(userSet, SetResourceProfile.META);

        String serialisedUserSet = baseRest.serializeUserSet(SetResourceProfile.META, userSet);
        assertNotNull(serialisedUserSet);
    }

}