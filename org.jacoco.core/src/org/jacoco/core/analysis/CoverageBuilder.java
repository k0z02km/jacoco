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
package org.jacoco.core.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.internal.analysis.BundleCoverageImpl;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.SourceFileCoverageImpl;
import org.jacoco.core.internal.analysis.SourceNodeImpl;

/**
 * Builder for hierarchical {@link ICoverageNode} structures from single
 * {@link IClassCoverage} nodes. The nodes are feed into the builder through its
 * {@link ICoverageVisitor} interface. Afterwards the aggregated data can be
 * obtained with {@link #getClasses()}, {@link #getSourceFiles()} or
 * {@link #getBundle(String)} in the following hierarchy:
 *
 * <pre>
 * {@link IBundleCoverage}
 * +-- {@link IPackageCoverage}*
 *     +-- {@link IClassCoverage}*
 *     +-- {@link ISourceFileCoverage}*
 * </pre>
 */
public class CoverageBuilder implements ICoverageVisitor {

	private final Map<String, IClassCoverage> classes;

	private Map<String, ISourceFileCoverage> sourcefiles;

	/**
	 * Create a new builder.
	 *
	 */
	public CoverageBuilder() {
		this.classes = new HashMap<String, IClassCoverage>();
		this.sourcefiles = new HashMap<String, ISourceFileCoverage>();
	}

	/**
	 * Returns all class nodes currently contained in this builder.
	 *
	 * @return all class nodes
	 */
	public Collection<IClassCoverage> getClasses() {
		build();
		return Collections.unmodifiableCollection(classes.values());
	}

	/**
	 * Returns all source file nodes currently contained in this builder.
	 *
	 * @return all source file nodes
	 */
	public Collection<ISourceFileCoverage> getSourceFiles() {
		build();
		return Collections.unmodifiableCollection(sourcefiles.values());
	}

	/**
	 * Creates a bundle from all nodes currently contained in this bundle.
	 *
	 * @param name
	 *            Name of the bundle
	 * @return bundle containing all classes and source files
	 */
	public IBundleCoverage getBundle(final String name) {
		build();
		return new BundleCoverageImpl(name, classes.values(),
				sourcefiles.values());
	}

	/**
	 * Returns all classes for which execution data does not match.
	 *
	 * @see IClassCoverage#isNoMatch()
	 * @return collection of classes with non-matching execution data
	 */
	public Collection<IClassCoverage> getNoMatchClasses() {
		final Collection<IClassCoverage> result = new ArrayList<IClassCoverage>();
		for (final IClassCoverage c : classes.values()) {
			if (c.isNoMatch()) {
				result.add(c);
			}
		}
		return result;
	}

	private void build() {

		// System.out.println("COVERAGE BUILDER CLASS COVERAGE OUTPUT");
		for (final IClassCoverage c : classes.values()) {
			c.print();
		}

		if (sourcefiles != null) {
			return;
		}
		this.sourcefiles = new HashMap<String, ISourceFileCoverage>();
		for (final IClassCoverage c : classes.values()) {
			for (final SourceNodeImpl fragment : ((ClassCoverageImpl) c)
					.getFragments()) {
				final SourceNodeImpl classCoverage = (SourceNodeImpl) classes
						.get(fragment.getName());
				if (classCoverage != null) {
					classCoverage.applyFragment(fragment);
				}
			}
		}
		for (final IClassCoverage coverage : classes.values()) {
			final String source = coverage.getSourceFileName();
			if (source != null) {
				final SourceFileCoverageImpl sourceFile = getSourceFile(source,
						coverage.getPackageName());
				sourceFile.increment(coverage);
			}
		}
	}

	// === ICoverageVisitor ===

	public void visitCoverage(final IClassCoverage coverage) {
		sourcefiles = null;
		final String name = coverage.getName();
		final IClassCoverage dup = classes.put(name, coverage);
		if (dup != null) {
			if (dup.getId() != coverage.getId()) {
				throw new IllegalStateException(
						"Can't add different class with same name: " + name);
			}
		}
	}

	private SourceFileCoverageImpl getSourceFile(final String filename,
			final String packagename) {
		final String key = packagename + '/' + filename;
		SourceFileCoverageImpl sourcefile = (SourceFileCoverageImpl) sourcefiles
				.get(key);
		if (sourcefile == null) {
			sourcefile = new SourceFileCoverageImpl(filename, packagename);
			sourcefiles.put(key, sourcefile);
		}
		return sourcefile;
	}

}
