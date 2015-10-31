package hu.hgj.sceletus.queue.simple;

import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

	public static <T> PatternFilter<T> fromRegexSet(Set<String> regexes) {
		return new PatternFilter<>(regexes.stream().map(Pattern::compile).collect(Collectors.toSet()));
	}

}
