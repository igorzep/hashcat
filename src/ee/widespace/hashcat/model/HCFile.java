package ee.widespace.hashcat.model;

import java.util.ArrayList;
import java.util.List;


public class HCFile {
	protected HCArchive archive;

	private final String location;

	private final List hashes = new ArrayList();
	private final List names = new ArrayList();

	private String info;

	public HCFile(String location) {
		this.location = location;
	}

	public String getLocation() {
		return location;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public int getNameCount() {
		return names.size();
	}

	public String getName(int n) {
		return (String) names.get(n);
	}

	public void addName(String name) {
		names.add(name);
	}

	public void removeName(String name) {
		names.remove(name);
	}

	public int getHashCount() {
		return hashes.size();
	}

	public HCHash getHash(int n) {
		return (HCHash) hashes.get(n);
	}

	public void addHash(HCHash hash) {
		hashes.add(hash);
		hash.file = this;
	}

	public void removeHash(HCHash hash) {
		hash.file = null;
		hashes.remove(hash);
	}
}
