package ru.goncharenko.account.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.regex.Pattern;

@Component
public class Generator {
	public String generateLogin(String firstname, String surname) {
		if (StringUtils.isBlank(firstname) || StringUtils.isBlank(surname)) {
			return "";
		}

		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

		String normalizedForName = Normalizer.normalize(firstname.toLowerCase().substring(0, 1), Normalizer.Form.NFD);
		String withoutDiacriticsForName = pattern.matcher(normalizedForName).replaceAll("");

		String normalized = Normalizer.normalize(surname.toLowerCase(), Normalizer.Form.NFD);
		String withoutDiacritics = pattern.matcher(normalized).replaceAll("");

		return withoutDiacriticsForName + "." + withoutDiacritics
				.replace("ё", "yo")
				.replace("ж", "zh")
				.replace("й", "j")
				.replace("ц", "ts")
				.replace("ч", "ch")
				.replace("ш", "sh")
				.replace("щ", "shch")
				.replace("ъ", "")
				.replace("ы", "y")
				.replace("ь", "")
				.replace("э", "e")
				.replace("ю", "yu")
				.replace("я", "ya");
	}
}
