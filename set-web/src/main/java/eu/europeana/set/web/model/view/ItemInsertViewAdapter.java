package eu.europeana.set.web.model.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.view.ItemInsertView;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;

public class ItemInsertViewAdapter implements ItemInsertView {

	UserSet userSet = null;
	
	public ItemInsertViewAdapter(UserSet userSet) {
		this.userSet = userSet;
	}
	
	@Override
	public String getId() {
		return userSet.getIdentifier();
	}
	
	@Override
	public String getType() {
		return userSet.getType();
	}

	@Override
	public int getTotal() {
		return userSet.getTotal();
	}

	@Override
	public String getModified() {
		DateFormat df = new SimpleDateFormat(WebUserSetFields.SET_DATE_FORMAT);
		return df.format(userSet.getModified());
	}

}
