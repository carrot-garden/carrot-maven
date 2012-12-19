package index;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class TestIndex {

	public void log(final Object text) {
		System.out.println("### " + text);
	}

	@Ignore
	@Test
	public void test() throws Exception {

		final WebClient webClient = new WebClient();

		final HtmlPage page = webClient
				.getPage("file:./src/test/resources/index .html");

		@SuppressWarnings("unchecked")
		final List<HtmlAnchor> list = (List<HtmlAnchor>) page.getByXPath("//a");

		for (final HtmlAnchor anchor : list) {

			log(anchor.getHrefAttribute());
			log(anchor.getTextContent());

		}

	}

}
