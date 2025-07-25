/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;

/**
 * Implementation of {@link IClassCoverage}.
 */
public class ClassCoverageImpl extends SourceNodeImpl
		implements IClassCoverage {

	private final long id;
	private final boolean noMatch;
	private final Collection<IMethodCoverage> methods;
	private String signature;
	private String superName;
	private String[] interfaces;
	private String sourceFileName;

	private Collection<SourceNodeImpl> fragments = Collections.emptyList();

	/**
	 * Creates a class coverage data object with the given parameters.
	 *
	 * @param name
	 *            VM name of the class
	 * @param id
	 *            class identifier
	 * @param noMatch
	 *            <code>true</code>, if class id does not match with execution
	 *            data
	 */
	public ClassCoverageImpl(final String name, final long id,
			final boolean noMatch) {
		super(ElementType.CLASS, name);
		this.id = id;
		this.noMatch = noMatch;
		this.methods = new ArrayList<IMethodCoverage>();
	}

	/**
	 * Add a method to this class.
	 *
	 * @param method
	 *            method data to add
	 */
	public void addMethod(final IMethodCoverage method) {
		this.methods.add(method);
		increment(method);
		// Class is considered as covered when at least one method is covered:
		if (methodCounter.getCoveredCount() > 0) {
			// System.out.println("Class COVERED");
			this.classCounter = CounterImpl.COUNTER_0_1;
		} else {
			// System.out.println("Class NOT COVERED");
			this.classCounter = CounterImpl.COUNTER_1_0;
		}
	}

	public void print() {
		/*
		 * System.out.println("Printing for: " + sourceFileName + " " +
		 * superName + " " + signature); System.out.println("Class counter: " +
		 * this.classCounter.missed + ", " + this.classCounter.covered);
		 *
		 * System.out.println("Method counter: " + this.methodCounter.missed +
		 * ", " + this.methodCounter.covered);
		 *
		 * for (final IMethodCoverage method : methods) { method.print(); } (
		 */
	}

	/**
	 * Sets the VM signature of the class.
	 *
	 * @param signature
	 *            VM signature of the class (may be <code>null</code>)
	 */
	public void setSignature(final String signature) {
		this.signature = signature;
	}

	/**
	 * Sets the VM name of the superclass.
	 *
	 * @param superName
	 *            VM name of the super class (may be <code>null</code>, i.e.
	 *            <code>java/lang/Object</code>)
	 */
	public void setSuperName(final String superName) {
		this.superName = superName;
	}

	/**
	 * Sets the VM names of implemented/extended interfaces.
	 *
	 * @param interfaces
	 *            VM names of implemented/extended interfaces
	 */
	public void setInterfaces(final String[] interfaces) {
		this.interfaces = interfaces;
	}

	/**
	 * Sets the name of the corresponding source file for this class.
	 *
	 * @param sourceFileName
	 *            name of the source file
	 */
	public void setSourceFileName(final String sourceFileName) {
		this.sourceFileName = sourceFileName;
	}

	/**
	 * @return fragments stored in this class
	 */
	public Collection<SourceNodeImpl> getFragments() {
		return fragments;
	}

	/**
	 * Stores fragments that contain coverage information about other nodes
	 * collected during the creation of this node.
	 *
	 * @param fragments
	 *            fragments to store
	 */
	public void setFragments(final Collection<SourceNodeImpl> fragments) {
		this.fragments = fragments;
	}

	@Override
	public boolean applyFragment(final SourceNodeImpl fragment) {
		super.applyFragment(fragment);
		for (final IMethodCoverage methodCoverage : methods) {
			final int mm = methodCoverage.getMethodCounter().getMissedCount();
			final int cm = methodCoverage.getMethodCounter().getCoveredCount();
			final int mc = methodCoverage.getComplexityCounter()
					.getMissedCount();
			final int cc = methodCoverage.getComplexityCounter()
					.getCoveredCount();
			if (((MethodCoverageImpl) methodCoverage).applyFragment(fragment)) {
				methodCounter = methodCounter.increment(
						methodCoverage.getMethodCounter().getMissedCount() - mm,
						methodCoverage.getMethodCounter().getCoveredCount()
								- cm);
				complexityCounter = complexityCounter.increment(
						methodCoverage.getComplexityCounter().getMissedCount()
								- mc,
						methodCoverage.getComplexityCounter().getCoveredCount()
								- cc);
			}
		}
		classCounter = methodCounter.getCoveredCount() > 0
				? CounterImpl.COUNTER_0_1
				: CounterImpl.COUNTER_1_0;
		return true;
	}

	// === IClassCoverage implementation ===

	public long getId() {
		return id;
	}

	public boolean isNoMatch() {
		return noMatch;
	}

	public String getSignature() {
		return signature;
	}

	public String getSuperName() {
		return superName;
	}

	public String[] getInterfaceNames() {
		return interfaces;
	}

	public String getPackageName() {
		final int pos = getName().lastIndexOf('/');
		return pos == -1 ? "" : getName().substring(0, pos);
	}

	public String getSourceFileName() {
		return sourceFileName;
	}

	public Collection<IMethodCoverage> getMethods() {
		return methods;
	}

}
