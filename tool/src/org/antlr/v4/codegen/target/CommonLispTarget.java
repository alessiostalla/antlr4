/*
 * Copyright (c) 2012-2017 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 */

package org.antlr.v4.codegen.target;

import org.antlr.v4.codegen.CodeGenerator;
import org.antlr.v4.codegen.Target;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CommonLispTarget extends Target {

	protected static final HashSet<String> reservedWords = new HashSet<>(Arrays.asList());

	public CommonLispTarget(CodeGenerator gen) {
		super(gen);
	}

	@Override
    public Set<String> getReservedWords() {
		return reservedWords;
	}

	@Override
	public boolean wantsBaseListener() {
		return false;
	}

	@Override
	public boolean wantsBaseVisitor() {
		return false;
	}
}
