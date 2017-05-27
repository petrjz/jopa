/**
 * Copyright (C) 2016 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.ontodriver.sesame.util;

import cz.cvut.kbss.ontodriver.model.*;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;

import java.util.Map;
import java.util.Optional;

public class AxiomBuilder {

    private final NamedResource subject;
    private final Map<IRI, Assertion> propertyToAssertion;

    private final Assertion unspecifiedProperty;

    public AxiomBuilder(NamedResource subject, Map<IRI, Assertion> propertyToAssertion, Assertion unspecifiedProperty) {
        this.subject = subject;
        this.propertyToAssertion = propertyToAssertion;
        this.unspecifiedProperty = unspecifiedProperty;
    }

    public Axiom<?> statementToAxiom(Statement statement) {
        final Assertion assertion = resolveAssertion(statement.getPredicate());

        return statementToAxiom(statement, assertion);
    }

    private Optional<Value<?>> resolveValue(Statement stmt, Assertion assertion) {
        if (assertion == null || SesameUtils.isBlankNode(stmt.getObject())) {
            return Optional.empty();
        }
        return createValue(assertion.getType(), stmt.getObject());
    }

    public Axiom<?> statementToAxiom(Statement statement, Assertion assertion) {
        final Optional<Value<?>> val = resolveValue(statement, assertion);
        return val.map(v -> new AxiomImpl<>(subject, assertion, v)).orElse(null);
    }

    private Assertion resolveAssertion(IRI predicate) {
        Assertion assertion = propertyToAssertion.get(predicate);
        if (assertion == null) {
            if (unspecifiedProperty != null) {
                assertion = Assertion
                        .createPropertyAssertion(SesameUtils.toJavaUri(predicate), unspecifiedProperty.isInferred());
            } else {
                assertion = null;
            }
        } else if (assertion.getType() == Assertion.AssertionType.PROPERTY) {
            // If the property was unspecified, create assertion based on the actual property URI
            assertion = Assertion.createPropertyAssertion(SesameUtils.toJavaUri(predicate), assertion.isInferred());
        }
        return assertion;
    }

    private Optional<Value<?>> createValue(Assertion.AssertionType assertionType, org.eclipse.rdf4j.model.Value value) {
        switch (assertionType) {
            case DATA_PROPERTY:
                if (!(value instanceof Literal)) {
                    return Optional.empty();
                }
                return Optional.of(new Value<>(SesameUtils.getDataPropertyValue((Literal) value)));
            case CLASS:
                if (!(value instanceof Resource)) {
                    return Optional.empty();
                }
                return Optional.of(new Value<>(SesameUtils.toJavaUri((Resource) value)));
            case OBJECT_PROPERTY:
                if (!(value instanceof Resource)) {
                    return Optional.empty();
                }
                return Optional.of(new Value<>(NamedResource.create(value.stringValue())));
            case ANNOTATION_PROPERTY:   // Intentional fall-through
            case PROPERTY:
                return Optional.of(resolveValue(value));
        }
        return Optional.empty();
    }

    private Value<?> resolveValue(org.eclipse.rdf4j.model.Value object) {
        if (object instanceof Literal) {
            return new Value<>(SesameUtils.getDataPropertyValue((Literal) object));
        } else {
            return new Value<>(NamedResource.create(object.stringValue()));
        }
    }
}
