package cz.cvut.kbss.owlpersistence.owlapi;

import java.lang.reflect.Field;

import cz.cvut.kbss.owlpersistence.model.annotations.FetchType;
import cz.cvut.kbss.owlpersistence.model.metamodel.DirectTypesSpecification;
import cz.cvut.kbss.owlpersistence.model.metamodel.ManagedType;

public class DirectTypesSpecificationImpl<X, Y> implements
		DirectTypesSpecification<X, Y> {
	private final ManagedType<X> declaringType;
	private final FetchType fetchType;
	private final Field javaField;
	private Class<Y> javaType;

	public DirectTypesSpecificationImpl(final ManagedType<X> declaringType,
			final FetchType fetchType, final Field javaField,
			final Class<Y> javaType) {

		this.declaringType = declaringType;
		this.fetchType = fetchType;
		this.javaField = javaField;
		this.javaType = javaType;
	}

	@Override
	public ManagedType<X> getDeclaringType() {
		return declaringType;
	}

	@Override
	public FetchType getFetchType() {
		return fetchType;
	}

	@Override
	public Field getJavaField() {
		return javaField;
	}

	@Override
	public Class<Y> getJavaType() {
		return javaType;
	}

}
