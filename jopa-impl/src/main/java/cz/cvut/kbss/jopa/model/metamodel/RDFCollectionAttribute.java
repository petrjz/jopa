package cz.cvut.kbss.jopa.model.metamodel;

import cz.cvut.kbss.jopa.model.IRI;
import cz.cvut.kbss.jopa.oom.converter.ConverterWrapper;
import cz.cvut.kbss.jopa.vocabulary.RDF;

import java.util.List;

public class RDFCollectionAttribute<X, V> extends ListAttributeImpl<X, V> {

    RDFCollectionAttribute(RDFCollectionAttributeBuilder<X, V> builder) {
        super(builder);
    }

    @Override
    public String toString() {
        return "RDFCollectionAttribute[" + getName() + "]";
    }

    static RDFCollectionAttributeBuilder builder(PropertyAttributes config) {
        return new RDFCollectionAttributeBuilder().collectionType(List.class).config(config);
    }

    static class RDFCollectionAttributeBuilder<X, V> extends ListAttributeBuilder<X, V> {

        RDFCollectionAttributeBuilder() {
            owlListClass(IRI.create(RDF.LIST));
            hasNextProperty(IRI.create(RDF.REST));
            hasContentsProperty(IRI.create(RDF.FIRST));
        }

        @Override
        public RDFCollectionAttributeBuilder<X, V> config(PropertyAttributes config) {
            super.config(config);
            return this;
        }

        @Override
        public RDFCollectionAttributeBuilder<X, V> collectionType(Class<List<V>> collectionType) {
            super.collectionType(collectionType);
            return this;
        }

        @Override
        public RDFCollectionAttributeBuilder<X, V> elementType(Type<V> elementType) {
            super.elementType(elementType);
            return this;
        }

        @Override
        public RDFCollectionAttributeBuilder<X, V> propertyInfo(PropertyInfo propertyInfo) {
            super.propertyInfo(propertyInfo);
            return this;
        }

        @Override
        public RDFCollectionAttributeBuilder<X, V> declaringType(ManagedType<X> declaringType) {
            super.declaringType(declaringType);
            return this;
        }

        @Override
        public RDFCollectionAttributeBuilder<X, V> inferred(boolean inferred) {
            super.inferred(inferred);
            return this;
        }

        @Override
        public RDFCollectionAttributeBuilder<X, V> includeExplicit(boolean includeExplicit) {
            super.includeExplicit(includeExplicit);
            return this;
        }

        @Override
        public RDFCollectionAttributeBuilder<X, V> converter(ConverterWrapper converter) {
            super.converter(converter);
            return this;
        }

        public RDFCollectionAttribute<X, V> build() {
            return new RDFCollectionAttribute<>(this);
        }
    }
}
