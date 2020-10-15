package eu.europeana.set.definitions.model.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import eu.europeana.set.definitions.exception.UserSetAttributeInstantiationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.agent.impl.Person;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.utils.UserSetUtils;

import eu.europeana.set.definitions.model.vocabulary.VisibilityTypes;
import org.apache.commons.io.IOUtils;


/**
 * This class prepares a test Annotation object including all sub types like Body or Target.
 * Created object is intended for testing.
 */
public class UserSetTestObjectBuilder {

	public final static String TEST_TITLE            = "Sportswear";
	public final static String TEST_DESCRIPTION      = 
			"From tennis ensemble to golf uniforms, browse Europeana Fashion wide collection of historical"
			+ " sportswear and activewear designs!";
	public final static String TEST_AGENT            = "testAgent";
	public final static String TEST_HOMEPAGE         = "http://www.pro.europeana.eu/web/europeana-creative";
    public final static String CONTENT_DIR           = "/content/";
    public final static String ITEMS_TEST_INPUT_FILE = CONTENT_DIR + "test_items.txt";
    public final static String ITEMS_1200_TEST_INPUT_FILE = CONTENT_DIR + "test_items_1200.txt";
	    
//	Logger logger = Logger.getLogger(getClass());
	
    UserSetUtils userSetUtils = new UserSetUtils();

    public UserSetUtils getUserSetUtils() {
      return userSetUtils;
    }
    
	public UserSet buildUserSet(UserSet userSet, boolean isCollection){
		return buildUserSet(userSet, ITEMS_TEST_INPUT_FILE, isCollection);
	}

	public UserSet buildUserSet(UserSet userSet, String classpathFile, boolean isCollection){

		userSet.setTitle(getUserSetUtils().createMap(Locale.ENGLISH.getLanguage(),
				TEST_TITLE));
		userSet.setDescription(getUserSetUtils().createMap(Locale.ENGLISH.getLanguage(),
				TEST_DESCRIPTION));

		if(isCollection) {
			userSet.setType(UserSetTypes.COLLECTION.getJsonValue());
			userSet.setVisibility(VisibilityTypes.PUBLIC.getJsonValue());
		} else {
			userSet.setType(UserSetTypes.BOOKMARKSFOLDER.getJsonValue());
			userSet.setVisibility(VisibilityTypes.PRIVATE.getJsonValue());
		}

		Date now = new Date();
		userSet.setCreated(now);
		userSet.setModified(now);
		
		Agent creator = new Person();
		creator.setName(TEST_AGENT);
		creator.setHttpUrl(UserSetUtils.buildCreatorUri(TEST_AGENT));
		creator.setHomepage(TEST_HOMEPAGE);
		userSet.setCreator(creator);
		try {
			userSet.setItems(loadItems(classpathFile));
		} catch (IOException e) {
			throw new UserSetAttributeInstantiationException("items", null, "cannot read item list from classpath: " + classpathFile, e);
		}
			
		return userSet;
	}

	private List<String> loadItems(String path) throws IOException {
		return IOUtils.readLines(getClass().getResourceAsStream(path), "UTF-8");
	}
	
	  /**
	   * This method returns the classpath file for the give path name
	   * @param fileName the name of the file to be searched in the classpath
	   * @return the File object 
	   * @throws URISyntaxException
	   * @throws IOException
	   * @throws FileNotFoundException
	   */
	  protected File getClasspathFile(String fileName)
	      throws URISyntaxException, IOException, FileNotFoundException {
	    URL resource = getClass().getResource(fileName);
	    if(resource == null)
	      return null;
	    URI fileLocation = resource.toURI();
	    return (new File(fileLocation));
	  }

	
}

