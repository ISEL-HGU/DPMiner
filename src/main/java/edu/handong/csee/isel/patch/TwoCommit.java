package edu.handong.csee.isel.patch;

public class TwoCommit {
	
	public String getOldCommitHash() {
		return oldCommitHash;
	}
	public String getNewCommitHash() {
		return newCommitHash;
	}
	public TwoCommit(String oldCommitHash, String newCommitHash) {
		this.oldCommitHash = oldCommitHash;
		this.newCommitHash = newCommitHash;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((newCommitHash == null) ? 0 : newCommitHash.hashCode());
		result = prime * result + ((oldCommitHash == null) ? 0 : oldCommitHash.hashCode());
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
		TwoCommit other = (TwoCommit) obj;
		if (newCommitHash == null) {
			if (other.newCommitHash != null)
				return false;
		} else if (!newCommitHash.equals(other.newCommitHash))
			return false;
		if (oldCommitHash == null) {
			if (other.oldCommitHash != null)
				return false;
		} else if (!oldCommitHash.equals(other.oldCommitHash))
			return false;
		return true;
	}

	String oldCommitHash;
	String newCommitHash;
}
