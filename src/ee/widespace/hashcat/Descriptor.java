package ee.widespace.hashcat;


/**
 * 
 * 
 * @author Igor Malinin
 */
public class Descriptor {
	private String hash;
	private long size;

	private static String toHexString(byte[] hash) {
		StringBuffer sb = new StringBuffer(hash.length * 2);

		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(hash[i] & 0xFF);
			if (hex.length() < 2) {
				sb.append('0');
			}
			sb.append(hex);
		}

		return sb.toString();
	}

	public Descriptor(byte[] hash, long size) {
		this(toHexString(hash), size);
	}

	public Descriptor(String hash, long size) {
		this.hash = hash;
		this.size = size;
	}

	public String getHash() {
		return hash;
	}

	public long getSize() {
		return size;
	}

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		Descriptor ent = (Descriptor) obj;

		return hash.equals(ent.hash) && size == size;
	}

	/*
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return hash.hashCode() + (int) size;
	}
}
