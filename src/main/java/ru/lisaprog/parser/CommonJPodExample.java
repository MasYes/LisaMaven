package ru.lisaprog.parser;

import de.intarsys.pdf.parser.COSLoadException;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.tools.locator.FileLocator;

import java.io.IOException;

/**
 * Created by Юлиан on 25.04.14.
 */
public class CommonJPodExample {
	private PDDocument doc;

	protected PDDocument basicOpen(String pathname) throws IOException,
			COSLoadException {
		FileLocator locator = new FileLocator(pathname);
		return PDDocument.createFromLocator(locator);
	}

	protected void basicSave(PDDocument doc, String outputFileName)
			throws IOException {
		FileLocator locator = new FileLocator(outputFileName);
		doc.save(locator, null);
	}

	/**
	 * Close the current document.
	 *
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (getDoc() != null) {
			getDoc().close();
		}
	}

	/**
	 * Create a new document.
	 */
	public void create() {
		// First create a new document.
		setDoc(PDDocument.createNew());
		// You could add more information about the environment:
		getDoc().setAuthor("intarsys consulting GmbH"); //$NON-NLS-1$
		getDoc().setCreator("intarsys PDF API"); //$NON-NLS-1$
	}

	/**
	 * The current document.
	 *
	 * @return The current document.
	 */
	public PDDocument getDoc() {
		return doc;
	}

	/**
	 * Open a document.
	 *
	 * @param pathname
	 *            The path name to the document.
	 * @throws COSLoadException
	 * @throws IOException
	 */
	public void open(String pathname) throws IOException, COSLoadException {
		setDoc(basicOpen(pathname));
	}

	/**
	 * Save current document to path.
	 *
	 * @param outputFileName
	 *            The destination path for the document.
	 * @throws IOException
	 */
	public void save(String outputFileName) throws IOException {
		basicSave(getDoc(), outputFileName);
	}

	/**
	 * Set the current document.
	 *
	 * @param doc
	 *            The new current document.
	 */
	protected void setDoc(PDDocument doc) {
		this.doc = doc;
	}
}
