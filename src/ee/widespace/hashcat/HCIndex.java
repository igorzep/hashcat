package ee.widespace.hashcat;

import ee.widespace.hashcat.model.HCArchive;
import ee.widespace.hashcat.model.HCCatalog;
import ee.widespace.hashcat.model.HCFile;
import ee.widespace.hashcat.model.HCHash;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * 
 * 
 * @author Igor Malinin
 */
public class HCIndex {
	private static final long SIZE_1K = 1024;
	private static final long SIZE_1M = SIZE_1K * 1024;
	private static final long SIZE_1G = SIZE_1M * 1024;

	private static final long[] SIZES = {
		SIZE_1M * 1, SIZE_1M * 4, SIZE_1M * 16, SIZE_1M * 64, SIZE_1M * 256,
		SIZE_1G * 1, SIZE_1G * 4, SIZE_1G * 16, SIZE_1G * 64, SIZE_1G * 256,
	};

	private static final String[] BASES = {
		"1M", "4M", "16M", "64M", "256M",
		"1G", "4G", "16G", "64G", "256G",
	};

	public static void main(String[] args) {
		try {
			switch (args.length) {
				case 0: {
					System.out.println("Usage:");
					System.out.println("a> index dir" );
					System.out.println("b> index out.xml dir1 dir2 ... dirN");
					return;
				}

				case 1: {
					File dir = new File(args[0]);
					File output = new File(dir, ".hashcat");
					HCIndex index = new HCIndex(dir.getName());

					index.index(dir);

					HCUtil.save(output, index.catalog);
					return;
				}

				default: {
					File output = new File(args[0]);
					HCIndex index = new HCIndex(output.getName());

					for (int i = 1; i < args.length; i++) {
						File dir = new File(args[i]);
						index.index(dir);
					}

					HCUtil.save(output, index.catalog);
					return;
				}
			}
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}

	private HCCatalog catalog;
	private HCArchive archive;

	public HCIndex(String id) {
		catalog = new HCCatalog(id);
	}

	public void index(File file) {
		archive = new HCArchive(file.getName());
		try {
			visit("", file);
		} finally {
			catalog.addArchive(archive);
			archive = null;
		}
	}

	protected void visit(String location, File file) {
		if (file.getName().startsWith(".")) {
			return; // skip hidden files and directories
		}

		if (file.isDirectory()) {
			File[] list = file.listFiles();
			for (int i = 0; i < list.length; i++) {
				File child = list[i];
				String path = child.getName();
				if (location.length() > 0) {
					path = location + "/" + path;
				}
				visit(path, child);
			}
		} else if (file.isFile()) {
			addFile(location, file);
		}
	}

	private void addFile(String location, File file) {
		HCFile rf = new HCFile(location);

		archive.addFile(rf);

		try {
			InputStream in = new FileInputStream(file);
			try {
				MessageDigest md5 = createMD5();
				MessageDigest sha1 = createSHA1();

				int i = 0;
				long base = SIZES[i];

				long length = 0;
				byte[] buf = new byte[8*1024];

				while (true) {
					int n = in.read(buf);
					if (n < 0) {
						break;
					}

					length += n;

					if (base <= length) {
						try {
							MessageDigest _md5 = (MessageDigest) md5.clone();
							MessageDigest _sha1 = (MessageDigest) sha1.clone();

							int pos = n - (int) (length - base);
							if (pos > 0) {
								_md5.update(buf, 0, pos);
								_sha1.update(buf, 0, pos);
							}

							rf.addHash(new HCHash(BASES[i],
								_md5.digest(), _sha1.digest()));

							if (++i < SIZES.length) {
								base = SIZES[i];
							} else {
								base = Long.MAX_VALUE; 
							}
						} catch (CloneNotSupportedException e) {
							throw new RuntimeException(e); // TODO
						}
					}

					md5.update(buf, 0, n);
					sha1.update(buf, 0, n);
				}

				rf.addHash(new HCHash(length, md5.digest(), sha1.digest()));
			} finally {
				in.close();
			}
		} catch (IOException e) {
			rf.setInfo("Error: " + e.getMessage());
			System.err.print(
				"Error reading file: " + file.getAbsolutePath());
		}
	}

	private static MessageDigest createMD5() {
		try {
			return MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private static MessageDigest createSHA1() {
		try {
			return MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
