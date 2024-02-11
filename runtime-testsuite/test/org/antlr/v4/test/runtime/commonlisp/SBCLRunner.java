/*
 * Copyright (c) 2012-2017 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 */
package org.antlr.v4.test.runtime.commonlisp;

import org.antlr.v4.test.runtime.*;
import org.antlr.v4.test.runtime.states.CompiledState;
import org.antlr.v4.test.runtime.states.GeneratedState;
import org.stringtemplate.v4.ST;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static org.antlr.v4.test.runtime.FileUtils.writeFile;

public class SBCLRunner extends RuntimeRunner {
	@Override
	public String getLanguage() {
		return "CommonLisp";
	}

	@Override
	public String getExtension() { return "lisp"; }

	@Override
	public String getBaseListenerSuffix() { return null; }

	@Override
	public String getBaseVisitorSuffix() { return null; }

	@Override
	public String getRuntimeToolName() { return "sbcl"; }

	private final static String normalizedRuntimePath = getRuntimePath("CommonLisp").replace('\\', '/');

	@Override
	protected void addExtraRecognizerParameters(ST template) {
		template.add("runtimePath", normalizedRuntimePath);
	}

	@Override
	protected String[] getExtraRunArgs() {
		return new String[] { "--non-interactive", "--load" };
	}
}
