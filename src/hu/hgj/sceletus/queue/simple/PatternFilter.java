package hu.hgj.sceletus.queue.simple;

import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class PatternFilter<T> implements Predicate<T> {

	protected final Set<Pattern> patterns;

	public PatternFilter(Set<Pattern> patterns) {
		this.patterns = patterns;
	}

	@Override
	public boolean test(T object) {
		for (Pattern pattern : patterns) {
			if (pattern.matcher(object.toString()).matches()) {
				return true;
			}
		}
		return false;
	}

}
