package cz.cvut.kbss.owlpersistence.model.ic;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLProperty;

abstract class AbstractParticipationConstraintImpl<P extends OWLProperty<?, ?>, O extends OWLObject>
		implements ParticipationConstraint<P, O> {

	private OWLClass subject;

	private P predicate;

	private O object;

	private int min;

	private int max;

	public AbstractParticipationConstraintImpl(final OWLClass subject,
			final P predicate, final O object, final int min, final int max) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		this.min = min;
		this.max = max;
	}

	@Override
	public O getObject() {
		return object;
	}

	@Override
	public P getPredicate() {
		return predicate;
	}

	@Override
	public OWLClass getSubject() {
		return subject;
	}

	@Override
	public int getMin() {
		return min;
	}

	@Override
	public int getMax() {
		return max;
	}
}