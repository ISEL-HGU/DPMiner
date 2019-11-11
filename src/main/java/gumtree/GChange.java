package gumtree;

public class GChange {
	public String change_type;
	public int node_type;
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((change_type == null) ? 0 : change_type.hashCode());
		result = prime * result + node_type;
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
		GChange other = (GChange) obj;
		if (change_type == null) {
			if (other.change_type != null)
				return false;
		} else if (!change_type.equals(other.change_type))
			return false;
		if (node_type != other.node_type)
			return false;
		return true;
	}
	public GChange(String change_type, int node_type) {
		super();
		this.change_type = change_type;
		this.node_type = node_type;
	}
	
	
}
