/*
 * Copyright (c) 2008, intarsys consulting GmbH
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Public License as published by the
 * Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * by MasYes: Практически всё - тупой копипаст экзампла из библиотеки, так что все претензии не ко мне ^^
 */
package ru.lisaprog.parser;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.*;
import java.io.IOException;
import java.util.Iterator;

import de.intarsys.pdf.content.CSDeviceBasedInterpreter;
import de.intarsys.pdf.content.CSException;
import de.intarsys.pdf.content.text.CSTextExtractor;
import de.intarsys.pdf.cos.COSVisitorException;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.pdf.pd.PDPageNode;
import de.intarsys.pdf.pd.PDPageTree;
import de.intarsys.pdf.tools.kernel.PDFGeometryTools;
import ru.lisaprog.Common;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.*;

import ru.lisaprog.UnsupportedFormatException;

/**
 * Extract complete text from document.
 *
 */
public class ExtractText extends CommonJPodExample{

	public static String parse(String file) {
		switch(file.substring(file.lastIndexOf("."))){
			case ".pdf" :	return parsePDF(file);
			case ".txt" :	return parseTXT(file);
			case ".doc" :	return parseDOC(file);
			case ".docx" :	return parseDOCX(file);
			default : throw new UnsupportedFormatException();
		}
	}

	public static String parseDOC(String file) {
		try{
			BufferedInputStream isr = new BufferedInputStream(new FileInputStream(file));
			WordExtractor word = new WordExtractor(isr);
			return word.getText();
		} catch (Exception e) {
//			Common.createLog(e);
			return "";
		}
	}

	public static String parseDOCX(String file) {
		try{
			BufferedInputStream isr = new BufferedInputStream(new FileInputStream(file));
			XWPFWordExtractor word = new XWPFWordExtractor(new XWPFDocument(isr));
			return word.getText();
		} catch (Exception e) {
//			Common.createLog(e);
			return "";
		}
	}

	public static String parsePDF(String file) {
		ExtractText client = new ExtractText();
		try {
			return client.run(file);
		} catch (Exception e) {
//			Common.createLog(e);
			return "";
		}
	}

	private static String parseTXT(String file) {
		File text = new File(file);
			try{
				System.gc();
				String str = "";
				InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "utf-8");
				BufferedReader reader;
				reader = new BufferedReader(isr);
				for (String line; (line = reader.readLine()) != null;) {
					str+= line + "\n";
				}
				if(!str.contains("�"))
					return str;
				str = "";
				isr = new InputStreamReader(new FileInputStream(file), "Windows-1251");
				reader = new BufferedReader(isr);
				for (String line; (line = reader.readLine()) != null;) {
					str+= line + "\n";
				}
				return str;
			}
			catch(IOException e){
				Common.createLog(e);
				return "";
			}
	}

	private void extractText(PDPageTree pageTree, StringBuilder sb) {
		for (Iterator it = pageTree.getKids().iterator(); it.hasNext();) {
			PDPageNode node = (PDPageNode) it.next();
			if (node.isPage()) {
				try {
					CSTextExtractor extractor = new CSTextExtractor();
					PDPage page = (PDPage) node;
					AffineTransform pageTx = new AffineTransform();
					PDFGeometryTools.adjustTransform(pageTx, page);
					extractor.setDeviceTransform(pageTx);
					CSDeviceBasedInterpreter interpreter = new CSDeviceBasedInterpreter(
							null, extractor);
					interpreter.process(page.getContentStream(), page
							.getResources());
					sb.append(extractor.getContent());
				} catch (CSException e) {
					e.printStackTrace();
				}
			} else {
				extractText((PDPageTree) node, sb);
			}
		}
	}

	private String extractText(String filename) throws COSVisitorException,
			IOException {
		PDDocument doc = getDoc();
		StringBuilder sb = new StringBuilder();
		extractText(doc.getPageTree(), sb);
		return sb.toString();
	}

	private String run(String file) throws Exception {
		try {
			open(file);
			return extractText(file);
		} finally {
			close();
		}
	}
}


