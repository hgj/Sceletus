package hu.hgj.sceletus.queue.simple;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PatternMapFilter<T extends Map<String, ?>> implements Predicate<T> {

	protected final Map<String, Pattern> patterns;

	public PatternMapFilter(Map<String, Pattern> patterns) {
		this.patterns = patterns;
	}

	@Override
	public boolean test(T map) {
		for (Map.Entry<String, Pattern> patternEntry : patterns.entrySet()) {
			String path = patternEntry.getKey();
			Pattern pattern = patternEntry.getValue();
			Object element;
			if (path.startsWith("$")) {
				// This is a JsonPath, try to get the element
				try {
					element = JsonPath.read(map, path);
				} catch (PathNotFoundException ignored) {
					// We did not find the element, so this topic does not match
					return false;
				}
			} else {
				// This is a normal key, try to get the element
				element = map.get(path);
			}
			if (element == null) {
				return false;
			}
			if (!pattern.matcher(element.toString()).matches()) {
				return false;
			}
		}
		return true;
	}

	public static <T extends Map<String, ?>> PatternMapFilter<T> fromRegexMap(Map<String, String> regexMap) {
		return new PatternMapFilter<>(regexMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> Pattern.compile(entry.getValue()))));
	}

}
