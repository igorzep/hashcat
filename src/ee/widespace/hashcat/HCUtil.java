package ee.widespace.hashcat;

import ee.widespace.hashcat.model.HCArchive;
import ee.widespace.hashcat.model.HCCatalog;
import ee.widespace.hashcat.model.HCFile;
import ee.widespace.hashcat.model.HCHash;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.io.IOException;


public class HCUtil extends DefaultHandler {
	public static HCCatalog load(File file) throws SAXException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();

			HCUtil handler = new HCUtil();
			parser.parse(file, handler);

			return handler.catalog;
		} catch (IOException e) {
			throw new SAXException(e);
		} catch (ParserConfigurationException e) {
			throw new SAXException(e);
		}
	}

	public static void save(File file, HCCatalog catalog) throws SAXException {
		try {
			SAXTransformerFactory factory = (SAXTransformerFactory)
				TransformerFactory.newInstance();
			TransformerHandler handler = factory.newTransformerHandler();
			Transformer transformer = handler.getTransformer();
			transformer.setOutputProperty("indent", "yes");
			transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "1");
			handler.setResult(new StreamResult(file));

			handler.startDocument();
			serialize(handler, catalog);
			handler.endDocument();
		} catch (TransformerConfigurationException e) {
			throw new SAXException(e);
		}
	}

	private static void serialize(
		TransformerHandler handler, HCCatalog catalog
	) throws SAXException {
		AttributesImpl attrs = new AttributesImpl();
		attrs.addAttribute("", "id", "id", "ID", catalog.getId());

		handler.startElement("", "catalog", "catalog", attrs);

		serialize(handler, "name", catalog.getName());
		serialize(handler, "info", catalog.getInfo());

		for (int i = 0, length = catalog.getArchiveCount(); i < length; i++) {
			serialize(handler, catalog.getArchive(i));
		}

		handler.endElement("", "catalog", "catalog");
	}

	private static void serialize(
		TransformerHandler handler, HCArchive archive
	) throws SAXException {
		AttributesImpl attrs = new AttributesImpl();
		attrs.addAttribute("", "id", "id", "ID", archive.getId());

		handler.startElement("", "archive", "archive", attrs);

		serialize(handler, "name", archive.getName());
		serialize(handler, "info", archive.getInfo());

		for (int i = 0, length = archive.getFileCount(); i < length; i++) {
			serialize(handler, archive.getFile(i));
		}

		handler.endElement("", "archive", "archive");
	}

	private static void serialize(
		TransformerHandler handler, HCFile file
	) throws SAXException {
		AttributesImpl attrs = new AttributesImpl();
		attrs.addAttribute("", "location", "location", "CDATA", file.getLocation());

		handler.startElement("", "file", "file", attrs);

		for (int i = 0, length = file.getNameCount(); i < length; i++) {
			serialize(handler, "name", file.getName(i));
		}

		for (int i = 0, length = file.getHashCount(); i < length; i++) {
			serialize(handler, file.getHash(i));
		}

		handler.endElement("", "file", "file");
	}

	private static void serialize(
		TransformerHandler handler, HCHash hash
	) throws SAXException {
		AttributesImpl attrs = new AttributesImpl();
		attrs.addAttribute("", "length", "length", "CDATA", hash.length);
		attrs.addAttribute("", "MD5", "MD5", "CDATA", hash.MD5);
		attrs.addAttribute("", "SHA1", "SHA1", "CDATA", hash.SHA1);

		handler.startElement("", "hash", "hash", attrs);
		handler.endElement("", "hash", "hash");
	}

	private static void serialize(
		TransformerHandler handler, String element, String value
	) throws SAXException {
		if (value == null) {
			return;
		}

		char[] buf = value.toCharArray();

		AttributesImpl attrs = new AttributesImpl();

		handler.startElement("", element, element, attrs);
		handler.characters(buf, 0, buf.length);
		handler.endElement("", element, element);
	}

	private StringBuffer buf;

	private HCCatalog catalog;
	private HCArchive archive;
	private HCFile file;

	private HCUtil() {}

	public void startElement(
		String uri, String localName, String qName, Attributes attrs
	) throws SAXException {
		if (qName.equals("name") || qName.equals("info")) {
			buf = new StringBuffer(50);
			return;
		}

		if (qName.equals("hash")) {
			if (file != null) {
				HCHash hash = new HCHash(
					attrs.getValue("length"),
					attrs.getValue("MD5"),
					attrs.getValue("SHA1"));
				file.addHash(hash);
			}
			return;
		}

		if (qName.equals("file")) {
			if (archive != null) {
				file = new HCFile(attrs.getValue("location"));
				archive.addFile(file);
			}
			return;
		}

		if (qName.equals("archive")) {
			if (catalog != null) {
				archive = new HCArchive(attrs.getValue("id"));
				catalog.addArchive(archive);
			}
			return;
		}

		if (qName.equals("catalog")) {
			catalog = new HCCatalog(attrs.getValue("id"));
			return;
		}
	}

	public void endElement(
		String uri, String localName, String qName
	) throws SAXException {
		if (qName.equals("name")) {
			if (buf == null) {
				return;
			}

			if (file != null) {
				file.addName(buf.toString());
			} else if (archive != null) {
				archive.setName(buf.toString());
			} else if (catalog != null) {
				catalog.setName(buf.toString());
			}

			buf = null;
			return;
		}

		if (qName.equals("info")) {
			if (buf == null) {
				return;
			}

			if (archive != null) {
				archive.setInfo(buf.toString());
			} else if (catalog != null) {
				catalog.setInfo(buf.toString());
			}

			buf = null;
			return;
		}

		if (qName.equals("hash")) {
			return;
		}

		if (qName.equals("file")) {
			file = null;
		}

		if (qName.equals("archive")) {
			archive = null;
			return;
		}

		if (qName.equals("catalog")) {
			return;
		}
	}

	public void characters(
		char[] ch, int start, int length
	) throws SAXException {
		if (buf != null) {
			buf.append(ch, start, length);
		}
	}
}
