package ee.widespace.hashcat.model;


public class HCHash {
	protected HCFile file;

	public final String length;
	public final String MD5;
	public final String SHA1;

	public HCHash(String length, String MD5, String SHA1) {
		this.length = length;
		this.MD5 = MD5;
		this.SHA1 = SHA1;
	}

	public HCHash(String length, byte[] MD5, byte[] SHA1) {
		this(length, toHexString(MD5), toHexString(SHA1));
	}

	public HCHash(long length, byte[] MD5, byte[] SHA1) {
		this(Long.toString(length), toHexString(MD5), toHexString(SHA1));
	}

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		HCHash hash = (HCHash) obj;

		return length.equals(hash.length) &&
			MD5.equals(hash.MD5) && SHA1.equals(hash.SHA1);
	}

	/*
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return length.hashCode() + MD5.hashCode() + SHA1.hashCode();
	}

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
}
