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

import java.util.Date;

import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.impl.BaseUserSet;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;

/**
 * This class prepares a test Annotation object including all sub types like Body or Target.
 * Created object is intended for testing.
 */
public class UserSetTestObjectBuilder {

	public final static String TEST_TITLE   = "testTitle";
	public final static String TEST_DESCRIPTION = "Test user set description.";
	    
	public UserSet createTestUserSetInstance() {
		
		UserSet userSet = new BaseUserSet();
		
		userSet.setTitle(TEST_TITLE);
		userSet.setTitle(TEST_DESCRIPTION);
		userSet.setType(UserSetTypes.COLLECTION.name());
        
		Date now = new Date();
		userSet.setCreated(now);
		userSet.setModified(now);
		
		return userSet;
	}

}

