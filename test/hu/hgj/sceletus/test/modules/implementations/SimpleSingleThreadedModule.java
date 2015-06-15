package hu.hgj.sceletus.test.modules.implementations;

import hu.hgj.sceletus.SingleThreadedModule;

import java.util.Set;

public class SimpleSingleThreadedModule extends SingleThreadedModule {

	protected final Set<Integer> outputSet;

	public SimpleSingleThreadedModule(Set<Integer> outputSet) {
		this.outputSet = outputSet;
	}

	@Override
	protected void main() {
		outputSet.add(0);
	}

}
