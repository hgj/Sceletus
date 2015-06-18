package hu.hgj.sceletus.test.modules.implementations;

import hu.hgj.sceletus.module.SingleThreadedModule;

import java.util.Set;

public class SimpleSingleThreadedModule extends SingleThreadedModule {

	protected final Set<Integer> outputSet;

	public SimpleSingleThreadedModule(String name, Set<Integer> outputSet) {
		super(name);
		this.outputSet = outputSet;
	}

	@Override
	protected void main() {
		outputSet.add(0);
	}

}
