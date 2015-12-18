package cz.cvut.kbss.jopa.query;

import cz.cvut.kbss.jopa.model.query.Parameter;
import cz.cvut.kbss.jopa.query.parameter.ParameterValue;

public class QueryParameter<T> implements Parameter<T> {

    private final String name;
    private final Integer position;

    private ParameterValue value;

    public QueryParameter(String name) {
        this.name = name;
        this.position = null;
        resetValue();
    }

    public QueryParameter(Integer position) {
        this.position = position;
        this.name = null;
        resetValue();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Integer getPosition() {
        return position;
    }

    public Object getIdentifier() {
        return name != null ? name : position;
    }

    public ParameterValue getValue() {
        return value;
    }

    public void setValue(Object value) {
        assert value != null;
        this.value = ParameterValue.create(value);
    }

    public void setValue(String value, String language) {
        assert value != null;
        this.value = ParameterValue.create(value, language);
    }

    public void resetValue() {
        this.value =
                name != null ? ParameterValue.createVariableValue(name) : ParameterValue.createVariableValue(position);
    }

    @Override
    public Class<T> getParameterType() {
        throw new IllegalStateException("Parameter types are not supported by the current implementation.");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueryParameter<?> that = (QueryParameter<?>) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return !(position != null ? !position.equals(that.position) : that.position != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (position != null ? position.hashCode() : 0);
        return result;
    }
}