package se.umu.humlab.pdfextract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

public class PDFCourier2TextTest {

    @Test
    public void shouldBeJapanese() throws IOException {
        PDFCourier2Text stripper = new PDFCourier2Text(5.5f, 8);
        stripper.extractText("src/test/resources/012656engo.pdf");
        List<List<PDFCourier2Text.TitleInfo>> titles = stripper.getTitles();
        assertEquals(35, titles.size());
        assertTrue(titles.get(2).get(0).title.contains("Japanese"));
        assertEquals(114, titles.get(2).get(0).position);
    }

    @Test
    public void pageShouldContainString() throws IOException {
        PDFCourier2Text stripper = new PDFCourier2Text(5.5f, 8);
        List<String> pages = stripper.extractText("src/test/resources/012656engo.pdf");
        assertEquals(35, pages.size());
        assertTrue(pages.get(1).contains("TREASURES"));
    }

    @Test
    public void theRubberManShouldHaveCorrectPosition() throws IOException {
        PDFCourier2Text stripper = new PDFCourier2Text(5.5f, 8);

        List<String> pages = stripper.extractText("src/test/resources/077050engo.pdf");
        List<List<PDFCourier2Text.TitleInfo>> titles = stripper.getTitles();

        String page_text = pages.get(33);
        List<PDFCourier2Text.TitleInfo> page_titles = titles.get(33);

        assertTrue("Correct text start", page_text.startsWith("set up its tents and give its street parade"));
        assertTrue("Correct text end", page_text.endsWith("(1983), and Verdi's opera Aida (1987)."));

        assertEquals("Number of titles", 1, page_titles.size());
        assertTrue("Title start", page_titles.get(0).title.startsWith("The rubber man"));

        int title_position = page_text.indexOf("The rubber man");

        assertTrue("Title position upper", page_titles.get(0).position <= title_position);
        assertTrue("Title position lower", page_titles.get(0).position > title_position - 10);

    }

}
