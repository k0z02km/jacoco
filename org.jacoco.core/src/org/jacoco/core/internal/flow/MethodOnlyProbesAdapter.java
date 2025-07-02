/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Katherine Zhang - adding method coverage only option
 *
 *******************************************************************************/

package org.jacoco.core.internal.flow;

import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AnalyzerAdapter;

/**
 * Adapter that creates additional visitor events for probes to be inserted into
 * a method.
 */
public final class MethodOnlyProbesAdapter extends MethodVisitor
		implements MethodProbeAdapter {

	private final MethodProbesVisitor probesVisitor;

	private final IProbeIdGenerator idGenerator;

	private AnalyzerAdapter analyzer;

	private boolean probeInserted;

	private final Map<Label, Label> tryCatchProbeLabels;

	/**
	 * Create a new adapter instance.
	 *
	 * @param probesVisitor
	 *            visitor to delegate to
	 * @param idGenerator
	 *            generator for unique probe ids
	 */
	public MethodOnlyProbesAdapter(final MethodProbesVisitor probesVisitor,
			final IProbeIdGenerator idGenerator) {
		super(InstrSupport.ASM_API_VERSION, probesVisitor);
		this.probesVisitor = probesVisitor;
		this.idGenerator = idGenerator;
		this.probeInserted = false;
		this.tryCatchProbeLabels = new HashMap<Label, Label>();

	}

	@Override
	public void visitCode() {
		super.visitCode();

		if (probeInserted) {
			System.out.println("[WARNING] 'nextId' CALLED MULTIPLE TIMES!!");
		} else {
			System.out.println(
					"Visiting Probe " + probesVisitor.getClass().getName());
			probesVisitor.visitProbe(idGenerator.nextId());
			probeInserted = true;
		}
	}

	/**
	 * If an analyzer is set {@link IFrame} handles are calculated and emitted
	 * to the probes methods.
	 *
	 * @param analyzer
	 *            optional analyzer to set
	 */

	public void setAnalyzer(final AnalyzerAdapter analyzer) {
		this.analyzer = analyzer;
	}

	@Override
	public void visitTryCatchBlock(final Label start, final Label end,
			final Label handler, final String type) {
		probesVisitor.visitTryCatchBlock(getTryCatchLabel(start),
				getTryCatchLabel(end), handler, type);
	}

	private Label getTryCatchLabel(Label label) {
		if (tryCatchProbeLabels.containsKey(label)) {
			label = tryCatchProbeLabels.get(label);
		}
		return label;
	}

	@Override
	public void visitLabel(final Label label) {
		if (LabelInfo.needsProbe(label)) {
			if (tryCatchProbeLabels.containsKey(label)) {
				probesVisitor.visitLabel(tryCatchProbeLabels.get(label));
			}
			// probesVisitor.visitProbe(idGenerator.nextId());
		}
		probesVisitor.visitLabel(label);
	}

	@Override
	public void visitInsn(final int opcode) {
		probesVisitor.visitInsn(opcode);
	}

	@Override
	public void visitJumpInsn(final int opcode, final Label label) {
		probesVisitor.visitJumpInsn(opcode, label);
	}

	@Override
	public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
			final Label[] labels) {
		probesVisitor.visitLookupSwitchInsn(dflt, keys, labels);
	}

	@Override
	public void visitTableSwitchInsn(final int min, final int max,
			final Label dflt, final Label... labels) {
		probesVisitor.visitTableSwitchInsn(min, max, dflt, labels);
	}

	private IFrame frame(final int popCount) {
		return FrameSnapshot.create(analyzer, popCount);
	}

}
