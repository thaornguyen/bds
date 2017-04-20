package vn.com.dsvn.dto;

public class ZillowLink {
	private String name;
	private String link;

	public ZillowLink(String name, String link) {
		this.name = name;
		this.link = link;
	}

	public ZillowLink() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public static ZillowLink toBDSLink(String line) {
		String[] tks = line.split("\t");
		String link = tks[tks.length - 1];
		String name = "";
		if (tks.length > 1) {
			for (int i = 0; i < tks.length - 1; i++) {
				name += tks[i] + "\t";
			}
			name = name.trim();
		}
		return new ZillowLink(name, link);
	}

	@Override
	public String toString() {
		return this.name + "\t" + this.link;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ZillowLink other = (ZillowLink) obj;
		if (link == null) {
			if (other.link != null)
				return false;
		} else if (!link.equals(other.link))
			return false;
		return true;
	}

}