package eu.europeana.api.myapi.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for UserSet controller.
 * 
 * For all the methods createUserSet , getUserSet , updateUserSet, deleteUserSet, deleteItemFromUserSet, 
 * insertItemIntoUserSet, isItemInUserSet
 * 
 * MockMvc test for the Main entry point for server-side Spring MVC. Should check for 200 Ok, 
 * 400 bad request (if required paremter are not passed), 
 * 401 unauthorized (if authentication provided is wrong),
 * and 404 Not found scenarios. Should also check all the headers added using the UserSetHttpHeaders constants 
 *
 * @author Roman Graf on 10-09-2020.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class MyApiControllerTest {

//    String BASE_URL = "http://localhost:8080/set/?";
//    String BASE_URL = "/set/?";
    String BASE_URL = "/set";
    
    public static final String TOKEN           = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI1SW55MldXRkhZQ1YxcFNNc0NKXzl2LVhaUUgwUk84c05KNUxLd2JHcmk0In0.eyJqdGkiOiIyMWYwZTA1Ny03YzVjLTQ2MTItOWZkZi0xNTQzNGRiYTk2NWYiLCJleHAiOjE2MDAxMjc1MTAsIm5iZiI6MCwiaWF0IjoxNjAwMDkxNTEwLCJpc3MiOiJodHRwczovL2F1dGguZXVyb3BlYW5hLmV1L2F1dGgvcmVhbG1zL2V1cm9wZWFuYSIsImF1ZCI6WyJ1c2Vyc2V0cyIsImFjY291bnQiXSwic3ViIjoiNjkzNTViODctOWY4NC00OGE4LTkwZDAtNDdhMjZlOWIyYTgyIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoidGVzdF9jbGllbnRfdXNlcnNldCIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6ImYxOGUxZjNkLWMxNDEtNGFmMC1hZGIyLWNmMmIwYTk2MGJiNSIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7InVzZXJzZXRzIjp7InJvbGVzIjpbInVzZXIiXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoiY2xpZW50X2luZm8gcHJvZmlsZSB1c2Vyc2V0cyBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJjbGllbnRfcHVibGljX2lkIjoiZjUwNGEwOTktODZkMS00NmMwLWJiMzItMDY5MWI2M2M1ZTQ5IiwibmFtZSI6IlVzZXJTZXQgUmVndWxhciBSdW5zY29wZSBUZXN0IiwicHJlZmVycmVkX3VzZXJuYW1lIjoidGVzdF91c2Vyc2V0X3JlZ3VsYXIiLCJnaXZlbl9uYW1lIjoiVXNlclNldCBSZWd1bGFyIiwiZmFtaWx5X25hbWUiOiJSdW5zY29wZSBUZXN0IiwiY2xpZW50X25hbWUiOiJSdW5zY29wZSBUZXN0IENsaWVudCIsImVtYWlsIjoidGVzdF91c2Vyc2V0X3JlZ3VsYXJAZXVyb3BlYW5hLmV1In0.HT6VszLoKqemD05N8i10VK2BOMlXpHc7SqtVjENwQ_s11gnbq8hN5oIlA9EB7WS1oJ1cXoxZY7GFqWPzrXuey-KCC3Uni0uoYREw1O1nppliEoHPmBWfotLNEXfwyAdcT-7P-LtIYwEIDYWdCTYUlxoB6Um5OqSZRHXtQv2rBnSPPzGIvU_M1zAJesRzXWf_CysJEvAHrbxeUL-m9Ww7dwlJJy52YHYM1WthJFCfZArEukS9xw8XVB9hRpEqpWE0zx3XmDgtTWSRR9IWM1zx-CT5ZUjzO-yEa7hkW6GrATObS-bhaZTD8-D-yDq0VwbzZPnUzgRdhH06fKg1h5KT2w";
//    public static final String TOKEN           = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI1SW55MldXRkhZQ1YxcFNNc0NKXzl2LVhaUUgwUk84c05KNUxLd2JHcmk0In0.eyJqdGkiOiIyMWYwZTA1Ny03YzVjLTQ2MTItOWZkZi0xNTQzNGRiYTk2NWYiLCJleHAiOjE2MDAxMjc1MTAsIm5iZiI6MCwiaWF0IjoxNjAwMDkxNTEwLCJpc3MiOiJodHRwczovL2F1dGguZXVyb3BlYW5hLmV1L2F1dGgvcmVhbG1zL2V1cm9wZWFuYSIsImF1ZCI6WyJ1c2Vyc2V0cyIsImFjY291bnQiXSwic3ViIjoiNjkzNTViODctOWY4NC00OGE4LTkwZDAtNDdhMjZlOWIyYTgyIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoidGVzdF9jbGllbnRfdXNlcnNldCIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6ImYxOGUxZjNkLWMxNDEtNGFmMC1hZGIyLWNmMmIwYTk2MGJiNSIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7InVzZXJzZXRzIjp7InJvbGVzIjpbInVzZXIiXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoiY2xpZW50X2luZm8gcHJvZmlsZSB1c2Vyc2V0cyBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJjbGllbnRfcHVibGljX2lkIjoiZjUwNGEwOTktODZkMS00NmMwLWJiMzItMDY5MWI2M2M1ZTQ5IiwibmFtZSI6IlVzZXJTZXQgUmVndWxhciBSdW5zY29wZSBUZXN0IiwicHJlZmVycmVkX3VzZXJuYW1lIjoidGVzdF91c2Vyc2V0X3JlZ3VsYXIiLCJnaXZlbl9uYW1lIjoiVXNlclNldCBSZWd1bGFyIiwiZmFtaWx5X25hbWUiOiJSdW5zY29wZSBUZXN0IiwiY2xpZW50X25hbWUiOiJSdW5zY29wZSBUZXN0IENsaWVudCIsImVtYWlsIjoidGVzdF91c2Vyc2V0X3JlZ3VsYXJAZXVyb3BlYW5hLmV1In0.HT6VszLoKqemD05N8i10VK2BOMlXpHc7SqtVjENwQ_s11gnbq8hN5oIlA9EB7WS1oJ1cXoxZY7GFqWPzrXuey-KCC3Uni0uoYREw1O1nppliEoHPmBWfotLNEXfwyAdcT-7P-LtIYwEIDYWdCTYUlxoB6Um5OqSZRHXtQv2rBnSPPzGIvU_M1zAJesRzXWf_CysJEvAHrbxeUL-m9Ww7dwlJJy52YHYM1WthJFCfZArEukS9xw8XVB9hRpEqpWE0zx3XmDgtTWSRR9IWM1zx-CT5ZUjzO-yEa7hkW6GrATObS-bhaZTD8-D-yDq0VwbzZPnUzgRdhH06fKg1h5KT2w";
//    public static final String TOKEN_API_KEY   = "test_key"; // this key is encoded in the token

    
    @Autowired
    private MockMvc mockMvc;
    
    /**
     * Test createUserSet 200 Ok response
     * 
     * http://localhost:8080/set/?profile=minimal
     * 
     * Authorization Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ3Y1N6TDZ0a3RCNFhHcUtjbEZncnVaaHQtX3d5MkZUV0FlWUtaYWNSOTNnIn0.eyJqdGkiOiIxNWQxNmU5My1jMTM5LTQzMTctYWJhZS05NGI4NzUzNTk3M2QiLCJleHAiOjE1OTk2NjI0NDksIm5iZiI6MCwiaWF0IjoxNTg0MTEwNDQ5LCJpc3MiOiJodHRwczovL2tleWNsb2FrLXRlc3QuZWFuYWRldi5vcmcvYXV0aC9yZWFsbXMvZXVyb3BlYW5hIiwiYXVkIjpbInVzZXJzZXRzIiwiYWNjb3VudCJdLCJzdWIiOiJhNTEyZGFhMy1kN2MzLTQ0YTEtOWFkOC1lOGI3ZGU4MDUyNjgiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJ0ZXN0LWNsaWVudC11c2Vyc2V0cyIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6IjE4YjE1NGNhLWZjZTAtNDVkYi1iYjAyLTUwZjhhNDQ1YmEwNCIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7InVzZXJzZXRzIjp7InJvbGVzIjpbInVzZXIiXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoiZW1haWwgdXNlcnNldHMgY2xpZW50X2luZm8gcHJvZmlsZSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJjbGllbnRfcHVibGljX2lkIjoiOTBkMzVhYzYtYmY4NS00YTIzLTkxYmItNWZlMzJmYjE4MGFjIiwibmFtZSI6IlJ1bnNjb3BlIFRlc3QgUmVndWxhciBVc2VyIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidGVzdF91c2Vyc2V0X3JlZ3VsYXIiLCJnaXZlbl9uYW1lIjoiUnVuc2NvcGUgVGVzdCIsImZhbWlseV9uYW1lIjoiUmVndWxhciBVc2VyIiwiY2xpZW50X25hbWUiOiJSdW5zY29wZSBUZXN0IENsaWVudCIsImVtYWlsIjoidGVzdC51c2Vyc2V0LnJlZ3VsYXJAZXVyb3BlYW5hLmV1In0.HB88C4QjFokCzgF8qq6izttYIqAvtT6J31KnMgPA1FW81k8XDDYwy3ATA8PlZIHYkFducvcd-ro_hyUXe--c-7HYJx5hrcm3_7SJR191ArI1krB5AZP-1oqdE2N3VqBCUvsn-IAA1USBgKYPsLnN8-3jWcPNBe57kmrM7xw7bytAq5fsyR_a_zjWiqRDOF851ReQ6lFBPpvOhIhDKk8M8X-sdjJITQysHKY4FuRfX1wq2wLqFbeirbKw4hw5jvGkYiEIM4kDnwXH7imwND9-awK_XE-0FmTDvenh8LF6l9ilQ_8_3SamB0h9nYeP-N9tfZ6lkYkM7HvJXj8P3NVcxA
     * Content-Type application/json 
     * 
     * {
  "type": "Collection",
  "title": {
     "en": "Sportswear"
  },
  "description": {
     "en": "From tennis ensemble to golf uniforms, browse Europeana Fashion wide collection of historical sportswear and activewear designs!"
  },
  "isDefinedBy": "https://api.europeana.eu/api/v2/search.json?query=*:*&profile=minimal&wskey=api2demo"
}
     */
    @Test
    public void testCreate_UserSet_200Ok() throws Exception {   
	
	String requestJson="{\n" + 
		"  \"type\": \"Collection\",\n" + 
		"  \"title\": {\n" + 
		"     \"en\": \"Sportswear\"\n" + 
		"  },\n" + 
		"  \"description\": {\n" + 
		"     \"en\": \"From tennis ensemble to golf uniforms, browse Europeana Fashion wide collection of historical sportswear and activewear designs!\"\n" + 
		"  },\n" + 
		"  \"isDefinedBy\": \"https://api.europeana.eu/api/v2/search.json?query=*:*&profile=minimal&wskey=api2demo\"\n" + 
		"}";

        mockMvc.perform(post(BASE_URL).param("profile", "minimal")
        	.contentType(MediaType.APPLICATION_JSON)
        	.content(requestJson)
//        	.header(HttpHeaders.AUTHORIZATION, "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ3Y1N6TDZ0a3RCNFhHcUtjbEZncnVaaHQtX3d5MkZUV0FlWUtaYWNSOTNnIn0.eyJqdGkiOiIxNWQxNmU5My1jMTM5LTQzMTctYWJhZS05NGI4NzUzNTk3M2QiLCJleHAiOjE1OTk2NjI0NDksIm5iZiI6MCwiaWF0IjoxNTg0MTEwNDQ5LCJpc3MiOiJodHRwczovL2tleWNsb2FrLXRlc3QuZWFuYWRldi5vcmcvYXV0aC9yZWFsbXMvZXVyb3BlYW5hIiwiYXVkIjpbInVzZXJzZXRzIiwiYWNjb3VudCJdLCJzdWIiOiJhNTEyZGFhMy1kN2MzLTQ0YTEtOWFkOC1lOGI3ZGU4MDUyNjgiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJ0ZXN0LWNsaWVudC11c2Vyc2V0cyIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6IjE4YjE1NGNhLWZjZTAtNDVkYi1iYjAyLTUwZjhhNDQ1YmEwNCIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7InVzZXJzZXRzIjp7InJvbGVzIjpbInVzZXIiXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoiZW1haWwgdXNlcnNldHMgY2xpZW50X2luZm8gcHJvZmlsZSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJjbGllbnRfcHVibGljX2lkIjoiOTBkMzVhYzYtYmY4NS00YTIzLTkxYmItNWZlMzJmYjE4MGFjIiwibmFtZSI6IlJ1bnNjb3BlIFRlc3QgUmVndWxhciBVc2VyIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidGVzdF91c2Vyc2V0X3JlZ3VsYXIiLCJnaXZlbl9uYW1lIjoiUnVuc2NvcGUgVGVzdCIsImZhbWlseV9uYW1lIjoiUmVndWxhciBVc2VyIiwiY2xpZW50X25hbWUiOiJSdW5zY29wZSBUZXN0IENsaWVudCIsImVtYWlsIjoidGVzdC51c2Vyc2V0LnJlZ3VsYXJAZXVyb3BlYW5hLmV1In0.HB88C4QjFokCzgF8qq6izttYIqAvtT6J31KnMgPA1FW81k8XDDYwy3ATA8PlZIHYkFducvcd-ro_hyUXe--c-7HYJx5hrcm3_7SJR191ArI1krB5AZP-1oqdE2N3VqBCUvsn-IAA1USBgKYPsLnN8-3jWcPNBe57kmrM7xw7bytAq5fsyR_a_zjWiqRDOF851ReQ6lFBPpvOhIhDKk8M8X-sdjJITQysHKY4FuRfX1wq2wLqFbeirbKw4hw5jvGkYiEIM4kDnwXH7imwND9-awK_XE-0FmTDvenh8LF6l9ilQ_8_3SamB0h9nYeP-N9tfZ6lkYkM7HvJXj8P3NVcxA")
        	.header(HttpHeaders.AUTHORIZATION, TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.CREATED.value()));
    }
    
//    @Test
    public void testMyControllerValidInput() throws Exception {
        // with accept header
        mockMvc.perform(get("/myApi/{someRequest}", "123test")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.OK.value()));

        // without accept header
        mockMvc.perform(get("/myApi/{someRequest}", "123test"))
                .andExpect(status().is(HttpStatus.OK.value()));
    }

//    @Test
    public void testMyControllerInvalidInput() throws Exception {
        // without accept header
        mockMvc.perform(get("/myApi/{someRequest}", "123-test"))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

    }

    /**
     * Test if CORS works for normal requests and error requests
     */
//    @Test
    public void testCORS() throws Exception {
        // normal (200 response) request
        mockMvc.perform(get("/myApi/{someRequest}", "123test")
                .header(HttpHeaders.ORIGIN, "https://test.com"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"));

        // error request
        mockMvc.perform(get("/myApi/{someRequest}", "123-test")
                .header(HttpHeaders.ORIGIN, "https://test.com"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"));
    }

}
