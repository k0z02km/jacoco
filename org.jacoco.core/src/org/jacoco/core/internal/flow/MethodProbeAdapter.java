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

import org.objectweb.asm.commons.AnalyzerAdapter;

public interface MethodProbeAdapter {

	void setAnalyzer(final AnalyzerAdapter analyzer);
}
