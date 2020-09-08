package eu.europeana.set.definitions.model.factory;

import eu.europeana.set.definitions.exception.UserSetInstantiationException;

/**
 * @deprecated not used by Jackson or morphia, but could be used in the future
 * @author GordeaS
 *
 */
public abstract class AbstractModelObjectFactory<O, E extends Enum<E>> {

	public O createModelObjectInstance(String modelObjectType) {
		return createObjectInstance(getEnumEntry(modelObjectType));
	}

	public O createObjectInstance(Enum<E> modelObjectType) {

		try {
			return getClassForType(modelObjectType).newInstance();

		} catch (UserSetInstantiationException e) {
			throw e;
		} catch (Exception e) {
			throw new UserSetInstantiationException(
					"Cannot instantiate object for type: " + modelObjectType.toString(), e);
		}
	}

	public Class<? extends O> getClassForType(String modelObjectType) {
		Enum<E> enumEntry = getEnumEntry(modelObjectType);
		return getClassForType(enumEntry);
	}

	private Enum<E> getEnumEntry(String modelObjectType) {
		return Enum.valueOf(getEnumClass(), modelObjectType.toUpperCase());
	}

	public abstract Class<? extends O> getClassForType(Enum<E> modelType);

	public abstract Class<E> getEnumClass();

}
