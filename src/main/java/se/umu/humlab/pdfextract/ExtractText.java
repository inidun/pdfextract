package se.umu.humlab.pdfextract;

import java.util.Arrays;
import java.util.List;

public class ExtractText {

	public static void main(String[] args) throws Exception {

		PDFCourier2Text stripper = new PDFCourier2Text(5.5f, 8);
		List<String> pages = stripper.extractText("src/test/resources/012656engo.pdf");
		List<List<PDFCourier2Text.TitleInfo>> titles = stripper.getTitles();
		System.out.println(titles.size());
		System.out.println(Arrays.toString(pages.toArray()));

	}

}
