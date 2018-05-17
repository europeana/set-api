/*
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package eu.europeana.set.definitions.model.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import eu.europeana.set.definitions.exception.UserSetAccessException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.agent.impl.SoftwareAgent;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.utils.UserSetUtils;

import org.apache.commons.io.FileUtils;


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
	    
//	Logger logger = Logger.getLogger(getClass());
	
    UserSetUtils userSetUtils = new UserSetUtils();

    public UserSetUtils getUserSetUtils() {
      return userSetUtils;
    }
    
	public UserSet buildUserSet(UserSet userSet) {

		userSet.setTitle(getUserSetUtils().createMap(Locale.ENGLISH.getLanguage(),
				TEST_TITLE));
		userSet.setDescription(getUserSetUtils().createMap(Locale.ENGLISH.getLanguage(),
				TEST_DESCRIPTION));

//		userSet.setTitle(TEST_TITLE);
//		userSet.setDescription(TEST_DESCRIPTION);
		userSet.setType(UserSetTypes.COLLECTION.name());
        
		Date now = new Date();
		userSet.setCreated(now);
		userSet.setModified(now);
		
		Agent creator = new SoftwareAgent();
		creator.setName(TEST_AGENT);
		creator.setHomepage(TEST_HOMEPAGE);
		userSet.setCreator(creator);
		
		try {
			userSet.setItems(loadItems());
		} catch (UserSetAccessException e) {
			System.out.println(UserSetAccessException.COULD_NOT_READ_FROM_FILE_ERROR + ". "+ e.getMessage());
		}
			
		return userSet;
	}

	private List<String> loadItems() throws UserSetAccessException {
		List<String> items = new ArrayList<String>();
		
	    File itemsTestInputFile;
		try {
			itemsTestInputFile = getClasspathFile(ITEMS_TEST_INPUT_FILE);
			String itemsStr =
				    FileUtils.readFileToString(itemsTestInputFile, "UTF-8");
            for (String item : itemsStr.split("\r\n")) {
                items.add(item);
            }
		} catch (URISyntaxException | IOException e) {
		      throw new UserSetAccessException(
		    		  UserSetAccessException.COULD_NOT_READ_FROM_FILE_ERROR, e);
		}
		return items;
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

