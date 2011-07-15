package ee.widespace.hashcat.model;

import java.util.ArrayList;
import java.util.List;


public class HCArchive {
	protected HCCatalog catalog;

	private final String id;

	private String name;
	private String info;

	private final List files = new ArrayList();

	public HCArchive(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public int getFileCount() {
		return files.size();
	}

	public HCFile getFile(int n) {
		return (HCFile) files.get(n);
	}

	public void addFile(HCFile file) {
		files.add(file);
		file.archive = this;
	}

	public void removeFile(HCFile file) {
		file.archive = null;
		files.remove(file);
	}
}
