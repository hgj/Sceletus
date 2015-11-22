package hu.hgj.sceletus.modules.implementations;

import hu.hgj.sceletus.module.CommandInterpreterModule;

import java.util.List;

public class FooBarCommandInterpreterModule<T> extends CommandInterpreterModule<T> {

	public FooBarCommandInterpreterModule(String name) {
		super(name);
	}

	@Override
	protected boolean filterCommand(String command) {
		return true;
	}

	@Override
	public String interpretCommand(List<String> arguments) {
		return "foobar";
	}

}
