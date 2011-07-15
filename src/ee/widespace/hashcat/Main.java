package ee.widespace.hashcat;

import ee.widespace.hashcat.model.HCCatalog;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * 
 * 
 * @author Igor Malinin
 */
public class Main {
	private static final long SIZE_1K = 1024;
	private static final long SIZE_1M = SIZE_1K * 1024;
	private static final long SIZE_1G = SIZE_1M * 1024;

	private static final long[] sizes = {
		SIZE_1M *   1, SIZE_1M *   3,
		SIZE_1M *  10, SIZE_1M *  30,
		SIZE_1M * 100, SIZE_1M * 300,
		SIZE_1G *   1, SIZE_1G *   3,
		SIZE_1G *  10, SIZE_1G *  30,
	};

	private static final String[] bases = {
		  "1M",   "3M",
		 "10M",  "30M",
		"100M", "300M",
		  "1G",   "3G",
		 "10G",  "30G",
	};



	private static Map archived = new HashMap();
	private static Map searched = new HashMap();

	private static PrintWriter out;

	public static void main(String[] args) {
		try {
			HCCatalog catalog = HCUtil.load(new File("example.xml"));
			HCUtil.save(new File("test.xml"), catalog);
		} catch (SAXException e) {
			e.printStackTrace();
		}

		/*
		out = new PrintWriter(System.out);
		try {
			out = new PrintWriter(new FileWriter("P:\\duplicate.log"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		scan(new File("P:\\Photo"));
		scan(new File("P:\\Video"));
		scan(new File("T:\\Unsorted"));

		printDuplicates();

		out.close();
		*/
	}

	private static void scan(File file) {
		if (file.isDirectory()) {
			File[] list = file.listFiles();
			for (int i = 0; i < list.length; i++) {
				scan(list[i]);
			}

			return;
		}

		if (file.isFile()) {
			try {
				InputStream in = new FileInputStream(file);
				try {
					MessageDigest digest = createMessageDigest();

					DigestInputStream din = new DigestInputStream(in, digest);

					byte[] buf = new byte[8*1024];
					while (din.read(buf) >= 0) {
						// read until end of file
					}

					addSearchedFile(digest.digest(), file);
				} finally {
					in.close();
				}
			} catch (IOException e) {
				System.err.print(
					"Error reading file: " + file.getAbsolutePath());
			}
		}
	}

	private static MessageDigest createMessageDigest() {
		try {
			return MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private static void addSearchedFile(byte[] hash, File file) {
		String path = file.getAbsolutePath();

		Descriptor descriptor = new Descriptor(hash, file.length());

		Object value = searched.get(descriptor);
		if (value == null) {
			searched.put(descriptor, path);
		} else if (value instanceof String) {
			String[] array = { (String) value, path };
			searched.put(descriptor, array);
		} else {
			String[] src = (String[]) value;
			String[] dst = new String[src.length + 1];
			System.arraycopy(src, 0, dst, 0, src.length);
			dst[src.length] = path;
			searched.put(descriptor, dst);
		}

		print(descriptor, path);
		out.flush();
	}

	private static void printDuplicates() {
		out.println();
		out.println("##################");
		out.println("### Duplicates ###");
		out.println("##################");
		out.println();

		Iterator i = searched.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			Object value = e.getValue();
			if (value instanceof String[]) {
				print((Descriptor) e.getKey(), value);
			}
		}
	}

	private static void print(Descriptor descriptor, Object value) {
		if (value instanceof String[]) {
			print(descriptor, (String[]) value);
		} else {
			print(descriptor, (String) value);
		}

		out.println();
	}

	private static void print(Descriptor descriptor, String file) {
		out.println(
			descriptor.getHash() + " " + descriptor.getSize() + ":");

		out.println(file);
	}

	private static void print(Descriptor descriptor, String[] files) {
		out.println(descriptor.getHash() + " " +
			descriptor.getSize() + " " + files.length + ":");

		for (int j = 0; j < files.length; j++) {
			out.println(files[j]);
		}
	}
}
