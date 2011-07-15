package ee.widespace.hashcat.model;

import java.util.ArrayList;
import java.util.List;


public class HCCatalog {
	private final String id;

	private String name;
	private String info;

	private final List archives = new ArrayList();

	public HCCatalog(String id) {
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

	public int getArchiveCount() {
		return archives.size();
	}

	public HCArchive getArchive(int n) {
		return (HCArchive) archives.get(n);
	}

	public void addArchive(HCArchive archive) {
		archives.add(archive);
		archive.catalog = this;
	}

	public void removeArchive(HCArchive archive) {
		archive.catalog = null;
		archives.remove(archive);
	}
}
