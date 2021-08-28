package me.neznamy.tab.api.placeholder;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;

/**
 * Representation of any placeholder
 */
public abstract class Placeholder {

	private static final String[] EMPTY_ARRAY = new String[0];
	
	//refresh interval of the placeholder
	private int refresh;
	
	//placeholder identifier including %
	protected String identifier;
	
	//premium replacements
	protected Map<Object, String> replacements = new HashMap<>();
	
	/**
	 * Constructs new instance with given parameters and loads placeholder output replacements
	 * @param identifier - placeholder identifier
	 * @param refresh - refresh interval in millieconds
	 */
	protected Placeholder(String identifier, int refresh) {
		if (refresh % 50 != 0) throw new IllegalArgumentException("Refresh interval must be divisible by 50");
		if (!identifier.startsWith("%") || !identifier.endsWith("%")) throw new IllegalArgumentException("Identifier must start and end with %");
		this.identifier = identifier;
		this.refresh = refresh;
		loadReplacements();
	}

	private void loadReplacements() {
		Map<Object, Object> original = TabAPI.getInstance().getConfig().getConfigurationSection("placeholder-output-replacements." + identifier);
		for (Entry<Object, Object> entry : original.entrySet()) {
			String key = entry.getKey().toString();
			String value = entry.getValue().toString();
			replacements.put(EnumChatFormat.color(key), EnumChatFormat.color(value));
			//snakeyaml converts yes & no to booleans, making them not work when used without "
			if (key.equals("true")) {
				replacements.put("yes", value);
			}
			if (key.equals("false")) {
				replacements.put("no", value);
			}
		}
	}
	
	/**
	 * Returns placeholder's identifier
	 * @return placeholder's identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * Returns placeholder's refresh interval
	 * @return placeholder's refresh interval
	 */
	public int getRefresh() {
		return refresh;
	}

	/**
	 * Replaces this placeholder in given string and returns output
	 * @param s - string to replace this placeholder in
	 * @param p - player to set placeholder for
	 * @return string with this placeholder replaced
	 */
	public String set(String s, TabPlayer p) {
		try {
			String originalvalue = getLastValue(p);
			String value = TabAPI.getInstance().getPlaceholderManager().findReplacement(replacements, originalvalue);
			value = replace(value, "%value%", originalvalue);
			return replace(s, identifier, value);
		} catch (Exception t) {
			TabAPI.getInstance().getErrorManager().printError("An error occurred when setting placeholder " + identifier + (p == null ? "" : " for " + p.getName()), t);
			return s;
		}
	}
	
	public String[] getNestedPlaceholders(String output) {
		if (!output.contains("%")) return EMPTY_ARRAY;
		return TabAPI.getInstance().getPlaceholderManager().detectPlaceholders(output).toArray(EMPTY_ARRAY);
	}
	
	private String replace(String string, String original, String replacement) {
		if (!string.contains(original)) return string;
		if (string.equals(original)) return replacement;
		return string.replace(original, replacement);
	}

	/**
	 * Applies all placeholders from outputs
	 * @param text - replaced placeholder
	 * @param p - player to replace for
	 * @return text with replaced placeholders in output
	 */
	protected Object setPlaceholders(Object text, TabPlayer p) {
		if (!(text instanceof String) || identifier.equals(text)) return text;
		Object replaced = text;
		for (String s : getNestedPlaceholders((String) text)) {
			if (s.equals("%value%") || s.equals(identifier) || s.startsWith("%rel_")) continue;
			replaced = TabAPI.getInstance().getPlaceholderManager().getPlaceholder(s).set(replaced.toString(), p);
		}
		return replaced;
	}
	
	/**
	 * Returns last known value for defined player
	 * @param p - player to check value for
	 * @return last known value
	 */
	public abstract String getLastValue(TabPlayer p);
}