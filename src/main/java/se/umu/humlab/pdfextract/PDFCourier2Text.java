package se.umu.humlab.pdfextract;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class PDFCourier2Text extends PDFTextStripper {
    public class TitleInfo {
        public int position;
        public String title = "";
        public boolean is_open = true;

        public TitleInfo(String title, int position) {
            this.title = title;
            this.position = position;
        }

        public void close() {
            is_open = false;
        }
    }

    private float titleFontSizeInPt = 5.5f;
    private float currentFontSizeInPt = 0f;
    private int minTitleLengthInCharacters = 4;
    private int minTitleCharacterDistance = 100;
    private int pageCharacterCount = 0;
    private int pageSeparatorCount = 0;
    private PDDocument document = null;
    private List<String> pages = null;
    private List<List<TitleInfo>> pageTitles = null;
    private List<TitleInfo> currentTitles = null;

    public PDFCourier2Text(float titleFontSizeInPt, int minTitleLengthInCharacters) throws IOException {
        this.titleFontSizeInPt = titleFontSizeInPt;
        this.minTitleLengthInCharacters = minTitleLengthInCharacters;
        setLineSeparator(LINE_SEPARATOR);
        setParagraphEnd("");
        setWordSeparator(" ");
    }

    protected TitleInfo getCurrentTitleInfo() {
        try {
            return currentTitles.get(currentTitles.size() - 1);
        } catch (Exception x) {
            return null;
        }
    }

    public PDDocument getDocument() {
        return document;
    }

    public void setDocument(PDDocument document) {
        this.document = document;
    }

    public int getPageSeparatorCount() {
        return pageSeparatorCount;
    }

    public void setPageSeparatorCount(int pageSeparatorCount) {
        this.pageSeparatorCount = pageSeparatorCount;
    }

    public List<String> extractText(String filename) throws IOException {
        output = new StringWriter();
        pages = new ArrayList<String>();
        pageTitles = new ArrayList<List<TitleInfo>>();
        PDDocument document = PDDocument.load(new File(filename));
        StringWriter sw = new StringWriter();
        writeText(document, sw);
        return pages;
    }

    public List<List<TitleInfo>> getTitles() {
        return pageTitles;
    }

    @Override
    protected void startDocument(PDDocument document) throws IOException {
    }

    @Override
    public void endDocument(PDDocument document) throws IOException {
    }

    @Override
    protected void writeLineSeparator() throws IOException {
        if (getCurrentTitleInfo() != null && getCurrentTitleInfo().is_open) {
            getCurrentTitleInfo().title += getLineSeparator();
        }
        setPageSeparatorCount(getPageSeparatorCount() + getLineSeparator().length());
        super.writeLineSeparator();
    }

    @Override
    protected void writeWordSeparator() throws IOException {

        if (getCurrentTitleInfo() != null && getCurrentTitleInfo().is_open) {
            getCurrentTitleInfo().title += getWordSeparator();
        }
        setPageSeparatorCount(getPageSeparatorCount() + getWordSeparator().length());
        super.writeWordSeparator();
    }

    @Override
    protected void writeCharacters(TextPosition text) throws IOException {
        setPageSeparatorCount(getPageSeparatorCount() + text.getUnicode().length());
        super.writeCharacters(text);
    }

    /**
     * Write a string to the output stream, maintain font state, and escape some
     * HTML characters. The font state is only preserved per word.
     *
     * @param text          The text to write to the stream.
     * @param textPositions the corresponding text positions
     * @throws IOException If there is an error writing to the stream.
     */
    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
        pageCharacterCount += text.length();

        if (textPositions.size() > 0) {
            TextPosition textPosition = textPositions.get(0);
            float fontSizeInPt = textPosition.getHeight(); // FIXME: This currently refers to maxHeight not inbuilt
                                                           // fontSizeInPt

            if (fontSizeHasIncreasedAboveThreshold(fontSizeInPt)) {
                // if (distanceToPreviousTitlePositionAboveThreshold()) {
                int currentTitleStartPosition = pageCharacterCount - text.length() + pageSeparatorCount;
                TitleInfo titleInfo = new TitleInfo(text, currentTitleStartPosition);
                currentTitles.add(titleInfo);
                // }
            } else if (fontSizeIsStillAboveThreshold(fontSizeInPt)) {

                if (getCurrentTitleInfo() == null || !getCurrentTitleInfo().is_open)
                    throw new IOException("logical error: still above but no open info");

                getCurrentTitleInfo().title += text;

            } else if (fontSizeHasDroppedBelowThreshold(fontSizeInPt)) {

                if (getCurrentTitleInfo() == null || !getCurrentTitleInfo().is_open)
                    throw new IOException("logical error: dropped but no open info");

                getCurrentTitleInfo().close();

            }
            currentFontSizeInPt = fontSizeInPt;
        }
        output.write(text);
    }

    private boolean distanceToPreviousTitlePositionAboveThreshold() {
        int previousTitlePosition = previousTitlePositionOnSamePage();
        if (previousTitlePosition < 0) {
            return true;
        }
        return pageCharacterCount - previousTitlePosition >= minTitleCharacterDistance;
    }

    private boolean fontSizeHasIncreasedAboveThreshold(float fontSizeInPt) {
        return fontSizeInPt >= titleFontSizeInPt && currentFontSizeInPt < titleFontSizeInPt;
    }

    private boolean fontSizeIsStillAboveThreshold(float fontSizeInPt) {
        return fontSizeInPt >= titleFontSizeInPt && currentFontSizeInPt >= titleFontSizeInPt;
    }

    private boolean fontSizeHasDroppedBelowThreshold(float fontSizeInPt) {
        return fontSizeInPt < titleFontSizeInPt && currentFontSizeInPt >= titleFontSizeInPt;
    }

    private int previousTitlePositionOnSamePage() {
        if (currentTitles.size() > 1) {
            return currentTitles.get(currentTitles.size() - 2).position;
        }
        return -1;
    }

    @Override
    protected void writeString(String text) throws IOException {
        if (getCurrentTitleInfo() != null && getCurrentTitleInfo().is_open) {
            getCurrentTitleInfo().title += text;
        }
        output.write(text);
    }

    @Override
    protected void writePageStart() throws IOException {
        output = new StringWriter();
        currentTitles = new ArrayList<TitleInfo>();
        pageCharacterCount = 0;
        currentFontSizeInPt = 0;
        setPageSeparatorCount(0);
        if (getCurrentTitleInfo() != null && getCurrentTitleInfo().is_open) {
            getCurrentTitleInfo().close();
        }
    }

    @Override
    protected void writePageEnd() throws IOException {
        String page = output.toString();
        pages.add(page);
        pageTitles.add(currentTitles);
        currentFontSizeInPt = 0;
        setPageSeparatorCount(getPageSeparatorCount() + getLineSeparator().length());
        super.writePageEnd();
        if (getCurrentTitleInfo() != null && getCurrentTitleInfo().is_open) {
            getCurrentTitleInfo().close();
        }
    }

    @Override
    protected void writeParagraphEnd() throws IOException {
        setPageSeparatorCount(getPageSeparatorCount() + getParagraphEnd().length());
        super.writeParagraphEnd();
    }

}
