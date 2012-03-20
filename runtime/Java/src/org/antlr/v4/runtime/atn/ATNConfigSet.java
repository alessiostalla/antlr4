/*
 [The "BSD license"]
 Copyright (c) 2011 Terence Parr
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.antlr.v4.runtime.atn;

import org.antlr.v4.runtime.misc.IntervalSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/** Specialized OrderedHashSet that can track info about the set.
 *  Might be able to optimize later w/o affecting code that uses this set.
 */
public class ATNConfigSet implements Set<ATNConfig> {
	public static class Key {
		ATNState state;
		int alt;
		SemanticContext semanticContext;

		public Key(ATNState state, int alt, SemanticContext semanticContext) {
			this.state = state;
			this.alt = alt;
			this.semanticContext = semanticContext;
		}

		public Key(ATNConfig c) {
			this(c.state, c.alt, c.semanticContext);
		}

		@Override
		public boolean equals(Object obj) {
			if ( obj==this ) return true;
			if ( this.hashCode() != obj.hashCode() ) return false;
			if ( !(obj instanceof Key) ) return false;
			Key key = (Key)obj;
			return this.state.stateNumber==key.state.stateNumber
				&& this.alt==key.alt
				&& this.semanticContext.equals(key.semanticContext);
		}

		@Override
		public int hashCode() {
			int hashCode = 7;
			hashCode = 5 * hashCode + state.stateNumber;
			hashCode = 5 * hashCode + alt;
			hashCode = 5 * hashCode + semanticContext.hashCode();
	        return hashCode;
		}
	}

	public class SetIterator implements Iterator<ATNConfig> {
		int i = 0;
		@Override
		public boolean hasNext() { return i < configs.size(); }

		@Override
		public ATNConfig next() {
			ATNConfig c = configs.get(i);
			i++;
			return c;
		}

		@Override
		public void remove() { throw new UnsupportedOperationException(); }
	}

	/** Track every config we add */
	protected final LinkedHashMap<Key,PredictionContext> configToContext =
		new LinkedHashMap<Key, PredictionContext>();

	/** Track the elements as they are added to the set; supports get(i) */
	protected final ArrayList<ATNConfig> configs = new ArrayList<ATNConfig>();

	// TODO: these fields make me pretty uncomfortable but nice to pack up info together, saves recomputation
	// TODO: can we track conflicts as they are added to save scanning configs later?
	public int uniqueAlt;
	public IntervalSet conflictingAlts;
	public boolean hasSemanticContext;
	public boolean dipsIntoOuterContext;

	public ATNConfigSet() { }

	public ATNConfigSet(ATNConfigSet old) {
		addAll(old);
		this.uniqueAlt = old.uniqueAlt;
		this.conflictingAlts = old.conflictingAlts;
		this.hasSemanticContext = old.hasSemanticContext;
		this.dipsIntoOuterContext = old.dipsIntoOuterContext;
	}

	/** Adding a new config means merging contexts with existing configs for
	 *  (s, i, pi, _)
	 *  We use (s,i,pi) as key
	 */
	@Override
	public boolean add(ATNConfig value) {
		Key key = new Key(value);
		PredictionContext existing = configToContext.get(key);
		if ( existing==null ) { // nothing there yet; easy, just add
			configs.add(value);
			configToContext.put(key, value.context);
			return true;
		}
		// a previous (s,i,pi,_), merge with it and save result
		PredictionContext merged = PredictionContext.merge(existing, value.context, true);
		configToContext.put(key, merged); // replace
		// if already there, must be in configs already
		return true;
	}

	/** Return the List holding list of table elements.  Note that you are
     *  NOT getting a copy so don't write to the list.
     */
    public List<ATNConfig> elements() {
        return configs;
    }

	public Set<ATNState> getStates() {
		Set<ATNState> states = new HashSet<ATNState>();
		for (Key key : this.configToContext.keySet()) {
			states.add(key.state);
		}
		return states;
	}

	public ATNConfig get(int i) {
		return configs.get(i);
	}

	public void remove(int i) {
		ATNConfig c = configs.remove(i);
		configToContext.remove(new Key(c));
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(configs.toString());
		if ( hasSemanticContext ) buf.append(",hasSemanticContext="+hasSemanticContext);
		if ( uniqueAlt!=ATN.INVALID_ALT_NUMBER ) buf.append(",uniqueAlt="+uniqueAlt);
		if ( conflictingAlts!=null ) buf.append(",conflictingAlts="+conflictingAlts);
		if ( dipsIntoOuterContext ) buf.append(",dipsIntoOuterContext");
		return buf.toString();
	}

	@Override
	public boolean addAll(Collection<? extends ATNConfig> coll) {
		for (ATNConfig c : coll) {
			add(c);
		}
		return false;
	}

	@Override
	public boolean equals(Object o) {
//		System.out.print("equals " + this + ", " + o+" = ");
		boolean same = configs!=null && configs.equals(((ATNConfigSet)o).configs);
//		System.out.println(same);
		return same;
	}

	@Override
	public int hashCode() {
		return configs.hashCode();
	}

	@Override
	public int size() {
		return configs.size();
	}

	@Override
	public boolean isEmpty() {
		return configs.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		if ( o instanceof ATNConfig ) {
			return configToContext.containsKey(new Key((ATNConfig)o));
		}
		return false;
	}

	@Override
	public Iterator<ATNConfig> iterator() {
		return new SetIterator();
	}

	@Override
	public void clear() {
		configToContext.clear();
		configs.clear();
	}

	// satisfy interface

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}
}
