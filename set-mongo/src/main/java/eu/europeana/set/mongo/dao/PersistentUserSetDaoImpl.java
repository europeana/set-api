package eu.europeana.set.mongo.dao;

import java.io.Serializable;
import java.util.List;
import javax.annotation.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;
import eu.europeana.api.commons.nosql.dao.impl.NosqlDaoImpl;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.mongo.model.PersistentUserSetImpl;
import eu.europeana.set.mongo.model.UserSetMongoConstants;
import eu.europeana.set.mongo.model.internal.GeneratedUserSetIdImpl;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.web.model.WebUserSetImpl;

public class PersistentUserSetDaoImpl <E extends PersistentUserSet, T extends Serializable>
		extends NosqlDaoImpl<E, T> implements PersistentUserSetDao<E, T>{

	@Resource
	private UserSetConfiguration configuration;
	private Object syncObj= new Object();
	
	protected final Logger logger = LogManager.getLogger(this.getClass());
	
	public UserSetConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(UserSetConfiguration configuration) {
		this.configuration = configuration;
	}
	
	public PersistentUserSetDaoImpl(Class<E> clazz, Datastore datastore) {
		super(datastore, clazz);
		datastore.ensureIndexes(PersistentUserSetImpl.class);
	}

	@SuppressWarnings("deprecation")
	public long generateNextUserSetId(String provider) {

		GeneratedUserSetIdImpl nextUserSetId = null;

		synchronized (syncObj) {

			Query<GeneratedUserSetIdImpl> q = getDatastore().createQuery(GeneratedUserSetIdImpl.class);
			q.filter(UserSetMongoConstants.MONGO_ID, provider);
			
			UpdateOperations<GeneratedUserSetIdImpl> uOps = getDatastore()
					.createUpdateOperations(GeneratedUserSetIdImpl.class)
					.inc(GeneratedUserSetIdImpl.SEQUENCE_COLUMN_NAME);
			// search UserSetId and get incremented UserSet number 
			nextUserSetId = getDatastore().findAndModify(q, uOps);
			
			if (nextUserSetId == null) {
				nextUserSetId = new GeneratedUserSetIdImpl( 
						provider, ""+1L);
				ds.save(nextUserSetId);
			}
		}

		return nextUserSetId.getUserSetId();
	}

	@Override
	public void deleteByObjectId(List<ObjectId> objectIds) {
		getDatastore().delete(WebUserSetImpl.class, objectIds);
	}

  @Override
  public void deleteByIdentifier(List<String> setIds) {
    Query<E> query = createQuery();
    query.filter( WebUserSetModelFields.IDENTIFIER + " in", setIds);
    getDatastore().delete(query); 
  }
}
