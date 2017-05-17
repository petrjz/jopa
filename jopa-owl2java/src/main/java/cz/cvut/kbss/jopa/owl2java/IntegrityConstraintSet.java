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
package cz.cvut.kbss.jopa.owl2java;

import cz.cvut.kbss.jopa.ic.api.AtomicSubClassConstraint;
import cz.cvut.kbss.jopa.ic.api.DataDomainConstraint;
import cz.cvut.kbss.jopa.ic.api.DataParticipationConstraint;
import cz.cvut.kbss.jopa.ic.api.DataRangeConstraint;
import cz.cvut.kbss.jopa.ic.api.IntegrityConstraint;
import cz.cvut.kbss.jopa.ic.api.IntegrityConstraintFactory;
import cz.cvut.kbss.jopa.ic.api.IntegrityConstraintVisitor;
import cz.cvut.kbss.jopa.ic.api.ObjectDomainConstraint;
import cz.cvut.kbss.jopa.ic.api.ObjectParticipationConstraint;
import cz.cvut.kbss.jopa.ic.api.ObjectRangeConstraint;
import cz.cvut.kbss.jopa.ic.impl.IntegrityConstraintFactoryImpl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;

public class IntegrityConstraintSet {
    private Map<OWLClass, List<IntegrityConstraint>> cConstraints = new HashMap<>();
    private Map<OWLClass, Map<OWLObjectProperty, Collection<IntegrityConstraint>>> opConstraints = new HashMap<>();
    private Map<OWLClass, Map<OWLDataProperty, Collection<IntegrityConstraint>>> dpConstraints = new HashMap<>();

    private IntegrityConstraintFactory integrityConstraintFactory = new IntegrityConstraintFactoryImpl();

    private <K, V> void addToMap(final K key, final V value, final Map<K, Collection<V>> map) {
        if (!map.containsKey(key)) {
            map.put(key, new HashSet<>());
        }
        map.get(key).add(value);
    }

    public void addIntegrityConstraint(final IntegrityConstraint ic) {
        ic.accept(new IntegrityConstraintVisitor() {
            @Override
            public void visit(AtomicSubClassConstraint cpc) {
                List<IntegrityConstraint> setC = cConstraints
                    .get(cpc.getSubClass());
                if (setC == null) {
                    setC = new ArrayList<>();
                    cConstraints.put(cpc.getSubClass(), setC);
                }
                setC.add(cpc);
            }

            @Override
            public void visit(DataParticipationConstraint cpc) {
                addToClassDataIntegrityConstraintSet(cpc.getSubject(), cpc.getPredicate(), cpc);
            }

            @Override
            public void visit(ObjectParticipationConstraint cpc) {
                addToClassObjectIntegrityConstraintSet(cpc.getSubject(), cpc.getPredicate(), cpc);
            }

            @Override
            public void visit(ObjectDomainConstraint cpc) {
                addToClassObjectIntegrityConstraintSet(cpc.getDomain(), cpc.getProperty(), cpc);
            }

            @Override
            public void visit(ObjectRangeConstraint cpc) {
                addToClassObjectIntegrityConstraintSet(cpc.getOWLClass(), cpc.getProperty(), cpc);
            }

            @Override
            public void visit(DataDomainConstraint cpc) {
                addToClassDataIntegrityConstraintSet(cpc.getDomain(), cpc.getProperty(), cpc);
            }

            @Override
            public void visit(DataRangeConstraint cpc) {
                addToClassDataIntegrityConstraintSet(cpc.getOWLClass(), cpc.getProperty(), cpc);
            }
        });
    }

    private Map<OWLObjectProperty, Collection<IntegrityConstraint>> addToClassObjectIntegrityConstraintSet(final OWLClass subjClass, final OWLObjectProperty p, final IntegrityConstraint ic) {
        Map<OWLObjectProperty, Collection<IntegrityConstraint>> map = opConstraints
            .get(subjClass);
        if (map == null) {
            map = new HashMap<>();
            opConstraints.put(subjClass, map);
        }
        addToMap(p, ic, map);
        return map;
    }

    private Map<OWLDataProperty, Collection<IntegrityConstraint>> addToClassDataIntegrityConstraintSet(final OWLClass subjClass, final OWLDataProperty p, final IntegrityConstraint ic) {
        Map<OWLDataProperty, Collection<IntegrityConstraint>> map = dpConstraints
            .get(subjClass);
        if (map == null) {
            map = new HashMap<>();
            dpConstraints.put(subjClass, map);
        }
        addToMap(p, ic, map);
        return map;
    }

    public List<IntegrityConstraint> getClassIntegrityConstraints(final OWLClass cls) {
        if (cConstraints.containsKey(cls)) {
            return cConstraints.get(cls);
        } else {
            return Collections.emptyList();
        }
    }

    public Collection<IntegrityConstraint> getClassObjectIntegrityConstraints(
        final OWLClass clazz,
        final OWLObjectProperty prop) {

        final Map<OWLObjectProperty, Collection<IntegrityConstraint>> constraints = opConstraints
            .get(clazz);

        if (constraints != null && constraints.containsKey(prop)) {
            return constraints.get(prop);
        } else {
            return Collections.emptyList();
        }
    }

    public Collection<IntegrityConstraint> getClassDataIntegrityConstraints(
        final OWLClass clazz,
        final OWLDataProperty prop) {

        final Map<OWLDataProperty, Collection<IntegrityConstraint>> constraints = dpConstraints
            .get(clazz);

        if (constraints != null && constraints.containsKey(prop)) {
            return constraints.get(prop);
        } else {
            return Collections.emptyList();
        }
    }
}